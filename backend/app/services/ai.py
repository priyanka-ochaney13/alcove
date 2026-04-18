from sqlalchemy.orm import Session
from datetime import datetime, timezone
from sqlalchemy import func

from app.models import Book, Review, Rating
from app.config import settings

def generate_fallback_summary(book: Book, reviews: list, db: Session) -> str:
    """
    Generate a fallback summary based on statistical data when AI is unavailable.
    """
    review_count = len(reviews)

    # Get average rating from reviews and ratings table
    avg_rating = None
    if reviews:
        # Check if reviews have ratings
        review_ratings = [r.rating for r in reviews if r.rating is not None]
        if review_ratings:
            avg_rating = sum(review_ratings) / len(review_ratings)

    # If no rating in reviews, check ratings table
    if avg_rating is None:
        rating_stats = db.query(func.avg(Rating.rating), func.count(Rating.id)).filter(
            Rating.book_id == book.id
        ).first()
        if rating_stats and rating_stats[1] > 0:  # count > 0
            avg_rating = float(rating_stats[0])

    # Generate fallback summary
    if review_count == 0:
        return "No reviews available for this book yet."

    summary_parts = []

    if avg_rating is not None:
        summary_parts.append(f"This book has received an average rating of {avg_rating:.1f} stars")

    summary_parts.append(f"from {review_count} reader{'s' if review_count != 1 else ''}")

    if book.ratings_count and book.ratings_count > 0:
        summary_parts.append(f"(Google Books: {book.ratings_count} ratings)")

    summary = " ".join(summary_parts) + "."

    # Add a note about AI summary being unavailable
    summary += " AI-powered summary currently unavailable."

    return summary

def generate_book_review_summary(book_id: int, db: Session):
    """
    Generates a 3-sentence AI summary of all user reviews for a given book
    and saves it to the database using Google Gemini API.
    Always sets some summary to prevent repeated background task attempts.
    """
    book = db.query(Book).filter(Book.id == book_id).first()
    if not book:
        print(f"Book {book_id} not found.")
        return

    reviews = db.query(Review).filter(Review.book_id == book_id).all()

    # Always set a summary_updated_at timestamp
    current_time = datetime.now(timezone.utc)

    try:
        # Check if AI is available
        if not settings.GEMINI_API_KEY or settings.GEMINI_API_KEY == "your_gemini_api_key_here":
            print(f"Gemini API key not configured for book {book_id}. Using fallback summary.")
            fallback_summary = generate_fallback_summary(book, reviews, db)
            book.ai_review_summary = fallback_summary
            book.summary_updated_at = current_time
            db.commit()
            return

        # Check if google-genai library is available
        try:
            from google import genai
        except Exception as e:
            print(f"google-genai library unavailable for book {book_id}: {e}. Using fallback summary.")
            fallback_summary = generate_fallback_summary(book, reviews, db)
            book.ai_review_summary = fallback_summary
            book.summary_updated_at = current_time
            db.commit()
            return

        if not reviews:
            print(f"No reviews for book {book_id}. Setting empty summary.")
            book.ai_review_summary = "No reviews available for this book yet."
            book.summary_updated_at = current_time
            db.commit()
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

            # Save AI-generated summary
            book.ai_review_summary = response.text.strip()
            book.summary_updated_at = current_time
            db.commit()

            print(f"Successfully generated & saved AI summary for book {book_id}")

        except Exception as e:
            print(f"AI API call failed for book {book_id}: {e}. Using fallback summary.")
            fallback_summary = generate_fallback_summary(book, reviews, db)
            book.ai_review_summary = fallback_summary
            book.summary_updated_at = current_time
            db.commit()

    except Exception as e:
        # Catch-all for any unexpected errors
        print(f"Unexpected error in summary generation for book {book_id}: {e}. Using basic fallback.")
        try:
            book.ai_review_summary = "Summary temporarily unavailable due to technical issues."
            book.summary_updated_at = current_time
            db.commit()
        except Exception as commit_error:
            print(f"Failed to save fallback summary for book {book_id}: {commit_error}")
            db.rollback()
