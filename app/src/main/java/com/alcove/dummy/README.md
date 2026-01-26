# Dummy Data Package

This package contains all dummy/mock data used for testing and UI demonstration purposes.

## Contents

### DummyDataGenerator.java
Provides static methods to generate realistic dummy data for:
- **Books**: Popular, Trending, Recommended, Search Results, Currently Reading, Want to Read, Already Read
- **Reviews**: Book reviews and user reviews
- **User Profile**: Sample user profile data

## Usage

All Activities import and use these dummy data generators:
- `MainActivity` - Displays Popular, Trending, and Recommended books
- `SearchActivity` - Shows search results
- `MyBookshelfActivity` - Displays user's book lists (Currently Reading, Want to Read, Already Read)
- `BookDetailsActivity` - Shows book reviews
- `MyReviewsActivity` - Displays user's reviews
- `UserProfileActivity` - Shows user profile information

## Note

This dummy data is **NOT** part of your actual UI screens. It's purely for testing and demonstration purposes. 

When you integrate a real backend/API, you can replace these dummy data methods with actual API calls by modifying the Activities to fetch data from your backend instead of this generator.

## Actual Books Used

The dummy data includes real book titles and authors such as:
- The Great Gatsby - F. Scott Fitzgerald
- To Kill a Mockingbird - Harper Lee
- 1984 - George Orwell
- Pride and Prejudice - Jane Austen
- And many more popular titles...

This ensures the UI looks realistic and professional.
