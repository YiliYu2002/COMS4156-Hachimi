# Activity Scheduler Client

A command-line client application for interacting with the Activity Scheduler REST API service.

## Features

- User registration and login
- Organization management (create, list)
- Membership management (create, list)
- Event management (create, list, view my events)
- Event attendee and RSVP management (invite, update RSVP, view attendees)

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Activity Scheduler service running and accessible

## Database Configuration Options

The Activity Scheduler service supports two database configurations:

### Option 1: GCP MySQL (Production/Default)

- **Database**: GCP MySQL (persistent, shared across instances)
- **Configuration**: Uses `application.yml` (contains GCP MySQL connection details)
- **Data Persistence**: Data persists across service restarts
- **Use Case**: Production deployment or testing with shared data

### Option 2: Local H2 (Development)

- **Database**: H2 in-memory database (local, non-persistent)
- **Configuration**: Uses `application-local.yml` profile
- **Data Persistence**: Data is lost when service stops
- **Use Case**: Local development and testing

## Starting the Service

### Start Service with GCP MySQL (Default)

```bash
cd activity-scheduler
mvn spring-boot:run
```

This will:
- Use the GCP MySQL database configured in `application.yml`
- Connect to the remote MySQL instance
- Persist all data across restarts

### Start Service with Local H2

```bash
cd activity-scheduler
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

This will:
- Use the H2 in-memory database
- Create tables automatically on startup
- Clear all data when the service stops

**Note**: The H2 console is available at `http://localhost:8080/h2-console` when using the local profile.

## Building the Client

1. **Navigate to the client directory:**
   ```bash
   cd activity-scheduler-client
   ```

2. **Build the client application:**
   ```bash
   mvn clean package
   ```

   This creates an executable JAR file: `target/activity-scheduler-client-1.0.0-jar-with-dependencies.jar`

## Running the Client

### Default (Connects to localhost:8080)

If your service is running locally (either GCP MySQL or Local H2):

```bash
java -jar target/activity-scheduler-client-1.0.0-jar-with-dependencies.jar
```

### Custom Service URL

If your service is deployed to a cloud environment (e.g., GCP):

```bash
java -jar target/activity-scheduler-client-1.0.0-jar-with-dependencies.jar https://your-gcp-service-url.com
```

### Using Environment Variable

```bash
export SERVICE_URL=https://your-service-url.com
java -jar target/activity-scheduler-client-1.0.0-jar-with-dependencies.jar
```

## Complete Workflow Example

### Using GCP MySQL

```bash
# Terminal 1: Start service with GCP MySQL
cd activity-scheduler
mvn spring-boot:run

# Wait for service to start (look for "Started ActivitySchedulerApplication")

# Terminal 2: Build and run client
cd activity-scheduler-client
mvn clean package
java -jar target/activity-scheduler-client-1.0.0-jar-with-dependencies.jar
```

### Using Local H2

```bash
# Terminal 1: Start service with Local H2
cd activity-scheduler
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Wait for service to start (look for "Started ActivitySchedulerApplication")

# Terminal 2: Build and run client
cd activity-scheduler-client
mvn clean package
java -jar target/activity-scheduler-client-1.0.0-jar-with-dependencies.jar
```

## Usage

1. Start the service (using either GCP MySQL or Local H2 configuration)
2. Build and run the client
3. Enter your email and display name to login/register
4. Use the menu to interact with the service

## Important Notes

- **GCP MySQL**: Data is shared across all service instances and persists after restarts
- **Local H2**: Data is stored in memory only and is lost when the service stops
- The client connects to the service at `http://localhost:8080` by default
- Multiple client instances can run simultaneously, each identified by a unique User ID

## Documentation

- See `../README.md` for project overview
- See `../E2E_TESTING.md` for end-to-end testing instructions
- See `ARCHITECTURE.md` for client architecture details
