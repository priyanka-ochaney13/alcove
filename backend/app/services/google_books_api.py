"""Service for interacting with Google Books API."""
import httpx
from typing import Optional, List, Dict, Any
from app.config import settings
from app.schemas import BookCreate
import json


class GoogleBooksService:
    """Service to interact with Google Books API."""

    BASE_URL = settings.GOOGLE_BOOKS_BASE_URL

    @staticmethod
    async def search_books(
        query: str,
        max_results: int = 10,
        start_index: int = 0
    ) -> Dict[str, Any]:
        """Search for books on Google Books API."""
        max_results = min(max_results, 40)

        params = {
            "q": query,
            "maxResults": max_results,
            "startIndex": start_index,
        }

        if settings.GOOGLE_BOOKS_API_KEY:
            params["key"] = settings.GOOGLE_BOOKS_API_KEY

        try:
            async with httpx.AsyncClient() as client:
                response = await client.get(
                    f"{GoogleBooksService.BASE_URL}/volumes",
                    params=params,
                    timeout=10.0
                )
                response.raise_for_status()
                return response.json()
        except httpx.HTTPError as e:
            return {"items": [], "totalItems": 0, "error": str(e)}

    @staticmethod
    async def get_book_by_id(volume_id: str) -> Optional[Dict[str, Any]]:
        """Get a specific book by Google Books volumeId."""
        params = {}
        if settings.GOOGLE_BOOKS_API_KEY:
            params["key"] = settings.GOOGLE_BOOKS_API_KEY

        try:
            async with httpx.AsyncClient() as client:
                response = await client.get(
                    f"{GoogleBooksService.BASE_URL}/volumes/{volume_id}",
                    params=params,
                    timeout=10.0
                )
                response.raise_for_status()
                return response.json()
        except httpx.HTTPError:
            return None

    @staticmethod
    def parse_book_data(volume_data: Dict[str, Any]) -> Optional[BookCreate]:
        """Parse Google Books API response into BookCreate schema."""
        try:
            volume_id = volume_data.get("id")
            info = volume_data.get("volumeInfo", {})

            if not volume_id or not info.get("title"):
                return None

            authors = info.get("authors", [])
            authors_str = ", ".join(authors) if authors else None

            categories = info.get("categories", [])
            categories_str = json.dumps(categories) if categories else None

            identifiers = info.get("industryIdentifiers", [])
            isbn_10 = None
            isbn_13 = None

            for identifier in identifiers:
                if identifier.get("type") == "ISBN_10":
                    isbn_10 = identifier.get("identifier")
                elif identifier.get("type") == "ISBN_13":
                    isbn_13 = identifier.get("identifier")

            return BookCreate(
                google_id=volume_id,
                title=info.get("title", "Unknown"),
                authors=authors_str,
                description=info.get("description"),
                isbn_10=isbn_10,
                isbn_13=isbn_13,
                published_date=info.get("publishedDate"),
                page_count=info.get("pageCount"),
                image_url=info.get("imageLinks", {}).get("thumbnail"),
                categories=categories_str,
                average_rating=info.get("averageRating"),
                ratings_count=info.get("ratingsCount"),
            )
        except Exception as e:
            print(f"Error parsing book data: {e}")
            return None

    @staticmethod
    async def search_and_parse(
        query: str,
        max_results: int = 10,
        start_index: int = 0
    ) -> List[Dict[str, Any]]:
        """Search Google Books API and return parsed book data."""
        results = await GoogleBooksService.search_books(query, max_results, start_index)
        books = []

        for item in results.get("items", []):
            parsed = GoogleBooksService.parse_book_data(item)
            if parsed:
                books.append(parsed.model_dump(exclude_none=True))

        return books
