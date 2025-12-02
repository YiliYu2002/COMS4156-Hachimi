# Team Hachimi — Event Scheduler API

## Part 1 — Team/Repo/Stack Status

**Language / Platform:** Java 17 + macOS

**GitHub Repository:** https://github.com/YiliYu2002/COMS4156-Hachimi

**Status Change:** N/A

**Team Members:**
- **AlexZhu2** - User, Organization, Membership Development - [AlexZhu2](https://github.com/AlexZhu2)
- **YiliYu2002** - Database Management, Product Manager - [YiliYu2002](https://github.com/YiliYu2002)
- **Doglily3** - Organization Development - [Doglily3](https://github.com/Doglily3)
- **jieji09** - Event Development - [jieji09](https://github.com/jieji09)

**Project Management:** Tasks are tracked using GitHub Projects. [Link to GitHub Projects](https://github.com/YiliYu2002/COMS4156-Hachimi/projects)

## Part 2 — Service Overview

### What the Service Does

A Group Activity Scheduler that coordinates lectures, meetings, and all kinds of events across many industries. This API service allows admin users to create events, add fellow users, and invite other users to arbitrary events. The API system enables users to preview conflicts through its functionality which assists them in better planning their events. The system aims to establish dependable multi-party scheduling that operates across different time zones and organizational boundaries and resource types (rooms, labs and equipment) through a straightforward standards-compliant API which developers can integrate into web and mobile applications.

The service maintains organized scheduling information through its database which includes users and organizations/teams and their respective calendars and events and event instances and invitation tracking with RSVP status and availability and busy time blocks and resources with their capacity and features and event constraints for required attendees and minimum participant numbers and event capacity. The system tracks operational data through notification delivery mechanisms (email/webhook) and audit logs of system modifications and API keys and role-based permissions and external calendar integration through links or tokens. The backend system requires cache memory storage for derived time interval indexes and availability rollups to achieve quick conflict detection and suggestion generation.

### Implementation Status vs. Proposal

**Implemented Features:**
- ✅ User management (registration, login, CRUD operations)
- ✅ Organization management (create, list, update, delete)
- ✅ Membership management (create, list with status tracking)
- ✅ Event management (create, list, update, delete, view by organization/user)
- ✅ Event attendee management (invite, RSVP status updates, view attendees)
- ✅ Conflict detection (automatic detection of scheduling conflicts)
- ✅ Role-based permissions (admin, user roles)
- ✅ RESTful API with OpenAPI 3.0 documentation
- ✅ Database persistence (MySQL for production, H2 for local development)
- ✅ Health monitoring endpoints
- ✅ Multi-client support (multiple client instances can run simultaneously)

**Partially Implemented / Future Enhancements:**
- ⚠️ **Notification delivery mechanisms (email/webhook)**: Currently not implemented. The service architecture supports this, but actual email/webhook delivery is planned for future iterations.
- ⚠️ **External calendar integration**: Not yet implemented. The data model supports this, but integration with external calendar systems (Google Calendar, Outlook) is planned for future work.
- ⚠️ **Cache memory for conflict detection**: Basic conflict detection is implemented, but advanced caching for time interval indexes and availability rollups is planned for optimization.
- ⚠️ **Resource management (rooms, labs, equipment)**: The event model supports capacity constraints, but dedicated resource entities and booking are planned for future iterations.

**Note:** The core functionality matches the proposal. The service provides a fully functional group activity scheduling API with conflict detection, role-based permissions, and multi-client support. Advanced features like external calendar integration and notification delivery are architectural extensions that can be added without changing the core API.

## Client Application

### Client Code Location

The client code is located in the same GitHub repository in the `activity-scheduler-client/` directory:

```
COMS4156-Hachimi/
├── activity-scheduler/          # Service code
└── activity-scheduler-client/   # Client code
    ├── src/
    │   └── main/java/com/example/client/
    │       ├── ActivitySchedulerClient.java
    │       ├── api/
    │       │   └── ApiClient.java
    │       └── model/            # Data models
    ├── pom.xml
    ├── README.md                 # Client-specific documentation
    └── ARCHITECTURE.md           # Client architecture details
```

### What the Client Does

The Activity Scheduler Client is a command-line application that demonstrates how to interact with the Activity Scheduler REST API service. It provides:

- **User Management**: Registration and login with email/display name
- **Organization Management**: Create, list, view, update, and delete organizations
- **Membership Management**: Create memberships and list user's memberships
- **Event Management**: Create, list, update, delete events; view events by organization/user
- **Event Attendee Management**: Invite attendees, update RSVP status, view event attendees
- **Conflict Detection**: Automatic detection of scheduling conflicts when creating events

The client serves as both a demonstration tool and a reference implementation for third-party developers.

### How to Build and Run the Client

#### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Activity Scheduler service running and accessible

#### Build the Client

```bash
# Navigate to client directory
cd activity-scheduler-client

# Build the client application
mvn clean package
```

This creates an executable JAR file: `target/activity-scheduler-client-1.0.0-jar-with-dependencies.jar`

#### Run the Client

**Default (Connects to localhost:8080):**

```bash
java -jar target/activity-scheduler-client-1.0.0-jar-with-dependencies.jar
```

**Custom Service URL:**

```bash
# Specify service URL as command-line argument
java -jar target/activity-scheduler-client-1.0.0-jar-with-dependencies.jar https://your-service-url.com

# Or use environment variable
export SERVICE_URL=https://your-service-url.com
java -jar target/activity-scheduler-client-1.0.0-jar-with-dependencies.jar
```

### How to Connect Client Instances to the Service

1. **Start the Service:**

   **Option A: GCP MySQL (Production/Default)**
   ```bash
   cd activity-scheduler
   mvn spring-boot:run
   ```

   **Option B: Local H2 (Development)**
   ```bash
   cd activity-scheduler
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```

2. **Start Client Instances:**

   In separate terminals, run:
   ```bash
   cd activity-scheduler-client
   java -jar target/activity-scheduler-client-1.0.0-jar-with-dependencies.jar
   ```

   Each client instance will:
   - Prompt for email and display name
   - Register or login the user
   - Receive a unique User ID (UUID)
   - Connect to the same service instance

### Multiple Client Support

The service supports multiple client instances running simultaneously. Here's how it works:

#### How Clients are Distinguished

**User ID Mechanism:**
1. When a client starts, it prompts for email and display name
2. The service assigns a unique UUID (User ID) to each user based on their email
3. Each client instance stores its `currentUserId` after login/registration
4. All API calls include the User ID to identify which client/user is making the request

**Data Model:**
- **Shared Data**: Organizations, Events, Memberships, and Attendees are shared across all clients
- **User Attribution**: All data is tagged with the creator's User ID (`createdBy` field)
- **User-Specific Queries**: Clients can filter data by User ID (e.g., "My Events", "My Memberships")

#### How to Verify Multi-Client Support

See [MULTI_CLIENT_VERIFICATION.md](MULTI_CLIENT_VERIFICATION.md) for detailed verification steps. Quick test:

1. Start the service
2. Run client instance 1 in terminal 1, register as `alice@test.com`
3. Run client instance 2 in terminal 2, register as `bob@test.com`
4. Create an organization from client 1
5. List organizations from client 2 - you should see the organization created by client 1
6. Both clients can work with the same data simultaneously

**Technical Details:**
- Each user gets a unique UUID that persists across sessions (same email = same User ID)
- Organizations and events are shared but attributed to their creator
- "My Events" endpoint filters by User ID: `/api/events/user/{userId}`
- "My Memberships" endpoint filters by User ID: `/api/memberships/user/{userId}`

## End-to-End Client/Service Testing

We have implemented comprehensive end-to-end tests where the client exercises the functionality of the service. These tests are **manually executed** using a detailed checklist.

### Test Documentation

See [E2E_TESTING.md](E2E_TESTING.md) for the complete end-to-end testing checklist.

### Test Coverage

The end-to-end tests cover:

1. **Service Health Check** - Verify service is running and accessible
2. **User Registration** - Register new users
3. **User Login** - Login with existing users
4. **Organization Management** - Create, list organizations
5. **Membership Management** - Create memberships, list user memberships
6. **Event Management** - Create, list, view, update, delete events
7. **Event Attendee Management** - Invite attendees, update RSVP, view attendees
8. **Multi-Client Testing** - Verify multiple clients can run simultaneously
9. **Error Handling** - Invalid inputs, service unavailable scenarios

### Running End-to-End Tests

**Prerequisites:**
1. Service is running (see [How to Connect Client Instances](#how-to-connect-client-instances-to-the-service))
2. Client is built (see [How to Build and Run the Client](#build-the-client))

**Test Execution:**
1. Follow the checklist in [E2E_TESTING.md](E2E_TESTING.md)
2. Execute each test case manually
3. Record results (Pass/Fail) and notes
4. Verify expected outcomes match actual results

**Why Manual Testing:**
End-to-end tests are run manually because:
- The client is an interactive command-line application
- Tests require user input and verification of output
- Some scenarios require multiple terminals and coordinated actions
- Manual testing allows for exploratory testing and edge case discovery

The checklist format ensures tests can be re-run consistently with the same inputs and expected outcomes.

## Continuous Integration

We have implemented a continuous integration pipeline using GitHub Actions that automates style checking, static analysis, and testing.

### CI Configuration

**Location:** `.github/workflows/maven-ci.yaml`

**Workflow:**
```yaml
name: Java CI with Maven

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - Checkout repository
      - Set up JDK 17
      - Build and verify (mvn clean verify)
      - Run Checkstyle (mvn checkstyle:check)
      - Run PMD (mvn pmd:check)
      - Run Tests with Coverage (mvn test jacoco:report)
      - Upload JaCoCo coverage report
```

### Automated Checks

The CI pipeline automatically runs:

1. **Maven Build** - Compiles the project and verifies dependencies
2. **Checkstyle** - Google Java Style Guide compliance checking
3. **PMD** - Static code analysis for bug detection
4. **Unit Tests** - JUnit 5 tests with Mockito
5. **Code Coverage** - JaCoCo coverage reporting (target: 80%+)

### CI Reports

CI reports are available in:
- **GitHub Actions**: View workflow runs in the "Actions" tab of the repository
- **Coverage Reports**: Uploaded as artifacts in each CI run
- **Code Quality Screenshots**: See `github_resources/` directory:
  - `checkstyle.png` - Checkstyle verification
  - `pmd.png` - PMD static analysis
  - `testing.png` - Test execution
  - `jacoco.png` - Code coverage report

### Manual Testing

**End-to-End Testing** is run manually (see [End-to-End Client/Service Testing](#end-to-end-clientservice-testing)) because:
- The client is an interactive command-line application
- Tests require user input and multiple terminal sessions
- Manual execution allows for exploratory testing

All other testing (unit tests, integration tests) is automated in the CI pipeline.

## Documentation

### API Documentation

#### Interactive API Documentation (Swagger UI)

Once the application is running, access the interactive API documentation at:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`
- **OpenAPI YAML**: `http://localhost:8080/v3/api-docs.yaml`

#### Static API Documentation

**REST API Documentation:**
- **Location**: `github_resources/api-docs.html`
- **Format**: OpenAPI 3.0 HTML documentation
- **Contents**: All REST endpoints, request/response schemas, status codes, examples

**Java API Documentation (Javadoc):**
- **Location**: `github_resources/apidocs/`
- **Generate**: `mvn javadoc:javadoc`
- **Contents**: Java classes, methods, parameters, return types

**OpenAPI Specification:**
- **Location**: `openapi.yaml` (root directory)
- **Format**: OpenAPI 3.0 YAML specification

### API Entry Points

#### Base URL
- **Local Development**: `http://localhost:8080`
- **Production**: Configure via `application.yml` or environment variables

#### Main API Endpoints

**User Management:**
- `POST /api/users/register` - Register a new user
- `GET /api/users/{id}` - Get user by ID
- `PUT /api/users/{id}/username` - Update user display name

**Organization Management:**
- `POST /api/organizations/create` - Create an organization
- `GET /api/organizations` - List all organizations
- `GET /api/organizations/{id}` - Get organization by ID
- `PUT /api/organizations/{id}` - Update organization
- `DELETE /api/organizations/{id}` - Delete organization

**Membership Management:**
- `POST /api/memberships/create` - Create a membership
- `GET /api/memberships` - List all memberships
- `GET /api/memberships/user/{userId}` - Get memberships for a user
- `GET /api/memberships/organization/{orgId}` - Get memberships for an organization

**Event Management:**
- `POST /api/events/create` - Create an event
- `GET /api/events` - List all events
- `GET /api/events/{id}` - Get event by ID
- `GET /api/events/user/{userId}` - Get events created by a user
- `GET /api/events/organization/{orgId}` - Get events for an organization
- `PUT /api/events/{id}` - Update an event
- `DELETE /api/events/{id}` - Delete an event

**Event Attendee Management:**
- `POST /api/attendees/invite` - Invite an attendee to an event
- `PUT /api/attendees/rsvp` - Update RSVP status
- `GET /api/attendees/event/{eventId}` - Get attendees for an event

**Conflict Detection:**
- `GET /api/conflicts/user/{userId}` - Get conflicts for a user
- `GET /api/conflicts/event/{eventId}` - Get conflicts for an event

**Health Monitoring:**
- `GET /health/basic` - Basic health check
- `GET /health/db` - Database health check

#### API Call Ordering Requirements

**Important:** Some endpoints must be called in a specific order:

1. **Users must be created before organizations**
   - Organizations require a valid `createdBy` user ID
   - Call `POST /api/users/register` first

2. **Users and organizations must exist before memberships**
   - Memberships require valid user and organization IDs
   - Create users and organizations before creating memberships

3. **Organizations must exist before events**
   - Events require a valid `orgId`
   - Create organizations before creating events

4. **Events must exist before inviting attendees**
   - Attendees require a valid event ID
   - Create events before inviting attendees

#### Status Codes

- `200 OK` - Request successful
- `201 Created` - Resource created successfully
- `400 Bad Request` - Invalid request data
- `404 Not Found` - Resource not found
- `409 Conflict` - Resource conflict (e.g., duplicate email)
- `500 Internal Server Error` - Server error

For complete API documentation including all endpoints, request/response schemas, status codes, and examples, see:
- **Interactive**: `http://localhost:8080/swagger-ui.html` (when service is running)
- **Static**: `github_resources/api-docs.html`

### How to Build and Test the Service Using CI

#### Local Build and Test

```bash
# Navigate to service directory
cd activity-scheduler

# Clean and compile
mvn clean compile

# Run all tests
mvn test

# Run tests with coverage
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html

# Run code quality checks
mvn checkstyle:check
mvn pmd:check

# Build JAR file
mvn clean package
```

#### CI Pipeline

The CI pipeline automatically runs on every push to `main` and on pull requests:

1. **Checkout** - Clones the repository
2. **Setup JDK 17** - Configures Java environment
3. **Build and Verify** - Runs `mvn clean verify`
4. **Checkstyle** - Validates code style
5. **PMD** - Runs static analysis
6. **Tests with Coverage** - Executes tests and generates coverage report
7. **Upload Reports** - Saves coverage reports as artifacts

**View CI Results:**
- Go to the "Actions" tab in GitHub
- Click on a workflow run to see detailed results
- Download coverage reports from artifacts

### Client Documentation

For detailed client documentation, see:
- **Client README**: `activity-scheduler-client/README.md`
- **Client Architecture**: `activity-scheduler-client/ARCHITECTURE.md`
- **End-to-End Testing**: `E2E_TESTING.md`
- **Multi-Client Verification**: `MULTI_CLIENT_VERIFICATION.md`

### Third-Party Client Development

To develop your own client program that uses the Activity Scheduler service:

#### 1. Understand the API

- Review the [API Documentation](#api-documentation) section above
- Access Swagger UI at `http://localhost:8080/swagger-ui.html` when the service is running
- Study the OpenAPI specification in `openapi.yaml`

#### 2. API Authentication/Identification

The service uses **User ID** for client identification:

- **Register/Login**: Call `POST /api/users/register` with email and display name
- **User ID**: The service returns a UUID (User ID) - store this for subsequent requests
- **Include User ID**: Include the User ID in request bodies (e.g., `createdBy` field) or as query parameters

**Example:**
```bash
# Register a user
POST /api/users/register
{
  "email": "user@example.com",
  "displayName": "John Doe"
}

# Response includes User ID
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "displayName": "John Doe"
}

# Use this ID in subsequent requests
POST /api/organizations/create
{
  "name": "My Organization",
  "createdBy": "550e8400-e29b-41d4-a716-446655440000"
}
```

#### 3. HTTP Client Setup

Use any HTTP client library in your preferred language:

- **Java**: `java.net.http.HttpClient` (Java 11+) or Apache HttpClient
- **Python**: `requests` library
- **JavaScript/Node.js**: `fetch` API or `axios`
- **cURL**: Command-line tool for testing

#### 4. Request/Response Format

- **Content-Type**: `application/json`
- **Request Body**: JSON objects matching the API schemas
- **Response Body**: JSON objects with resource data
- **Status Codes**: Check status codes (200, 201, 400, 404, 409, 500)

#### 5. Error Handling

Handle common error scenarios:

- **400 Bad Request**: Invalid input data - check request body format
- **404 Not Found**: Resource doesn't exist - verify IDs are correct
- **409 Conflict**: Duplicate resource (e.g., email already exists)
- **500 Internal Server Error**: Server error - retry or contact support

#### 6. Multi-Client Support

Your client should:
- Store the User ID after registration/login
- Include User ID in requests that require user identification
- Handle concurrent requests from multiple client instances
- Filter data by User ID when needed (e.g., "My Events")

#### 7. Reference Implementation

Study the provided client as a reference:
- **Location**: `activity-scheduler-client/src/main/java/com/example/client/`
- **Key Files**:
  - `ActivitySchedulerClient.java` - Main client logic
  - `ApiClient.java` - HTTP client wrapper
  - `model/` - Data models matching API schemas

#### 8. Testing Your Client

- Start the service: `cd activity-scheduler && mvn spring-boot:run`
- Test your client against the service
- Use the end-to-end test checklist in `E2E_TESTING.md` as a guide
- Verify multi-client support by running multiple instances

### Configuration Files

All configuration files are included in the repository:

**Service Configuration:**
- `activity-scheduler/src/main/resources/application.yml` - Main configuration (GCP MySQL)
- `activity-scheduler/src/main/resources/application-local.yml` - Local H2 configuration
- `activity-scheduler/src/main/resources/application-prod.yml.example` - Production template
- `activity-scheduler/pom.xml` - Maven project configuration
- `activity-scheduler/google_checks.xml` - Checkstyle configuration

**Client Configuration:**
- `activity-scheduler-client/pom.xml` - Maven project configuration

**CI/CD Configuration:**
- `.github/workflows/maven-ci.yaml` - GitHub Actions workflow

**Note:** Sensitive information (database passwords, API keys) should not be committed. Use environment variables or configuration files that are excluded from version control (see `.gitignore`).

## 🛠️ Technology Stack

- **Language**: Java 17 LTS
- **Framework**: Spring Boot 3.5.6
- **Build Tool**: Maven 3.9.5
- **Database**: MySQL 8.0+ (production), H2 (local development)
- **API Documentation**: OpenAPI 3.0 / Swagger
- **Testing**: JUnit 5, Mockito
- **Code Coverage**: JaCoCo
- **Code Quality**: Checkstyle (Google Style Guide), PMD
- **CI/CD**: GitHub Actions

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+ (for production) or H2 (for local development)
- Git

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/YiliYu2002/COMS4156-Hachimi.git
cd COMS4156-Hachimi
```

### 2. Build and Run the Service

**Option A: GCP MySQL (Production/Default)**
```bash
cd activity-scheduler
mvn spring-boot:run
```

**Option B: Local H2 (Development)**
```bash
cd activity-scheduler
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

The service will start on `http://localhost:8080`

### 3. Build and Run the Client

```bash
cd activity-scheduler-client
mvn clean package
java -jar target/activity-scheduler-client-1.0.0-jar-with-dependencies.jar
```

### 4. Verify Installation

```bash
# Health check
curl http://localhost:8080/health/basic

# Should return: "Application is running"
```

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
| `created_by` | CHAR(36) | User ID of creator (foreign key) |
| `created_at` | TIMESTAMP | Creation timestamp |

### Memberships Table

| Column | Type | Description |
|--------|------|-------------|
| `org_id` | CHAR(36) | Organization ID (composite primary key) |
| `user_id` | CHAR(36) | User ID (composite primary key) |
| `status` | ENUM | Membership status (ACTIVE, INVITED, SUSPENDED) |
| `created_at` | TIMESTAMP | Creation timestamp |

### Events Table

| Column | Type | Description |
|--------|------|-------------|
| `id` | CHAR(36) | Primary key (UUID) |
| `org_id` | CHAR(36) | Organization ID (foreign key) |
| `title` | VARCHAR(255) | Event title |
| `description` | TEXT | Event description |
| `start_at` | TIMESTAMP | Event start time |
| `end_at` | TIMESTAMP | Event end time |
| `capacity` | INTEGER | Maximum attendees (nullable) |
| `created_by` | CHAR(36) | User ID of creator (foreign key) |
| `created_at` | TIMESTAMP | Creation timestamp |

### Event Attendees Table

| Column | Type | Description |
|--------|------|-------------|
| `event_id` | CHAR(36) | Event ID (composite primary key) |
| `user_id` | CHAR(36) | User ID (composite primary key) |
| `rsvp_status` | ENUM | RSVP status (PENDING, YES, NO) |
| `created_at` | TIMESTAMP | Creation timestamp |

## 🧪 Testing

### Unit and Integration Tests

```bash
cd activity-scheduler

# Run all tests
mvn test

# Run tests with coverage
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Code Quality Checks

```bash
# Format code (Google Java Format)
mvn fmt:format

# Checkstyle (Google Java Style Guide)
mvn checkstyle:check

# PMD (Static code analysis)
mvn pmd:check

# Run all quality checks
mvn clean compile test checkstyle:check pmd:check
```

### End-to-End Tests

See [E2E_TESTING.md](E2E_TESTING.md) for the complete end-to-end testing checklist.

## 📊 Code Quality & Coverage

- **Checkstyle**: Google Java Style Guide compliance
- **PMD**: Static code analysis
- **JaCoCo**: Code coverage reporting (target: 80%+)
- **JUnit 5**: Comprehensive testing
- **Mockito**: Mock testing framework

**Code Quality Screenshots:**
- Checkstyle: `github_resources/checkstyle.png`
- PMD: `github_resources/pmd.png`
- Testing: `github_resources/testing.png`
- JaCoCo: `github_resources/jacoco.png`

## Third-Party Code

### Dependencies (Managed via Maven)

All third-party libraries are managed through Maven and specified in `pom.xml`. They are automatically downloaded from Maven Central Repository during build. Key dependencies include:

- **Spring Boot** - Framework and dependencies
- **Spring Data JPA** - Database access
- **Hibernate** - ORM framework
- **MySQL Connector** - MySQL database driver
- **H2 Database** - In-memory database for testing
- **Jackson** - JSON serialization/deserialization
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework
- **JaCoCo** - Code coverage
- **Checkstyle** - Code style checking
- **PMD** - Static analysis

**Location**: Dependencies are declared in:
- `activity-scheduler/pom.xml`
- `activity-scheduler-client/pom.xml`

**Source**: All dependencies are downloaded from Maven Central Repository (https://repo.maven.apache.org/maven2/)

### Third-Party Tools

- **Google Java Format** - Code formatting (via `fmt-maven-plugin`)
- **Google Java Style Guide** - Checkstyle rules (`google_checks.xml`)
  - **Source**: https://github.com/google/styleguide/blob/gh-pages/eclipse-java-google-style.xml
  - **Location**: `activity-scheduler/google_checks.xml`

### AI Tools Usage

This project utilized various AI tools to assist in development, testing, and documentation. All AI-generated code is clearly marked throughout the codebase.

**AI Tools Used:**

1. **ChatGPT (Free Tier)**
   - **Source**: OpenAI ChatGPT (free tier with .edu email)
   - **Usage**: Code review, debugging, and architectural guidance
   - **Generated Code**: Error handling improvements, test case scenarios, documentation templates

2. **Cursor AI (Education)**
   - **Source**: Cursor IDE with education access
   - **Usage**: Code refactoring, documentation generation, and README updates
   - **Generated Content**: README.md sections, API endpoint documentation, database schema tables

**AI-Generated Code Marking:**
- Test cases were generated with AI assistance, then thoroughly reviewed and verified
- JAVADOC comments were generated by AI tools after reading API definitions
- README.md sections were generated with AI assistance and reviewed for accuracy

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Additional Documentation

- **Local Development**: [LOCAL_DEVELOPMENT.md](LOCAL_DEVELOPMENT.md)
- **GCP Deployment**: [GCP_DEPLOYMENT.md](GCP_DEPLOYMENT.md)
- **Database Configuration**: [DATABASE_CONFIGURATION.md](DATABASE_CONFIGURATION.md)
- **End-to-End Testing**: [E2E_TESTING.md](E2E_TESTING.md)
- **Multi-Client Verification**: [MULTI_CLIENT_VERIFICATION.md](MULTI_CLIENT_VERIFICATION.md)
- **Client README**: [activity-scheduler-client/README.md](activity-scheduler-client/README.md)
- **Client Architecture**: [activity-scheduler-client/ARCHITECTURE.md](activity-scheduler-client/ARCHITECTURE.md)