@echo off
REM Quick start script for Alcove Backend
REM Run this from E:\alcove\backend

echo.
echo ====================================
echo   ALCOVE BACKEND QUICK START
echo ====================================
echo.

REM Check if venv exists
if not exist "venv" (
    echo [1/4] Creating virtual environment...
    python -m venv venv
    echo.
)

echo [2/4] Activating virtual environment...
call venv\Scripts\activate.bat
echo Virtual environment activated!
echo.

REM Check if dependencies are installed
echo [3/4] Checking dependencies...
pip list | findstr /i "fastapi" >nul
if errorlevel 1 (
    echo Installing dependencies...
    pip install -r requirements.txt
) else (
    echo Dependencies already installed!
)
echo.

REM Check if .env file exists
if not exist ".env" (
    echo [4/4] Creating .env file from template...
    copy .env.example .env
    echo.
    echo WARNING: Please edit .env and update DATABASE_URL with your credentials!
    echo Open .env file and update these values:
    echo   - DATABASE_URL: your PostgreSQL connection string
    echo   - SECRET_KEY: a random secret string
    echo.
    pause
) else (
    echo [4/4] .env file found!
)

echo.
echo ====================================
echo   Starting FastAPI Server...
echo ====================================
echo.
echo Server will run on: http://localhost:8000
echo Swagger UI: http://localhost:8000/docs
echo.
echo Press CTRL+C to stop the server
echo.

python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
