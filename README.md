# Activity Scheduler

A Spring Boot REST API application for managing user activities and scheduling. This project provides a comprehensive backend service with user management capabilities, built with modern Java technologies and best practices.

## 🚀 Features

- **User Management**: Complete CRUD operations for user entities
- **RESTful API**: Well-documented REST endpoints with OpenAPI/Swagger documentation
- **Database Integration**: MySQL database with JPA/Hibernate
- **Code Quality**: Integrated with Checkstyle, PMD, and JaCoCo for code quality assurance
- **Testing**: Comprehensive unit and integration tests
- **CI/CD**: GitHub Actions workflow for automated testing and deployment

## 🛠️ Technology Stack

- **Java 17**
- **Spring Boot 3.5.6**
- **Spring Data JPA**
- **MySQL Database**
- **Maven** (Build Tool)
- **OpenAPI 3.0** (API Documentation)
- **JUnit 5** (Testing)
- **Mockito** (Mocking)
- **JaCoCo** (Code Coverage)
- **Checkstyle** (Code Style)
- **PMD** (Code Analysis)

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- Git

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd activity-scheduler
```

### 2. Database Setup

Create a MySQL database and update the configuration in `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/your_database
    username: your_username
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
```

### 3. Run the Application

```bash
# Compile and run
mvn clean spring-boot:run

# Or build and run JAR
mvn clean package
java -jar target/activity-scheduler-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

## 📚 API Documentation

### Interactive API Documentation (Swagger UI)

Once the application is running, access the interactive API documentation at:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`
- **OpenAPI YAML**: `http://localhost:8080/v3/api-docs.yaml`

### API Endpoints

#### User Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/users` | Get all users |
| `GET` | `/api/users/{id}` | Get user by ID |
| `POST` | `/api/users` | Create a new user |
| `GET` | `/api/users/exists` | Check if user exists by email |

#### Example API Usage

**Create a User:**
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "displayName": "John Doe",
    "tz": "UTC"
  }'
```

**Get All Users:**
```bash
curl -X GET http://localhost:8080/api/users
```

## 🏗️ Project Structure

```
activity-scheduler/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/activityscheduler/
│   │   │       ├── ActivitySchedulerApplication.java
│   │   │       ├── health/
│   │   │       │   └── HealthController.java
│   │   │       └── user/
│   │   │           ├── controller/
│   │   │           │   └── UserController.java
│   │   │           ├── model/
│   │   │           │   └── User.java
│   │   │           └── repository/
│   │   │               └── UserRepository.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application.properties
│   └── test/
│       └── java/
│           └── com/example/activityscheduler/
│               ├── ActivitySchedulerApplicationTests.java
│               └── user/
│                   └── UserControllerTests.java
├── .github/
│   └── workflow/
│       └── maven-ci.yaml
├── .gitattributes
├── .gitignore
├── google_checks.xml
├── mvnw
├── mvnw.cmd
└── pom.xml
```

## 🧪 Testing

### Run Tests

```bash
# Run all tests
mvn test

# Run tests with coverage
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Code Quality Checks

```bash
# Checkstyle
mvn checkstyle:check

# PMD
mvn pmd:check

# Format code
mvn fmt:format
```

## 📊 Code Quality & Coverage

This project maintains high code quality standards with:

- **Checkstyle**: Google Java Style Guide compliance
- **PMD**: Static code analysis
- **JaCoCo**: Code coverage reporting
- **JUnit 5**: Comprehensive testing
- **Mockito**: Mock testing framework

## 🚀 CI/CD Pipeline

The project includes a GitHub Actions workflow (`.github/workflow/maven-ci.yaml`) that:

- Runs on every push and pull request
- Executes Maven build and tests
- Generates code coverage reports
- Ensures code quality standards

## 📝 Database Schema

### Users Table

| Column | Type | Description |
|--------|------|-------------|
| `id` | CHAR(36) | Primary key (UUID) |
| `email` | VARCHAR(320) | User email (unique) |
| `display_name` | VARCHAR(255) | User display name |
| `tz` | VARCHAR(64) | Timezone |
| `is_active` | BOOLEAN | Account status |
| `created_at` | TIMESTAMP | Creation timestamp |

## 🔧 Configuration

### Application Properties

Key configuration options in `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/activity_scheduler
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

server:
  port: 8080
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Authors

- **AlexZhu2** - *Initial work* - [AlexZhu2](https://github.com/AlexZhu2)
- **YiliYu2002** - *Database Management* - [YiliYu2002](https://github.com/YiliYu2002)

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- OpenAPI community for API documentation standards
- All contributors and testers

---

## 📞 Support

If you have any questions or need help, please:

1. Check the [API Documentation](http://localhost:8080/swagger-ui.html)
2. Review the [Issues](../../issues) page
3. Create a new issue if needed

**Happy Coding! 🎉**
