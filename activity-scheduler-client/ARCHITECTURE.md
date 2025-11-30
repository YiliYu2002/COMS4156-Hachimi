# Activity Scheduler Client Architecture

This document describes the architecture and design of the Activity Scheduler client application.

## Overview

The Activity Scheduler Client is a command-line interface (CLI) application that provides a user-friendly way to interact with the Activity Scheduler REST API service. It handles user authentication, API communication, and provides an interactive menu system for managing organizations, events, memberships, and attendees.

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────┐
│              ActivitySchedulerClient (Main)              │
│  - CLI Interface                                         │
│  - Menu System                                           │
│  - User Input/Output                                     │
│  - currentUserId Management                              │
└──────────────────┬──────────────────────────────────────┘
                   │
                   │ Uses
                   ↓
┌─────────────────────────────────────────────────────────┐
│                    ApiClient                             │
│  - HTTP Communication                                    │
│  - JSON Serialization/Deserialization                  │
│  - Error Handling                                       │
│  - Service URL Management                               │
└──────────────────┬──────────────────────────────────────┘
                   │
                   │ Uses
                   ↓
┌─────────────────────────────────────────────────────────┐
│                    Model Classes                         │
│  - User, Organization, Event, Membership, Attendee      │
│  - Request DTOs (UserRegistrationRequest, etc.)        │
│  - Response DTOs                                        │
└─────────────────────────────────────────────────────────┘
```

## File Structure

```
activity-scheduler-client/
├── pom.xml
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── example/
│                   └── client/
│                       ├── ActivitySchedulerClient.java  (Main class)
│                       ├── api/
│                       │   └── ApiClient.java           (HTTP client)
│                       └── model/
│                           ├── User.java
│                           ├── Organization.java
│                           ├── Event.java
│                           ├── Membership.java
│                           ├── Attendee.java
│                           ├── UserRegistrationRequest.java
│                           ├── OrganizationCreationRequest.java
│                           ├── MembershipRequest.java
│                           ├── AttendeeRequest.java
│                           └── RsvpUpdateRequest.java
└── README.md
```

## Component Details

### 1. ActivitySchedulerClient (Main Class)

**Location:** `src/main/java/com/example/client/ActivitySchedulerClient.java`

**Responsibilities:**
- Application entry point (`main` method)
- CLI interface and menu system
- User input/output handling
- User session management (`currentUserId`)
- Date/time parsing with flexible format support
- Orchestrates calls to `ApiClient`

**Key Features:**
- Interactive menu system
- User login/registration flow
- Error handling with user-friendly messages
- Flexible date format parsing (supports both `yyyy-MM-dd` and `yyyy-M-d`)

**Key Methods:**
- `main(String[] args)` - Entry point, handles service URL configuration
- `run()` - Main application loop
- `loginOrRegister()` - User authentication
- `createOrganization()`, `createEvent()`, etc. - Feature methods
- `parseDateTime(String)` - Flexible date parsing

### 2. ApiClient

**Location:** `src/main/java/com/example/client/api/ApiClient.java`

**Responsibilities:**
- HTTP communication with the REST API
- JSON serialization/deserialization
- Request/response handling
- Error handling and exception propagation

**Key Features:**
- Uses Java 11+ `HttpClient` for HTTP requests
- Jackson `ObjectMapper` for JSON processing
- JavaTimeModule for `LocalDateTime` support
- Connection timeout configuration
- Health check endpoint support

**Key Methods:**
- `checkBasicHealth()` - Service health check
- `registerUser()` - User registration
- `getUserByEmail()` - User lookup
- `createOrganization()` - Organization creation
- `createEvent()` - Event creation
- `createMembership()` - Membership creation
- `createAttendee()` - Attendee invitation
- `updateRsvpStatus()` - RSVP status update
- Various list/get methods

### 3. Model Classes

**Location:** `src/main/java/com/example/client/model/`

**Purpose:** Data Transfer Objects (DTOs) that mirror the service's API models.

**Classes:**
- **User** - User entity with id, email, displayName, etc.
- **Organization** - Organization entity
- **Event** - Event entity with start/end times, capacity, etc.
- **Membership** - User-organization relationship
- **Attendee** - Event attendee with RSVP status
- **UserRegistrationRequest** - Request DTO for user registration
- **OrganizationCreationRequest** - Request DTO for organization creation
- **MembershipRequest** - Request DTO for membership creation
- **AttendeeRequest** - Request DTO for attendee invitation
- **RsvpUpdateRequest** - Request DTO for RSVP updates

**Design Pattern:** All model classes use:
- Jackson annotations (`@JsonProperty`) for JSON mapping
- Standard getters/setters
- Default constructors for Jackson deserialization

## Data Flow

### Example: Creating an Organization

```
1. User selects option "1" (Create Organization)
   ↓
2. ActivitySchedulerClient.createOrganization()
   - Prompts for organization name
   - Gets currentUserId from session
   ↓
3. ApiClient.createOrganization(name, currentUserId)
   - Creates OrganizationCreationRequest object
   - Serializes to JSON using ObjectMapper
   - Sends HTTP POST to /api/organizations/create
   ↓
4. Service processes request and returns Organization JSON
   ↓
5. ApiClient deserializes JSON to Organization object
   ↓
6. ActivitySchedulerClient displays success message with organization details
```

### Example: User Login/Registration

```
1. ActivitySchedulerClient.loginOrRegister()
   - Prompts for email and display name
   ↓
2. ApiClient.getUserByEmail(email)
   - Checks if user exists
   ↓
3a. If user exists:
    - Returns User object with existing User ID
    - ActivitySchedulerClient stores currentUserId
3b. If user doesn't exist:
    - ApiClient.registerUser(email, displayName)
    - Service creates new user and returns User object
    - ActivitySchedulerClient stores currentUserId
```

## Configuration

### Service URL Configuration

The client supports three ways to configure the service URL (in priority order):

1. **Command-line argument:**
   ```bash
   java -jar app.jar https://service-url.com
   ```

2. **Environment variable:**
   ```bash
   export SERVICE_URL=https://service-url.com
   java -jar app.jar
   ```

3. **Default value:**
   ```bash
   java -jar app.jar
   # Uses: http://localhost:8080
   ```

### Dependencies

**Maven Dependencies:**
- `jackson-databind` - JSON processing
- `jackson-datatype-jsr310` - Java 8 Date/Time API support

**Java Version:** 17+

## Error Handling

### Strategy

1. **ApiClient Level:**
   - HTTP errors are converted to `IOException` with status code and message
   - JSON parsing errors are wrapped in `IOException`

2. **ActivitySchedulerClient Level:**
   - All API calls are wrapped in try-catch blocks
   - User-friendly error messages are displayed
   - Application continues running after errors (doesn't crash)

### Example Error Flow

```
Service returns HTTP 400 (Bad Request)
  ↓
ApiClient throws IOException("HTTP 400: Invalid data")
  ↓
ActivitySchedulerClient catches exception
  ↓
Displays: "Error creating organization: HTTP 400: Invalid data"
  ↓
Returns to main menu (application continues)
```

## Multi-Client Support

### How It Works

1. **User ID as Session Identifier:**
   - Each client instance maintains a `currentUserId`
   - All API calls include this User ID (in request body or headers)

2. **Service-Side Attribution:**
   - Service uses User ID to attribute actions to specific users
   - Data is shared but operations are tracked by User ID

3. **No Client-Side Coordination:**
   - Clients don't communicate with each other
   - All coordination happens through the service

### Example: Two Clients

```
Client 1 (Alice, User ID: abc-123)
  - Creates Organization "Org1"
  - Creates Event "Event1"

Client 2 (Bob, User ID: def-456)
  - Can see "Org1" (shared data)
  - Can see "Event1" (shared data)
  - "My Events" shows only events created by def-456
  - Can create membership in "Org1"
```

## Design Principles

1. **Separation of Concerns:**
   - `ActivitySchedulerClient` handles UI/UX
   - `ApiClient` handles HTTP/API communication
   - Model classes handle data representation

2. **Error Resilience:**
   - Graceful error handling at all levels
   - User-friendly error messages
   - Application doesn't crash on errors

3. **Flexibility:**
   - Configurable service URL
   - Flexible date format parsing
   - Extensible menu system

4. **Simplicity:**
   - No complex frameworks
   - Standard Java libraries only
   - Clear, readable code

## Future Enhancements

Potential improvements:

1. **Configuration File:**
   - Store service URL in config file
   - Remember last used user

2. **Better Error Messages:**
   - More specific error messages
   - Suggestions for common errors

3. **Input Validation:**
   - Client-side validation before API calls
   - Better date/time input handling

4. **Batch Operations:**
   - Create multiple events at once
   - Bulk invite attendees

5. **Export/Import:**
   - Export events to calendar format
   - Import from CSV

## Testing

See `E2E_TESTING.md` for end-to-end testing instructions.

## Related Documentation

- `README.md` - Project overview and usage
- `LOCAL_DEVELOPMENT.md` - Local development setup
- `GCP_DEPLOYMENT.md` - Cloud deployment guide
- `MULTI_CLIENT_VERIFICATION.md` - Multi-client testing guide
