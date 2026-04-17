"""Authentication service for user management with Firebase."""
from datetime import datetime, timezone
from typing import Optional, Tuple
from sqlalchemy.orm import Session
from app.models import User


class AuthService:
    """Service for user management with Firebase authentication."""

    @staticmethod
    def create_or_update_user_from_firebase(
        db: Session,
        firebase_uid: str,
        email: str,
        display_name: Optional[str] = None,
        photo_url: Optional[str] = None
    ) -> Tuple[User, bool]:
        """
        Create or update a user from Firebase authentication data.

        Args:
            db: Database session
            firebase_uid: Firebase user ID
            email: User email
            display_name: User display name from Firebase
            photo_url: User photo URL from Firebase

        Returns:
            Tuple of (User, is_newly_created)
        """
        # Check if user exists by firebase_uid
        user = db.query(User).filter(User.firebase_uid == firebase_uid).first()
        
        if user:
            # Update user info if changed
            if user.full_name != display_name or user.email != email:
                user.full_name = display_name
                user.email = email
                user.updated_at = datetime.now(timezone.utc)
                db.add(user)
                db.commit()
                db.refresh(user)
            return user, False
        
        # Create new user
        # Generate username from email if display_name not provided
        username = display_name.replace(" ", "").lower() if display_name else email.split("@")[0]
        
        # Ensure username is unique
        base_username = username
        counter = 1
        while db.query(User).filter(User.username == username).first():
            username = f"{base_username}{counter}"
            counter += 1
        
        new_user = User(
            firebase_uid=firebase_uid,
            email=email,
            username=username,
            full_name=display_name,
            is_active=True
        )
        
        db.add(new_user)
        db.commit()
        db.refresh(new_user)
        
        return new_user, True

