#!/bin/bash
# Quick start script for Alcove Backend (macOS/Linux)
# Run this from alcove/backend

echo ""
echo "===================================="
echo "  ALCOVE BACKEND QUICK START"
echo "===================================="
echo ""

# Check if venv exists
if [ ! -d "venv" ]; then
    echo "[1/4] Creating virtual environment..."
    python3 -m venv venv
    echo ""
fi

echo "[2/4] Activating virtual environment..."
source venv/bin/activate
echo "Virtual environment activated!"
echo ""

# Check if dependencies are installed
echo "[3/4] Checking dependencies..."
if ! pip list | grep -i fastapi > /dev/null; then
    echo "Installing dependencies..."
    pip install -r requirements.txt
else
    echo "Dependencies already installed!"
fi
echo ""

# Check if .env file exists
if [ ! -f ".env" ]; then
    echo "[4/4] Creating .env file from template..."
    cp .env.example .env
    echo ""
    echo "WARNING: Please edit .env and update DATABASE_URL with your credentials!"
    echo "Edit .env file and update these values:"
    echo "  - DATABASE_URL: your PostgreSQL connection string"
    echo "  - SECRET_KEY: a random secret string"
    echo ""
    read -p "Press Enter to continue..."
else
    echo "[4/4] .env file found!"
fi

echo ""
echo "===================================="
echo "   Starting FastAPI Server..."
echo "===================================="
echo ""
echo "Server will run on: http://localhost:8000"
echo "Swagger UI: http://localhost:8000/docs"
echo ""
echo "Press CTRL+C to stop the server"
echo ""

python -m uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
