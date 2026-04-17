"""API router configuration."""
from fastapi import APIRouter, Depends, HTTPException, BackgroundTasks
from sqlalchemy.orm import Session
from typing import List, Optional
from datetime import datetime, timezone
from app.database import get_db, SessionLocal
from app.models import Book, Genre, Author, Review, CustomShelf, ShelfBook, User, ReadingProgress, UserPreferences
from app.schemas import BookResponse, BookDetailResponse, GenreResponse, FirebaseSyncRequest, FirebaseSyncResponse, CustomShelfResponse, CustomShelfCreate, AddBookToShelfRequest, ReviewResponse, ReviewWithUserResponse, ReviewCreate, UserUpdate, UserResponse, ReadingProgressResponse, ReadingProgressCreate, ReadingProgressUpdate, UserPreferencesResponse, UserPreferencesUpdate, ReadingStatisticsResponse
from firebase_admin import auth
from app.services.ai import generate_book_review_summary
from app.api.dependencies import get_current_user
from app.services.auth_service import AuthService

api_router = APIRouter()

# --- Helper logic for Background Tasks ---
def bg_generate_summary(book_id: int):
    # Isolated DB session for the background thread
    db = SessionLocal()
    try:
        generate_book_review_summary(book_id, db)
    finally:
        db.close()

# ----------------- Auth / Sync -----------------
@api_router.post("/users/auth", response_model=FirebaseSyncResponse, tags=["Auth"])
def sync_user(request: FirebaseSyncRequest, db: Session = Depends(get_db)):
    """Sync Firebase user with backend database."""
    try:
        decoded_token = auth.verify_id_token(request.token)
        uid = decoded_token['uid']
        email = decoded_token.get('email')
        display_name = decoded_token.get('name')

        # Use AuthService to create or update user (handles uniqueness and updates)
        user, is_new = AuthService.create_or_update_user_from_firebase(
            db=db,
            firebase_uid=uid,
            email=email,
            display_name=display_name
        )

        if is_new:
            # Create default shelves for new users
            default_shelves = [
                CustomShelf(user_id=user.id, name="Read", is_default=True, is_exclusive=True, position=1),
                CustomShelf(user_id=user.id, name="Currently Reading", is_default=True, is_exclusive=True, position=2),
                CustomShelf(user_id=user.id, name="Want to Read", is_default=True, is_exclusive=True, position=3)
            ]
            db.add_all(default_shelves)
            db.commit()

        return FirebaseSyncResponse(user=user, is_new_user=is_new)
    except (auth.InvalidIdTokenError, auth.ExpiredIdTokenError, auth.RevokedIdTokenError):
        raise HTTPException(status_code=401, detail="Invalid authentication credentials")
    except ValueError:
        raise HTTPException(status_code=503, detail="Authentication service is not configured")
    except Exception:
        raise HTTPException(status_code=401, detail="Invalid authentication credentials")

# ----------------- User Profile -----------------
@api_router.get("/user/profile", response_model=UserResponse, tags=["User"])
def get_user_profile(current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    """Get current user's profile information."""
    return current_user

@api_router.put("/user/profile", response_model=UserResponse, tags=["User"])
def update_user_profile(profile_update: UserUpdate, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    """Update current user's profile information."""
    # Update fields if provided
    if profile_update.username is not None:
        # Check if username is already taken by another user
        existing_user = db.query(User).filter(User.username == profile_update.username, User.id != current_user.id).first()
        if existing_user:
            raise HTTPException(status_code=400, detail="Username already taken")
        current_user.username = profile_update.username

    if profile_update.email is not None:
        # Check if email is already taken by another user
        existing_user = db.query(User).filter(User.email == profile_update.email, User.id != current_user.id).first()
        if existing_user:
            raise HTTPException(status_code=400, detail="Email already taken")
        current_user.email = profile_update.email

    if profile_update.full_name is not None:
        current_user.full_name = profile_update.full_name

    if profile_update.bio is not None:
        current_user.bio = profile_update.bio

    if profile_update.avatar_url is not None:
        current_user.avatar_url = profile_update.avatar_url

    db.commit()
    db.refresh(current_user)
    return current_user

# ----------------- Books -----------------
@api_router.get("/books", response_model=List[BookResponse], tags=["Books"])
def get_books(skip: int = 0, limit: int = 50, genre_slug: Optional[str] = None, db: Session = Depends(get_db)):
    query = db.query(Book)
    if genre_slug:
        query = query.join(Book.genres).filter(Genre.slug == genre_slug)
    return query.offset(skip).limit(limit).all()

@api_router.get("/books/popular", response_model=List[BookResponse], tags=["Books"])
def get_popular_books(limit: int = 10, db: Session = Depends(get_db)):
    books = db.query(Book).order_by(Book.ratings_count.desc()).limit(limit).all()
    return books

@api_router.get("/books/trending", response_model=List[BookResponse], tags=["Books"])
def get_trending_books(limit: int = 10, db: Session = Depends(get_db)):
    books = db.query(Book).order_by(Book.average_rating.desc()).limit(limit).all()
    return books

@api_router.get("/books/recommended", response_model=List[BookResponse], tags=["Books"])
def get_recommended_books(limit: int = 10, db: Session = Depends(get_db)):
    books = db.query(Book).order_by(Book.id.desc()).limit(limit).all()
    return books

@api_router.get("/books/search", response_model=List[BookResponse], tags=["Books"])
def search_books(
    q: str = "",
    author: Optional[str] = None,
    genre: Optional[str] = None,
    min_rating: Optional[float] = None,
    max_rating: Optional[float] = None,
    publication_year: Optional[int] = None,
    isbn: Optional[str] = None,
    limit: int = 20,
    db: Session = Depends(get_db)
):
    query = db.query(Book)
    
    # Title search
    if q:
        query = query.filter(Book.title.ilike(f"%{q}%"))
    
    # Author search
    if author:
        query = query.join(Book.authors).filter(Author.name.ilike(f"%{author}%"))
    
    # Genre search
    if genre:
        query = query.join(Book.genres).filter(Genre.name.ilike(f"%{genre}%"))
    
    # Rating filters
    if min_rating is not None:
        query = query.filter(Book.average_rating >= min_rating)
    if max_rating is not None:
        query = query.filter(Book.average_rating <= max_rating)
    
    # Publication year filter
    if publication_year is not None:
        query = query.filter(Book.published_date.like(f"{publication_year}%"))
    
    # ISBN search
    if isbn:
        query = query.filter(
            (Book.isbn_10.ilike(f"%{isbn}%")) | 
            (Book.isbn_13.ilike(f"%{isbn}%"))
        )
    
    return query.limit(limit).all()

@api_router.get("/books/{book_id}", response_model=BookDetailResponse, tags=["Books"])
def get_book(book_id: int, background_tasks: BackgroundTasks, db: Session = Depends(get_db)):
    book = db.query(Book).filter(Book.id == book_id).first()
    if not book:
        raise HTTPException(status_code=404, detail="Book not found")
        
    # Check if AI Summary exists. If missing, trigger generation asynchronously.
    if not book.ai_review_summary:
        background_tasks.add_task(bg_generate_summary, book_id)
        
    return book

# ----------------- Genres -----------------
@api_router.get("/genres", response_model=List[GenreResponse], tags=["Genres"])
def get_genres(db: Session = Depends(get_db)):
    return db.query(Genre).order_by(Genre.name).all()

# ----------------- Shelves -----------------
@api_router.get("/user/shelves", response_model=List[CustomShelfResponse], tags=["Shelves"])
def get_user_shelves(current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    # Ensure default shelves exist for the user
    existing_shelves = db.query(CustomShelf).filter(CustomShelf.user_id == current_user.id).all()
    existing_names = {shelf.name for shelf in existing_shelves}
    
    default_shelves = [
        ("Read", 1),
        ("Currently Reading", 2),
        ("Want to Read", 3)
    ]
    
    for name, position in default_shelves:
        if name not in existing_names:
            new_shelf = CustomShelf(
                user_id=current_user.id,
                name=name,
                is_default=True,
                is_exclusive=True,
                position=position
            )
            db.add(new_shelf)
    
    db.commit()
    
    return db.query(CustomShelf).filter(CustomShelf.user_id == current_user.id).order_by(CustomShelf.position).all()

@api_router.get("/user/books/{book_id}/shelf-status", tags=["Shelves"])
def get_book_shelf_status(book_id: int, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    """Check if a book is on any of the user's shelves."""
    shelf_book = db.query(ShelfBook).join(CustomShelf).filter(
        ShelfBook.book_id == book_id,
        CustomShelf.user_id == current_user.id
    ).first()
    
    if shelf_book:
        shelf = db.query(CustomShelf).filter(CustomShelf.id == shelf_book.shelf_id).first()
        return {"shelf": shelf.name if shelf else None}
    return {"shelf": None}

@api_router.get("/user/shelf/reading", response_model=List[BookResponse], tags=["Shelves"])
def get_user_currently_reading(current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    shelf = db.query(CustomShelf).filter(CustomShelf.user_id == current_user.id, CustomShelf.name == "Currently Reading").first()
    if not shelf:
        return []
    books = db.query(Book).join(ShelfBook).filter(ShelfBook.shelf_id == shelf.id).all()
    return books

@api_router.get("/user/shelf/want_to_read", response_model=List[BookResponse], tags=["Shelves"])
def get_user_want_to_read(current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    shelf = db.query(CustomShelf).filter(CustomShelf.user_id == current_user.id, CustomShelf.name == "Want to Read").first()
    if not shelf:
        return []
    books = db.query(Book).join(ShelfBook).filter(ShelfBook.shelf_id == shelf.id).all()
    return books

@api_router.get("/user/shelf/read", response_model=List[BookResponse], tags=["Shelves"])
def get_user_already_read(current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    shelf = db.query(CustomShelf).filter(CustomShelf.user_id == current_user.id, CustomShelf.name == "Read").first()
    if not shelf:
        return []
    books = db.query(Book).join(ShelfBook).filter(ShelfBook.shelf_id == shelf.id).all()
    return books

@api_router.post("/shelves/{shelf_id}/books", tags=["Shelves"])
def add_book_to_shelf(shelf_id: int, request: AddBookToShelfRequest, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    shelf = db.query(CustomShelf).filter(CustomShelf.id == shelf_id).first()
    if not shelf:
        raise HTTPException(status_code=404, detail="Shelf not found")

    book = db.query(Book).filter(Book.id == request.book_id).first()
    if not book:
        raise HTTPException(status_code=404, detail="Book not found")

    # Check if already in shelf
    exists = db.query(ShelfBook).filter_by(shelf_id=shelf.id, book_id=book.id).first()
    if exists:
        return {"status": "success", "message": "Book already in shelf"}

    # If shelf is exclusive, remove from other exclusive shelves
    if shelf.is_exclusive:
        exclusive_shelves = db.query(CustomShelf.id).filter_by(user_id=shelf.user_id, is_exclusive=True).all()
        exclusive_shelf_ids = [s[0] for s in exclusive_shelves]
        db.query(ShelfBook).filter(
            ShelfBook.book_id == book.id,
            ShelfBook.shelf_id.in_(exclusive_shelf_ids)
        ).delete(synchronize_session=False)
        db.commit()

    shelf_book = ShelfBook(shelf_id=shelf.id, book_id=book.id)
    db.add(shelf_book)
    db.commit()
    return {"status": "success", "message": "Book added to shelf"}

# ----------------- Reviews -----------------
@api_router.get("/user/reviews", response_model=List[ReviewWithUserResponse], tags=["Reviews"])
def get_my_reviews(current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    return db.query(Review).filter(Review.user_id == current_user.id).order_by(Review.created_at.desc()).limit(10).all()

@api_router.get("/books/{book_id}/reviews", response_model=List[ReviewWithUserResponse], tags=["Reviews"])
def get_book_reviews(book_id: int, skip: int = 0, limit: int = 50, db: Session = Depends(get_db)):
    return db.query(Review).filter(Review.book_id == book_id).order_by(Review.created_at.desc()).offset(skip).limit(limit).all()

@api_router.post("/books/{book_id}/reviews", response_model=ReviewResponse, tags=["Reviews"])
def create_review(book_id: int, review: ReviewCreate, background_tasks: BackgroundTasks, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    db_book = db.query(Book).filter(Book.id == book_id).first()
    if not db_book:
         raise HTTPException(status_code=404, detail="Book not found")
         
    # Check if review exists
    db_review = db.query(Review).filter(Review.book_id == book_id, Review.user_id == current_user.id).first()
    if db_review:
        db_review.title = review.title
        db_review.content = review.content
    else:
        db_review = Review(
            book_id=book_id,
            user_id=current_user.id,
            title=review.title,
            content=review.content
        )
        db.add(db_review)
    
    db.commit()
    db.refresh(db_review)
    
    # Invalidate old summary and generate a new one now that reviews have changed
    db_book.ai_review_summary = None 
    db.commit()
    background_tasks.add_task(bg_generate_summary, book_id)
    
    return db_review

@api_router.delete("/books/{book_id}/reviews", tags=["Reviews"])
def delete_review(book_id: int, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    db_review = db.query(Review).filter(Review.book_id == book_id, Review.user_id == current_user.id).first()
    if not db_review:
        raise HTTPException(status_code=404, detail="Review not found")
    
    db.delete(db_review)
    db.commit()
    
    # Invalidate old summary and generate a new one now that reviews have changed
    db_book = db.query(Book).filter(Book.id == book_id).first()
    if db_book:
        db_book.ai_review_summary = None 
        db.commit()
    
    return {"status": "success", "message": "Review deleted successfully"}

# ----------------- Reading Progress -----------------
@api_router.get("/user/reading-progress", response_model=List[ReadingProgressResponse], tags=["Reading Progress"])
def get_user_reading_progress(current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    """Get all reading progress for the current user."""
    return db.query(ReadingProgress).filter(ReadingProgress.user_id == current_user.id).all()

@api_router.get("/user/books/{book_id}/reading-progress", response_model=ReadingProgressResponse, tags=["Reading Progress"])
def get_book_reading_progress(book_id: int, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    """Get reading progress for a specific book."""
    progress = db.query(ReadingProgress).filter(
        ReadingProgress.user_id == current_user.id,
        ReadingProgress.book_id == book_id
    ).first()
    
    if not progress:
        # Return default progress if none exists
        return ReadingProgressResponse(
            id=0,
            user_id=current_user.id,
            book_id=book_id,
            current_page=0,
            total_pages=None,
            start_date=None,
            end_date=None,
            is_completed=False,
            created_at=datetime.now(timezone.utc),
            updated_at=datetime.now(timezone.utc)
        )
    
    return progress

@api_router.post("/user/books/{book_id}/reading-progress", response_model=ReadingProgressResponse, tags=["Reading Progress"])
def create_or_update_reading_progress(book_id: int, progress_data: ReadingProgressUpdate, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    """Create or update reading progress for a book."""
    # Check if book exists
    book = db.query(Book).filter(Book.id == book_id).first()
    if not book:
        raise HTTPException(status_code=404, detail="Book not found")
    
    # Check if progress already exists
    progress = db.query(ReadingProgress).filter(
        ReadingProgress.user_id == current_user.id,
        ReadingProgress.book_id == book_id
    ).first()
    
    if progress:
        # Update existing progress
        if progress_data.current_page is not None:
            progress.current_page = progress_data.current_page
        if progress_data.total_pages is not None:
            progress.total_pages = progress_data.total_pages
        if progress_data.start_date is not None:
            progress.start_date = progress_data.start_date
        if progress_data.end_date is not None:
            progress.end_date = progress_data.end_date
        if progress_data.is_completed is not None:
            progress.is_completed = progress_data.is_completed
            if progress_data.is_completed and progress.end_date is None:
                progress.end_date = datetime.now(timezone.utc)
    else:
        # Create new progress
        progress = ReadingProgress(
            user_id=current_user.id,
            book_id=book_id,
            current_page=progress_data.current_page or 0,
            total_pages=progress_data.total_pages,
            start_date=progress_data.start_date or datetime.now(timezone.utc),
            end_date=progress_data.end_date,
            is_completed=progress_data.is_completed or False
        )
        db.add(progress)
    
    db.commit()
    db.refresh(progress)
    return progress

# ----------------- User Preferences -----------------
@api_router.get("/user/preferences", response_model=UserPreferencesResponse, tags=["Preferences"])
def get_user_preferences(current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    """Get user preferences."""
    preferences = db.query(UserPreferences).filter(UserPreferences.user_id == current_user.id).first()
    if not preferences:
        # Create default preferences
        preferences = UserPreferences(user_id=current_user.id)
        db.add(preferences)
        db.commit()
        db.refresh(preferences)
    return preferences

@api_router.put("/user/preferences", response_model=UserPreferencesResponse, tags=["Preferences"])
def update_user_preferences(preferences_update: UserPreferencesUpdate, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    """Update user preferences."""
    preferences = db.query(UserPreferences).filter(UserPreferences.user_id == current_user.id).first()
    if not preferences:
        preferences = UserPreferences(user_id=current_user.id)
        db.add(preferences)
    
    # Update fields if provided
    if preferences_update.monthly_reading_goal is not None:
        preferences.monthly_reading_goal = preferences_update.monthly_reading_goal
    if preferences_update.reading_reminder_enabled is not None:
        preferences.reading_reminder_enabled = preferences_update.reading_reminder_enabled
    if preferences_update.reading_reminder_time is not None:
        preferences.reading_reminder_time = preferences_update.reading_reminder_time
    if preferences_update.reading_reminder_days is not None:
        preferences.reading_reminder_days = preferences_update.reading_reminder_days
    if preferences_update.favorite_genres is not None:
        preferences.favorite_genres = preferences_update.favorite_genres
    
    db.commit()
    db.refresh(preferences)
    return preferences

# ----------------- Reading Statistics -----------------
@api_router.get("/user/statistics", response_model=ReadingStatisticsResponse, tags=["Statistics"])
def get_user_reading_statistics(current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    """Get user's reading statistics."""
    from datetime import datetime
    from sqlalchemy import func
    
    current_year = datetime.now(timezone.utc).year
    current_month = datetime.now(timezone.utc).month
    
    # Books read this year
    books_read_this_year = db.query(func.count(ReadingProgress.id)).filter(
        ReadingProgress.user_id == current_user.id,
        ReadingProgress.is_completed == True,
        func.extract('year', ReadingProgress.end_date) == current_year
    ).scalar() or 0
    
    # Current month progress
    current_month_progress = db.query(func.count(ReadingProgress.id)).filter(
        ReadingProgress.user_id == current_user.id,
        ReadingProgress.is_completed == True,
        func.extract('year', ReadingProgress.end_date) == current_year,
        func.extract('month', ReadingProgress.end_date) == current_month
    ).scalar() or 0
    
    # Total books read
    total_books_read = db.query(func.count(ReadingProgress.id)).filter(
        ReadingProgress.user_id == current_user.id,
        ReadingProgress.is_completed == True
    ).scalar() or 0
    
    # Currently reading
    currently_reading = db.query(func.count(ShelfBook.id)).join(CustomShelf).filter(
        CustomShelf.user_id == current_user.id,
        CustomShelf.name == "Currently Reading"
    ).scalar() or 0
    
    # Monthly reading goal (from preferences)
    preferences = db.query(UserPreferences).filter(UserPreferences.user_id == current_user.id).first()
    monthly_goal = preferences.monthly_reading_goal if preferences else 12
    
    # Average rating given
    avg_rating = db.query(func.avg(Rating.rating)).filter(Rating.user_id == current_user.id).scalar()
    
    # Favorite genre (most read)
    favorite_genre_query = db.query(
        Genre.name,
        func.count(ReadingProgress.id).label('count')
    ).join(Book.genres).join(ReadingProgress, Book.id == ReadingProgress.book_id).filter(
        ReadingProgress.user_id == current_user.id,
        ReadingProgress.is_completed == True
    ).group_by(Genre.id, Genre.name).order_by(func.count(ReadingProgress.id).desc()).first()
    
    favorite_genre = favorite_genre_query[0] if favorite_genre_query else None
    
    return ReadingStatisticsResponse(
        books_read_this_year=books_read_this_year,
        monthly_reading_goal=monthly_goal,
        current_month_progress=current_month_progress,
        total_books_read=total_books_read,
        currently_reading=currently_reading,
        average_rating_given=float(avg_rating) if avg_rating else None,
        favorite_genre=favorite_genre
    )
