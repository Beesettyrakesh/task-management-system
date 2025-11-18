# Current Progress - Week 1, Day 7
*Postman API Testing & Bug Fixes Guide*

---

## 📍 Current Status

**Date:** November 18, 2024  
**Phase:** Week 1, Day 7 - Postman Testing  
**Progress:** 16.7% Complete (7/42 days)  
**Focus:** Testing all CRUD operations and fixing any discovered bugs  

---

## 🎯 Today's Objectives (Day 7)

### Primary Goals
1. **Test all API endpoints** using Postman
2. **Verify CRUD operations** work correctly
3. **Identify and fix bugs** discovered during testing
4. **Document API behavior** for future reference
5. **Prepare for Week 2** (Authentication setup)

### Expected Time Investment: 2 hours

---

## 🧪 Postman Testing Checklist

### Setup Phase
- [ ] Open Postman application
- [ ] Create new collection: "Task Management API"
- [ ] Create folder: "Tasks" within the collection
- [ ] Set base URL: `http://localhost:8080`

### API Testing Sequence

#### 1. POST /api/tasks - Create Task ✅
**Endpoint:** `POST http://localhost:8080/api/tasks`

**Headers:**
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

**Expected Response (201 Created):**
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

**Test Cases:**
- [ ] Valid task creation
- [ ] Missing required fields (title)
- [ ] Invalid enum values (status, priority)
- [ ] Future due date validation

---

#### 2. GET /api/tasks - Get All Tasks ✅
**Endpoint:** `GET http://localhost:8080/api/tasks`

**Expected Response (200 OK):**
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
  }
]
```

**Test Cases:**
- [ ] Empty database (should return empty array)
- [ ] Multiple tasks (should return all tasks)
- [ ] Response format consistency

---

#### 3. GET /api/tasks/{id} - Get Single Task ✅
**Endpoint:** `GET http://localhost:8080/api/tasks/1`

**Expected Response (200 OK):**
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

**Test Cases:**
- [ ] Valid task ID (should return task)
- [ ] Invalid task ID (should return 404)
- [ ] Non-numeric ID (should return 400)

---

#### 4. PUT /api/tasks/{id} - Update Task ✅
**Endpoint:** `PUT http://localhost:8080/api/tasks/1`

**Headers:**
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

**Expected Response (200 OK):**
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

**Test Cases:**
- [ ] Valid update (should return updated task)
- [ ] Invalid task ID (should return 404)
- [ ] Partial update (only some fields)
- [ ] Invalid field values

---

#### 5. DELETE /api/tasks/{id} - Delete Task ✅
**Endpoint:** `DELETE http://localhost:8080/api/tasks/1`

**Expected Response (204 No Content):**
```
(Empty response body)
```

**Test Cases:**
- [ ] Valid task ID (should return 204)
- [ ] Invalid task ID (should return 404)
- [ ] Verify task is actually deleted (GET should return 404)

---

## 🐛 Common Issues & Solutions

### Issue 1: Application Not Starting
**Symptoms:** 
- Port 8080 already in use
- Database connection errors

**Solutions:**
```bash
# Kill process on port 8080
sudo lsof -t -i tcp:8080 | xargs kill -9

# Verify PostgreSQL is running
brew services start postgresql
# or
sudo systemctl start postgresql

# Check database exists
psql -U postgres -c "CREATE DATABASE task_management;"
```

### Issue 2: JSON Parsing Errors
**Symptoms:**
- 400 Bad Request on POST/PUT
- Invalid JSON format errors

**Solutions:**
- Verify Content-Type header: `application/json`
- Check JSON syntax (no trailing commas)
- Ensure enum values match exactly: `TODO`, `IN_PROGRESS`, `DONE`
- Ensure priority values match: `LOW`, `MEDIUM`, `HIGH`

### Issue 3: 404 Not Found
**Symptoms:**
- Endpoints returning 404
- Application seems to be running

**Solutions:**
- Verify base URL: `http://localhost:8080`
- Check controller mapping: `/api/tasks`
- Ensure application is running on correct port
- Check Spring Boot logs for errors

### Issue 4: Foreign Key Constraint Violations
**Symptoms:**
- Database constraint errors
- Cannot insert task without user

**Solutions:**
- Currently user relationship is nullable in code but database expects it
- Temporary fix: Make user optional in Task entity
- Proper fix: Implement authentication in Week 2

---

## 📝 Bug Tracking Template

### Bug Report Format
```
**Bug ID:** BUG-001
**Date:** 2024-11-18
**Reporter:** Your Name
**Severity:** High/Medium/Low
**Status:** Open/In Progress/Fixed

**Description:**
Brief description of the issue

**Steps to Reproduce:**
1. Step 1
2. Step 2
3. Step 3

**Expected Result:**
What should happen

**Actual Result:**
What actually happens

**Solution:**
How it was fixed (if resolved)

**Commit Hash:**
Git commit that fixes the issue
```

---

## 🔍 Testing Results Documentation

### Test Execution Log
```
Date: 2024-11-18
Time: 11:30 AM
Tester: [Your Name]

POST /api/tasks
├── ✅ Valid task creation - PASS
├── ❌ Missing title validation - FAIL (Bug-001)
├── ✅ Invalid enum handling - PASS
└── ✅ Response format - PASS

GET /api/tasks
├── ✅ Empty database - PASS
├── ✅ Multiple tasks - PASS
└── ✅ Response format - PASS

GET /api/tasks/{id}
├── ✅ Valid ID - PASS
├── ✅ Invalid ID (404) - PASS
└── ❌ Non-numeric ID handling - FAIL (Bug-002)

PUT /api/tasks/{id}
├── ✅ Valid update - PASS
├── ✅ Invalid ID (404) - PASS
└── ✅ Partial update - PASS

DELETE /api/tasks/{id}
├── ✅ Valid deletion - PASS
├── ✅ Invalid ID (404) - PASS
└── ✅ Verification of deletion - PASS
```

---

## 📊 Week 1 Completion Summary

### Achievements ✅
- [x] **Day 1:** Environment Setup Complete
- [x] **Day 2:** Spring Boot Project Running
- [x] **Day 3:** User Entity & Repository
- [x] **Day 4:** Task Entity with Relationships
- [x] **Day 5:** TaskService CRUD Operations
- [x] **Day 6:** REST Controller Implementation
- [x] **Day 7:** API Testing & Bug Fixes *(IN PROGRESS)*

### Code Quality Metrics
```
Lines of Code: ~300
Classes Created: 7
API Endpoints: 5
Database Tables: 2
Test Coverage: Manual testing (Unit tests in Week 5)
```

### Skills Demonstrated
- ✅ Spring Boot project setup
- ✅ JPA entity relationships
- ✅ Repository pattern implementation
- ✅ Service layer design
- ✅ REST API development
- ✅ Database schema design
- ✅ Exception handling
- ✅ API testing with Postman

---

## 🚀 Week 2 Preparation

### Immediate Next Steps (Day 8)
1. **Add Spring Security dependencies** to pom.xml
2. **Study JWT authentication flow**
3. **Plan authentication architecture**
4. **Review Spring Security documentation**

### Week 2 Dependencies
```xml
<!-- Add to pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
</dependency>
```

### Authentication Flow Planning
```
1. User Registration → Hash password → Store in database
2. User Login → Validate credentials → Generate JWT token
3. Protected Endpoints → Validate JWT → Allow access
4. Token Expiration → Return 401 → Require re-login
```

---

## 📚 Study Materials for Week 2

### Required Reading
- [Spring Security Architecture](https://spring.io/guides/topicals/spring-security-architecture)
- [JWT Introduction](https://jwt.io/introduction)
- [Spring Boot Security Tutorial](https://spring.io/guides/gs/securing-web/)

### YouTube Videos
- "Spring Security JWT Tutorial" by Amigoscode
- "Spring Boot Authentication" by Java Brains
- "JWT Explained" by Programming with Mosh

---

## ✅ Day 7 Completion Checklist

### Before Moving to Week 2
- [ ] All API endpoints tested in Postman
- [ ] All discovered bugs documented and fixed
- [ ] Postman collection saved and exported
- [ ] Code committed to Git with message: "Week 1 complete - Bug fixes after API testing"
- [ ] Week 1 achievements documented
- [ ] Week 2 dependencies researched
- [ ] Authentication flow understood
- [ ] Next day's tasks planned

### Git Commit Commands
```bash
# Stage all changes
git add .

# Commit with descriptive message
git commit -m "Week 1 Day 7: Complete API testing and bug fixes

- Tested all CRUD endpoints in Postman
- Fixed validation issues
- Documented API behavior
- Ready for Week 2 authentication implementation"

# Push to remote repository
git push origin main
```

---

**🎯 Week 1 Goal Status: COMPLETE** ✅  
**📅 Next Phase:** Week 2 - Authentication & Security  
**🚀 Confidence Level:** Ready to proceed with JWT implementation  

*Remember: You've successfully built a working REST API with full CRUD operations. Week 2 will add the security layer that makes this production-ready!*
