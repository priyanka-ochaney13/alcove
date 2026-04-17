"""Firebase authentication service."""
import firebase_admin
from firebase_admin import credentials, auth as firebase_auth
from typing import Optional, Dict
from app.config import settings

# Initialize Firebase
firebase_initialized = True
try:
    cred = credentials.Certificate(settings.FIREBASE_CREDENTIALS_PATH)
    firebase_admin.initialize_app(cred)
    print("Firebase initialized successfully")
except Exception as e:
    print(f"Warning: Firebase initialization failed: {e}")
    print("Make sure firebase-credentials.json contains a valid service account key")
    print("Get it from: Firebase Console → Project Settings → Service Accounts → Generate New Private Key")
    firebase_initialized = False


class FirebaseAuthService:
    """Service for Firebase authentication."""

    @staticmethod
    def verify_token(token: str) -> Optional[Dict]:
        """
        Verify a Firebase ID token.

        Args:
            token: Firebase ID token from client

        Returns:
            Dictionary with user info if valid, None if invalid
        """
        try:
            decoded_token = firebase_auth.verify_id_token(token)
            return decoded_token
        except Exception as e:
            print(f"Token verification failed: {e}")
            return None

    @staticmethod
    def get_user_info(uid: str) -> Optional[Dict]:
        """
        Get user information from Firebase.

        Args:
            uid: Firebase user ID

        Returns:
            User information dictionary
        """
        try:
            user = firebase_auth.get_user(uid)
            return {
                "uid": user.uid,
                "email": user.email,
                "display_name": user.display_name,
                "photo_url": user.photo_url,
                "email_verified": user.email_verified,
            }
        except Exception as e:
            print(f"Error fetching user: {e}")
            return None

    @staticmethod
    def extract_uid_from_token(token: str) -> Optional[str]:
        """
        Extract Firebase UID from ID token.

        Args:
            token: Firebase ID token

        Returns:
            Firebase UID or None if invalid
        """
        decoded = FirebaseAuthService.verify_token(token)
        if decoded:
            return decoded.get("uid")
        return None
