# API Documentation & Setup Guide
*Complete Reference for Task Management System API*

---

## 📋 Table of Contents

1. [**Project Setup**](#-project-setup)
2. [**Environment Configuration**](#-environment-configuration)
3. [**API Endpoints**](#-api-endpoints)
4. [**Authentication Flow**](#-authentication-flow)
5. [**Error Handling**](#-error-handling)
6. [**Testing Guide**](#-testing-guide)
7. [**Deployment Configuration**](#-deployment-configuration)

---

## 🚀 Project Setup

### Prerequisites
- **Java 17+** (verified with `java -version`)
- **Maven 3.6+**
- **PostgreSQL 12+**
- **IntelliJ IDEA Ultimate** (recommended)
- **Postman** (for API testing)
- **Node.js 18+** (for React frontend)

### Local Development Setup

#### 1. Database Setup
```sql
-- Create database
CREATE DATABASE task_management;

-- Create user (optional)
CREATE USER taskmanager WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE task_management TO taskmanager;
```

#### 2. Clone and Setup Backend
```bash
# Clone repository
git clone https://github.com/Beesettyrakesh/task-management-system.git
cd task-management-system

# Build project
mvn clean install

# Run application
mvn spring-boot:run
```

#### 3. Verify Setup
- Application: http://localhost:8080
- Database: Check pgAdmin for tables (users, tasks)
- Health Check: GET http://localhost:8080/actuator/health

---

## ⚙️ Environment Configuration

### application.properties (Development)
```properties
# Application
spring.application.name=taskmanagement

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/task_management
spring.datasource.username=postgres
spring.datasource.password=admin
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

# JWT Configuration (Week 2)
jwt.secret=yourSecretKeyHereMakeItLongAndSecure
jwt.expiration=86400000

# File Upload Configuration (Week 4)
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
file.upload-dir=./uploads

# Email Configuration (Week 5)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Logging Configuration
logging.level.root=INFO
logging.level.com.rakesh.taskmanagement=DEBUG
logging.file.name=logs/application.log
```

### application-prod.properties (Production)
```properties
# Production Database (Railway/Heroku)
spring.datasource.url=${DATABASE_URL}
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# Security
jwt.secret=${JWT_SECRET}

# Email
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}

# CORS for production
management.endpoints.web.cors.allowed-origins=${FRONTEND_URL}
```

---

## 🔌 API Endpoints

### Base URL
- **Development:** `http://localhost:8080/api`
- **Production:** `https://your-app.railway.app/api`

---

## 📝 Task Management API (Week 1 - Current Implementation)

### 1. Create Task
**Endpoint:** `POST /api/tasks`

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "title": "Complete Spring Boot setup",
  "description": "Setup basic CRUD operations",
  "status": "TODO",
  "priority": "HIGH",
  "dueDate": "2024-12-31"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "title": "Complete Spring Boot setup",
  "description": "Setup basic CRUD operations",
  "status": "TODO",
  "priority": "HIGH",
  "dueDate": "2024-12-31",
  "user": null,
  "createdAt": "2024-11-18T11:30:00",
  "updatedAt": "2024-11-18T11:30:00",
  "createdBy": null,
  "lastModifiedBy": null
}
```

**Error Responses:**
- `400 Bad Request` - Invalid request body
- `500 Internal Server Error` - Server error

---

### 2. Get All Tasks
**Endpoint:** `GET /api/tasks`

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "title": "Complete Spring Boot setup",
    "description": "Setup basic CRUD operations",
    "status": "TODO",
    "priority": "HIGH",
    "dueDate": "2024-12-31",
    "createdAt": "2024-11-18T11:30:00",
    "updatedAt": "2024-11-18T11:30:00"
  },
  {
    "id": 2,
    "title": "Learn React",
    "description": "Build frontend application",
    "status": "IN_PROGRESS",
    "priority": "MEDIUM",
    "dueDate": "2024-12-20",
    "createdAt": "2024-11-18T12:00:00",
    "updatedAt": "2024-11-18T12:00:00"
  }
]
```

---

### 3. Get Task by ID
**Endpoint:** `GET /api/tasks/{id}`

**Path Parameters:**
- `id` (Long) - Task ID

**Response (200 OK):**
```json
{
  "id": 1,
  "title": "Complete Spring Boot setup",
  "description": "Setup basic CRUD operations",
  "status": "TODO",
  "priority": "HIGH",
  "dueDate": "2024-12-31",
  "createdAt": "2024-11-18T11:30:00",
  "updatedAt": "2024-11-18T11:30:00"
}
```

**Error Responses:**
- `404 Not Found` - Task not found

---

### 4. Update Task
**Endpoint:** `PUT /api/tasks/{id}`

**Path Parameters:**
- `id` (Long) - Task ID

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "title": "Complete Spring Boot setup - UPDATED",
  "description": "Setup basic CRUD operations with testing",
  "status": "IN_PROGRESS",
  "priority": "MEDIUM",
  "dueDate": "2024-12-25"
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "title": "Complete Spring Boot setup - UPDATED",
  "description": "Setup basic CRUD operations with testing",
  "status": "IN_PROGRESS",
  "priority": "MEDIUM",
  "dueDate": "2024-12-25",
  "createdAt": "2024-11-18T11:30:00",
  "updatedAt": "2024-11-18T11:35:00"
}
```

**Error Responses:**
- `404 Not Found` - Task not found
- `400 Bad Request` - Invalid request body

---

### 5. Delete Task
**Endpoint:** `DELETE /api/tasks/{id}`

**Path Parameters:**
- `id` (Long) - Task ID

**Response (204 No Content):**
```
(Empty response body)
```

**Error Responses:**
- `404 Not Found` - Task not found

---

## 🔐 Authentication API (Week 2 - Planned Implementation)

### 1. User Registration
**Endpoint:** `POST /api/auth/register`

**Request Body:**
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "password123"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "createdAt": "2024-11-18T11:30:00"
}
```

---

### 2. User Login
**Endpoint:** `POST /api/auth/login`

**Request Body:**
```json
{
  "username": "johndoe",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "johndoe",
  "email": "john@example.com"
}
```

---

## 🏷️ Tag Management API (Week 4 - Planned Implementation)

### 1. Create Tag
**Endpoint:** `POST /api/tags`

**Request Headers:**
```
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "urgent",
  "color": "#ff0000"
}
```

---

### 2. Get User Tags
**Endpoint:** `GET /api/tags`

**Request Headers:**
```
Authorization: Bearer <jwt_token>
```

---

### 3. Assign Tag to Task
**Endpoint:** `POST /api/tasks/{taskId}/tags/{tagId}`

**Request Headers:**
```
Authorization: Bearer <jwt_token>
```

---

## 📁 File Upload API (Week 4 - Planned Implementation)

### 1. Upload File to Task
**Endpoint:** `POST /api/tasks/{taskId}/attachments`

**Request Headers:**
```
Authorization: Bearer <jwt_token>
Content-Type: multipart/form-data
```

**Request Body:**
```
Form data with file field
```

---

### 2. Download File
**Endpoint:** `GET /api/attachments/{id}/download`

**Request Headers:**
```
Authorization: Bearer <jwt_token>
```

---

## 🔒 Authentication Flow

### JWT Token Usage (Week 2)

#### 1. Login Process
```
POST /api/auth/login
↓
Receive JWT token
↓
Store token in localStorage/cookies
↓
Include in subsequent requests
```

#### 2. Protected Endpoint Access
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### 3. Token Validation Flow
```
Client Request with JWT
↓
JwtAuthenticationFilter extracts token
↓
Validate token signature and expiration
↓
Set authentication in SecurityContext
↓
Allow access to protected resource
```

---

## ❌ Error Handling

### Standard Error Response Format
```json
{
  "timestamp": "2024-11-18T11:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Task not found with id: 1",
  "path": "/api/tasks/1"
}
```

### HTTP Status Codes Used

| Status Code | Description | Usage |
|-------------|-------------|-------|
| 200 | OK | Successful GET, PUT requests |
| 201 | Created | Successful POST requests |
| 204 | No Content | Successful DELETE requests |
| 400 | Bad Request | Invalid request body/parameters |
| 401 | Unauthorized | Missing or invalid authentication |
| 403 | Forbidden | Access denied |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Duplicate resource (username/email) |
| 500 | Internal Server Error | Server-side errors |

### Validation Errors (Week 5)
```json
{
  "timestamp": "2024-11-18T11:30:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Validation errors occurred",
  "validationErrors": {
    "title": "Title is required",
    "email": "Email format is invalid"
  },
  "path": "/api/tasks"
}
```

---

## 🧪 Testing Guide

### Postman Collection Setup

#### 1. Create Collection
```
Collection Name: Task Management API
Base URL: {{baseUrl}} = http://localhost:8080/api
```

#### 2. Environment Variables
```
baseUrl: http://localhost:8080/api
authToken: (to be set after login)
```

#### 3. Test Scripts (Week 2)
```javascript
// Login test - store token
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has token", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.token).to.be.a('string');
    pm.environment.set("authToken", jsonData.token);
});
```

#### 4. Authorization Setup
```
Type: Bearer Token
Token: {{authToken}}
```

### Testing Checklist (Day 7)

#### Basic CRUD Testing
- [ ] **POST /api/tasks** - Create task with valid data
- [ ] **POST /api/tasks** - Create task with missing title (400 error)
- [ ] **POST /api/tasks** - Create task with invalid enum values
- [ ] **GET /api/tasks** - Get all tasks (empty array initially)
- [ ] **GET /api/tasks** - Get all tasks (with data)
- [ ] **GET /api/tasks/1** - Get existing task
- [ ] **GET /api/tasks/999** - Get non-existent task (404 error)
- [ ] **PUT /api/tasks/1** - Update existing task
- [ ] **PUT /api/tasks/999** - Update non-existent task (404 error)
- [ ] **DELETE /api/tasks/1** - Delete existing task
- [ ] **DELETE /api/tasks/999** - Delete non-existent task (404 error)

#### Data Validation Testing
- [ ] **Enum Values:** status must be TODO, IN_PROGRESS, or DONE
- [ ] **Enum Values:** priority must be LOW, MEDIUM, or HIGH
- [ ] **Date Format:** dueDate must be in YYYY-MM-DD format
- [ ] **Required Fields:** title is required
- [ ] **Field Lengths:** description max 1000 characters

#### Edge Cases
- [ ] **Empty Request Body:** POST with empty JSON
- [ ] **Null Values:** POST with null title
- [ ] **Invalid JSON:** Malformed request body
- [ ] **Large Payload:** Very long description
- [ ] **Special Characters:** Title with special characters
- [ ] **Unicode:** Title with unicode characters

---

## 🔧 Curl Commands Reference

### Create Task
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Complete Spring Boot setup",
    "description": "Setup basic CRUD operations",
    "status": "TODO",
    "priority": "HIGH",
    "dueDate": "2024-12-31"
  }'
```

### Get All Tasks
```bash
curl -X GET http://localhost:8080/api/tasks
```

### Get Task by ID
```bash
curl -X GET http://localhost:8080/api/tasks/1
```

### Update Task
```bash
curl -X PUT http://localhost:8080/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Complete Spring Boot setup - UPDATED",
    "description": "Setup basic CRUD operations with testing",
    "status": "IN_PROGRESS",
    "priority": "MEDIUM",
    "dueDate": "2024-12-25"
  }'
```

### Delete Task
```bash
curl -X DELETE http://localhost:8080/api/tasks/1
```

### With Authentication (Week 2)
```bash
curl -X GET http://localhost:8080/api/tasks \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## 🚀 Deployment Configuration

### Docker Setup (Week 6)

#### Dockerfile (Backend)
```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### docker-compose.yml

```yaml
version: '3.8'
services:
  db:
    image: postgres:15
    environment:
      POSTGRES_DB: task_management
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"

  backend:
    build: ..
    ports:
      - "8080:8080"
    depends_on:
      - db
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/task_management
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres

volumes:
  postgres_data:
```

### Railway Deployment
```bash
# Install Railway CLI
npm install -g @railway/cli

# Login and deploy
railway login
railway init
railway up
```

### Environment Variables (Production)
```
DATABASE_URL=postgresql://user:pass@host:port/db
JWT_SECRET=your-production-secret-key
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
FRONTEND_URL=https://your-frontend.vercel.app
```

---

## 📊 Monitoring & Health Checks

### Actuator Endpoints (Week 5)
```
GET /actuator/health - Application health
GET /actuator/info - Application information
GET /actuator/metrics - Application metrics
GET /actuator/loggers - Logger configuration
```

### Health Check Response
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 499963174912,
        "free": 91943301120,
        "threshold": 10485760,
        "path": "/Users/rakeshbeesetty/SpringBoot/taskmanagement"
      }
    }
  }
}
```

---

## 🔍 Troubleshooting Common Issues

### Database Connection Issues
```
Error: Could not create connection to database server
Solution: 
1. Verify PostgreSQL is running
2. Check database credentials in application.properties
3. Ensure database 'task_management' exists
```

### Port Already in Use
```
Error: Port 8080 was already in use
Solution: 
1. Kill process: sudo lsof -t -i tcp:8080 | xargs kill -9
2. Or change port: server.port=8081 in application.properties
```

### JSON Parsing Errors
```
Error: JSON parse error: Cannot deserialize value
Solution:
1. Verify Content-Type: application/json header
2. Check enum values match exactly (case-sensitive)
3. Validate JSON syntax
```

### CORS Issues (Week 3)
```
Error: CORS policy blocked the request
Solution:
1. Verify @CrossOrigin annotation in controllers
2. Update allowed origins in production
3. Check frontend URL configuration
```

---

## 📈 Performance Optimization (Week 5)

### Database Indexing
```sql
-- Indexes for better query performance
CREATE INDEX idx_task_user_id ON tasks(user_id);
CREATE INDEX idx_task_status ON tasks(status);
CREATE INDEX idx_task_due_date ON tasks(due_date);
CREATE INDEX idx_task_created_at ON tasks(created_at);
```

### JPA Query Optimization
```java
// Avoid N+1 queries
@Query("SELECT t FROM Task t LEFT JOIN FETCH t.user WHERE t.id = :id")
Task findTaskWithUser(@Param("id") Long id);

// Pagination for large datasets
Page<Task> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
```

### Caching Configuration
```java
@Cacheable(value = "tasks", key = "#userId")
public List<Task> getUserTasks(Long userId) {
    return taskRepository.findByUserId(userId);
}
```

---

## 📚 Additional Resources

### API Documentation Tools
- **Swagger UI:** http://localhost:8080/swagger-ui.html (Week 5)
- **Postman Collections:** Export/import for team sharing
- **API Blueprint:** Alternative documentation format

### Development Tools
- **H2 Console:** For testing with in-memory database
- **Spring Boot DevTools:** Auto-restart during development
- **Actuator:** Production monitoring and metrics

### Learning Resources
- [Spring Boot Reference Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

---

**Last Updated:** November 18, 2024 - Week 1, Day 7  
**API Version:** v1.0.0  
**Spring Boot Version:** 3.5.7  

*This documentation serves as your complete API reference and will be updated as new features are implemented throughout the 42-day development plan.*
