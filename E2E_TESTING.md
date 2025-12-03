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

## Summary

**Total Tests:** 16
**Passed:** ___
**Failed:** ___
**Pass Rate:** ___%

**Date Tested:** ___________
**Tester:** ___________
