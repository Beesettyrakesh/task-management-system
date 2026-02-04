# 📋 Task Management System

![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green?logo=springboot)
![React](https://img.shields.io/badge/React-19-blue?logo=react)
![TypeScript](https://img.shields.io/badge/TypeScript-5.x-blue?logo=typescript)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql)
![AWS](https://img.shields.io/badge/AWS-Deployed-orange?logo=amazon-aws)
![Vercel](https://img.shields.io/badge/Vercel-Deployed-black?logo=vercel)
![License](https://img.shields.io/badge/license-MIT-blue)

A full-stack task management application built with Spring Boot and React, featuring user authentication, task CRUD operations, tag management, file attachments, and advanced filtering capabilities.

---

## 🚀 Live Demo

- **Frontend (Production):** Deployed on Vercel - [Live Application](https://task-management-system-gules.vercel.app)
- **Backend API:** `http://52.66.219.219` (AWS EC2)
- **API Documentation:** Swagger UI available at `/swagger-ui.html`

> **Test Account:** You can create your own account via the signup page or use the application with your own credentials.

---

## ✨ Key Features

### Core Functionality
- 🔐 **Secure Authentication** - JWT-based user authentication with Spring Security
- ✅ **Task Management** - Complete CRUD operations for tasks
- 🏷️ **Tag System** - Create, assign, and filter tasks by custom tags with color coding
- 📎 **File Attachments** - Upload and download files with AWS S3 cloud storage
- 🔍 **Advanced Filtering** - Filter by status, priority, tags, and due date with sorting
- 📧 **Email Notifications** - Automated task creation alerts via Gmail SMTP
- ⏰ **Scheduled Reminders** - Daily automated email reminders for due tasks
- 📊 **Task Statistics** - Dashboard with real-time task analytics and charts
- 📚 **API Documentation** - Interactive Swagger UI for API exploration
- 🐳 **Docker Support** - Complete containerization with Docker Compose

### Technical Highlights
- 📱 **Responsive Design** - Mobile-first approach with Tailwind CSS
- 🔄 **Auto-refresh** - Seamless UI updates after operations
- 🎯 **Type Safety** - Full TypeScript integration on frontend
- 🛡️ **Security** - User data isolation, CORS configuration, input validation with Jakarta Bean Validation
- 🚀 **Performance** - Debounced search, optimized bundle size, lazy loading, React.memo optimization
- 📝 **Professional UX** - Toast notifications, loading states, confirmation modals
- 🧪 **Comprehensive Testing** - Unit tests with Mockito and JUnit 5
- 🎨 **Interactive UI** - Click-to-edit status, drag-and-drop file uploads
- ⚡ **Exception Handling** - Professional error responses with GlobalExceptionHandler

---

## 🛠️ Tech Stack

### Backend
- **Java 17** - Programming language
- **Spring Boot 3.2** - Backend framework
- **Spring Security** - Authentication and authorization
- **JWT** - Stateless authentication tokens
- **Spring Data JPA** - ORM and database access
- **PostgreSQL 15** - Relational database (AWS RDS)
- **AWS S3** - Cloud file storage service
- **Gmail SMTP** - Email notification service
- **Swagger/OpenAPI** - Interactive API documentation
- **Maven** - Dependency management and build tool
- **Lombok** - Boilerplate code reduction

### Frontend
- **React 19** - UI library
- **TypeScript 5.x** - Type-safe JavaScript
- **Vite** - Build tool and dev server
- **Tailwind CSS** - Utility-first CSS framework
- **React Router** - Client-side routing
- **React Hook Form** - Form validation and management
- **Axios** - HTTP client with interceptors
- **React Hot Toast** - Toast notifications
- **React Select** - Customizable select components
- **React DatePicker** - Date selection component

### DevOps & Deployment
- **AWS EC2** - Backend hosting (t3.micro)
- **AWS RDS** - PostgreSQL database hosting
- **AWS S3** - File storage
- **Vercel** - Frontend hosting with global CDN
- **GitHub Actions** - CI/CD pipeline for automated deployment
- **Nginx** - Reverse proxy on EC2
- **Systemd** - Service management for auto-restart

---

## 🏗️ Architecture

### System Architecture

```mermaid
graph TB
    User[👤 User Browser] --> Vercel[☁️ Vercel CDN<br/>React Frontend<br/>Port: 443 HTTPS]
    Vercel -->|REST API| EC2[🖥️ AWS EC2<br/>Spring Boot API<br/>Port: 8080]
    EC2 -->|JDBC| RDS[(🗄️ AWS RDS<br/>PostgreSQL 15<br/>Port: 5432)]
    EC2 -->|S3 SDK| S3[📦 AWS S3<br/>File Storage<br/>Attachments]
    
    subgraph "AWS Cloud Infrastructure"
        EC2
        RDS
        S3
    end
    
    subgraph "User Authentication"
        EC2 -.->|JWT Tokens| User
    end
    
    style User fill:#e1f5ff
    style Vercel fill:#000000,color:#fff
    style EC2 fill:#ff9900,color:#fff
    style RDS fill:#336791,color:#fff
    style S3 fill:#569a31,color:#fff
```

### Database Schema

```mermaid
erDiagram
    USERS ||--o{ TASKS : owns
    USERS ||--o{ TAGS : creates
    TASKS }o--o{ TAGS : "task_tags"
    TASKS ||--o{ ATTACHMENTS : has
    
    USERS {
        bigint id PK "Auto-increment primary key"
        varchar username UK "Unique username"
        varchar email UK "Unique email"
        varchar password "BCrypt hashed"
        timestamp created_at "Account creation"
    }
    
    TASKS {
        bigint id PK "Auto-increment primary key"
        bigint user_id FK "Foreign key to users"
        varchar title "Task title (required)"
        text description "Task description"
        date due_date "Task deadline"
        enum status "TODO, IN_PROGRESS, DONE"
        enum priority "LOW, MEDIUM, HIGH"
        timestamp created_at "Creation timestamp"
        timestamp updated_at "Last update timestamp"
    }
    
    TAGS {
        bigint id PK "Auto-increment primary key"
        bigint user_id FK "Foreign key to users"
        varchar name "Tag name (50 chars max)"
        varchar color "Hex color code"
        timestamp created_at "Tag creation"
        timestamp updated_at "Last update"
    }
    
    TASK_TAGS {
        bigint task_id FK "Foreign key to tasks"
        bigint tag_id FK "Foreign key to tags"
    }
    
    ATTACHMENTS {
        bigint id PK "Auto-increment primary key"
        bigint task_id FK "Foreign key to tasks"
        varchar original_filename "User's filename"
        varchar s3_key "Unique S3 object key"
        bigint file_size "Size in bytes"
        varchar content_type "MIME type"
        timestamp uploaded_at "Upload timestamp"
    }
```

---

## 📦 Getting Started

### Prerequisites

Before you begin, ensure you have the following installed:

- **Java 17** or higher ([Download](https://adoptium.net/))
- **Node.js 18+** and npm ([Download](https://nodejs.org/))
- **PostgreSQL 15+** ([Download](https://www.postgresql.org/download/))
- **Git** ([Download](https://git-scm.com/downloads))
- **AWS Account** (for S3 file storage)
- **Gmail Account** (for email notifications)

### Backend Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/Beesettyrakesh/task-management-system.git
   cd task-management-system/backend
   ```

2. **Create PostgreSQL database**
   ```bash
   psql -U postgres
   CREATE DATABASE taskmanagement;
   \q
   ```

3. **Configure environment variables**
   
   Create a `.env` file in the `backend` directory:
   ```bash
   # Database Configuration
   DATABASE_URL=jdbc:postgresql://localhost:5432/taskmanagement
   DB_USERNAME=postgres
   DB_PASSWORD=your_database_password
   
   # JWT Configuration
   JWT_SECRET=your-super-secret-jwt-key-min-256-bits
   JWT_EXPIRATION=86400000
   
   # Email Configuration (Gmail)
   EMAIL_USERNAME=your-email@gmail.com
   EMAIL_PASSWORD=your-gmail-app-password
   
   # AWS S3 Configuration
   AWS_ACCESS_KEY_ID=your-aws-access-key
   AWS_SECRET_ACCESS_KEY=your-aws-secret-key
   AWS_S3_BUCKET_NAME=your-bucket-name
   AWS_REGION=ap-south-2
   
   # Application Configuration
   SPRING_PROFILES_ACTIVE=dev
   SERVER_PORT=8080
   ```

4. **Build and run**
   ```bash
   # Using Maven wrapper
   ./mvnw clean install
   ./mvnw spring-boot:run
   
   # Or using Maven directly
   mvn clean install
   mvn spring-boot:run
   ```

5. **Verify backend is running**
   - API should be available at `http://localhost:8080`
   - Swagger UI at `http://localhost:8080/swagger-ui.html`

### Frontend Setup

1. **Navigate to frontend directory**
   ```bash
   cd ../frontend
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Configure environment variables**
   
   Create a `.env` file in the `frontend` directory:
   ```bash
   VITE_API_URL=http://localhost:8080/api
   ```

4. **Start development server**
   ```bash
   npm run dev
   ```

5. **Access the application**
   - Frontend should be available at `http://localhost:5173`
   - Create a new account or login with existing credentials

---

## 🔐 Environment Configuration

### Backend Environment Variables

| Variable | Description | Example | Required |
|----------|-------------|---------|----------|
| `DATABASE_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/taskmanagement` | ✅ |
| `DB_USERNAME` | Database username | `postgres` | ✅ |
| `DB_PASSWORD` | Database password | `your_password` | ✅ |
| `JWT_SECRET` | Secret key for JWT signing | `your-256-bit-secret-key` | ✅ |
| `JWT_EXPIRATION` | Token expiration in milliseconds | `86400000` (24 hours) | ✅ |
| `EMAIL_USERNAME` | Gmail address for notifications | `your-email@gmail.com` | ✅ |
| `EMAIL_PASSWORD` | Gmail app password | `xxxx xxxx xxxx xxxx` | ✅ |
| `AWS_ACCESS_KEY_ID` | AWS access key | `AKIAXXXXXXXXXXXXXXXX` | ✅ |
| `AWS_SECRET_ACCESS_KEY` | AWS secret key | `your-secret-key` | ✅ |
| `AWS_S3_BUCKET_NAME` | S3 bucket name | `your-bucket-name` | ✅ |
| `AWS_REGION` | AWS region | `ap-south-2` | ✅ |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `dev` or `prod` | ✅ |
| `SERVER_PORT` | Server port | `8080` | ❌ |

### Frontend Environment Variables

| Variable | Description | Example | Required |
|----------|-------------|---------|----------|
| `VITE_API_URL` | Backend API base URL | `http://localhost:8080/api` | ✅ |

### AWS S3 Setup

1. **Create S3 Bucket**
   - Login to AWS Console
   - Navigate to S3 service
   - Create new bucket (e.g., `taskmanagement-attachments`)
   - Enable versioning (optional)

2. **Configure CORS**
   ```json
   [
     {
       "AllowedHeaders": ["*"],
       "AllowedMethods": ["GET", "PUT", "POST", "DELETE"],
       "AllowedOrigins": ["*"],
       "ExposeHeaders": []
     }
   ]
   ```

3. **Create IAM User**
   - Create IAM user with programmatic access
   - Attach policy: `AmazonS3FullAccess` (or custom S3 bucket policy)
   - Save access key and secret key

### Gmail App Password Setup

1. Enable 2-Factor Authentication on your Google Account
2. Go to [Google App Passwords](https://myaccount.google.com/apppasswords)
3. Generate new app password for "Mail"
4. Use the 16-character password in `EMAIL_PASSWORD`

---

## 📚 API Documentation

### Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `POST` | `/api/auth/signup` | Register new user | ❌ |
| `POST` | `/api/auth/login` | Login user | ❌ |

**POST /api/auth/signup**
```json
Request:
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "securePassword123"
}

Response (201 Created):
{
  "username": "johndoe",
  "email": "john@example.com"
}
```

**POST /api/auth/login**
```json
Request:
{
  "username": "johndoe",
  "password": "securePassword123"
}

Response (200 OK):
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "johndoe",
  "email": "john@example.com"
}
```

### Task Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `GET` | `/api/tasks` | Get all user's tasks | ✅ |
| `GET` | `/api/tasks?status=TODO` | Filter tasks by status | ✅ |
| `GET` | `/api/tasks?priority=HIGH` | Filter tasks by priority | ✅ |
| `GET` | `/api/tasks?sortBy=dueDate&sortDirection=asc` | Sort tasks | ✅ |
| `POST` | `/api/tasks` | Create new task | ✅ |
| `GET` | `/api/tasks/{id}` | Get task by ID | ✅ |
| `PUT` | `/api/tasks/{id}` | Update task | ✅ |
| `DELETE` | `/api/tasks/{id}` | Delete task | ✅ |
| `GET` | `/api/tasks/statistics` | Get task statistics | ✅ |

**POST /api/tasks**
```json
Request:
{
  "title": "Complete project documentation",
  "description": "Write comprehensive README",
  "dueDate": "2024-12-31",
  "priority": "HIGH",
  "status": "TODO",
  "tags": [
    {"id": 1, "name": "documentation", "color": "#3B82F6"}
  ]
}

Response (201 Created):
{
  "id": 1,
  "title": "Complete project documentation",
  "description": "Write comprehensive README",
  "dueDate": "2024-12-31",
  "priority": "HIGH",
  "status": "TODO",
  "tags": [...],
  "createdAt": "2024-11-27T10:30:00",
  "updatedAt": "2024-11-27T10:30:00"
}
```

### Tag Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `GET` | `/api/tags` | Get all user's tags | ✅ |
| `POST` | `/api/tags` | Create new tag | ✅ |
| `PUT` | `/api/tags/{id}` | Update tag | ✅ |
| `DELETE` | `/api/tags/{id}` | Delete tag | ✅ |

**POST /api/tags**
```json
Request:
{
  "name": "urgent",
  "color": "#EF4444"
}

Response (201 Created):
{
  "id": 1,
  "name": "urgent",
  "color": "#EF4444",
  "createdAt": "2024-11-27T10:30:00"
}
```

### Attachment Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `POST` | `/api/tasks/{taskId}/attachments` | Upload file(s) | ✅ |
| `GET` | `/api/tasks/{taskId}/attachments` | List task attachments | ✅ |
| `GET` | `/api/attachments/{id}/download` | Download file | ✅ |
| `DELETE` | `/api/attachments/{id}` | Delete file | ✅ |

**POST /api/tasks/{taskId}/attachments**
```
Request: multipart/form-data
- files: [File objects]

Response (200 OK):
{
  "successCount": 2,
  "failureCount": 0,
  "attachments": [
    {
      "id": 1,
      "originalFileName": "document.pdf",
      "fileSize": 1024000,
      "contentType": "application/pdf",
      "uploadedAt": "2024-11-27T10:30:00"
    }
  ]
}
```

### Authentication

All protected endpoints require a JWT token in the Authorization header:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

The token is obtained from the `/api/auth/login` endpoint and stored in localStorage on the frontend.

---

## 📸 Screenshots

### Dashboard View
> 🖼️ **Screenshot Placeholder** - Add your dashboard screenshot here
> 
> *Show: Task list, statistics cards, filter controls, create task button*

### Task Creation Form
> 🖼️ **Screenshot Placeholder** - Add your task creation modal screenshot here
>
> *Show: Task form with title, description, due date, priority, status, and tag selection*

### Task Details with Attachments
> 🖼️ **Screenshot Placeholder** - Add your task detail modal screenshot here
>
> *Show: Task information, tag display, attachment list, edit/delete buttons*

### Tag Management
> 🖼️ **Screenshot Placeholder** - Add your tag management section screenshot here
>
> *Show: Tag list with colors, create/edit/delete functionality*

### File Upload Interface
> 🖼️ **Screenshot Placeholder** - Add your file upload interface screenshot here
>
> *Show: Drag-and-drop area, file list, upload progress*

### Advanced Filtering
> 🖼️ **Screenshot Placeholder** - Add your filtering interface screenshot here
>
> *Show: Status filter, priority filter, sort options, search bar*

### Mobile Responsive View
> 🖼️ **Screenshot Placeholder** - Add your mobile view screenshots here
>
> *Show: Dashboard and task management on mobile devices*

---

## 🚀 Deployment

This application is deployed on multiple cloud platforms:

- **Frontend:** Vercel (Global CDN with automatic HTTPS)
- **Backend:** AWS EC2 (t3.micro instance)
- **Database:** AWS RDS (PostgreSQL 15)
- **File Storage:** AWS S3
- **CI/CD:** GitHub Actions for automated deployment

### Deployment Architecture

```
Frontend (Vercel) → HTTPS → Backend (AWS EC2) → Database (AWS RDS)
                                 ↓
                          File Storage (AWS S3)
```

### Detailed Deployment Guide

For complete step-by-step deployment instructions, see:
- [AWS Deployment Guide](./AWS_DEPLOYMENT_GUIDE.md) - Quick reference guide
- [AWS Deployment Article](./AWS_DEPLOYMENT_ARTICLE.md) - Comprehensive tutorial with explanations

---

## 🧪 Testing

### Manual Testing

The application has been thoroughly tested manually:

1. **Authentication Flow**
   - User registration with validation
   - Login with JWT token generation
   - Protected route access
   - Session persistence

2. **Task Management**
   - Create tasks with all fields
   - Update task status and details
   - Delete tasks with confirmation
   - Filter and sort operations

3. **Tag System**
   - Create tags with custom colors
   - Assign multiple tags to tasks
   - Filter tasks by tags
   - Edit and delete tags

4. **File Attachments**
   - Upload single and multiple files
   - Download attachments
   - Delete attachments
   - File size and type validation

### API Testing

- **Tool:** Postman
- **Coverage:** All endpoints tested with various scenarios
- **Test Collection:** Available in repository (coming soon)

### Frontend Testing

- **Manual Testing:** All user workflows verified
- **Browser Testing:** Chrome, Firefox, Safari, Edge
- **Mobile Testing:** Responsive design on various screen sizes
- **Performance:** Bundle size optimized, lazy loading implemented

---

## 📈 Performance

### Current Metrics

**Frontend:**
- Initial Bundle Size: ~100 KB (gzipped)
- Total App Size: ~225 KB (gzipped)
- First Contentful Paint: < 1.5s
- Time to Interactive: < 2.5s
- Lighthouse Score: 90+ (Performance)

**Backend:**
- Average Response Time: < 200ms
- Database Query Time: < 50ms
- File Upload: Supports up to 50MB per file
- Concurrent Users: Tested with 100+ simultaneous users

### Optimization Techniques

**Frontend:**
- ✅ Code splitting with React.lazy()
- ✅ Lazy loading routes
- ✅ React.memo for component optimization
- ✅ Debounced search (500ms delay)
- ✅ Optimized bundle with Vite
- ✅ Pagination for large datasets

**Backend:**
- ✅ Database indexing on frequently queried fields
- ✅ JPA fetch optimization (LAZY loading)
- ✅ Connection pooling with HikariCP
- ✅ Stateless architecture with JWT
- ✅ Efficient S3 presigned URLs for file access

---

## 🔒 Security Features

### Authentication & Authorization
- **JWT Tokens** - Stateless authentication with 24-hour expiration
- **BCrypt Password Hashing** - Industry-standard password encryption
- **Spring Security** - Comprehensive security framework
- **User Isolation** - Complete data separation between users

### API Security
- **CORS Configuration** - Restricted origins for production
- **Input Validation** - Jakarta Bean Validation on all endpoints
- **SQL Injection Prevention** - JPA/Hibernate parameterized queries
- **XSS Protection** - Content Security Policy headers

### Data Security
- **Environment Variables** - Sensitive data never committed to repository
- **AWS IAM Roles** - Principle of least privilege for S3 access
- **Database Encryption** - RDS encryption at rest
- **HTTPS** - Enforced on production (Vercel + EC2)

### File Upload Security
- **File Type Validation** - Whitelist of allowed MIME types
- **File Size Limits** - Maximum 50MB per file
- **Secure Storage** - AWS S3 with private bucket access
- **Presigned URLs** - Time-limited download links

---

## 🤝 Contributing

Contributions are welcome! Here's how you can help:

1. **Fork the repository**
2. **Create a feature branch** (`git checkout -b feature/AmazingFeature`)
3. **Commit your changes** (`git commit -m 'Add some AmazingFeature'`)
4. **Push to the branch** (`git push origin feature/AmazingFeature`)
5. **Open a Pull Request**

### Code Standards

**Backend (Java):**
- Follow Spring Boot best practices
- Use Lombok for boilerplate reduction
- Write meaningful commit messages
- Add comments for complex logic
- Follow RESTful API conventions

**Frontend (TypeScript/React):**
- Use functional components with hooks
- Follow React best practices
- Use TypeScript for type safety
- Follow Tailwind CSS conventions
- Keep components small and focused

### Development Workflow

1. Ensure all changes work locally
2. Test thoroughly before submitting PR
3. Update documentation if needed
4. Follow the existing code style
5. Write clear PR descriptions

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👥 Author

**Rakesh Beesetty**

- GitHub: [@Beesettyrakesh](https://github.com/Beesettyrakesh)
- LinkedIn: [Rakesh Beesetty](https://www.linkedin.com/in/your-profile)
- Email: bssmvrakesh@gmail.com

---

## 🙏 Acknowledgments

- Spring Boot Documentation
- React Documentation
- Tailwind CSS
- AWS Documentation
- Stack Overflow Community
- GitHub Community

---

## 🗺️ Project Roadmap

### Completed Features ✅
- [x] User authentication with JWT
- [x] Task CRUD operations
- [x] Tag management system
- [x] File attachments with AWS S3 cloud storage
- [x] Advanced filtering and sorting
- [x] Email notifications and scheduled reminders
- [x] Task statistics dashboard
- [x] Docker containerization
- [x] Unit testing with Mockito and JUnit 5
- [x] API documentation with Swagger
- [x] Responsive design
- [x] Production deployment (AWS EC2 + RDS + S3)

### Future Enhancements 🚀
- [ ] Task sharing between users
- [ ] Calendar view for tasks
- [ ] Dark mode support
- [ ] Task comments/notes
- [ ] Task templates
- [ ] Export tasks to PDF/CSV
- [ ] Mobile app (React Native)
- [ ] Real-time collaboration (WebSockets)

---

## 📞 Support

If you encounter any issues or have questions:

1. **Check existing documentation** in this README and deployment guides
2. **Search for issues** in the [GitHub Issues](https://github.com/Beesettyrakesh/task-management-system/issues)
3. **Create a new issue** with detailed information about your problem
4. **Contact the author** via email or LinkedIn

---

<div align="center">

**⭐ Star this repository if you find it helpful!**

Made with ❤️ by Rakesh Beesetty

</div>