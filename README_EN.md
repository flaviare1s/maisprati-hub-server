# +praTiHub - Backend

**[🇧🇷 Português](README.md)** | **🇺🇸 English**

[![Java](https://img.shields.io/badge/Java-17-red?logo=java)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-green?logo=spring)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-7.0-green?logo=mongodb)](https://www.mongodb.com/)
[![Docker](https://img.shields.io/badge/Docker-3.8-blue?logo=docker)](https://www.docker.com/)

RESTful API built with **Java + Spring Boot** and **MongoDB**, featuring JWT authentication, environment profiles, and Docker support.

---

## 🛠️ Technologies

- **Spring Boot 3.5** - Main framework
- **MongoDB** - NoSQL database
- **Spring Security + JWT** - Authentication and authorization
- **BCrypt** - Password hashing
- **Swagger/OpenAPI** - API documentation available at `http://localhost:8080/swagger-ui/index.html`
- **Docker** - Containerization
- **Lombok** - Boilerplate reduction
- **Maven** - Dependency management

---

## 🚀 Quick Start

### Prerequisites
- [Java 17](https://www.oracle.com/java/technologies/downloads/#java17)
- MongoDB Server
- [Docker](https://www.docker.com/) and Docker Compose (optional)
- [MongoDB Compass](https://www.mongodb.com/try/download/compass) (optional)


### Initial Setup

1. **Clone the repository**
```bash
git clone https://github.com/flaviare1s/maisprati-hub-server.git
cd maisprati-hub-server
```

2. **Configure environment variables**

Create a `.env` file in the project root:
```env
JWT_SECRET=your_base64_encoded_secret_here
APP_FRONTEND_URL=http://localhost:3000
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
GMAIL_API_REFRESH_TOKEN=your_gmail_refresh_token
EMAIL_FROM=your_email@gmail.com
```

3. **Start MongoDB**

- Option A - Use Docker
```bash
docker-compose -f docker/docker-compose.dev.yml up -d
```
- Option B - Use MongoDB Server installed on your PC

4. **Run the application**

**Default profile (dev):**
```bash
# Linux/Mac
./mvnw spring-boot:run

# Windows
.\mvnw.cmd spring-boot:run
```

**Dev profile (explicit):**
```bash
# Linux/Mac
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Windows
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

**Prod profile:**
```bash
# Linux/Mac
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

# Windows
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=prod"
```

The API will be available at `http://localhost:8080`

**Swagger Documentation:** `http://localhost:8080/swagger-ui/index.html`

---

## 🏗️ Architecture

The project follows a clean and well-defined layered architecture:

```
src/main/java/com/maisprati/hub/
├── presentation/         # Controllers and API DTOs
├── application/          # Services and business logic
├── domain/              # Entities and enums
└── infrastructure/      # Repositories, security and configurations
```

### Request Flow
```
Client → Controller → Service → Repository → MongoDB
```

---

## 🔐 Authentication

The system uses **JWT (JSON Web Tokens)** for authentication.

### User Roles
- **ADMIN** - Full administrative access
- **STUDENT** - Student access

### Public Endpoints
- `POST /api/auth/register` - New user registration
- `POST /api/auth/login` - Login and JWT token retrieval

### Protected Endpoints
Require authorization header:
```http
Authorization: Bearer <your_jwt_token>
```

| Method | Endpoint          | Access                  | Description                  |
|--------|-------------------|-------------------------|------------------------------|
| GET    | /api/users        | ADMIN                   | List all users               |
| GET    | /api/users/{id}   | ADMIN or own user       | Get specific user            |
| PUT    | /api/users/{id}   | ADMIN or own user       | Update user                  |
| DELETE | /api/users/{id}   | ADMIN                   | Remove user                  |

---

## 🐳 Docker

### Useful Commands

```bash
# Start containers
docker-compose -f docker/docker-compose.dev.yml up -d

# Stop containers (keeps data)
docker-compose -f docker/docker-compose.dev.yml stop

# Restart containers
docker-compose -f docker/docker-compose.dev.yml start

# Stop and remove containers (keeps volumes)
docker-compose -f docker/docker-compose.dev.yml down

# Stop and remove everything (including data)
docker-compose -f docker/docker-compose.dev.yml down -v

# Access MongoDB shell
docker exec -it mongodev mongosh -u admin -p admin123 --authenticationDatabase admin
```

### MongoDB Connection

**Via MongoDB Compass:**
```
mongodb://admin:admin123@localhost:27017/maisprati-hub
```

**Local configuration (without Docker):**
```
mongodb://localhost:27017/maisprati-hub
```

---

## 📝 Development

### Build the Project
```bash
./mvnw clean install
```

### Run Tests
```bash
./mvnw test
```

### Environment Profiles
- **dev** (default) - Local development
- **prod** - Production (MongoDB Atlas)

### Lombok

The project uses Lombok to reduce boilerplate code.

**VS Code:** Install the extension [Lombok Annotations Support](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-lombok)

**IntelliJ IDEA:**
1. Install the Lombok plugin (Settings → Plugins → Marketplace)
2. Enable Annotation Processing (Settings → Build, Execution, Deployment → Compiler → Annotation Processors)
