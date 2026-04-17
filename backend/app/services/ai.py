from sqlalchemy.orm import Session
import datetime
from datetime import timezone

from app.models import Book, Review
from app.config import settings

def generate_book_review_summary(book_id: int, db: Session):
    """
    Generates a 3-sentence AI summary of all user reviews for a given book
    and saves it to the database using Google Gemini API.
    """
    if not settings.GEMINI_API_KEY or settings.GEMINI_API_KEY == "your_gemini_api_key_here":
        print(f"Skipping summary for book {book_id}: Gemini API Key not configured.")
        return

    try:
        from google import genai
    except Exception as e:
        print(f"Skipping summary for book {book_id}: google-genai is unavailable ({e}).")
        return

    book = db.query(Book).filter(Book.id == book_id).first()
    if not book:
        print(f"Book {book_id} not found.")
        return

    reviews = db.query(Review).filter(Review.book_id == book_id).all()
    if not reviews:
        print(f"No reviews for book {book_id}. Skipping summary.")
        return

    # Compile reviews into a list
    review_texts = [f"- {r.title or 'Review'}: {r.content}" for r in reviews]
    reviews_joined = "\n".join(review_texts)

    prompt = (
        f"Summarize these user reviews for the book '{book.title}'. "
        f"Highlight common praises and criticisms in exactly 3 sentences.\n\nReviews:\n{reviews_joined}"
    )

    try:
        client = genai.Client(api_key=settings.GEMINI_API_KEY)

        print(f"Generating AI Summary for book {book_id}...")
        response = client.models.generate_content(
            model='gemini-2.5-flash',
            contents=prompt,
        )

        # Save to DB
        book.ai_review_summary = response.text.strip()
        book.summary_updated_at = datetime.datetime.now(timezone.utc)
        db.commit()

        print(f"Successfully generated & saved summary for book {book_id}")
    except Exception as e:
        print(f"Failed to generate AI summary for book {book_id} due to an error: {e}")
