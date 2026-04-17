from pydantic_settings import BaseSettings
from typing import Optional

class Settings(BaseSettings):
    """Application settings loaded from environment variables."""

    # Database (SQLite)
    DATABASE_URL: str = "sqlite:///./alcove_database.db"

    # Google Books API
    GOOGLE_BOOKS_API_KEY: Optional[str] = None
    GOOGLE_BOOKS_BASE_URL: str = "https://www.googleapis.com/books/v1"

    # Gemini AI
    GEMINI_API_KEY: Optional[str] = None

    # Firebase (for authentication)
    FIREBASE_CREDENTIALS_PATH: str = "firebase-credentials.json"


    # Server
    DEBUG: bool = False
    ENVIRONMENT: str = "development"
    API_V1_STR: str = "/api/v1"
    PROJECT_NAME: str = "Alcove"

    class Config:
        env_file = ".env"
        case_sensitive = True

settings = Settings()
