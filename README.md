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
- **Language:** Java 17
- **Database:** PostgreSQL 15
- **Security:** JWT Authentication with Spring Security
- **Build Tool:** Maven
- **Container:** Docker

### Frontend
- **Framework:** React 19.2.0
- **Build Tool:** Vite 7.2.4
- **Styling:** Tailwind CSS 3.4.18
- **HTTP Client:** Axios 1.13.2
- **Routing:** React Router DOM 7.9.6
- **Server:** Nginx (in Docker)

## Development Setup

### Option 1: Docker Setup (Recommended) 🐳

The easiest way to run the entire application stack.

#### Prerequisites
- Docker Desktop
- Docker Compose

#### Quick Start
```bash
# Start all services (PostgreSQL, Backend, Frontend)
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

#### Access Points
- **Frontend:** http://localhost:3000
- **Backend API:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **PostgreSQL:** localhost:5433

#### Docker Commands Reference

**Start Services:**
```bash
# Start in foreground (see logs)
docker-compose up

# Start in background (detached mode)
docker-compose up -d

# Rebuild and start
docker-compose up --build
```

**Stop Services:**
```bash
# Stop services (keeps containers)
docker-compose stop

# Stop and remove containers
docker-compose down

# Stop and remove containers + volumes (deletes database data)
docker-compose down -v
```

**View Logs:**
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f postgres

# Last N lines
docker-compose logs --tail=50 backend
```

**Service Management:**
```bash
# Check service status
docker-compose ps

# Restart a specific service
docker-compose restart backend

# Rebuild a specific service
docker-compose up -d --build frontend

# Execute commands in container
docker-compose exec backend bash
docker-compose exec postgres psql -U postgres -d task_management
```

#### Docker Services

**PostgreSQL (Database):**
- Port: 5433 (external) → 5432 (internal)
- Database: task_management
- User: postgres
- Password: admin
- Volume: postgres-data (persists data)

**Backend (Spring Boot API):**
- Port: 8080
- Multi-stage build (Maven + Eclipse Temurin JRE 17)
- Environment variables from root `.env` file
- Depends on PostgreSQL health check

**Frontend (React + Nginx):**
- Port: 3000 (external) → 80 (internal)
- Multi-stage build (Node 20 + Nginx Alpine)
- Nginx configured for SPA routing
- Production-optimized build

#### Important Notes

**Separate Databases:**
- Local development uses PostgreSQL on port 5432
- Docker uses PostgreSQL on port 5433
- Both can run simultaneously without conflicts
- Docker database starts fresh (create new account)

**Environment Files:**
- Root `.env` - Used by Docker Compose
- `backend/.env` - Used for local development
- `backend/.env.example` - Template with required variables

**Data Persistence:**
- Database data persists in Docker volume `postgres-data`
- Survives container restarts
- Deleted only with `docker-compose down -v`

---

### Option 2: Local Development Setup

For development without Docker.

#### Prerequisites
- Java 17+
- Node.js 18+
- PostgreSQL 15+
- Maven

#### Backend Setup
```bash
cd backend
mvn clean install
mvn spring-boot:run
```
The API will be available at `http://localhost:8080`

#### Frontend Setup
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
