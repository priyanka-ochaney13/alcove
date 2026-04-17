from sqlalchemy import Column, Integer, String, Text, Float, DateTime, ForeignKey, Table, Boolean, Date, UniqueConstraint
from sqlalchemy.orm import relationship
from app.database import Base
from datetime import datetime, timezone

# --- JUNCTION TABLES (Many-to-Many) ---

class BookAuthor(Base):
    __tablename__ = "book_author"
    book_id = Column(Integer, ForeignKey("book.id", ondelete="CASCADE"), primary_key=True)
    author_id = Column(Integer, ForeignKey("author.id", ondelete="CASCADE"), primary_key=True)
    role = Column(String(50), nullable=True)
    position = Column(Integer, default=1)

class BookGenre(Base):
    __tablename__ = "book_genre"
    book_id = Column(Integer, ForeignKey("book.id", ondelete="CASCADE"), primary_key=True)
    genre_id = Column(Integer, ForeignKey("genre.id", ondelete="CASCADE"), primary_key=True)
    votes = Column(Integer, default=0)

class ShelfBook(Base):
    __tablename__ = "shelf_book"
    shelf_id = Column(Integer, ForeignKey("custom_shelf.id", ondelete="CASCADE"), primary_key=True)
    book_id = Column(Integer, ForeignKey("book.id", ondelete="CASCADE"), primary_key=True)
    position = Column(Integer, default=0)
    added_at = Column(DateTime, default=datetime.now(timezone.utc))

# --- ENTITY MODELS ---

class User(Base):
    __tablename__ = "user"
    id = Column(Integer, primary_key=True, index=True)
    firebase_uid = Column(String, unique=True, index=True, nullable=False)
    email = Column(String, unique=True, index=True, nullable=False)
    username = Column(String, unique=True, index=True, nullable=False)
    full_name = Column(String, nullable=True)
    bio = Column(Text, nullable=True)
    avatar_url = Column(String, nullable=True)
    is_active = Column(Boolean, default=True)
    is_superuser = Column(Boolean, default=False)
    created_at = Column(DateTime, default=datetime.now(timezone.utc))
    updated_at = Column(DateTime, default=datetime.now(timezone.utc), onupdate=datetime.now(timezone.utc))

    # Relationships
    shelves = relationship("CustomShelf", back_populates="user", cascade="all, delete-orphan")
    reviews = relationship("Review", back_populates="user", cascade="all, delete-orphan")
    ratings = relationship("Rating", back_populates="user", cascade="all, delete-orphan")
    reading_progress = relationship("ReadingProgress", back_populates="user", cascade="all, delete-orphan")
    preferences = relationship("UserPreferences", back_populates="user", cascade="all, delete-orphan")

class CustomShelf(Base):
    __tablename__ = "custom_shelf"
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("user.id", ondelete="CASCADE"), index=True)
    name = Column(String(100), nullable=False)
    description = Column(Text, nullable=True)
    is_default = Column(Boolean, default=False)
    is_exclusive = Column(Boolean, default=False)
    position = Column(Integer, default=0)
    created_at = Column(DateTime, default=datetime.now(timezone.utc))

    __table_args__ = (UniqueConstraint('user_id', 'name', name='_user_shelf_uc'),)

    # Relationships
    user = relationship("User", back_populates="shelves")
    books = relationship("Book", secondary="shelf_book", back_populates="shelves")

class Book(Base):
    __tablename__ = "book"
    id = Column(Integer, primary_key=True, index=True)
    google_id = Column(String, unique=True, index=True, nullable=True)
    title = Column(String, index=True, nullable=False)
    description = Column(Text, nullable=True)
    isbn_10 = Column(String(10), nullable=True, index=True)
    isbn_13 = Column(String(13), nullable=True, index=True)
    published_date = Column(String, nullable=True)
    page_count = Column(Integer, nullable=True)
    language = Column(String, default="en")
    image_url = Column(String, nullable=True)
    average_rating = Column(Float, nullable=True)
    ratings_count = Column(Integer, nullable=True)
    created_at = Column(DateTime, default=datetime.now(timezone.utc))
    updated_at = Column(DateTime, default=datetime.now(timezone.utc), onupdate=datetime.now(timezone.utc))

    # --- AI Feature Columns ---
    ai_review_summary = Column(Text, nullable=True)
    summary_updated_at = Column(DateTime, nullable=True)

    # Relationships
    authors = relationship("Author", secondary="book_author", back_populates="books")
    genres = relationship("Genre", secondary="book_genre", back_populates="books")
    shelves = relationship("CustomShelf", secondary="shelf_book", back_populates="books")
    reviews = relationship("Review", back_populates="book", cascade="all, delete-orphan")
    ratings = relationship("Rating", back_populates="book", cascade="all, delete-orphan")
    reading_progress = relationship("ReadingProgress", back_populates="book", cascade="all, delete-orphan")

class Author(Base):
    __tablename__ = "author"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, nullable=False, index=True)
    bio = Column(Text, nullable=True)
    birth_date = Column(Date, nullable=True)
    image_url = Column(String, nullable=True)
    website = Column(String, nullable=True)
    books_count = Column(Integer, default=0)
    created_at = Column(DateTime, default=datetime.now(timezone.utc))
    updated_at = Column(DateTime, default=datetime.now(timezone.utc), onupdate=datetime.now(timezone.utc))

    # Relationships
    books = relationship("Book", secondary="book_author", back_populates="authors")

class Genre(Base):
    __tablename__ = "genre"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, unique=True, index=True, nullable=False)
    slug = Column(String, unique=True, index=True, nullable=False)
    description = Column(Text, nullable=True)
    books_count = Column(Integer, default=0)
    created_at = Column(DateTime, default=datetime.now(timezone.utc))

    # Relationships
    books = relationship("Book", secondary="book_genre", back_populates="genres")

class Review(Base):
    __tablename__ = "review"
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("user.id", ondelete="CASCADE"), index=True)
    book_id = Column(Integer, ForeignKey("book.id", ondelete="CASCADE"), index=True)
    title = Column(String, nullable=True)
    content = Column(Text, nullable=False)
    rating = Column(Integer, nullable=True)  # 1-5 stars for quick rating
    created_at = Column(DateTime, default=datetime.now(timezone.utc))
    updated_at = Column(DateTime, default=datetime.now(timezone.utc), onupdate=datetime.now(timezone.utc))

    # Relationships
    user = relationship("User", back_populates="reviews")
    book = relationship("Book", back_populates="reviews")

class Rating(Base):
    __tablename__ = "rating"
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("user.id", ondelete="CASCADE"), index=True)
    book_id = Column(Integer, ForeignKey("book.id", ondelete="CASCADE"), index=True)
    rating = Column(Integer, nullable=False)  # 1-5 stars
    created_at = Column(DateTime, default=datetime.now(timezone.utc))
    updated_at = Column(DateTime, default=datetime.now(timezone.utc), onupdate=datetime.now(timezone.utc))

    # Relationships
    user = relationship("User", back_populates="ratings")
    book = relationship("Book", back_populates="ratings")

class ReadingProgress(Base):
    __tablename__ = "reading_progress"
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("user.id", ondelete="CASCADE"), index=True)
    book_id = Column(Integer, ForeignKey("book.id", ondelete="CASCADE"), index=True)
    current_page = Column(Integer, default=0)
    total_pages = Column(Integer, nullable=True)
    start_date = Column(DateTime, nullable=True)
    end_date = Column(DateTime, nullable=True)
    is_completed = Column(Boolean, default=False)
    created_at = Column(DateTime, default=datetime.now(timezone.utc))
    updated_at = Column(DateTime, default=datetime.now(timezone.utc), onupdate=datetime.now(timezone.utc))

    # Relationships
    user = relationship("User", back_populates="reading_progress")
    book = relationship("Book", back_populates="reading_progress")

    __table_args__ = (UniqueConstraint('user_id', 'book_id', name='_user_book_progress_uc'),)

class UserPreferences(Base):
    __tablename__ = "user_preferences"
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("user.id", ondelete="CASCADE"), index=True)
    monthly_reading_goal = Column(Integer, default=12)
    reading_reminder_enabled = Column(Boolean, default=True)
    reading_reminder_time = Column(String, default="20:00")  # HH:MM format
    reading_reminder_days = Column(String, default="1,2,3,4,5,6,7")  # Comma-separated day numbers (1=Monday, 7=Sunday)
    favorite_genres = Column(Text, nullable=True)  # JSON string of genre IDs
    created_at = Column(DateTime, default=datetime.now(timezone.utc))
    updated_at = Column(DateTime, default=datetime.now(timezone.utc), onupdate=datetime.now(timezone.utc))

    # Relationships
    user = relationship("User", back_populates="preferences")

    __table_args__ = (UniqueConstraint('user_id', name='_user_preferences_uc'),)
