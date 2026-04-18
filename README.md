# Alcove

**Alcove** is a comprehensive full-stack application designed to provide a seamless user experience through its Android mobile interface and a powerful Python-based backend. The project is structured to support modern development practices, ensuring scalability and maintainability.

The backend is built with **FastAPI**, a high-performance web framework, which offers a robust API to the mobile client. It leverages **SQLAlchemy** for database interactions, providing a flexible and powerful Object-Relational Mapper (ORM). Database migrations are managed by **Alembic**, ensuring that the database schema evolves in a controlled and predictable manner.

The Android application serves as the frontend, offering an intuitive and responsive user interface. It is a native Android app developed using the latest tools and technologies, with **Gradle** as the build system.

This README provides a guide to setting up and running both the backend and the Android application, along with an overview of the project's structure and key technologies.

## About Alcove

Alcove is a book lover's companion, designed to enhance the reading experience. It's a place to discover new books, track your reading progress, and connect with a community of fellow readers.

### Key Features

- **Discover**: Browse a vast library of books, with recommendations tailored to your tastes. Explore popular, trending, and recommended books to find your next great read.
- **Track**: Keep a log of your reading journey. Move books to your "Currently Reading," "Want to Read," and "Read" shelves. Track your progress page by page and set personal reading goals.
- **Review and Rate**: Share your thoughts on books you've read by writing reviews and giving ratings. See what others are saying and get AI-powered summaries of reviews.
- **Personal Shelves**: Organize your library with custom shelves. Create as many shelves as you like to categorize your books your way.
- **Community**: See what your friends are reading and discover new books through their recommendations.

## Database Design

The database is designed to support the core features of the application, with a focus on scalability and data integrity. The schema includes tables for books, authors, genres, users, reviews, shelves, and reading progress.

[View the database schema diagram](https://drive.google.com/file/d/11lpbKGZuDMezHc7N5yeYXu9nD3BVk2FK/view?usp=drive_link)

This relational design allows for efficient querying and a flexible structure that can evolve with the application's needs.

## Demo

[Watch the demo video](https://drive.google.com/file/d/182MxVtQiKoHlAjLVngEmfoJ5-1ws8EfS/view?usp=sharing)

## Backend

The backend is built using the FastAPI framework and provides a robust API for the Alcove mobile application.

### Key Technologies

- **FastAPI**: A modern, high-performance web framework for building APIs with Python.
- **SQLAlchemy**: A powerful SQL toolkit and Object-Relational Mapper (ORM) for Python.
- **Alembic**: A lightweight database migration tool for SQLAlchemy.
- **Pydantic**: Data validation and settings management using Python type annotations.
- **Firebase Admin**: For integration with Firebase services.
- **Google Generative AI**: For leveraging Google's generative AI capabilities.

### Getting Started

1. **Install dependencies**:
   ```bash
   pip install -r backend/requirements.txt
   ```

2. **Run the application**:
   ```bash
   uvicorn app.main:app --reload
   ```

## Android App

The Android application is the client-side of Alcove, providing a user-friendly interface to interact with the backend services.

### Key Technologies

- **Android SDK**: The project is a native Android application.
- **Gradle**: The build system used for the Android app.

### Getting Started

1. **Open the project**: Open the `alcove` directory in Android Studio.
2. **Build the project**: Gradle will automatically sync and build the project.
3. **Run the app**: Run the application on an emulator or a physical device.

## Project Structure

```
alcove/
├── app/                  # Android application source code
├── backend/              # FastAPI backend source code
│   ├── app/              # Main application package
│   │   ├── api/          # API endpoints
│   │   ├── services/     # Business logic
│   │   ├── models.py     # SQLAlchemy models
│   │   ├── schemas.py    # Pydantic schemas
│   │   └── main.py       # FastAPI application entry point
│   ├── requirements.txt  # Python dependencies
│   └── ...
├── alembic/              # Alembic database migrations
└── ...
```
