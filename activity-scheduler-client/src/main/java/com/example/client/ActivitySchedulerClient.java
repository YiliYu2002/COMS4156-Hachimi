package com.example.client;

import com.example.client.api.ApiClient;
import com.example.client.model.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class ActivitySchedulerClient {
  private static final String DEFAULT_SERVICE_URL = "http://localhost:8080";
  private static final DateTimeFormatter DATE_FORMATTER = 
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
  private static final DateTimeFormatter DATE_FORMATTER_FLEXIBLE = 
      DateTimeFormatter.ofPattern("yyyy-M-d HH:mm");  // Supports single digit month/day
  private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = 
      DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");

  private ApiClient apiClient;
  private Scanner scanner;
  private String currentUserId;
  private String currentUserDisplayName;
  private String serviceUrl;

  public ActivitySchedulerClient(String serviceUrl) {
    this.serviceUrl = serviceUrl;
    this.apiClient = new ApiClient(serviceUrl);
    this.scanner = new Scanner(System.in);
  }

  public static void main(String[] args) {
    // Supports three methods (in priority order):
    // 1. Command line arguments
    // 2. Environment variable SERVICE_URL
    // 3. Default value http://localhost:8080
    String serviceUrl = args.length > 0 
        ? args[0] 
        : System.getenv().getOrDefault("SERVICE_URL", DEFAULT_SERVICE_URL);

    ActivitySchedulerClient client = new ActivitySchedulerClient(serviceUrl);
    client.run();
  }

  public void run() {
    System.out.println("=== Activity Scheduler Client ===");
    System.out.println("Connecting to service at: " + serviceUrl);

    // Health check
    try {
      String health = apiClient.checkBasicHealth();
      System.out.println("✓ Service is running: " + health);
    } catch (Exception e) {
      String errorMsg = e.getMessage();
      if (errorMsg == null || errorMsg.isEmpty()) {
        errorMsg = e.getClass().getSimpleName() + ": " + e.toString();
      }
      System.err.println("✗ Health check failed: " + errorMsg);
      System.err.println("ERROR: Cannot connect to service at " + serviceUrl);
      System.err.println("Please ensure the service is running.");
      System.err.println("To start the service:");
      System.err.println("  - GCP MySQL: cd activity-scheduler && mvn spring-boot:run");
      System.err.println("  - Local H2:  cd activity-scheduler && mvn spring-boot:run -Dspring-boot.run.profiles=local");
      return;
    }

    // User login/registration
    if (!loginOrRegister()) {
      return;
    }

    // Main menu loop
    while (true) {
      printMenu();
      String choice = scanner.nextLine().trim();

      try {
        switch (choice) {
          // Organizations
          case "1":
            createOrganization();
            break;
          case "2":
            listOrganizations();
            break;
          case "3":
            viewOrganizationDetails();
            break;
          case "4":
            updateOrganization();
            break;
          case "5":
            deleteOrganization();
            break;
          
          // Memberships
          case "6":
            createMembership();
            break;
          case "7":
            listMyMemberships();
            break;
          
          // Events
          case "8":
            createEvent();
            break;
          case "9":
            listAllEvents();
            break;
          case "10":
            listMyEvents();
            break;
          case "11":
            viewEventDetails();
            break;
          case "12":
            updateEvent();
            break;
          case "13":
            deleteEvent();
            break;
          case "14":
            listEventsByOrganization();
            break;
          case "15":
            listEventsByOrganizationAndUser();
            break;
          
          // Attendees
          case "16":
            inviteAttendee();
            break;
          case "17":
            updateRsvp();
            break;
          case "18":
            viewEventAttendees();
            break;
          
          // Users
          case "19":
            viewUserDetails();
            break;
          case "20":
            updateUserDisplayName();
            break;
          case "21":
            deleteUser();
            break;
          
          // Conflict Detection
          case "22":
            checkConflictsAmongAcceptedEvents();
            break;
          case "23":
            checkConflictsWithPendingEvent();
            break;
          
          // Quick Actions & Info
          case "24":
            showStatistics();
            break;
          case "25":
            exportEventsToText();
            break;
          case "26":
            logout();
            break;
          
          case "0":
            System.out.println("Goodbye!");
            return;
          default:
            System.out.println("Invalid choice. Please try again.");
        }
      } catch (Exception e) {
        System.err.println("Error: " + e.getMessage());
      }
    }
  }

  private boolean loginOrRegister() {
    System.out.println("\n=== Login / Register ===");
    System.out.print("Email: ");
    String email = scanner.nextLine().trim();

    try {
      // Check if user exists first
      User existingUser = apiClient.getUserByEmail(email);
      if (existingUser != null) {
        currentUserId = existingUser.getId();
        currentUserDisplayName = existingUser.getDisplayName();
        System.out.println("✓ Logged in as: " + existingUser.getDisplayName() + " (ID: " + currentUserId + ")");
        return true;
      }

      // User doesn't exist, prompt for display name to register
      System.out.print("Display Name (for new account): ");
      String displayName = scanner.nextLine().trim();
      
      if (displayName.isEmpty()) {
        System.err.println("Error: Display name is required for registration.");
        return false;
      }

      // Register new user
      User newUser = apiClient.registerUser(email, displayName);
      currentUserId = newUser.getId();
      currentUserDisplayName = newUser.getDisplayName();
      System.out.println("✓ Registered and logged in as: " + newUser.getDisplayName() + " (ID: " + currentUserId + ")");
      return true;
    } catch (Exception e) {
      System.err.println("Error during login/registration: " + e.getMessage());
      return false;
    }
  }

  private void printMenu() {
    System.out.println("\n" + "=".repeat(50));
    System.out.println("Activity Scheduler - Logged in as: " + currentUserDisplayName + " (ID: " + currentUserId + ")");
    System.out.println("=".repeat(50));
    
    System.out.println("\n--- ORGANIZATIONS ---");
    System.out.println("1. Create Organization");
    System.out.println("2. List All Organizations");
    System.out.println("3. View Organization Details");
    System.out.println("4. Update Organization");
    System.out.println("5. Delete Organization");
    
    System.out.println("\n--- MEMBERSHIPS ---");
    System.out.println("6. Create Membership");
    System.out.println("7. List My Memberships");
    
    System.out.println("\n--- EVENTS ---");
    System.out.println("8. Create Event");
    System.out.println("9. List All Events in My Organizations");
    System.out.println("10. List Events I Created");
    System.out.println("11. View Event Details");
    System.out.println("12. Update Event");
    System.out.println("13. Delete Event");
    System.out.println("14. List Events by Organization");
    System.out.println("15. List Events by Organization & User");
    
    System.out.println("\n--- ATTENDEES ---");
    System.out.println("16. Invite Attendee to Event");
    System.out.println("17. Update RSVP Status");
    System.out.println("18. View Event Attendees");
    
    System.out.println("\n--- USERS ---");
    System.out.println("19. View User Details");
    System.out.println("20. Update My Display Name");
    System.out.println("21. Delete User");
    
    System.out.println("\n--- CONFLICT DETECTION ---");
    System.out.println("22. Check Conflicts Among My Accepted Events");
    System.out.println("23. Check if Event Conflicts with My Accepted Events");
    
    System.out.println("\n--- QUICK ACTIONS & INFO ---");
    System.out.println("24. Show Statistics");
    System.out.println("25. Export Events to Text");
    System.out.println("26. Logout / Switch User");
    
    System.out.println("\n0. Exit");
    System.out.println("\nTip: Type 'back', 'cancel', or 'q' during any operation to return to this menu");
    System.out.print("\nChoose an option: ");
  }

  // Organization Methods
  private void createOrganization() {
    System.out.println("\n=== Create Organization ===");
    System.out.println("(Type 'back', 'cancel', or 'q' to return to main menu)");
    String name = promptWithCancel("Organization name: ");
    if (name == null) return;
    
    try {
      Organization org = apiClient.createOrganization(name, currentUserId);
      System.out.println("\n✓ Organization created successfully!");
      displayOrganizationDetails(org);
    } catch (Exception e) {
      System.err.println("Error creating organization: " + e.getMessage());
    }
  }

  private void listOrganizations() {
    System.out.println("\n=== All Organizations ===");
    try {
      List<Organization> orgs = apiClient.getAllOrganizations();
      if (orgs.isEmpty()) {
        System.out.println("No organizations found.");
      } else {
        System.out.println(String.format("%-40s %-40s", "ID", "Name"));
        System.out.println("-".repeat(80));
        for (Organization org : orgs) {
          System.out.println(String.format("%-40s %-40s", 
              truncate(org.getId(), 40), org.getName()));
        }
      }
    } catch (Exception e) {
      System.err.println("Error listing organizations: " + e.getMessage());
    }
  }

  private void viewOrganizationDetails() {
    System.out.println("\n=== View Organization Details ===");
    System.out.println("(Type 'back', 'cancel', or 'q' to return to main menu)");
    String orgId = promptWithCancel("Organization ID: ");
    if (orgId == null) return;
    
    try {
      Organization org = apiClient.getOrganizationById(orgId);
      if (org == null) {
        System.out.println("Organization not found.");
        return;
      }
      displayOrganizationDetails(org);
      
      // Show member count
      try {
        long memberCount = apiClient.getActiveMemberCountByOrganization(orgId);
        System.out.println("Active Members: " + memberCount);
      } catch (Exception e) {
        // Ignore if endpoint not available
      }
    } catch (Exception e) {
      System.err.println("Error viewing organization: " + e.getMessage());
    }
  }

  private void updateOrganization() {
    System.out.println("\n=== Update Organization ===");
    System.out.println("(Type 'back', 'cancel', or 'q' to return to main menu)");
    String orgId = promptWithCancel("Organization ID: ");
    if (orgId == null) return;
    String newName = promptWithCancel("New name: ");
    if (newName == null) return;
    
    try {
      Organization org = apiClient.updateOrganization(orgId, newName);
      System.out.println("\n✓ Organization updated successfully!");
      displayOrganizationDetails(org);
    } catch (Exception e) {
      System.err.println("Error updating organization: " + e.getMessage());
    }
  }

  private void deleteOrganization() {
    System.out.println("\n=== Delete Organization ===");
    System.out.println("(Type 'back', 'cancel', or 'q' to return to main menu)");
    String orgId = promptWithCancel("Organization ID: ");
    if (orgId == null) return;
    String confirm = promptWithCancel("Are you sure you want to delete this organization? (yes/no): ");
    if (confirm == null) return;
    confirm = confirm.toLowerCase();
    
    if (!confirm.equals("yes")) {
      System.out.println("Deletion cancelled.");
      return;
    }
    
    try {
      apiClient.deleteOrganization(orgId);
      System.out.println("✓ Organization deleted successfully!");
    } catch (Exception e) {
      System.err.println("Error deleting organization: " + e.getMessage());
    }
  }

  private void displayOrganizationDetails(Organization org) {
    System.out.println("\n" + "-".repeat(50));
    System.out.println("Organization Details:");
    System.out.println("-".repeat(50));
    System.out.println("ID:          " + org.getId());
    System.out.println("Name:        " + org.getName());
    if (org.getCreatedAt() != null) {
      System.out.println("Created At:  " + formatDateTime(org.getCreatedAt()));
    }
    if (org.getCreatedBy() != null) {
      System.out.println("Created By:  " + org.getCreatedBy());
    }
    System.out.println("-".repeat(50));
  }

  // Membership Methods
  private void createMembership() {
    System.out.println("\n=== Create Membership ===");
    System.out.println("(Type 'back', 'cancel', or 'q' to return to main menu)");
    String orgId = promptWithCancel("Organization ID: ");
    if (orgId == null) return;
    String userId = promptWithCancel("User ID: ");
    if (userId == null) return;
    String status = promptWithCancel("Status (ACTIVE/INVITED/SUSPENDED, default: INVITED): ");
    if (status == null) return;
    if (status.isEmpty()) {
      status = "INVITED";
    }
    
    try {
      Membership membership = apiClient.createMembership(orgId, userId, status);
      System.out.println("✓ Membership created successfully!");
      System.out.println("Organization ID: " + membership.getOrgId());
      System.out.println("User ID: " + membership.getUserId());
      System.out.println("Status: " + membership.getStatus());
    } catch (Exception e) {
      System.err.println("Error creating membership: " + e.getMessage());
    }
  }

  private void listMyMemberships() {
    System.out.println("\n=== My Memberships ===");
    try {
      List<Membership> memberships = apiClient.getMembershipsByUser(currentUserId);
      if (memberships.isEmpty()) {
        System.out.println("No memberships found.");
      } else {
        System.out.println(String.format("%-40s %-30s %-20s", "Organization ID", "Organization Name", "Status"));
        System.out.println("-".repeat(90));
        for (Membership m : memberships) {
          // Get organization details to show name
          String orgName = "Unknown";
          try {
            Organization org = apiClient.getOrganizationById(m.getOrgId());
            if (org != null && org.getName() != null) {
              orgName = org.getName();
            }
          } catch (Exception e) {
            // If we can't get organization details, just use "Unknown"
          }
          
          System.out.println(String.format("%-40s %-30s %-20s", 
              truncate(m.getOrgId(), 40),
              truncate(orgName, 30),
              m.getStatus()));
        }
      }
    } catch (Exception e) {
      System.err.println("Error listing memberships: " + e.getMessage());
    }
  }

  // Event Methods
  private void createEvent() {
    System.out.println("\n=== Create Event ===");
    System.out.println("(Type 'back', 'cancel', or 'q' at any prompt to return to main menu)");
    
    // First, show available organizations
    System.out.println("\nAvailable organizations:");
    try {
      List<Organization> orgs = apiClient.getAllOrganizations();
      if (orgs.isEmpty()) {
        System.out.println("  (No organizations found. Please create an organization first!)");
      } else {
        for (Organization org : orgs) {
          System.out.println("  ID: " + org.getId() + ", Name: " + org.getName());
        }
      }
    } catch (Exception e) {
      System.err.println("Warning: Could not list organizations: " + e.getMessage());
    }
    
    String orgId = promptWithCancel("\nOrganization ID: ");
    if (orgId == null) return;
    
    if (orgId.isEmpty()) {
      System.err.println("Error: Organization ID cannot be empty.");
      return;
    }
    
    String title = promptWithCancel("Event title: ");
    if (title == null) return;
    
    String description = promptWithCancel("Description (optional): ");
    if (description == null) return;
    
    String startStr = promptWithCancel("Start time (yyyy-MM-dd HH:mm, e.g., 2025-12-01 10:00): ");
    if (startStr == null) return;
    LocalDateTime startAt = parseDateTime(startStr);
    
    String endStr = promptWithCancel("End time (yyyy-MM-dd HH:mm, e.g., 2025-12-01 11:00): ");
    if (endStr == null) return;
    LocalDateTime endAt = parseDateTime(endStr);
    
    if (endAt.isBefore(startAt) || endAt.isEqual(startAt)) {
      System.err.println("Error: End time must be after start time.");
      return;
    }
    
    String capacityStr = promptWithCancel("Capacity (optional, press Enter for unlimited): ");
    if (capacityStr == null) return;
    Integer capacity = capacityStr.isEmpty() ? null : Integer.parseInt(capacityStr);
    
    try {
      Event event = apiClient.createEvent(orgId, currentUserId, title, description, 
          startAt, endAt, capacity);
      System.out.println("\n✓ Event created successfully!");
      displayEventDetails(event);
      
      // Check for conflicts with user's accepted events
      System.out.println("\nChecking for scheduling conflicts...");
      try {
        List<Event> conflicts = apiClient.findConflictsWithPendingEvent(currentUserId, event.getId());
        if (!conflicts.isEmpty()) {
          System.out.println("\n⚠ Warning: This event conflicts with " + conflicts.size() + " of your accepted event(s):");
          displayEventsTable(conflicts);
          System.out.println("\nConsider adjusting the time or declining conflicting invitations.");
        } else {
          System.out.println("✓ No conflicts detected with your accepted events.");
        }
      } catch (Exception e) {
        // Silently ignore conflict check errors (endpoint might not be available)
        String errorMsg = e.getMessage();
        if (errorMsg != null && errorMsg.contains("CONFLICT_DETECTION_NOT_AVAILABLE")) {
          // Don't show error message for automatic conflict checks
        } else {
          System.out.println("(Conflict check unavailable)");
        }
      }
    } catch (Exception e) {
      String errorMsg = e.getMessage();
      if (errorMsg != null && errorMsg.contains("does not exist")) {
        System.err.println("Error: The organization with the provided ID does not exist.");
        System.err.println("Please make sure:");
        System.err.println("  1. You have created an organization first (option 1)");
        System.err.println("  2. You are using the correct Organization ID");
        System.err.println("  3. The service has not been restarted (which clears the database)");
      } else if (errorMsg != null && (errorMsg.contains("conflict") || errorMsg.contains("Conflict"))) {
        System.err.println("⚠ Warning: " + errorMsg);
        System.err.println("The event was still created, but there may be scheduling conflicts.");
      } else {
        System.err.println("Error creating event: " + errorMsg);
      }
    }
  }

  private void listAllEvents() {
    System.out.println("\n=== All Events in My Organizations ===");
    System.out.println("(Shows all events from organizations you're a member of, created by anyone)");
    try {
      // Get events from all organizations the user is a member of
      List<Membership> memberships = apiClient.getMembershipsByUser(currentUserId);
      if (memberships.isEmpty()) {
        System.out.println("No memberships found. You need to be a member of an organization to see events.");
        return;
      }
      
      List<Event> allEvents = new java.util.ArrayList<>();
      for (Membership membership : memberships) {
        try {
          List<Event> orgEvents = apiClient.getEventsByOrganization(membership.getOrgId());
          allEvents.addAll(orgEvents);
        } catch (Exception e) {
          // Ignore errors for individual organizations
        }
      }
      
      if (allEvents.isEmpty()) {
        System.out.println("No events found in your organizations.");
      } else {
        displayEventsTable(allEvents);
      }
    } catch (Exception e) {
      System.err.println("Error listing events: " + e.getMessage());
    }
  }

  private void listMyEvents() {
    System.out.println("\n=== Events I Created ===");
    System.out.println("(Shows only events that you created)");
    try {
      List<Event> events = apiClient.getEventsByUser(currentUserId);
      if (events.isEmpty()) {
        System.out.println("No events found.");
      } else {
        displayEventsTable(events);
      }
    } catch (Exception e) {
      System.err.println("Error listing my events: " + e.getMessage());
    }
  }

  private void viewEventDetails() {
    System.out.println("\n=== View Event Details ===");
    System.out.println("(Type 'back', 'cancel', or 'q' to return to main menu)");
    
    // Get all events the user has been invited to
    System.out.println("\nYour event invitations:");
    try {
      List<Attendee> attendees = apiClient.getAttendeesByUser(currentUserId);
      
      if (attendees.isEmpty()) {
        System.out.println("No event invitations found.");
        return;
      }
      
      // Get event details for each attendee
      List<Event> events = new java.util.ArrayList<>();
      List<Attendee> validAttendees = new java.util.ArrayList<>();
      
      for (Attendee attendee : attendees) {
        try {
          Event event = apiClient.getEventById(attendee.getEventId());
          if (event != null) {
            events.add(event);
            validAttendees.add(attendee);
          }
        } catch (Exception e) {
          // Skip events that can't be retrieved
        }
      }
      
      if (events.isEmpty()) {
        System.out.println("No valid events found.");
        return;
      }
      
      // Display numbered list
      System.out.println(String.format("%-5s %-40s %-30s %-20s %-20s", 
          "#", "Event ID", "Title", "RSVP Status", "Start Time"));
      System.out.println("-".repeat(115));
      
      for (int i = 0; i < events.size(); i++) {
        Event event = events.get(i);
        Attendee attendee = validAttendees.get(i);
        String status = attendee.getRsvpStatus();
        String statusDisplay = status;
        if (status.equalsIgnoreCase("YES")) {
          statusDisplay = "✓ YES";
        } else if (status.equalsIgnoreCase("NO")) {
          statusDisplay = "✗ NO";
        } else if (status.equalsIgnoreCase("PENDING")) {
          statusDisplay = "? PENDING";
        }
        
        System.out.println(String.format("%-5s %-40s %-30s %-20s %-20s",
            (i + 1),
            truncate(event.getId(), 40),
            truncate(event.getTitle(), 30),
            statusDisplay,
            formatDateTime(event.getStartAt())));
      }
      
      System.out.print("\nSelect event number to view details: ");
      String choiceStr = scanner.nextLine().trim();
      
      if (isCancelCommand(choiceStr)) {
        System.out.println("Operation cancelled. Returning to main menu.");
        return;
      }
      
      int choice;
      try {
        choice = Integer.parseInt(choiceStr);
        if (choice < 1 || choice > events.size()) {
          System.err.println("Invalid selection. Please choose a number between 1 and " + events.size());
          return;
        }
      } catch (NumberFormatException e) {
        System.err.println("Invalid input. Please enter a number.");
        return;
      }
      
      Event selectedEvent = events.get(choice - 1);
      String eventId = selectedEvent.getId();
      
      // Display event details
      displayEventDetails(selectedEvent);
      
      // Show attendee statistics
      try {
        long totalCount = apiClient.getAttendeeCountByEvent(eventId);
        long yesCount = apiClient.getAttendeeCountByEventAndStatus(eventId, "YES");
        long noCount = apiClient.getAttendeeCountByEventAndStatus(eventId, "NO");
        long pendingCount = apiClient.getAttendeeCountByEventAndStatus(eventId, "PENDING");
        
        System.out.println("\nAttendee Statistics:");
        System.out.println("  Total: " + totalCount);
        System.out.println("  Accepted (YES): " + yesCount);
        System.out.println("  Declined (NO): " + noCount);
        System.out.println("  Pending: " + pendingCount);
        if (selectedEvent.getCapacity() != null) {
          System.out.println("  Capacity: " + yesCount + "/" + selectedEvent.getCapacity());
        }
      } catch (Exception e) {
        // Ignore if endpoints not available
      }
      
      // Check for conflicts
      try {
        List<Event> conflicts = apiClient.findConflictsWithPendingEvent(currentUserId, eventId);
        if (!conflicts.isEmpty()) {
          System.out.println("\n⚠ Warning: This event conflicts with " + conflicts.size() + " of your accepted event(s):");
          displayEventsTable(conflicts);
          System.out.println("\nConsider adjusting the time or declining conflicting invitations.");
        }
      } catch (Exception e) {
        // Silently ignore conflict check errors
      }
      
    } catch (Exception e) {
      System.err.println("Error viewing event: " + e.getMessage());
    }
  }

  private void updateEvent() {
    System.out.println("\n=== Update Event ===");
    System.out.println("(Type 'back', 'cancel', or 'q' at any prompt to return to main menu)");
    String eventId = promptWithCancel("Event ID: ");
    if (eventId == null) return;
    
    try {
      Event existingEvent = apiClient.getEventById(eventId);
      if (existingEvent == null) {
        System.out.println("Event not found.");
        return;
      }
      
      System.out.println("Current event details:");
      displayEventDetails(existingEvent);
      System.out.println("\nEnter new values (press Enter to keep current value, or 'back' to cancel):");
      
      String orgId = promptWithCancel("Organization ID [" + existingEvent.getOrgId() + "]: ");
      if (orgId == null) return;
      if (orgId.isEmpty()) {
        orgId = existingEvent.getOrgId();
      }
      
      String title = promptWithCancel("Title [" + existingEvent.getTitle() + "]: ");
      if (title == null) return;
      if (title.isEmpty()) {
        title = existingEvent.getTitle();
      }
      
      String description = promptWithCancel("Description [" + (existingEvent.getDescription() != null ? existingEvent.getDescription() : "") + "]: ");
      if (description == null) return;
      if (description.isEmpty()) {
        description = existingEvent.getDescription();
      }
      
      String startStr = promptWithCancel("Start time [" + existingEvent.getStartAt() + "]: ");
      if (startStr == null) return;
      LocalDateTime startAt = startStr.isEmpty() ? existingEvent.getStartAt() : parseDateTime(startStr);
      
      String endStr = promptWithCancel("End time [" + existingEvent.getEndAt() + "]: ");
      if (endStr == null) return;
      LocalDateTime endAt = endStr.isEmpty() ? existingEvent.getEndAt() : parseDateTime(endStr);
      
      if (endAt.isBefore(startAt) || endAt.isEqual(startAt)) {
        System.err.println("Error: End time must be after start time.");
        return;
      }
      
      String capacityStr = promptWithCancel("Capacity [" + (existingEvent.getCapacity() != null ? existingEvent.getCapacity() : "unlimited") + "]: ");
      if (capacityStr == null) return;
      Integer capacity = capacityStr.isEmpty() ? existingEvent.getCapacity() : 
          (capacityStr.equalsIgnoreCase("unlimited") ? null : Integer.parseInt(capacityStr));
      
      Event updatedEvent = apiClient.updateEvent(eventId, orgId, title, description, 
          startAt, endAt, capacity);
      System.out.println("\n✓ Event updated successfully!");
      displayEventDetails(updatedEvent);
      
      // Check for conflicts with user's accepted events
      System.out.println("\nChecking for scheduling conflicts...");
      try {
        List<Event> conflicts = apiClient.findConflictsWithPendingEvent(currentUserId, eventId);
        if (!conflicts.isEmpty()) {
          System.out.println("\n⚠ Warning: This event conflicts with " + conflicts.size() + " of your accepted event(s):");
          displayEventsTable(conflicts);
          System.out.println("\nConsider adjusting the time or declining conflicting invitations.");
        } else {
          System.out.println("✓ No conflicts detected with your accepted events.");
        }
      } catch (Exception e) {
        // Silently ignore conflict check errors (endpoint might not be available)
        String errorMsg = e.getMessage();
        if (errorMsg != null && errorMsg.contains("CONFLICT_DETECTION_NOT_AVAILABLE")) {
          // Don't show error message for automatic conflict checks
        } else {
          System.out.println("(Conflict check unavailable)");
        }
      }
    } catch (Exception e) {
      String errorMsg = e.getMessage();
      if (errorMsg != null && (errorMsg.contains("conflict") || errorMsg.contains("Conflict"))) {
        System.err.println("⚠ Warning: " + errorMsg);
        System.err.println("The event was still updated, but there may be scheduling conflicts.");
      } else {
        System.err.println("Error updating event: " + errorMsg);
      }
    }
  }

  private void deleteEvent() {
    System.out.println("\n=== Delete Event ===");
    System.out.println("(Type 'back', 'cancel', or 'q' to return to main menu)");
    String eventId = promptWithCancel("Event ID: ");
    if (eventId == null) return;
    String confirm = promptWithCancel("Are you sure you want to delete this event? (yes/no): ");
    if (confirm == null) return;
    confirm = confirm.toLowerCase();
    
    if (!confirm.equals("yes")) {
      System.out.println("Deletion cancelled.");
      return;
    }
    
    try {
      apiClient.deleteEvent(eventId, currentUserId);
      System.out.println("✓ Event deleted successfully!");
    } catch (Exception e) {
      System.err.println("Error deleting event: " + e.getMessage());
    }
  }

  private void listEventsByOrganization() {
    System.out.println("\n=== Events by Organization ===");
    System.out.println("(Type 'back', 'cancel', or 'q' to return to main menu)");
    String orgId = promptWithCancel("Organization ID: ");
    if (orgId == null) return;
    
    try {
      List<Event> events = apiClient.getEventsByOrganization(orgId);
      if (events.isEmpty()) {
        System.out.println("No events found for this organization.");
      } else {
        displayEventsTable(events);
      }
    } catch (Exception e) {
      System.err.println("Error listing events: " + e.getMessage());
    }
  }

  private void listEventsByOrganizationAndUser() {
    System.out.println("\n=== Events by Organization & User ===");
    System.out.println("(Type 'back', 'cancel', or 'q' at any prompt to return to main menu)");
    
    String orgId = promptWithCancel("Organization ID: ");
    if (orgId == null) return;
    
    String userId = promptWithCancel("User ID: ");
    if (userId == null) return;
    
    try {
      List<Event> events = apiClient.getEventsByOrganizationAndUser(orgId, userId);
      if (events.isEmpty()) {
        System.out.println("No events found.");
      } else {
        displayEventsTable(events);
      }
    } catch (Exception e) {
      System.err.println("Error listing events: " + e.getMessage());
    }
  }

  private void displayEventsTable(List<Event> events) {
    System.out.println(String.format("%-40s %-30s %-20s %-20s", 
        "ID", "Title", "Start", "End"));
    System.out.println("-".repeat(110));
    for (Event event : events) {
      System.out.println(String.format("%-40s %-30s %-20s %-20s",
          truncate(event.getId(), 40),
          truncate(event.getTitle(), 30),
          formatDateTime(event.getStartAt()),
          formatDateTime(event.getEndAt())));
    }
  }

  private void displayEventDetails(Event event) {
    System.out.println("\n" + "-".repeat(50));
    System.out.println("Event Details:");
    System.out.println("-".repeat(50));
    System.out.println("ID:          " + event.getId());
    System.out.println("Title:       " + event.getTitle());
    if (event.getDescription() != null && !event.getDescription().isEmpty()) {
      System.out.println("Description: " + event.getDescription());
    }
    System.out.println("Start:       " + formatDateTime(event.getStartAt()));
    System.out.println("End:         " + formatDateTime(event.getEndAt()));
    if (event.getCapacity() != null) {
      System.out.println("Capacity:    " + event.getCapacity());
    } else {
      System.out.println("Capacity:    Unlimited");
    }
    System.out.println("Org ID:      " + event.getOrgId());
    if (event.getCreatedBy() != null) {
      System.out.println("Created By:  " + event.getCreatedBy());
    }
    if (event.getCreatedAt() != null) {
      System.out.println("Created At:  " + formatDateTime(event.getCreatedAt()));
    }
    System.out.println("-".repeat(50));
  }

  // Attendee Methods
  private void inviteAttendee() {
    System.out.println("\n=== Invite Attendee ===");
    System.out.println("(Type 'back', 'cancel', or 'q' at any prompt to return to main menu)");
    
    // Step 1: Show available events from user's organizations (numbered list)
    System.out.println("\nAvailable events (from your organizations):");
    List<Event> allEvents = new java.util.ArrayList<>();
    try {
      List<Membership> memberships = apiClient.getMembershipsByUser(currentUserId);
      for (Membership membership : memberships) {
        try {
          List<Event> orgEvents = apiClient.getEventsByOrganization(membership.getOrgId());
          allEvents.addAll(orgEvents);
        } catch (Exception e) {
          // Ignore errors for individual organizations
        }
      }
      
      if (allEvents.isEmpty()) {
        System.out.println("  (No events found. Please create an event first!)");
        return;
      } else {
        System.out.println(String.format("%-5s %-40s %-30s %-20s", "#", "Event ID", "Title", "Start Time"));
        System.out.println("-".repeat(95));
        for (int i = 0; i < allEvents.size(); i++) {
          Event event = allEvents.get(i);
          System.out.println(String.format("%-5s %-40s %-30s %-20s",
              (i + 1),
              truncate(event.getId(), 40),
              truncate(event.getTitle(), 30),
              formatDateTime(event.getStartAt())));
        }
      }
    } catch (Exception e) {
      System.err.println("Error: Could not list events: " + e.getMessage());
      return;
    }
    
    // Step 2: Select event by number
    String eventChoice = promptWithCancel("\nSelect event number: ");
    if (eventChoice == null) return;
    
    int eventIndex;
    try {
      eventIndex = Integer.parseInt(eventChoice) - 1;
      if (eventIndex < 0 || eventIndex >= allEvents.size()) {
        System.err.println("Invalid selection. Please choose a number between 1 and " + allEvents.size());
        return;
      }
    } catch (NumberFormatException e) {
      System.err.println("Invalid input. Please enter a number.");
      return;
    }
    
    Event selectedEvent = allEvents.get(eventIndex);
    String eventId = selectedEvent.getId();
    String orgId = selectedEvent.getOrgId();
    
    System.out.println("\nSelected Event: " + selectedEvent.getTitle());
    
    // Step 3: Get organization members (only ACTIVE members, excluding current user)
    System.out.println("\nOrganization members (active members only):");
    List<Membership> orgMemberships;
    try {
      orgMemberships = apiClient.getMembershipsByOrganization(orgId);
    } catch (Exception e) {
      System.err.println("Error: Could not retrieve organization members: " + e.getMessage());
      return;
    }
    
    if (orgMemberships.isEmpty()) {
      System.out.println("  (No members found in this organization!)");
      return;
    }
    
    // Filter: Only ACTIVE members, exclude current user
    List<Membership> activeMemberships = new java.util.ArrayList<>();
    for (Membership membership : orgMemberships) {
      String status = membership.getStatus();
      String userId = membership.getUserId();
      // Only include ACTIVE members who are not the current user
      if (status != null && status.equalsIgnoreCase("ACTIVE") && !userId.equals(currentUserId)) {
        activeMemberships.add(membership);
      }
    }
    
    if (activeMemberships.isEmpty()) {
      System.out.println("  (No active members found in this organization to invite!)");
      System.out.println("  (Note: Only members with ACTIVE status are shown, and you cannot invite yourself)");
      return;
    }
    
    // Get user details for each active member
    List<User> orgMembers = new java.util.ArrayList<>();
    List<String> memberUserIds = new java.util.ArrayList<>();
    for (Membership membership : activeMemberships) {
      try {
        User user = apiClient.getUserById(membership.getUserId());
        if (user != null) {
          orgMembers.add(user);
          memberUserIds.add(user.getId());
        }
      } catch (Exception e) {
        // Skip users that can't be retrieved
      }
    }
    
    if (orgMembers.isEmpty()) {
      System.out.println("  (No valid active members found!)");
      return;
    }
    
    // Step 4: Display members in numbered list
    System.out.println(String.format("%-5s %-40s %-30s %-30s", "#", "User ID", "Name", "Email"));
    System.out.println("-".repeat(105));
    for (int i = 0; i < orgMembers.size(); i++) {
      User user = orgMembers.get(i);
      System.out.println(String.format("%-5s %-40s %-30s %-30s",
          (i + 1),
          truncate(user.getId(), 40),
          truncate(user.getDisplayName(), 30),
          truncate(user.getEmail(), 30)));
    }
    
    // Step 5: Select users to invite (multiple or all)
    System.out.println("\nYou can invite:");
    System.out.println("  - Specific members: Enter numbers separated by commas (e.g., 1,3,5)");
    System.out.println("  - All members: Type 'all'");
    String userChoice = promptWithCancel("\nSelect member(s) to invite: ");
    if (userChoice == null) return;
    
    List<String> userIdsToInvite = new java.util.ArrayList<>();
    
    if (userChoice.trim().equalsIgnoreCase("all")) {
      // Invite all members
      userIdsToInvite.addAll(memberUserIds);
    } else {
      // Parse comma-separated numbers
      String[] numbers = userChoice.split(",");
      for (String numStr : numbers) {
        try {
          int userIndex = Integer.parseInt(numStr.trim()) - 1;
          if (userIndex >= 0 && userIndex < orgMembers.size()) {
            userIdsToInvite.add(memberUserIds.get(userIndex));
          } else {
            System.err.println("Warning: Invalid number " + numStr.trim() + " will be skipped.");
          }
        } catch (NumberFormatException e) {
          System.err.println("Warning: '" + numStr.trim() + "' is not a valid number and will be skipped.");
        }
      }
    }
    
    if (userIdsToInvite.isEmpty()) {
      System.err.println("No valid members selected.");
      return;
    }
    
    // Step 6: Invite selected users
    System.out.println("\nInviting " + userIdsToInvite.size() + " member(s)...");
    int successCount = 0;
    int failCount = 0;
    
    for (String userId : userIdsToInvite) {
      try {
        // Check if user is already an attendee
        try {
          List<Attendee> existingAttendees = apiClient.getAttendeesByEvent(eventId);
          boolean alreadyInvited = existingAttendees.stream()
              .anyMatch(a -> a.getUserId().equals(userId));
          if (alreadyInvited) {
            System.out.println("  ⚠ User " + userId + " is already invited to this event. Skipping.");
            continue;
          }
        } catch (Exception e) {
          // Continue if we can't check existing attendees
        }
        
        apiClient.createAttendee(eventId, userId, "PENDING");
        successCount++;
        User user = apiClient.getUserById(userId);
        String userName = user != null ? user.getDisplayName() : userId;
        System.out.println("  ✓ Invited: " + userName);
      } catch (Exception e) {
        failCount++;
        User user = null;
        try {
          user = apiClient.getUserById(userId);
        } catch (Exception ex) {
          // Ignore
        }
        String userName = user != null ? user.getDisplayName() : userId;
        System.err.println("  ✗ Failed to invite " + userName + ": " + e.getMessage());
      }
    }
    
    System.out.println("\n" + "=".repeat(50));
    System.out.println("Invitation Summary:");
    System.out.println("  Successful: " + successCount);
    System.out.println("  Failed: " + failCount);
    System.out.println("=".repeat(50));
  }

  private void updateRsvp() {
    System.out.println("\n=== Update RSVP Status ===");
    System.out.println("Your event invitations:");
    
    try {
      // Get all events the user has been invited to
      List<Attendee> attendees = apiClient.getAttendeesByUser(currentUserId);
      
      if (attendees.isEmpty()) {
        System.out.println("No event invitations found.");
        return;
      }
      
      // Get event details for each attendee
      List<Event> events = new java.util.ArrayList<>();
      List<Attendee> validAttendees = new java.util.ArrayList<>();
      
      for (Attendee attendee : attendees) {
        try {
          Event event = apiClient.getEventById(attendee.getEventId());
          if (event != null) {
            events.add(event);
            validAttendees.add(attendee);
          }
        } catch (Exception e) {
          // Skip events that can't be retrieved
        }
      }
      
      if (events.isEmpty()) {
        System.out.println("No valid events found.");
        return;
      }
      
      // Display numbered list
      System.out.println(String.format("%-5s %-40s %-30s %-20s %-20s", 
          "#", "Event ID", "Title", "Current RSVP", "Start Time"));
      System.out.println("-".repeat(115));
      
      for (int i = 0; i < events.size(); i++) {
        Event event = events.get(i);
        Attendee attendee = validAttendees.get(i);
        String status = attendee.getRsvpStatus();
        String statusDisplay = status;
        if (status.equalsIgnoreCase("YES")) {
          statusDisplay = "✓ YES";
        } else if (status.equalsIgnoreCase("NO")) {
          statusDisplay = "✗ NO";
        } else if (status.equalsIgnoreCase("PENDING")) {
          statusDisplay = "? PENDING";
        }
        
        System.out.println(String.format("%-5s %-40s %-30s %-20s %-20s",
            (i + 1),
            truncate(event.getId(), 40),
            truncate(event.getTitle(), 30),
            statusDisplay,
            formatDateTime(event.getStartAt())));
      }
      
      System.out.print("\nSelect event number to update RSVP: ");
      String choiceStr = scanner.nextLine().trim();
      
      int choice;
      try {
        choice = Integer.parseInt(choiceStr);
        if (choice < 1 || choice > events.size()) {
          System.err.println("Invalid selection. Please choose a number between 1 and " + events.size());
          return;
        }
      } catch (NumberFormatException e) {
        System.err.println("Invalid input. Please enter a number.");
        return;
      }
      
      Event selectedEvent = events.get(choice - 1);
      Attendee selectedAttendee = validAttendees.get(choice - 1);
      String currentStatus = selectedAttendee.getRsvpStatus();
      
      System.out.println("\nSelected Event: " + selectedEvent.getTitle());
      System.out.println("Current RSVP Status: " + currentStatus);
      System.out.print("New RSVP Status (PENDING/YES/NO): ");
      String newRsvpStatus = scanner.nextLine().trim().toUpperCase();
      
      if (!newRsvpStatus.equals("PENDING") && !newRsvpStatus.equals("YES") && !newRsvpStatus.equals("NO")) {
        System.err.println("Invalid RSVP status. Must be: PENDING, YES, or NO");
        return;
      }
      
      if (newRsvpStatus.equals(currentStatus)) {
        System.out.println("RSVP status is already " + currentStatus + ". No change needed.");
        return;
      }
      
      try {
        apiClient.updateRsvpStatus(selectedEvent.getId(), currentUserId, newRsvpStatus, currentUserId);
        System.out.println("✓ RSVP status updated successfully!");
        System.out.println("  Event: " + selectedEvent.getTitle());
        System.out.println("  Status changed from " + currentStatus + " to " + newRsvpStatus);
      } catch (Exception e) {
        System.err.println("Error updating RSVP status: " + e.getMessage());
      }
      
    } catch (Exception e) {
      System.err.println("Error retrieving your event invitations: " + e.getMessage());
    }
  }

  private void viewEventAttendees() {
    System.out.println("\n=== Event Attendees ===");
    System.out.println("(Type 'back', 'cancel', or 'q' to return to main menu)");
    
    // Get all events the user has been invited to
    System.out.println("\nYour event invitations:");
    try {
      List<Attendee> attendees = apiClient.getAttendeesByUser(currentUserId);
      
      if (attendees.isEmpty()) {
        System.out.println("No event invitations found.");
        return;
      }
      
      // Get event details for each attendee
      List<Event> events = new java.util.ArrayList<>();
      List<Attendee> validAttendees = new java.util.ArrayList<>();
      
      for (Attendee attendee : attendees) {
        try {
          Event event = apiClient.getEventById(attendee.getEventId());
          if (event != null) {
            events.add(event);
            validAttendees.add(attendee);
          }
        } catch (Exception e) {
          // Skip events that can't be retrieved
        }
      }
      
      if (events.isEmpty()) {
        System.out.println("No valid events found.");
        return;
      }
      
      // Display numbered list
      System.out.println(String.format("%-5s %-40s %-30s %-20s %-20s", 
          "#", "Event ID", "Title", "RSVP Status", "Start Time"));
      System.out.println("-".repeat(115));
      
      for (int i = 0; i < events.size(); i++) {
        Event event = events.get(i);
        Attendee attendee = validAttendees.get(i);
        String status = attendee.getRsvpStatus();
        String statusDisplay = status;
        if (status.equalsIgnoreCase("YES")) {
          statusDisplay = "✓ YES";
        } else if (status.equalsIgnoreCase("NO")) {
          statusDisplay = "✗ NO";
        } else if (status.equalsIgnoreCase("PENDING")) {
          statusDisplay = "? PENDING";
        }
        
        System.out.println(String.format("%-5s %-40s %-30s %-20s %-20s",
            (i + 1),
            truncate(event.getId(), 40),
            truncate(event.getTitle(), 30),
            statusDisplay,
            formatDateTime(event.getStartAt())));
      }
      
      System.out.print("\nSelect event number to view attendees: ");
      String choiceStr = scanner.nextLine().trim();
      
      if (isCancelCommand(choiceStr)) {
        System.out.println("Operation cancelled. Returning to main menu.");
        return;
      }
      
      int choice;
      try {
        choice = Integer.parseInt(choiceStr);
        if (choice < 1 || choice > events.size()) {
          System.err.println("Invalid selection. Please choose a number between 1 and " + events.size());
          return;
        }
      } catch (NumberFormatException e) {
        System.err.println("Invalid input. Please enter a number.");
        return;
      }
      
      Event selectedEvent = events.get(choice - 1);
      String eventId = selectedEvent.getId();
      
      // Display event attendees
      System.out.println("\nEvent: " + selectedEvent.getTitle());
      System.out.println("Attendees:");
      System.out.println("-".repeat(100));
      
      try {
        List<Attendee> eventAttendees = apiClient.getAttendeesByEvent(eventId);
        if (eventAttendees.isEmpty()) {
          System.out.println("No attendees found for this event.");
        } else {
          System.out.println(String.format("%-40s %-30s %-20s", "User ID", "Name", "RSVP Status"));
          System.out.println("-".repeat(100));
          for (Attendee attendee : eventAttendees) {
            // Get user details to show name
            String userName = "Unknown";
            try {
              User user = apiClient.getUserById(attendee.getUserId());
              if (user != null && user.getDisplayName() != null) {
                userName = user.getDisplayName();
              }
            } catch (Exception e) {
              // If we can't get user details, just use "Unknown"
            }
            
            String status = attendee.getRsvpStatus();
            String statusDisplay = status;
            if (status.equalsIgnoreCase("YES")) {
              statusDisplay = "✓ YES";
            } else if (status.equalsIgnoreCase("NO")) {
              statusDisplay = "✗ NO";
            } else if (status.equalsIgnoreCase("PENDING")) {
              statusDisplay = "? PENDING";
            }
            System.out.println(String.format("%-40s %-30s %-20s", 
                truncate(attendee.getUserId(), 40),
                truncate(userName, 30),
                statusDisplay));
          }
        }
      } catch (Exception e) {
        System.err.println("Error viewing attendees: " + e.getMessage());
      }
      
    } catch (Exception e) {
      System.err.println("Error retrieving your event invitations: " + e.getMessage());
    }
  }

  // User Methods
  private void viewUserDetails() {
    System.out.println("\n=== View User Details ===");
    System.out.println("(Type 'back', 'cancel', or 'q' to return to main menu)");
    String userId = promptWithCancel("User ID (press Enter to view your details): ");
    if (userId == null) return;
    
    // If user pressed Enter (empty string), use current user ID
    if (userId.isEmpty()) {
      userId = currentUserId;
    }
    
    try {
      User user = apiClient.getUserById(userId);
      if (user == null) {
        System.out.println("User not found.");
        return;
      }
      displayUserDetails(user);
      
      // Show membership count
      try {
        long membershipCount = apiClient.getMembershipCountByUser(userId);
        System.out.println("Memberships: " + membershipCount);
      } catch (Exception e) {
        // Ignore if endpoint not available
      }
    } catch (Exception e) {
      System.err.println("Error viewing user: " + e.getMessage());
    }
  }

  private void updateUserDisplayName() {
    System.out.println("\n=== Update My Display Name ===");
    System.out.println("(Type 'back', 'cancel', or 'q' to return to main menu)");
    String newDisplayName = promptWithCancel("New display name: ");
    if (newDisplayName == null) return;
    
    if (newDisplayName.isEmpty()) {
      System.err.println("Error: Display name cannot be empty.");
      return;
    }
    
    try {
      User user = apiClient.updateUserDisplayName(currentUserId, newDisplayName);
      currentUserDisplayName = user.getDisplayName();
      System.out.println("✓ Display name updated successfully!");
      displayUserDetails(user);
    } catch (Exception e) {
      System.err.println("Error updating display name: " + e.getMessage());
    }
  }

  private void deleteUser() {
    System.out.println("\n=== Delete User ===");
    System.out.print("User ID: ");
    String userId = scanner.nextLine().trim();
    
    if (userId.equals(currentUserId)) {
      System.out.print("⚠ Warning: You are about to delete your own account. Continue? (yes/no): ");
    } else {
      System.out.print("Are you sure you want to delete this user? (yes/no): ");
    }
    String confirm = scanner.nextLine().trim().toLowerCase();
    
    if (!confirm.equals("yes")) {
      System.out.println("Deletion cancelled.");
      return;
    }
    
    try {
      apiClient.deleteUser(userId);
      System.out.println("✓ User deleted successfully!");
      if (userId.equals(currentUserId)) {
        System.out.println("Your account has been deleted. Goodbye!");
        System.exit(0);
      }
    } catch (Exception e) {
      System.err.println("Error deleting user: " + e.getMessage());
    }
  }

  private void displayUserDetails(User user) {
    System.out.println("\n" + "-".repeat(50));
    System.out.println("User Details:");
    System.out.println("-".repeat(50));
    System.out.println("ID:          " + user.getId());
    System.out.println("Email:       " + user.getEmail());
    System.out.println("Display Name:" + user.getDisplayName());
    if (user.getCreatedAt() != null) {
      System.out.println("Created At:  " + formatDateTime(user.getCreatedAt()));
    }
    if (user.getActive() != null) {
      System.out.println("Active:      " + (user.getActive() ? "Yes" : "No"));
    }
    System.out.println("-".repeat(50));
  }

  // Conflict Detection Methods
  private void checkConflictsAmongAcceptedEvents() {
    System.out.println("\n=== Check Conflicts Among Accepted Events ===");
    System.out.println("Note: This checks for conflicts among events you have accepted (RSVP = YES).");
    System.out.println("(Type 'back', 'cancel', or 'q' to return to main menu)");
    String userId = promptWithCancel("User ID (press Enter to use your ID [" + currentUserId + "]): ");
    if (userId == null) return;
    if (userId.isEmpty()) {
      userId = currentUserId;
    }
    
    System.out.print("RSVP Status to check (YES/ACCEPTED, default: YES): ");
    String status = scanner.nextLine().trim().toUpperCase();
    if (status.isEmpty()) {
      status = "YES";
    }
    
    // Map common variations to accepted values
    if (status.equals("ACCEPTED")) {
      status = "YES";
    }
    
    if (!status.equals("YES")) {
      System.err.println("Invalid RSVP status. This endpoint only checks conflicts among accepted events.");
      System.err.println("Status must be: YES or ACCEPTED");
      System.err.println("Note: Use option 23 to check if a specific event conflicts with your accepted events.");
      return;
    }
    
    try {
      List<Event> conflicts = apiClient.findConflictsAmongAcceptedEvents(userId, status);
      
      if (conflicts.isEmpty()) {
        System.out.println("\n✓ No conflicts found among " + status + " events for user " + userId);
      } else {
        System.out.println("\n⚠ " + conflicts.size() + " conflict(s) found among " + status + " events:");
        System.out.println("=".repeat(80));
        displayEventsTable(conflicts);
        System.out.println("\n⚠ Warning: These events have overlapping time slots!");
        System.out.println("You may need to adjust your schedule or decline some invitations.");
      }
    } catch (Exception e) {
      String errorMsg = e.getMessage();
      if (errorMsg != null && errorMsg.contains("CONFLICT_DETECTION_NOT_AVAILABLE")) {
        System.err.println("\n⚠ Conflict detection is not available on this server.");
        System.err.println("The conflict detection feature may not be implemented in the backend yet.");
        System.err.println("Please contact your system administrator or check the server configuration.");
      } else if (errorMsg != null && errorMsg.contains("not found")) {
        System.err.println("Error: User not found with ID: " + userId);
      } else {
        System.err.println("Error checking conflicts: " + errorMsg);
      }
    }
  }

  private void checkConflictsWithPendingEvent() {
    System.out.println("\n=== Check Conflicts with Event ===");
    System.out.println("Note: This checks if a specific event conflicts with your accepted events.");
    System.out.println("(Useful for checking before accepting an invitation or creating a new event)");
    System.out.println("(Type 'back', 'cancel', or 'q' at any prompt to return to main menu)");
    
    // Show available events from user's organizations
    System.out.println("\nAvailable events (from your organizations):");
    try {
      List<Membership> memberships = apiClient.getMembershipsByUser(currentUserId);
      List<Event> allEvents = new java.util.ArrayList<>();
      for (Membership membership : memberships) {
        try {
          List<Event> orgEvents = apiClient.getEventsByOrganization(membership.getOrgId());
          allEvents.addAll(orgEvents);
        } catch (Exception e) {
          // Ignore errors for individual organizations
        }
      }
      
      if (allEvents.isEmpty()) {
        System.out.println("  (No events found. Please create an event first!)");
      } else {
        for (Event event : allEvents) {
          System.out.println("  ID: " + event.getId() + ", Title: " + event.getTitle() + 
              ", Start: " + formatDateTime(event.getStartAt()));
        }
      }
    } catch (Exception e) {
      System.err.println("Warning: Could not list events: " + e.getMessage());
    }
    
    String userId = promptWithCancel("\nUser ID (press Enter to use your ID [" + currentUserId + "]): ");
    if (userId == null) return;
    if (userId.isEmpty()) {
      userId = currentUserId;
    }
    
    String eventId = promptWithCancel("Event ID to check: ");
    if (eventId == null) return;
    
    if (eventId.isEmpty()) {
      System.err.println("Error: Event ID cannot be empty.");
      return;
    }
    
    try {
      // First, get the event details
      Event eventToCheck = apiClient.getEventById(eventId);
      if (eventToCheck == null) {
        System.err.println("Error: Event not found with ID: " + eventId);
        return;
      }
      
      System.out.println("\nChecking conflicts for event:");
      displayEventDetails(eventToCheck);
      
      List<Event> conflicts = apiClient.findConflictsWithPendingEvent(userId, eventId);
      
      if (conflicts.isEmpty()) {
        System.out.println("\n✓ No conflicts found! This event does not conflict with your accepted events.");
      } else {
        System.out.println("\n⚠ " + conflicts.size() + " conflict(s) found with your accepted events:");
        System.out.println("=".repeat(80));
        displayEventsTable(conflicts);
        System.out.println("\n⚠ Warning: This event conflicts with the following accepted events!");
        System.out.println("You may need to decline this invitation or adjust your schedule.");
      }
      } catch (Exception e) {
      String errorMsg = e.getMessage();
      if (errorMsg != null && errorMsg.contains("CONFLICT_DETECTION_NOT_AVAILABLE")) {
        System.err.println("\n⚠ Conflict detection is not available on this server.");
        System.err.println("The conflict detection feature may not be implemented in the backend yet.");
        System.err.println("Please contact your system administrator or check the server configuration.");
      } else if (errorMsg != null && errorMsg.contains("not found")) {
        if (errorMsg.contains("User")) {
          System.err.println("Error: User not found with ID: " + userId);
        } else {
          System.err.println("Error: Event not found with ID: " + eventId);
        }
      } else {
        System.err.println("Error checking conflicts: " + errorMsg);
      }
    }
  }

  // Quick Actions & Info
  private void showStatistics() {
    System.out.println("\n=== Statistics ===");
    try {
      List<User> users = apiClient.getAllUsers();
      List<Organization> orgs = apiClient.getAllOrganizations();
      List<Membership> memberships = apiClient.getMembershipsByUser(currentUserId);
      
      System.out.println("\nOverall Statistics:");
      System.out.println("  Total Users:        " + users.size());
      System.out.println("  Total Organizations:" + orgs.size());
      System.out.println("  My Memberships:     " + memberships.size());
      
      List<Event> myEvents = apiClient.getEventsByUser(currentUserId);
      System.out.println("\nMy Statistics:");
      System.out.println("  Events Created:     " + myEvents.size());
      
      // Count attendees for my events
      long totalAttendees = 0;
      for (Event event : myEvents) {
        try {
          totalAttendees += apiClient.getAttendeeCountByEvent(event.getId());
        } catch (Exception e) {
          // Ignore
        }
      }
      System.out.println("  Total Attendees:    " + totalAttendees);
      
      // Count events by organization
      int totalEventsInMyOrgs = 0;
      for (Membership membership : memberships) {
        try {
          List<Event> orgEvents = apiClient.getEventsByOrganization(membership.getOrgId());
          totalEventsInMyOrgs += orgEvents.size();
        } catch (Exception e) {
          // Ignore
        }
      }
      System.out.println("  Events in My Orgs:  " + totalEventsInMyOrgs);
      
    } catch (Exception e) {
      System.err.println("Error retrieving statistics: " + e.getMessage());
    }
  }

  private void exportEventsToText() {
    System.out.println("\n=== Export Events to Text ===");
    System.out.print("Export (1) My Events, (2) Events by Organization: ");
    String choice = scanner.nextLine().trim();
    
    List<Event> events;
    try {
      switch (choice) {
        case "1":
          events = apiClient.getEventsByUser(currentUserId);
          break;
        case "2":
          System.out.print("Organization ID: ");
          String orgId = scanner.nextLine().trim();
          events = apiClient.getEventsByOrganization(orgId);
          break;
        default:
          System.out.println("Invalid choice.");
          return;
      }
      
      if (events.isEmpty()) {
        System.out.println("No events to export.");
        return;
      }
      
      System.out.println("\n" + "=".repeat(80));
      System.out.println("EXPORTED EVENTS");
      System.out.println("=".repeat(80));
      System.out.println("Export Date: " + formatDateTime(LocalDateTime.now()));
      System.out.println("Total Events: " + events.size());
      System.out.println("=".repeat(80) + "\n");
      
      for (int i = 0; i < events.size(); i++) {
        Event event = events.get(i);
        System.out.println("Event #" + (i + 1));
        System.out.println("-".repeat(80));
        System.out.println("ID:          " + event.getId());
        System.out.println("Title:       " + event.getTitle());
        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
          System.out.println("Description: " + event.getDescription());
        }
        System.out.println("Start:       " + formatDateTime(event.getStartAt()));
        System.out.println("End:         " + formatDateTime(event.getEndAt()));
        if (event.getCapacity() != null) {
          System.out.println("Capacity:    " + event.getCapacity());
        }
        System.out.println("Org ID:      " + event.getOrgId());
        if (event.getCreatedBy() != null) {
          System.out.println("Created By:  " + event.getCreatedBy());
        }
        System.out.println();
      }
      
      System.out.println("=".repeat(80));
      System.out.println("End of Export");
      System.out.println("=".repeat(80));
      
    } catch (Exception e) {
      System.err.println("Error exporting events: " + e.getMessage());
    }
  }

  private void logout() {
    System.out.println("\n=== Logout / Switch User ===");
    System.out.println("Logging out...");
    currentUserId = null;
    currentUserDisplayName = null;
    if (!loginOrRegister()) {
      System.out.println("Failed to login. Exiting...");
      System.exit(0);
    }
  }

  // Utility Methods
  /**
   * Checks if the user input is a cancellation command.
   * Returns true if user wants to cancel and go back to main menu.
   */
  private boolean isCancelCommand(String input) {
    if (input == null) {
      return false;
    }
    String lower = input.trim().toLowerCase();
    return lower.equals("back") || lower.equals("cancel") || lower.equals("exit") 
        || lower.equals("q") || lower.equals("0") || lower.equals("menu");
  }

  /**
   * Prompts user for input with cancellation support.
   * Returns null if user wants to cancel, otherwise returns the trimmed input.
   */
  private String promptWithCancel(String prompt) {
    System.out.print(prompt);
    String input = scanner.nextLine().trim();
    if (isCancelCommand(input)) {
      System.out.println("Operation cancelled. Returning to main menu.");
      return null;
    }
    return input;
  }

  private String formatDateTime(LocalDateTime dateTime) {
    if (dateTime == null) {
      return "N/A";
    }
    try {
      return dateTime.format(DISPLAY_DATE_FORMATTER);
    } catch (Exception e) {
      return dateTime.toString();
    }
  }

  private String truncate(String str, int maxLength) {
    if (str == null) {
      return "";
    }
    if (str.length() <= maxLength) {
      return str;
    }
    return str.substring(0, maxLength - 3) + "...";
  }

  /**
   * Parses a date-time string with flexible format support.
   * Supports both "yyyy-MM-dd HH:mm" and "yyyy-M-d HH:mm" formats.
   * 
   * @param dateTimeStr the date-time string to parse
   * @return the parsed LocalDateTime
   * @throws RuntimeException if parsing fails
   */
  private LocalDateTime parseDateTime(String dateTimeStr) {
    try {
      // Try standard format first (yyyy-MM-dd HH:mm)
      try {
        return LocalDateTime.parse(dateTimeStr, DATE_FORMATTER);
      } catch (Exception e) {
        // If that fails, try flexible format (yyyy-M-d HH:mm)
        return LocalDateTime.parse(dateTimeStr, DATE_FORMATTER_FLEXIBLE);
      }
    } catch (Exception e) {
      throw new RuntimeException(
          "Invalid date format. Please use yyyy-MM-dd HH:mm (e.g., 2025-12-01 10:00) or "
          + "yyyy-M-d HH:mm (e.g., 2025-12-1 10:00). Error: " + e.getMessage(), e);
    }
  }
}
