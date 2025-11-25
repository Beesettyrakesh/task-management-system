# Task Management System

A full-stack task management application built with Spring Boot backend and React frontend.

## Project Structure

```
TaskManagement-App/
├── backend/          # Spring Boot API (Java)
├── frontend/         # React Application (Vite)
└── README.md        # This file
```

## Technology Stack

### Backend
- **Framework:** Spring Boot 3.5.7
- **Language:** Java 25
- **Database:** PostgreSQL
- **Security:** JWT Authentication with Spring Security
- **Build Tool:** Maven

### Frontend
- **Framework:** React 19.2.0
- **Build Tool:** Vite 7.2.4
- **Styling:** Tailwind CSS 3.4.18
- **HTTP Client:** Axios 1.13.2
- **Routing:** React Router DOM 7.9.6

## Development Setup

### Prerequisites
- Java 25+
- Node.js 18+
- PostgreSQL
- Maven

### Backend Setup
```bash
cd backend
mvn clean install
mvn spring-boot:run
```
The API will be available at `http://localhost:8080`

### Frontend Setup
```bash
cd frontend
npm install
npm run dev
```
The React app will be available at `http://localhost:5173`

## API Endpoints

### Authentication
- `POST /api/auth/signup` - User registration
- `POST /api/auth/login` - User login

### Tasks (Protected)
- `GET /api/tasks` - Get user's tasks
- `POST /api/tasks` - Create new task
- `GET /api/tasks/{id}` - Get specific task
- `PUT /api/tasks/{id}` - Update task
- `DELETE /api/tasks/{id}` - Delete task

## Features

### Completed ✅
- Backend Spring Boot API with JWT authentication
- User registration and login
- Protected task CRUD operations
- Cross-user access prevention
- React project setup with Vite
- Tailwind CSS configuration

### In Development 🚧
- React authentication system
- Task management UI
- Frontend-backend integration

### Planned 📋
- Task filtering and search
- Task priority and status management
- Responsive design
- Production deployment

## Database Schema

### Users Table
- id (Primary Key)
- username (Unique)
- email (Unique)
- password (BCrypt encrypted)
- created_date, modified_date

### Tasks Table
- id (Primary Key)
- title
- description
- status (TODO, IN_PROGRESS, DONE)
- priority (LOW, MEDIUM, HIGH)
- due_date
- user_id (Foreign Key)
- created_date, modified_date

## Security Features
- JWT-based authentication
- Password encryption with BCrypt
- User-specific data isolation
- CORS configuration for frontend integration

## Development Progress
Currently at **Week 3, Day 13** of a 42-day development roadmap (31% complete).

## Contributing
This is a personal learning project. The development follows a structured 42-day roadmap for building a production-ready full-stack application.
