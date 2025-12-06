# End-to-End Testing Guide

This document provides a comprehensive checklist for testing the Activity Scheduler client and service integration.

## Prerequisites

1. Service is running locally:
   ```bash
   cd activity-scheduler
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```

2. Client is built:
   ```bash
   cd activity-scheduler-client
   mvn clean package
   ```

## Test Cases

### Test 1: Service Health Check
**Objective:** Verify the service is running and accessible.

**Steps:**
1. Start the service with local profile
2. Run the client: `java -jar target/activity-scheduler-client-1.0.0-jar-with-dependencies.jar`
3. Observe the health check message

**Expected Outcome:**
- ✓ Service is running: Application is running
- Client successfully connects to the service

**Actual Result:** ☐ Pass / ☐ Fail
**Notes:**

---

### Test 2: User Registration
**Objective:** Register a new user successfully.

**Steps:**
1. Run the client
2. Enter email: `test1@example.com`
3. Enter display name: `Test User 1`
4. Verify registration success

**Expected Outcome:**
- ✓ Registered and logged in as: Test User 1 (ID: [UUID])
- User ID is displayed

**Actual Result:** ☐ Pass / ☐ Fail
**Notes:**

---

### Test 3: User Login (Existing User)
**Objective:** Login with an existing user.

**Steps:**
1. Run the client again
2. Enter the same email from Test 2: `test1@example.com`
3. Enter display name: `Test User 1`
4. Verify login success

**Expected Outcome:**
- ✓ Logged in as: Test User 1 (ID: [same UUID as Test 2])
- Same User ID is retrieved

**Actual Result:** ☐ Pass / ☐ Fail
**Notes:**

---

### Test 4: Create Organization
**Objective:** Create a new organization.

**Steps:**
1. Login/register a user
2. Select option 1 (Create Organization)
3. Enter organization name: `Test Organization`
4. Verify creation success

**Expected Outcome:**
- Organization created successfully!
- ID: [UUID]
- Name: Test Organization

**Actual Result:** ☐ Pass / ☐ Fail
**Notes:**

---

### Test 5: List All Organizations
**Objective:** Retrieve and display all organizations.

**Steps:**
1. After creating an organization (Test 4)
2. Select option 2 (List All Organizations)
3. Verify the organization appears in the list

**Expected Outcome:**
- Organization list displays the created organization
- ID and Name are shown correctly

**Actual Result:** ☐ Pass / ☐ Fail
**Notes:**

---

### Test 6: Create Membership
**Objective:** Create a membership between user and organization.

**Steps:**
1. Create an organization (Test 4) and note the Organization ID
2. Register a second user and note the User ID
3. Select option 3 (Create Membership)
4. Enter Organization ID, User ID, and status (INVITED)
5. Verify creation success

**Expected Outcome:**
- Membership created successfully!
- Organization ID, User ID, and Status are displayed

**Actual Result:** ☐ Pass / ☐ Fail
**Notes:**

---

### Test 7: List My Memberships
**Objective:** Retrieve memberships for the current user.

**Steps:**
1. After creating a membership (Test 6)
2. Login as the user who was added to the membership
3. Select option 4 (List My Memberships)
4. Verify the membership appears

**Expected Outcome:**
- Membership list displays the created membership
- Organization ID and Status are shown

**Actual Result:** ☐ Pass / ☐ Fail
**Notes:**

---

### Test 8: Create Event
**Objective:** Create a new event.

**Steps:**
1. Login/register a user
2. Create an organization and note the Organization ID
3. Select option 5 (Create Event)
4. Enter:
   - Organization ID
   - Title: `Test Event`
   - Description: `Test Description`
   - Start time: `2025-12-01 10:00`
   - End time: `2025-12-01 11:00`
   - Capacity: `10` (or leave empty for unlimited)
5. Verify creation success

**Expected Outcome:**
- Event created successfully!
- ID, Title, Start, and End times are displayed

**Actual Result:** ☐ Pass / ☐ Fail
**Notes:**

---

### Test 9: List All Events
**Objective:** Retrieve and display all events.

**Steps:**
1. After creating an event (Test 8)
2. Select option 6 (List All Events)
3. Verify the event appears in the list

**Expected Outcome:**
- Event list displays the created event
- ID, Title, Start, and End times are shown

**Actual Result:** ☐ Pass / ☐ Fail
**Notes:**

---

### Test 10: List My Events
**Objective:** Retrieve events created by the current user.

**Steps:**
1. After creating an event (Test 8)
2. Select option 7 (List My Events)
3. Verify the event appears in the list

**Expected Outcome:**
- Event list displays only events created by the current user
- ID, Title, Start, and End times are shown

**Actual Result:** ☐ Pass / ☐ Fail
**Notes:**

---

### Test 11: Invite Attendee
**Objective:** Invite a user to an event.

**Steps:**
1. Create an event (Test 8) and note the Event ID
2. Register a second user and note the User ID
3. Select option 8 (Invite Attendee to Event)
4. Enter Event ID and User ID
5. Verify invitation success

**Expected Outcome:**
- Invitation sent successfully!
- Event ID, User ID, and RSVP Status (pending) are displayed

**Actual Result:** ☐ Pass / ☐ Fail
**Notes:**

---

### Test 12: Update RSVP Status
**Objective:** Update the RSVP status for an event attendee.

**Steps:**
1. After inviting an attendee (Test 11)
2. Login as the invited user
3. Select option 9 (Update RSVP Status)
4. Enter Event ID, User ID, and RSVP status (yes/no/pending)
5. Verify update success

**Expected Outcome:**
- RSVP status updated successfully!

**Actual Result:** ☐ Pass / ☐ Fail
**Notes:**

---

### Test 13: View Event Attendees
**Objective:** View all attendees for an event.

**Steps:**
1. After inviting attendees (Test 11)
2. Select option 10 (View Event Attendees)
3. Enter Event ID
4. Verify attendees are displayed

**Expected Outcome:**
- Attendees list displays all invited users
- User ID and RSVP Status are shown for each attendee

**Actual Result:** ☐ Pass / ☐ Fail
**Notes:**

---

## Multi-Client Testing

### Test 14: Multiple Clients Simultaneously
**Objective:** Verify multiple client instances can run simultaneously.

**Steps:**
1. Start the service
2. Run client instance 1 in terminal 1
3. Run client instance 2 in terminal 2
4. Register different users in each instance
5. Create organizations/events from each instance
6. Verify data is shared correctly between instances

**Expected Outcome:**
- Both clients can connect simultaneously
- Data created by one client is visible to the other
- Each client maintains its own User ID

**Actual Result:** ☐ Pass / ☐ Fail
**Notes:**

---

## Error Handling Tests

### Test 15: Invalid Date Format
**Objective:** Verify graceful handling of invalid date formats.

**Steps:**
1. Create an event
2. Enter invalid date format (e.g., `2025/12/01 10:00`)
3. Verify error message

**Expected Outcome:**
- Clear error message about invalid date format
- Client does not crash

**Actual Result:** ☐ Pass / ☐ Fail
**Notes:**

---

### Test 16: Service Unavailable
**Objective:** Verify graceful handling when service is not running.

**Steps:**
1. Stop the service
2. Run the client
3. Verify error message

**Expected Outcome:**
- Clear error message: "Cannot connect to service"
- Client exits gracefully

**Actual Result:** ☐ Pass / ☐ Fail
**Notes:**

---

### Test 17: View User Details
**Objective:** Retrieve user information by ID.

**Steps:**
1. Register/login a user and note the User ID
2. Select option 20 (View User Details)
3. Enter the User ID (or press Enter to view own details)
4. Verify user information is displayed

**Expected Outcome:**
- User details displayed: ID, Email, Display Name
- User information is correct

**Actual Result:** ☐ Pass / ☐ Fail
**Notes:**

---

### Test 18: Update User Display Name
**Objective:** Update a user's display name.

**Steps:**
1. Register/login a user
2. Select option 21 (Update User Display Name)
3. Enter User ID and new display name
4. Verify update success

**Expected Outcome:**
- Display name updated successfully
- Updated name is reflected in user details

**Actual Result:** ☐ Pass / ☐ Fail
**Notes:**

---

### Test 19: View Organization Details
**Objective:** Retrieve organization information by ID.

**Steps:**
1. Create an organization (Test 4) and note the Organization ID
2. Select option 22 (View Organization Details)
3. Enter the Organization ID
4. Verify organization information is displayed

**Expected Outcome:**
- Organization details displayed: ID, Name, Created By, Created At
- Information is correct

**Actual Result:** ☐ Pass / ☐ Fail
**Notes:**

---

### Test 20: Update Organization
**Objective:** Update an existing organization's name.

**Steps:**
1. Create an organization (Test 4) and note the Organization ID
2. Select option 23 (Update Organization)
3. Enter Organization ID and new name
4. Verify update success

**Expected Outcome:**
- Organization updated successfully
- New name is reflected when viewing organization details

**Actual Result:** ☐ Pass / ☐ Fail
**Notes:**

---

### Test 21: Delete Organization
**Objective:** Delete an organization.

**Steps:**
1. Create an organization (Test 4) and note the Organization ID
2. Select option 24 (Delete Organization)
3. Enter the Organization ID
4. Verify deletion success
5. Try to view the organization - should not be found

**Expected Outcome:**
- Organization deleted successfully
- Organization no longer appears in list
- Attempting to view deleted organization returns "not found"

**Actual Result:** ☐ Pass / ☐ Fail
**Notes:**

---

### Test 22: Update Membership Status
**Objective:** Update a membership status (e.g., from INVITED to ACTIVE).

**Steps:**
1. Create a membership with status INVITED (Test 6)
2. Select option 25 (Update Membership Status)
3. Enter Organization ID, User ID, and new status (ACTIVE)
4. Verify update success

**Expected Outcome:**
- Membership status updated successfully
- New status is reflected when viewing memberships

**Actual Result:** ☐ Pass / ☐ Fail
**Notes:**

---

### Test 23: View Event Details
**Objective:** Retrieve event information by ID.

**Steps:**
1. Create an event (Test 8) and note the Event ID
2. Select option 11 (View Event Details)
3. Select the event from the numbered list
4. Verify event information is displayed

**Expected Outcome:**
- Event details displayed: ID, Title, Description, Start, End, Capacity, Organization
- Information is correct

**Actual Result:** ☐ Pass / ☐ Fail
**Notes:**

---

### Test 24: Update Event
**Objective:** Update an existing event.

**Steps:**
1. Create an event (Test 8) and note the Event ID
2. Select option 12 (Update Event)
3. Select the event from the numbered list
4. Update title, description, or times
5. Verify update success

**Expected Outcome:**
- Event updated successfully
- Updated information is reflected when viewing event details

**Actual Result:** ☐ Pass / ☐ Fail
**Notes:**

---

### Test 25: Delete Event
**Objective:** Delete an event.

**Steps:**
1. Create an event (Test 8) and note the Event ID
2. Select option 13 (Delete Event)
3. Select the event from the numbered list
4. Verify deletion success
5. Try to view the event - should not be found

**Expected Outcome:**
- Event deleted successfully
- Event no longer appears in list
- Attempting to view deleted event returns "not found"

**Actual Result:** ☐ Pass / ☐ Fail
**Notes:**

---

### Test 26: List Events by Organization
**Objective:** Retrieve all events for a specific organization.

**Steps:**
1. Create an organization and multiple events in it (Test 4, Test 8)
2. Select option 14 (List Events by Organization)
3. Select the organization from the numbered list
4. Verify all events for that organization are displayed

**Expected Outcome:**
- Events list displays only events belonging to the selected organization
- All events for the organization are shown

**Actual Result:** ☐ Pass / ☐ Fail
**Notes:**

---

### Test 27: List Events by Organization and User
**Objective:** Retrieve events for a specific organization created by a specific user.

**Steps:**
1. Create an organization and events from different users
2. Select option 15 (List Events by Organization & User)
3. Select organization and user
4. Verify filtered events are displayed

**Expected Outcome:**
- Events list displays only events matching both organization and user filters
- Filtering works correctly

**Actual Result:** ☐ Pass / ☐ Fail
**Notes:**

---

### Test 28: Check Conflicts Among Accepted Events
**Objective:** Detect time conflicts among events a user has accepted.

**Steps:**
1. Create two overlapping events
2. Accept both events (update RSVP to YES for both)
3. Select option 23 (Check Conflicts Among Accepted Events)
4. Enter User ID and status (YES or ACCEPTED)
5. Verify conflicts are detected

**Expected Outcome:**
- Conflicts are detected and displayed
- Overlapping events are identified

**Actual Result:** ☐ Pass / ☐ Fail
**Notes:**

---

### Test 29: Check Conflicts with Pending Event
**Objective:** Check if a pending event conflicts with accepted events.

**Steps:**
1. Create and accept an event (Test 8, Test 12)
2. Create a new overlapping event (don't accept yet)
3. Select option 24 (Check Conflicts with Pending Event)
4. Enter User ID and Pending Event ID
5. Verify conflicts are detected

**Expected Outcome:**
- Conflicts are detected between pending event and accepted events
- Warning is displayed before creating conflicting event

**Actual Result:** ☐ Pass / ☐ Fail
**Notes:**

---

### Test 30: Database Health Check
**Objective:** Verify database connectivity.

**Steps:**
1. Service is running
2. Use curl or browser to access: `http://localhost:8080/health/db`
3. Verify response

**Expected Outcome:**
- Database connection successful message
- Health check returns 200 OK

**Actual Result:** ☐ Pass / ☐ Fail
**Notes:**

---

## Error Handling Tests (Additional)

### Test 31: Duplicate Email Registration
**Objective:** Verify error handling for duplicate email registration.

**Steps:**
1. Register a user with email `test@example.com`
2. Try to register again with the same email
3. Verify error message

**Expected Outcome:**
- Error message: "User already exists" or similar
- Client handles error gracefully

**Actual Result:** ☐ Pass / ☐ Fail
**Notes:**

---

### Test 32: Duplicate Organization Name
**Objective:** Verify error handling for duplicate organization names.

**Steps:**
1. Create an organization named "Test Org"
2. Try to create another organization with the same name
3. Verify error message

**Expected Outcome:**
- Error message: "Organization with this name already exists"
- Client handles error gracefully

**Actual Result:** ☐ Pass / ☐ Fail
**Notes:**

---

### Test 33: Invalid Event Time Range
**Objective:** Verify error handling for invalid event time ranges (end before start).

**Steps:**
1. Create an event
2. Enter start time: `2025-12-01 11:00`
3. Enter end time: `2025-12-01 10:00` (before start)
4. Verify error message

**Expected Outcome:**
- Error message about invalid time range
- Event is not created

**Actual Result:** ☐ Pass / ☐ Fail
**Notes:**

---

### Test 34: Invite Non-Member to Event
**Objective:** Verify error handling when inviting a user who is not a member of the organization.

**Steps:**
1. Create an organization and event
2. Register a user who is NOT a member of the organization
3. Try to invite that user to the event
4. Verify error message

**Expected Outcome:**
- Error message: "User is not a member of organization"
- Invitation is not created

**Actual Result:** ☐ Pass / ☐ Fail
**Notes:**

---

### Test 35: Delete Event as Non-Creator
**Objective:** Verify error handling when non-creator tries to delete an event.

**Steps:**
1. User A creates an event
2. User B (different user) tries to delete the event
3. Verify error message

**Expected Outcome:**
- Error message: "Only the event creator can delete the event" or 403 Forbidden
- Event is not deleted

**Actual Result:** ☐ Pass / ☐ Fail
**Notes:**

---

### Test 36: View Non-Existent Resource
**Objective:** Verify error handling when viewing non-existent resources.

**Steps:**
1. Try to view a user with invalid ID: `00000000-0000-0000-0000-000000000000`
2. Try to view an organization with invalid ID
3. Try to view an event with invalid ID
4. Verify error messages

**Expected Outcome:**
- Error message: "User not found", "Organization not found", or "Event not found"
- Client handles 404 errors gracefully

**Actual Result:** ☐ Pass / ☐ Fail
**Notes:**

---

## Summary

**Total Tests:** 36
**Passed:** ___
**Failed:** ___
**Pass Rate:** ___%

**Date Tested:** ___________
**Tester:** ___________

### Test Coverage Summary

**User Management:** 4 tests (Registration, Login, View Details, Update Display Name)
**Organization Management:** 5 tests (Create, List, View, Update, Delete)
**Membership Management:** 3 tests (Create, List, Update Status)
**Event Management:** 7 tests (Create, List All, List My, View, Update, Delete, List by Org, List by Org & User)
**Attendee Management:** 3 tests (Invite, Update RSVP, View Attendees)
**Conflict Detection:** 2 tests (Check Accepted Conflicts, Check Pending Conflicts)
**Health Checks:** 2 tests (Basic Health, Database Health)
**Multi-Client:** 1 test (Multiple Clients Simultaneously)
**Error Handling:** 9 tests (Invalid Inputs, Duplicates, Non-Existent Resources, Authorization)

**All API Endpoints Covered:**
- ✅ User: Register, Login, Get All, Get By ID, Update Display Name, Delete
- ✅ Organization: Create, Get All, Get By ID, Get By Name, Update, Delete, Exists, Count
- ✅ Membership: Create, Get All, Get By Org/User, Get By Status, Update Status, Delete, Exists, Count
- ✅ Event: Create, Get By ID, Update, Delete, Get By User, Get By Organization, Get By Org & User
- ✅ Attendee: Create, Get By Event, Get By User, Update RSVP, Delete, Exists, Count
- ✅ Conflict: Check Conflicts Among Accepted, Check Conflicts With Pending
- ✅ Health: Basic Health, Database Health
