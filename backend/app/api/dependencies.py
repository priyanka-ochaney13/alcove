"""Shared dependencies for all endpoints."""
from fastapi import Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.database import get_db
from app.models import User
from app.services.firebase_auth import FirebaseAuthService, firebase_initialized
from app.services.auth_service import AuthService
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials

security = HTTPBearer()


async def get_current_user(
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: Session = Depends(get_db)
) -> User:
    """
    Dependency to get current authenticated user from Firebase token.

    Used across all protected endpoints.
    Auto-creates user if this is their first request.
    """
    # For development: if Firebase is not initialized, create a mock user
    if not firebase_initialized:
        print("Firebase not initialized - using mock user for development")
        # Create or get a mock user
        mock_uid = "dev-user-123"
        user = db.query(User).filter(User.firebase_uid == mock_uid).first()

        if not user:
            try:
                user, _ = AuthService.create_or_update_user_from_firebase(
                    db=db,
                    firebase_uid=mock_uid,
                    email="dev@example.com",
                    display_name="Dev User"
                )
            except Exception as e:
                raise HTTPException(
                    status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                    detail=f"Failed to create mock user: {str(e)}"
                )
        return user

    token = credentials.credentials
    decoded_token = FirebaseAuthService.verify_token(token)

    if not decoded_token:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or expired Firebase token"
        )

    uid = decoded_token.get("uid")
    email = decoded_token.get("email")
    display_name = decoded_token.get("name")
    
    if not uid or not email:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Firebase token missing required fields (uid, email)"
        )
    
    # Try to find existing user
    user = db.query(User).filter(User.firebase_uid == uid).first()
    
    if not user:
        # Auto-create user on first access
        try:
            user, _ = AuthService.create_or_update_user_from_firebase(
                db=db,
                firebase_uid=uid,
                email=email,
                display_name=display_name
            )
        except Exception as e:
            raise HTTPException(
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                detail=f"Failed to create user: {str(e)}"
            )
    
    if not user.is_active:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="User account is inactive"
        )

    return user
