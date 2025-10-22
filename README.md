# Activity Scheduler

A Spring Boot REST API application for managing user activities and scheduling. This project provides a comprehensive backend service with user management capabilities, built with modern Java technologies and best practices.

## 🚀 Features

- **User Management**: Complete CRUD operations for user entities with email validation
- **Organization Management**: Full organization lifecycle management with creator tracking
- **Membership Management**: User-organization relationship management with status tracking
- **RESTful API**: Well-documented REST endpoints with OpenAPI/Swagger documentation
- **Database Integration**: MySQL database with JPA/Hibernate and composite keys
- **Health Monitoring**: Application and database health check endpoints
- **Code Quality**: Integrated with Checkstyle, PMD, and JaCoCo for code quality assurance
- **Testing**: Comprehensive unit and integration tests with 80%+ coverage target
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
git clone https://github.com/YiliYu2002/COMS4156-Hachimi.git
cd activity-scheduler
```

### 2. Prerequisites Setup

**Required Software:**
- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- Git

**Verify Installation:**
```bash
java -version  # Should show Java 17+
mvn -version  # Should show Maven 3.6+
mysql --version  # Should show MySQL 8.0+
```

### 3. Database Setup

**Option 1: Use Existing Database (Recommended)**
The application is configured to use a remote MySQL database. No local setup required. You will need to add your cloud database url and corresponding port, username, and password to `src/main/resources/application.yml`. See the [Application Properties](#application-properties) section below for detailed configuration options.

**Option 2: Local Database Setup**
If you want to use a local database, update `src/main/resources/application.yml`:

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

### 4. Build the Application

```bash
# Clean and compile
mvn clean compile

# Run all tests
mvn test

# Build the JAR file
mvn clean package

# Run code quality checks
mvn checkstyle:check
mvn pmd:check
```

### 5. Run the Application

**Option 1: Maven Spring Boot Plugin (Recommended for Development)**
```bash
mvn spring-boot:run
```

**Option 2: JAR File**
```bash
# Build first
mvn clean package

# Run the JAR
java -jar target/activity-scheduler-0.0.1-SNAPSHOT.jar
```

**Option 3: Maven Wrapper (if available)**
```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

### 6. Verify Installation

**Health Checks:**
```bash
# Basic health check
curl http://localhost:8080/health/basic

# Database health check
curl http://localhost:8080/health/db
```

**API Documentation:**
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## 📚 API Documentation

### Interactive API Documentation (Swagger UI)

Once the application is running, access the interactive API documentation at:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`
- **OpenAPI YAML**: `http://localhost:8080/v3/api-docs.yaml`

### API Endpoints

#### Health Check Endpoints

| Method | Endpoint | Description | Status Codes |
|--------|----------|-------------|-------------|
| `GET` | `/health/basic` | Basic application health check | 200: Application running |
| `GET` | `/health/db` | Database connectivity check | 200: Database connected |

**Side Effects:** None

#### User Management

| Method | Endpoint | Description | Status Codes |
|--------|----------|-------------|-------------|
| `GET` | `/api/users` | Get all users | 200: Success |
| `GET` | `/api/users/{id}` | Get user by ID | 200: Found, 404: Not found |
| `GET` | `/api/users/exists?email={email}` | Check if user exists by email | 200: Boolean result |
| `POST` | `/api/users/register` | Create a new user | 200: Created, 400: Invalid data, 409: User exists |

**Request/Response Examples:**

**Create a User:**
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "displayName": "John Doe"
  }'
```

**Response (200):**
```json
{
  "id": "uuid-generated-id",
  "email": "user@example.com",
  "displayName": "John Doe",
  "isActive": true,
  "createdAt": "2024-01-01T00:00:00Z"
}
```

**Side Effects:** User creation adds a new record to the database


**Get All Users:**
```bash
curl -X GET http://localhost:8080/api/users
```

#### Organization Management

| Method | Endpoint | Description | Status Codes |
|--------|----------|-------------|-------------|
| `GET` | `/api/organizations` | Get all organizations | 200: Success |
| `GET` | `/api/organizations/{id}` | Get organization by ID | 200: Found, 404: Not found |
| `GET` | `/api/organizations/by-name?name={name}` | Get organization by name | 200: Found, 404: Not found |
| `GET` | `/api/organizations/exists?name={name}` | Check if organization exists | 200: Boolean result |
| `GET` | `/api/organizations/count` | Get organization count | 200: Count |
| `POST` | `/api/organizations/create` | Create new organization | 201: Created, 400: Invalid data, 409: Name exists |
| `PUT` | `/api/organizations/{id}` | Update organization | 200: Updated, 400: Invalid data, 404: Not found, 409: Name exists |
| `DELETE` | `/api/organizations/{id}` | Delete organization | 204: Deleted, 404: Not found |

**Request/Response Examples:**

**Create Organization:**
```bash
curl -X POST http://localhost:8080/api/organizations/create \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Acme Corp",
    "createdBy": "user-uuid"
  }'
```

**Response (201):**
```json
{
  "id": "org-uuid",
  "name": "Acme Corp",
  "createdBy": "user-uuid",
  "createdAt": "2024-01-01T00:00:00Z"
}
```

**Side Effects:** Organization creation adds a new record to the database

#### Membership Management

| Method | Endpoint | Description | Status Codes |
|--------|----------|-------------|-------------|
| `GET` | `/api/memberships` | Get all memberships | 200: Success |
| `GET` | `/api/memberships/{orgId}/{userId}` | Get specific membership | 200: Found, 404: Not found |
| `GET` | `/api/memberships/organization/{orgId}` | Get memberships by organization | 200: Success |
| `GET` | `/api/memberships/user/{userId}` | Get memberships by user | 200: Success |
| `GET` | `/api/memberships/status/{status}` | Get memberships by status | 200: Success |
| `GET` | `/api/memberships/{orgId}/{userId}/exists` | Check membership existence | 200: Boolean result |
| `GET` | `/api/memberships/organization/{orgId}/active-count` | Count active members | 200: Count |
| `GET` | `/api/memberships/user/{userId}/count` | Count user memberships | 200: Count |
| `POST` | `/api/memberships` | Create new membership | 200: Created, 400: Invalid data, 409: Exists |
| `PUT` | `/api/memberships/{orgId}/{userId}/status` | Update membership status | 200: Updated, 400: Invalid data, 404: Not found |
| `DELETE` | `/api/memberships/{orgId}/{userId}` | Delete membership | 200: Deleted, 404: Not found |

**Request/Response Examples:**

**Create Membership:**
```bash
curl -X POST http://localhost:8080/api/memberships \
  -H "Content-Type: application/json" \
  -d '{
    "orgId": "org-uuid",
    "userId": "user-uuid",
    "status": "INVITED"
  }'
```

**Response (200):**
```json
{
  "orgId": "org-uuid",
  "userId": "user-uuid",
  "status": "INVITED",
  "createdAt": "2024-01-01T00:00:00Z"
}
```

**Update Membership Status:**
```bash
curl -X PUT http://localhost:8080/api/memberships/org-uuid/user-uuid/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "ACTIVE"
  }'
```

**Valid Membership Status Values:**
- `ACTIVE` - User is an active member
- `INVITED` - User has been invited but not yet accepted
- `SUSPENDED` - User's membership has been suspended

**Side Effects:** Membership operations modify the relationship between users and organizations

### API Call Ordering Requirements

**Required Order:**
1. **Users must be created before organizations** - Organizations require a valid `createdBy` user ID
2. **Users and organizations must exist before memberships** - Memberships require valid user and organization IDs

**Forbidden Orders:**
- Cannot create organizations without existing users
- Cannot create memberships without existing users and organizations
- Cannot update membership status to ACTIVE/SUSPENDED without valid membership

### Error Codes

| Status Code | Description | Common Causes |
|-------------|-------------|---------------|
| 200 | Success | Request completed successfully |
| 201 | Created | Resource created successfully |
| 204 | No Content | Resource deleted successfully |
| 400 | Bad Request | Invalid request data or parameters |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Resource already exists or constraint violation |

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
│   │   │       ├── user/
│   │   │       │   ├── controller/
│   │   │       │   │   └── UserController.java
│   │   │       │   ├── dto/
│   │   │       │   │   └── UserRegistrationRequest.java
│   │   │       │   ├── model/
│   │   │       │   │   └── User.java
│   │   │       │   └── repository/
│   │   │       │       └── UserRepository.java
│   │   │       ├── organization/
│   │   │       │   ├── controller/
│   │   │       │   │   └── OrganizationController.java
│   │   │       │   ├── dto/
│   │   │       │   │   └── OrganizationCreationRequest.java
│   │   │       │   ├── model/
│   │   │       │   │   └── Organization.java
│   │   │       │   ├── repository/
│   │   │       │   │   └── OrganizationRepository.java
│   │   │       │   └── service/
│   │   │       │       └── OrganizationService.java
│   │   │       ├── membership/
│   │   │       │   ├── controller/
│   │   │       │   │   └── MembershipController.java
│   │   │       │   ├── model/
│   │   │       │   │   ├── Membership.java
│   │   │       │   │   ├── MembershipId.java
│   │   │       │   │   └── MembershipStatus.java
│   │   │       │   ├── repository/
│   │   │       │   │   └── MembershipRepository.java
│   │   │       │   └── service/
│   │   │       │       └── MembershipService.java
│   │   │       ├── event/
│   │   │       │   ├── controller/
│   │   │       │   ├── model/
│   │   │       │   └── repository/
│   │   │       ├── conflict/
│   │   │       └── invitation/
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application.properties
│   └── test/
│       └── java/
│           └── com/example/activityscheduler/
│               ├── ActivitySchedulerApplicationTests.java
│               ├── user/
│               │   ├── UserControllerTests.java
│               │   └── UserTests.java
│               ├── organization/
│               │   ├── OrganizationControllerTests.java
│               │   └── OrganizationTests.java
│               └── membership/
│                   ├── MembershipControllerTests.java
│                   ├── MembershipIdTests.java
│                   ├── MembershipServiceTests.java
│                   ├── MembershipStatusTests.java
│                   └── MembershipTests.java
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

### Test Categories

**Unit Tests:**
- User entity tests
- Organization entity tests  
- Membership entity tests
- Service layer tests
- Controller tests

**Integration Tests:**
- API endpoint tests
- Database integration tests
- Repository tests

### Code Quality Checks

```bash
# Checkstyle (Google Java Style Guide)
mvn checkstyle:check

# PMD (Static code analysis)
mvn pmd:check

# Format code (Google Java Format)
mvn fmt:format

# Run all quality checks
mvn clean compile test checkstyle:check pmd:check
```

### Test Coverage

The project uses JaCoCo for code coverage reporting:
- **Target Coverage:** 80%+ line coverage
- **Report Location:** `target/site/jacoco/index.html`
- **Coverage includes:** All main source files excluding generated code

## 📊 Code Quality & Coverage

This project maintains high code quality standards with:

- **Checkstyle**: Google Java Style Guide compliance
- **PMD**: Static code analysis
- **JaCoCo**: Code coverage reporting
- **JUnit 5**: Comprehensive testing
- **Mockito**: Mock testing framework

## 🚀 CI/CD Pipeline

**Recommended GitHub Actions Workflow (`.github/workflows/maven-ci.yaml`):**
```yaml
name: Maven CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        
    - name: Run tests
      run: mvn clean test
      
    - name: Run code quality checks
      run: mvn checkstyle:check pmd:check
      
    - name: Generate coverage report
      run: mvn jacoco:report
      
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v3
```

**Pipeline Features:**
- Runs on every push and pull request
- Executes Maven build and tests
- Generates code coverage reports
- Ensures code quality standards
- Caches Maven dependencies for faster builds

## 📝 Database Schema

### Users Table

| Column | Type | Description |
|--------|------|-------------|
| `id` | CHAR(36) | Primary key (UUID) |
| `email` | VARCHAR(320) | User email (unique) |
| `display_name` | VARCHAR(255) | User display name |
| `is_active` | BOOLEAN | Account status |
| `created_at` | TIMESTAMP | Creation timestamp |

### Organizations Table

| Column | Type | Description |
|--------|------|-------------|
| `id` | CHAR(36) | Primary key (UUID) |
| `name` | VARCHAR(255) | Organization name (unique) |
| `created_by` | VARCHAR(36) | User ID of creator (foreign key) |
| `created_at` | TIMESTAMP | Creation timestamp |

### Memberships Table

| Column | Type | Description |
|--------|------|-------------|
| `org_id` | CHAR(36) | Organization ID (composite primary key) |
| `user_id` | CHAR(36) | User ID (composite primary key) |
| `status` | ENUM | Membership status (ACTIVE, INVITED, SUSPENDED) |
| `created_at` | TIMESTAMP | Creation timestamp |

**Note:** The `memberships` table uses a composite primary key (`org_id`, `user_id`) to ensure unique user-organization relationships.

### Membership Status Values

| Status | Description |
|--------|-------------|
| `ACTIVE` | User is an active member of the organization |
| `INVITED` | User has been invited but hasn't accepted yet |
| `SUSPENDED` | User's membership has been suspended |

## 🔧 Configuration

### Configuration Files

The project includes the following configuration files:

**Main Configuration:**
- `src/main/resources/application.yml` - Main application configuration
- `src/main/resources/application.properties` - Alternative properties format
- `pom.xml` - Maven project configuration
- `google_checks.xml` - Checkstyle configuration

### Application Properties

**Current Configuration (`application.yml`):**
We do not wish to expose our db configuration in a public GitHub repository
```yaml
spring:
  datasource:
    url: jdbc:mysql://{your_db_external_ip}:3306/{your_database_name}
    username: {your_user_name}
    password: {your_pass_word}
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
  jackson:
    mapper:
      ACCEPT_CASE_INSENSITIVE_ENUMS: true

management:
  endpoints:
    web:
      exposure:
        include: health, info
  endpoint:
    health:
      show-details: always

info:
  build:
    name: activity-scheduler
    version: 0.0.1-SNAPSHOT
    group: com.example
    artifact: activity-scheduler
    description: Activity Scheduler application

server:
  port: 8080
```

### Environment Variables

**Database Configuration:**
- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password
- `DB_URL` - Database URL

**Server Configuration:**
- `SERVER_PORT` - Server port (default: 8080)

### Maven Configuration

**Key Maven Plugins:**
- `spring-boot-maven-plugin` - Spring Boot packaging
- `maven-checkstyle-plugin` - Code style checking
- `maven-pmd-plugin` - Static code analysis
- `jacoco-maven-plugin` - Code coverage reporting
- `fmt-maven-plugin` - Code formatting

### Code Quality Configuration

**Checkstyle:** Uses Google Java Style Guide (`google_checks.xml`)
**PMD:** Static code analysis with default rules
**JaCoCo:** Code coverage reporting with 80%+ target

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## AI Tools Usage

This project utilized various AI tools to assist in development, testing, and documentation. All AI-generated code is clearly marked throughout the codebase.

### AI Tools Used

**1. ChatGPT (Free Tier)**
- **Source**: OpenAI ChatGPT (free tier with .edu email)
- **Usage**: Code review, debugging, and architectural guidance
- **Prompts**: 
  - "Review this Spring Boot controller for best practices"
  - "Help debug JPA composite key implementation"
  - "Suggest improvements for test coverage"
- **Generated Code**: 
  - Error handling improvements
  - Test case scenarios
  - Documentation templates

**2. Cursor AI (Free Tier)**
- **Source**: Cursor IDE with free tier access
- **Usage**: Code refactoring, documentation generation, and README updates
- **Prompts**:
  - "Generate comprehensive API documentation"
  - "Create database schema documentation"
  - "Update README with current project structure"
- **Generated Content**:
  - README.md sections
  - API endpoint documentation
  - Database schema tables

### AI-Generated Code Marking

We utilized AI to generate test cases in unit testing stage. After the AI generated tests, we 
thoroughly examined the test cases and confirmed that they cover most branches and use cases.
We also prompted AI tools to generate JAVADOC, the AI tools first read through our API definition 
and implemented the JAVADOCs accordingly. In addition, we prompted Cursor to generate this README.md 
to include an overall summary of our project.

## 👥 Authors

- **AlexZhu2** - *User, Organization, Membership Development* - [AlexZhu2](https://github.com/AlexZhu2)
- **YiliYu2002** - *Database Management, Product Manager* - [YiliYu2002](https://github.com/YiliYu2002)

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
