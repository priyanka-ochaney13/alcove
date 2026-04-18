"""API router configuration."""
from fastapi import APIRouter, Depends, HTTPException, BackgroundTasks
from sqlalchemy.orm import Session, joinedload
from typing import List, Optional
from datetime import datetime, timezone, date
from app.database import get_db, SessionLocal
from app.models import Book, Genre, Author, Review, CustomShelf, ShelfBook, User, ReadingProgress, UserPreferences, Rating, BookAuthor, BookGenre
from app.schemas import BookResponse, BookDetailResponse, GenreResponse, FirebaseSyncRequest, FirebaseSyncResponse, CustomShelfResponse, CustomShelfCreate, AddBookToShelfRequest, ReviewResponse, ReviewWithUserResponse, ReviewCreate, UserUpdate, UserResponse, ReadingProgressResponse, ReadingProgressCreate, ReadingProgressUpdate, UserPreferencesResponse, UserPreferencesUpdate, ReadingStatisticsResponse, BookCreate
from firebase_admin import auth
from app.services.ai import generate_book_review_summary
from app.api.dependencies import get_current_user
from app.services.auth_service import AuthService
import logging

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
        decoded_token = auth.verify_id_token(request.token, clock_skew_seconds=30)
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
    except ValueError as e:
        raise HTTPException(status_code=503, detail=f"Authentication service error: {e}")
    except Exception as e:
        logging.error(f"An unexpected error occurred during user sync: {e}", exc_info=True)
        raise HTTPException(status_code=401, detail="Invalid authentication credentials")

# ----------------- User Profile -----------------
@api_router.get("/user/profile", response_model=UserResponse, tags=["User"])
def get_user_profile(current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    """Get current user's profile information."""
    return current_user

@api_router.put("/user/profile", response_model=UserResponse, tags=["User"])
def update_user_profile(profile_update: UserUpdate, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    """Update current user's profile information."""
    logging.basicConfig(level=logging.INFO)
    logging.info(f"Updating profile for user {current_user.id}. Received data: {profile_update.dict()}")
    # Update fields if provided
    if profile_update.username is not None:
        # Check if username is already taken by another user
        existing_user = db.query(User).filter(User.username == profile_update.username, User.id != current_user.id).first()
        if existing_user:
            raise HTTPException(status_code=400, detail="Username already taken")
        current_user.username = profile_update.username
        logging.info(f"Updating username to {profile_update.username}")

    if profile_update.email is not None:
        # Check if email is already taken by another user
        existing_user = db.query(User).filter(User.email == profile_update.email, User.id != current_user.id).first()
        if existing_user:
            raise HTTPException(status_code=400, detail="Email already taken")
        current_user.email = profile_update.email
        logging.info(f"Updating email to {profile_update.email}")

    if profile_update.full_name is not None:
        current_user.full_name = profile_update.full_name

    if profile_update.bio is not None:
        current_user.bio = profile_update.bio
        logging.info(f"Updating bio to {profile_update.bio}")

    if profile_update.avatar_url is not None:
        current_user.avatar_url = profile_update.avatar_url
        logging.info(f"Updating avatar_url to {profile_update.avatar_url}")

    db.commit()
    db.refresh(current_user)
    logging.info(f"Profile update committed for user {current_user.id}. New username: {current_user.username}")
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
    
    # Author search - use exists to avoid duplicate results
    if author:
        query = query.filter(
            Book.id.in_(
                db.query(BookAuthor.book_id).join(Author).filter(Author.name.ilike(f"%{author}%"))
            )
        )
    
    # Genre search - use exists to avoid duplicate results
    if genre:
        query = query.filter(
            Book.id.in_(
                db.query(BookGenre.book_id).join(Genre).filter(Genre.name.ilike(f"%{genre}%"))
            )
        )
    
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
    book = db.query(Book).options(
        joinedload(Book.reviews).joinedload(Review.user),
        joinedload(Book.authors),
        joinedload(Book.genres)
    ).filter(Book.id == book_id).first()
    if not book:
        raise HTTPException(status_code=404, detail="Book not found")
        
    # Check if AI Summary exists. If missing, trigger generation asynchronously.
    if not book.ai_review_summary:
        background_tasks.add_task(bg_generate_summary, book_id)
        
    return book

@api_router.post("/books/submit", response_model=BookResponse, tags=["Books"])
def submit_book(book_data: BookCreate, db: Session = Depends(get_db), current_user: User = Depends(get_current_user)):
    """Submit a new book to the database."""
    # Optional: Add admin/moderator check here
    # if not current_user.is_admin:
    #     raise HTTPException(status_code=403, detail="Only administrators can submit new books.")

    # Check if a book with the same title already exists to avoid duplicates
    existing_book = db.query(Book).filter(Book.title.ilike(book_data.title)).first()
    if existing_book:
        raise HTTPException(status_code=400, detail="A book with this title already exists.")

    # Create new Book instance
    new_book = Book(
        title=book_data.title,
        description=book_data.description,
        page_count=book_data.page_count,
        published_date=book_data.published_date,
        image_url=book_data.image_url,
        # Default values for fields not in BookCreate
        ratings_count=0,
        average_rating=0.0
    )

    # Handle authors
    if book_data.authors:
        author_names = [name.strip() for name in book_data.authors.split(',')]
        for name in author_names:
            author = db.query(Author).filter(Author.name.ilike(name)).first()
            if not author:
                author = Author(name=name)
                db.add(author)
            new_book.authors.append(author)

    # Handle genres/categories
    if book_data.categories:
        genre_names = [name.strip() for name in book_data.categories.split(',')]
        for name in genre_names:
            genre = db.query(Genre).filter(Genre.name.ilike(name)).first()
            if not genre:
                # You might want to handle slug creation more robustly
                genre = Genre(name=name, slug=name.lower().replace(' ', '-'))
                db.add(genre)
            new_book.genres.append(genre)

    db.add(new_book)
    db.commit()
    db.refresh(new_book)

    return new_book

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
        return {"shelf": shelf.name if shelf else None, "shelf_id": shelf.id if shelf else None}
    return {"shelf": None, "shelf_id": None}

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
    logging.info(f"Attempting to add book to shelf. Shelf ID: {shelf_id}, Request: {request.dict()}")
    shelf = db.query(CustomShelf).filter(CustomShelf.id == shelf_id).first()
    if not shelf:
        logging.error(f"Shelf with ID {shelf_id} not found.")
        raise HTTPException(status_code=404, detail="Shelf not found")

    book = db.query(Book).filter(Book.id == request.book_id).first()
    if not book:
        logging.error(f"Book with ID {request.book_id} not found.")
        raise HTTPException(status_code=404, detail="Book not found")

    # Check if already in shelf
    exists = db.query(ShelfBook).filter_by(shelf_id=shelf.id, book_id=book.id).first()
    if exists:
        logging.warning(f"Book {book.id} is already in shelf {shelf.id}.")
        return {"status": "success", "message": "Book already in shelf"}

    # If shelf is exclusive, remove from other exclusive shelves (but not this one)
    if shelf.is_exclusive:
        # Get all exclusive shelves except the current target shelf
        other_exclusive_shelves = db.query(CustomShelf.id).filter(
            CustomShelf.user_id == shelf.user_id,
            CustomShelf.is_exclusive == True,
            CustomShelf.id != shelf.id  # Exclude the target shelf
        ).all()
        other_exclusive_shelf_ids = [s[0] for s in other_exclusive_shelves]

        if other_exclusive_shelf_ids:
            # Remove book from other exclusive shelves
            deleted_count = db.query(ShelfBook).filter(
                ShelfBook.book_id == book.id,
                ShelfBook.shelf_id.in_(other_exclusive_shelf_ids)
            ).delete(synchronize_session=False)

            if deleted_count > 0:
                print(f"Removed book {book.id} from {deleted_count} other exclusive shelves")
                logging.info(f"Removed book {book.id} from {deleted_count} other exclusive shelves.")

    # Add book to the target shelf
    shelf_book = ShelfBook(shelf_id=shelf.id, book_id=book.id)
    db.add(shelf_book)
    db.commit()
    logging.info(f"Successfully added book {book.id} to shelf {shelf.id}.")
    return {"status": "success", "message": "Book added to shelf"}

@api_router.delete("/shelves/{shelf_id}/books/{book_id}", tags=["Shelves"])
def remove_book_from_shelf(shelf_id: int, book_id: int, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    """Remove a book from a specific shelf."""
    shelf = db.query(CustomShelf).filter(CustomShelf.id == shelf_id).first()
    if not shelf:
        raise HTTPException(status_code=404, detail="Shelf not found")

    # Check if the shelf belongs to the current user
    if shelf.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="Access denied")

    # Check if book exists in the shelf
    shelf_book = db.query(ShelfBook).filter_by(shelf_id=shelf_id, book_id=book_id).first()
    if not shelf_book:
        raise HTTPException(status_code=404, detail="Book not found in this shelf")

    # Remove the book from the shelf
    db.delete(shelf_book)
    db.commit()

    return {"status": "success", "message": "Book removed from shelf"}

# ----------------- Reviews -----------------
@api_router.get("/user/reviews", response_model=List[ReviewWithUserResponse], tags=["Reviews"])
def get_my_reviews(current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    return db.query(Review).filter(Review.user_id == current_user.id).order_by(Review.created_at.desc()).limit(10).all()

@api_router.get("/books/{book_id}/reviews", response_model=List[ReviewWithUserResponse], tags=["Reviews"])
def get_book_reviews(book_id: int, skip: int = 0, limit: int = 50, db: Session = Depends(get_db)):
    return db.query(Review).filter(Review.book_id == book_id).order_by(Review.created_at.desc()).offset(skip).limit(limit).all()

@api_router.post("/books/{book_id}/reviews", response_model=ReviewWithUserResponse, tags=["Reviews"])
def create_review(book_id: int, review: ReviewCreate, background_tasks: BackgroundTasks, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    db_book = db.query(Book).filter(Book.id == book_id).first()
    if not db_book:
         raise HTTPException(status_code=404, detail="Book not found")
         
    # Check if review exists
    db_review = db.query(Review).filter(Review.book_id == book_id, Review.user_id == current_user.id).first()
    if db_review:
        db_review.title = review.title
        db_review.content = review.content
        db_review.rating = review.rating
    else:
        db_review = Review(
            book_id=book_id,
            user_id=current_user.id,
            title=review.title,
            content=review.content,
            rating=review.rating
        )
        db.add(db_review)
    
    db.commit()
    db.refresh(db_review)
    
    # Invalidate old summary and generate a new one now that reviews have changed
    db_book.ai_review_summary = None 
    db.commit()
    background_tasks.add_task(bg_generate_summary, book_id)
    
    return db_review


@api_router.put("/books/{book_id}/reviews", response_model=ReviewWithUserResponse, tags=["Reviews"])
def update_review(book_id: int, review: ReviewCreate, background_tasks: BackgroundTasks, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    """Update a review for a book."""
    db_book = db.query(Book).filter(Book.id == book_id).first()
    if not db_book:
        raise HTTPException(status_code=404, detail="Book not found")
    
    # Check if review exists
    db_review = db.query(Review).filter(Review.book_id == book_id, Review.user_id == current_user.id).first()
    if not db_review:
        raise HTTPException(status_code=404, detail="Review not found")
    
    # Update review
    db_review.title = review.title
    db_review.content = review.content
    db_review.rating = review.rating
    
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
    """Get reading progress for a specific book. If none exists, create a default one."""
    progress = db.query(ReadingProgress).filter(
        ReadingProgress.user_id == current_user.id,
        ReadingProgress.book_id == book_id
    ).first()

    if not progress:
        # If no progress exists, create a new, empty progress object.
        # This ensures the app always receives a valid object.
        book = db.query(Book).filter(Book.id == book_id).first()
        if not book:
            raise HTTPException(status_code=404, detail="Book not found")

        progress = ReadingProgress(
            user_id=current_user.id,
            book_id=book_id,
            current_page=0,
            total_pages=book.page_count,  # Pre-fill total pages from book data
            start_date=None,
            end_date=None,
            is_completed=False
        )
        db.add(progress)
        db.commit()
        db.refresh(progress)
        logging.info(f"Created new default reading progress for book {book_id} for user {current_user.id}")

    return progress

@api_router.post("/user/books/{book_id}/reading-progress", response_model=ReadingProgressResponse, tags=["Reading Progress"])
def create_or_update_reading_progress(book_id: int, progress_data: ReadingProgressUpdate, current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    """Create or update reading progress for a book."""
    logging.info(f"Received progress update for book {book_id}, user {current_user.id}. Data: {progress_data.dict()}")

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
        logging.info(f"Found existing progress for book {book_id}. Current state: is_completed={progress.is_completed}, end_date={progress.end_date}")
        # Update existing progress
        if progress_data.current_page is not None:
            progress.current_page = progress_data.current_page
        if progress_data.total_pages is not None:
            progress.total_pages = progress_data.total_pages
        if progress_data.start_date is not None:
            progress.start_date = progress_data.start_date
        if progress_data.end_date is not None:
            progress.end_date = progress_data.end_date

        # Handle completion status
        if progress_data.is_completed is not None and progress_data.is_completed:
            if not progress.is_completed: # Only update if it's a new completion
                progress.is_completed = True
                if progress.end_date is None:
                    progress.end_date = date.today()
                logging.info(f"Marking book {book_id} as completed. New state: is_completed={progress.is_completed}, end_date={progress.end_date}")
        # Auto-completion based on page count
        elif progress.total_pages and progress.current_page is not None and progress.current_page >= progress.total_pages:
             if not progress.is_completed:
                progress.is_completed = True
                if progress.end_date is None:
                    progress.end_date = date.today()
                logging.info(f"Auto-completing book {book_id}. New state: is_completed={progress.is_completed}, end_date={progress.end_date}")

    else:
        logging.info(f"No existing progress found for book {book_id}. Creating new entry.")
        # Create new progress
        is_completed = progress_data.is_completed or False
        current_page = progress_data.current_page or 0
        total_pages = progress_data.total_pages

        # Auto-completion for new entries
        if total_pages and current_page >= total_pages:
            is_completed = True

        end_date = progress_data.end_date
        if is_completed and end_date is None:
            end_date = date.today()

        progress = ReadingProgress(
            user_id=current_user.id,
            book_id=book_id,
            current_page=current_page,
            total_pages=total_pages,
            start_date=progress_data.start_date or date.today(),
            end_date=end_date,
            is_completed=is_completed
        )
        db.add(progress)
        logging.info(f"Created new progress for book {book_id}. State: is_completed={progress.is_completed}, end_date={progress.end_date}")

    db.commit()
    db.refresh(progress)
    logging.info(f"Committed progress for book {book_id}. Final state: is_completed={progress.is_completed}, end_date={progress.end_date}")

    # Handle shelf movement if book is marked as completed
    if progress.is_completed:
        logging.info(f"Book {book_id} is completed, attempting to move to 'Read' shelf.")
        try:
            read_shelf = db.query(CustomShelf).filter(
                CustomShelf.user_id == current_user.id,
                CustomShelf.name == "Read"
            ).first()
            if read_shelf:
                # Use the existing add_book_to_shelf logic for consistency
                # This handles removing the book from other exclusive shelves
                add_request = AddBookToShelfRequest(book_id=book_id)
                add_book_to_shelf(shelf_id=read_shelf.id, request=add_request, current_user=current_user, db=db)
                logging.info(f"Successfully moved book {book_id} to 'Read' shelf.")
                # No need to commit here as add_book_to_shelf does it.
        except Exception as e:
            # The operation shouldn't fail the whole progress update.
            # Log the error for debugging.
            logging.error(f"Failed to move book {book_id} to 'Read' shelf for user {current_user.id}: {str(e)}")
            pass # Continue even if shelf movement fails

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
    if preferences_update.yearly_reading_goal is not None:
        preferences.yearly_reading_goal = preferences_update.yearly_reading_goal
    if preferences_update.favorite_genres is not None:
        preferences.favorite_genres = preferences_update.favorite_genres
    if preferences_update.timezone is not None:
        preferences.timezone = preferences_update.timezone
    
    db.commit()
    db.refresh(preferences)
    return preferences

# ----------------- Reading Statistics -----------------
@api_router.get("/user/statistics", response_model=ReadingStatisticsResponse, tags=["Statistics"])
def get_user_reading_statistics(current_user: User = Depends(get_current_user), db: Session = Depends(get_db)):
    """Get user's reading statistics."""
    from sqlalchemy import func, extract

    # Get user preferences for goals
    preferences = db.query(UserPreferences).filter(UserPreferences.user_id == current_user.id).first()
    monthly_goal = preferences.monthly_reading_goal if preferences else 12
    yearly_goal = preferences.yearly_reading_goal if preferences and preferences.yearly_reading_goal > 0 else monthly_goal * 12

    # --- Calculate Books Read This Year ---
    read_shelf = db.query(CustomShelf).filter(
        CustomShelf.user_id == current_user.id,
        CustomShelf.name == "Read"
    ).first()

    books_read_this_year = 0
    if read_shelf:
        current_year = datetime.now().year
        
        # Query ShelfBook for books added to the "Read" shelf this year
        books_read_this_year = db.query(ShelfBook).filter(
            ShelfBook.shelf_id == read_shelf.id,
            extract('year', ShelfBook.added_at) == current_year
        ).count()

    # --- Other Statistics ---
    total_books_read = db.query(ShelfBook).filter(ShelfBook.shelf_id == read_shelf.id).count() if read_shelf else 0
    
    currently_reading_shelf = db.query(CustomShelf).filter(
        CustomShelf.user_id == current_user.id,
        CustomShelf.name == "Currently Reading"
    ).first()
    currently_reading_count = db.query(ShelfBook).filter(ShelfBook.shelf_id == currently_reading_shelf.id).count() if currently_reading_shelf else 0

    want_to_read_shelf = db.query(CustomShelf).filter(
        CustomShelf.user_id == current_user.id,
        CustomShelf.name == "Want to Read"
    ).first()
    want_to_read_count = db.query(ShelfBook).filter(ShelfBook.shelf_id == want_to_read_shelf.id).count() if want_to_read_shelf else 0

    # --- Calculate Average Rating ---
    # Combine ratings from both the 'rating' and 'review' tables
    user_ratings = db.query(Rating.rating).filter(Rating.user_id == current_user.id).all()
    user_review_ratings = db.query(Review.rating).filter(Review.user_id == current_user.id, Review.rating.isnot(None)).all()
    
    all_ratings = [r[0] for r in user_ratings] + [r[0] for r in user_review_ratings]
    
    avg_rating = sum(all_ratings) / len(all_ratings) if all_ratings else None
    logging.info(f"Calculated average rating for user {current_user.id}: {avg_rating}")

    return ReadingStatisticsResponse(
        books_read_this_year=books_read_this_year,
        monthly_reading_goal=monthly_goal,
        yearly_reading_goal=yearly_goal,
        total_books_read=total_books_read,
        currently_reading=currently_reading_count,
        want_to_read=want_to_read_count,
        average_rating_given=avg_rating
    )

