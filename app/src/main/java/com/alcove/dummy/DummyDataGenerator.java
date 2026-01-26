package com.alcove.dummy;

import com.alcove.Book;
import com.alcove.Review;
import com.alcove.MyReviewItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for generating realistic dummy data for the app
 */
public class DummyDataGenerator {

    // ========== BOOKS DATA ==========
    public static List<Book> getPopularBooks() {
        List<Book> books = new ArrayList<>();
        books.add(new Book(1, "The Great Gatsby", "F. Scott Fitzgerald", 4.2f, 128500));
        books.add(new Book(2, "To Kill a Mockingbird", "Harper Lee", 4.8f, 215000));
        books.add(new Book(3, "1984", "George Orwell", 4.5f, 198000));
        books.add(new Book(4, "Pride and Prejudice", "Jane Austen", 4.6f, 175000));
        books.add(new Book(5, "The Catcher in the Rye", "J.D. Salinger", 4.1f, 95000));
        return books;
    }

    public static List<Book> getTrendingBooks() {
        List<Book> books = new ArrayList<>();
        books.add(new Book(6, "It Ends with Us", "Colleen Hoover", 4.7f, 450000));
        books.add(new Book(7, "Fourth Wing", "Rebecca Yarros", 4.8f, 380000));
        books.add(new Book(8, "The Seven Husbands of Evelyn Hugo", "Taylor Jenkins Reid", 4.6f, 295000));
        books.add(new Book(9, "Romantic Comedy", "Curtis Sittenfeld", 4.3f, 145000));
        books.add(new Book(10, "Lessons in Chemistry", "Bonnie Garmus", 4.5f, 210000));
        return books;
    }

    public static List<Book> getRecommendedBooks() {
        List<Book> books = new ArrayList<>();
        books.add(new Book(11, "Brave New World", "Aldous Huxley", 4.0f, 125000));
        books.add(new Book(12, "The Midnight Library", "Matt Haig", 4.2f, 380000));
        books.add(new Book(13, "Verity", "Colleen Hoover", 4.6f, 520000));
        books.add(new Book(14, "Project Hail Mary", "Andy Weir", 4.7f, 285000));
        books.add(new Book(15, "The Housemaid", "Freida McFadden", 4.5f, 195000));
        return books;
    }

    public static List<Book> getSearchResults() {
        List<Book> books = new ArrayList<>();
        books.add(new Book(1, "The Great Gatsby", "F. Scott Fitzgerald", 4.2f, 128500));
        books.add(new Book(16, "The Beautiful and Damned", "F. Scott Fitzgerald", 3.8f, 32000));
        books.add(new Book(17, "Tender Is the Night", "F. Scott Fitzgerald", 3.7f, 28500));
        books.add(new Book(18, "This Side of Paradise", "F. Scott Fitzgerald", 3.6f, 21000));
        books.add(new Book(19, "The Great Gatsby (Annotated)", "F. Scott Fitzgerald", 4.3f, 15000));
        books.add(new Book(20, "Gatsby", "Various", 3.5f, 8000));
        return books;
    }

    public static List<Book> getCurrentlyReading() {
        List<Book> books = new ArrayList<>();
        books.add(new Book(6, "It Ends with Us", "Colleen Hoover", 4.7f, 450000));
        books.add(new Book(12, "The Midnight Library", "Matt Haig", 4.2f, 380000));
        return books;
    }

    public static List<Book> getWantToRead() {
        List<Book> books = new ArrayList<>();
        books.add(new Book(1, "The Great Gatsby", "F. Scott Fitzgerald", 4.2f, 128500));
        books.add(new Book(3, "1984", "George Orwell", 4.5f, 198000));
        books.add(new Book(7, "Fourth Wing", "Rebecca Yarros", 4.8f, 380000));
        books.add(new Book(14, "Project Hail Mary", "Andy Weir", 4.7f, 285000));
        return books;
    }

    public static List<Book> getAlreadyRead() {
        List<Book> books = new ArrayList<>();
        books.add(new Book(2, "To Kill a Mockingbird", "Harper Lee", 4.8f, 215000));
        books.add(new Book(4, "Pride and Prejudice", "Jane Austen", 4.6f, 175000));
        books.add(new Book(5, "The Catcher in the Rye", "J.D. Salinger", 4.1f, 95000));
        books.add(new Book(11, "Brave New World", "Aldous Huxley", 4.0f, 125000));
        return books;
    }

    // ========== REVIEWS DATA ==========
    public static List<Review> getBookReviews() {
        List<Review> reviews = new ArrayList<>();
        reviews.add(new Review(1, "Sarah M.", 5, "Absolutely stunning! This book completely captured my heart. The prose is beautiful and the story is unforgettable."));
        reviews.add(new Review(2, "John D.", 5, "A masterpiece of American literature. Fitzgerald's writing is exquisite and the characters are unforgettable."));
        reviews.add(new Review(3, "Emma L.", 4, "Great story, though a bit slow in some parts. Still highly recommended for anyone who loves classics."));
        reviews.add(new Review(4, "Mike T.", 4, "Well-written and engaging. The ending was perfect. Loved it!"));
        reviews.add(new Review(5, "Lisa K.", 5, "One of the best books I've ever read. Gatsby's story is timeless and deeply moving."));
        reviews.add(new Review(6, "David R.", 3, "Good book but not as amazing as everyone says. Still worth reading though."));
        return reviews;
    }

    public static List<MyReviewItem> getUserReviews() {
        List<MyReviewItem> reviews = new ArrayList<>();
        reviews.add(new MyReviewItem(1, "The Great Gatsby", "F. Scott Fitzgerald", 5, "Absolutely phenomenal! This is my all-time favorite book. The writing style is gorgeous and the story is captivating from start to finish."));
        reviews.add(new MyReviewItem(2, "1984", "George Orwell", 4, "A terrifying and brilliant dystopian novel. It made me think deeply about society and control. Highly recommended!"));
        reviews.add(new MyReviewItem(3, "To Kill a Mockingbird", "Harper Lee", 5, "A timeless classic that tackles important themes. Scout's journey is beautiful and the message is powerful."));
        reviews.add(new MyReviewItem(4, "Pride and Prejudice", "Jane Austen", 5, "Elizabeth Bennet is one of the most likable characters in literature. This romance is charming and witty."));
        return reviews;
    }

    // ========== USER PROFILE DATA ==========
    public static class UserProfile {
        public String name = "Alexandra Chen";
        public String email = "alex.chen@email.com";
        public int booksRead = 47;
        public int currentlyReading = 2;
        public int wantToRead = 18;
        public String bio = "Bookworm 📚 | Coffee lover ☕ | Always reading | Book recommendations welcomed!";
        public String joinDate = "Joined Jan 2024";
    }

    public static UserProfile getUserProfile() {
        return new UserProfile();
    }
}
