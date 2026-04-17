from pydantic import BaseModel, EmailStr, Field
from typing import Optional, List
from datetime import datetime, date

# ============= Ref Schemas (to avoid circular imports later) =============
class ReviewResponse(BaseModel):
    id: int
    user_id: int
    book_id: int
    title: Optional[str] = None
    content: str
    rating: Optional[int] = None
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True

class SimpleUserResponse(BaseModel):
    id: int
    username: str
    full_name: Optional[str] = None

    class Config:
        from_attributes = True

class ReviewWithUserResponse(ReviewResponse):
    user: Optional[SimpleUserResponse] = None

class RatingResponse(BaseModel):
    id: int
    user_id: int
    book_id: int
    rating: int
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True

# ============= User Schemas =============
class UserBase(BaseModel):
    email: EmailStr
    username: str
    full_name: Optional[str] = None
    bio: Optional[str] = None
    avatar_url: Optional[str] = None

class UserCreate(UserBase):
    pass

class UserUpdate(BaseModel):
    email: Optional[EmailStr] = None
    username: Optional[str] = None
    full_name: Optional[str] = None
    bio: Optional[str] = None
    avatar_url: Optional[str] = None

class UserResponse(UserBase):
    id: int
    firebase_uid: str
    is_active: bool
    is_superuser: bool
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True

class FirebaseSyncRequest(BaseModel):
    """Firebase token from Android app for sync-user endpoint."""
    token: str = Field(..., description="Firebase ID token from Android app")

class FirebaseSyncResponse(BaseModel):
    """Response from sync-user endpoint."""
    user: UserResponse
    is_new_user: bool

    class Config:
        from_attributes = True

# ============= Author & Genre Schemas =============
class AuthorBase(BaseModel):
    name: str
    bio: Optional[str] = None
    birth_date: Optional[date] = None
    image_url: Optional[str] = None
    website: Optional[str] = None

class AuthorResponse(AuthorBase):
    id: int
    books_count: int
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True

class GenreBase(BaseModel):
    name: str
    slug: str
    description: Optional[str] = None

class GenreResponse(GenreBase):
    id: int
    books_count: int
    created_at: datetime

    class Config:
        from_attributes = True

# ============= Custom Shelf Schemas =============
class CustomShelfBase(BaseModel):
    name: str
    description: Optional[str] = None
    is_exclusive: Optional[bool] = False

class CustomShelfCreate(CustomShelfBase):
    pass

class CustomShelfResponse(CustomShelfBase):
    id: int
    user_id: int
    is_default: bool
    position: int
    created_at: datetime

    class Config:
        from_attributes = True

# ============= Book Schemas =============
class BookBase(BaseModel):
    title: str
    description: Optional[str] = None
    isbn_10: Optional[str] = None
    isbn_13: Optional[str] = None
    published_date: Optional[str] = None
    page_count: Optional[int] = None
    language: str = "en"
    image_url: Optional[str] = None
    average_rating: float = 0.0
    ratings_count: int = 0

class BookResponse(BookBase):
    id: int
    google_id: Optional[str] = None
    created_at: datetime
    updated_at: datetime
    authors: List[AuthorResponse] = []
    genres: List[GenreResponse] = []

    class Config:
        from_attributes = True

class BookDetailResponse(BookResponse):
    reviews: List[ReviewWithUserResponse] = []
    ai_review_summary: Optional[str] = None
    summary_updated_at: Optional[datetime] = None

class BookCreate(BaseModel):
    google_id: Optional[str] = None
    title: str
    authors: Optional[str] = None  # Comma-separated string for now
    description: Optional[str] = None
    isbn_10: Optional[str] = None
    isbn_13: Optional[str] = None
    published_date: Optional[str] = None
    page_count: Optional[int] = None
    image_url: Optional[str] = None
    categories: Optional[str] = None  # JSON string
    average_rating: Optional[float] = None
    ratings_count: Optional[int] = None

# ============= Review Create & Rating Create =============
class ReviewBaseDef(BaseModel):
    title: Optional[str] = None
    content: str = Field(..., min_length=1, max_length=5000)

class ReviewCreate(ReviewBaseDef):
    pass

class ReviewUpdate(ReviewBaseDef):
    pass

class RatingBaseDef(BaseModel):
    rating: int = Field(..., ge=1, le=5)

class RatingCreate(RatingBaseDef):
    pass

class RatingUpdate(RatingBaseDef):
    pass

class AddBookToShelfRequest(BaseModel):
    book_id: int

# ============= Reading Progress Schemas =============
class ReadingProgressBase(BaseModel):
    current_page: int = 0
    total_pages: Optional[int] = None
    start_date: Optional[datetime] = None
    end_date: Optional[datetime] = None
    is_completed: bool = False

class ReadingProgressCreate(ReadingProgressBase):
    book_id: int

class ReadingProgressUpdate(BaseModel):
    current_page: Optional[int] = None
    total_pages: Optional[int] = None
    start_date: Optional[datetime] = None
    end_date: Optional[datetime] = None
    is_completed: Optional[bool] = None

class ReadingProgressResponse(ReadingProgressBase):
    id: int
    user_id: int
    book_id: int
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True

# ============= User Preferences Schemas =============
class UserPreferencesBase(BaseModel):
    monthly_reading_goal: int = 12
    reading_reminder_enabled: bool = True
    reading_reminder_time: str = "20:00"
    reading_reminder_days: str = "1,2,3,4,5,6,7"
    favorite_genres: Optional[str] = None

class UserPreferencesCreate(UserPreferencesBase):
    pass

class UserPreferencesUpdate(BaseModel):
    monthly_reading_goal: Optional[int] = None
    reading_reminder_enabled: Optional[bool] = None
    reading_reminder_time: Optional[str] = None
    reading_reminder_days: Optional[str] = None
    favorite_genres: Optional[str] = None

class UserPreferencesResponse(UserPreferencesBase):
    id: int
    user_id: int
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True

# ============= Statistics Schemas =============
class ReadingStatisticsResponse(BaseModel):
    books_read_this_year: int
    monthly_reading_goal: int
    current_month_progress: int
    total_books_read: int
    currently_reading: int
    average_rating_given: Optional[float] = None
    favorite_genre: Optional[str] = None
