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

  private ApiClient apiClient;
  private Scanner scanner;
  private String currentUserId;
  private String serviceUrl;

  public ActivitySchedulerClient(String serviceUrl) {
    this.serviceUrl = serviceUrl;
    this.apiClient = new ApiClient(serviceUrl);
    this.scanner = new Scanner(System.in);
  }

  public static void main(String[] args) {
    // 支持三种方式（按优先级）：
    // 1. 命令行参数
    // 2. 环境变量 SERVICE_URL
    // 3. 默认值 http://localhost:8080
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
          case "1":
            createOrganization();
            break;
          case "2":
            listOrganizations();
            break;
          case "3":
            createMembership();
            break;
          case "4":
            listMyMemberships();
            break;
          case "5":
            createEvent();
            break;
          case "6":
            listAllEvents();
            break;
          case "7":
            listMyEvents();
            break;
          case "8":
            inviteAttendee();
            break;
          case "9":
            updateRsvp();
            break;
          case "10":
            viewEventAttendees();
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
    System.out.print("Display Name: ");
    String displayName = scanner.nextLine().trim();

    try {
      // Check if user exists
      User existingUser = apiClient.getUserByEmail(email);
      if (existingUser != null) {
        currentUserId = existingUser.getId();
        System.out.println("✓ Logged in as: " + existingUser.getDisplayName() + " (ID: " + currentUserId + ")");
        return true;
      }

      // Register new user
      User newUser = apiClient.registerUser(email, displayName);
      currentUserId = newUser.getId();
      System.out.println("✓ Registered and logged in as: " + newUser.getDisplayName() + " (ID: " + currentUserId + ")");
      return true;
    } catch (Exception e) {
      System.err.println("Error during login/registration: " + e.getMessage());
      return false;
    }
  }

  private void printMenu() {
    System.out.println("\n=== Main Menu ===");
    System.out.println("1. Create Organization");
    System.out.println("2. List All Organizations");
    System.out.println("3. Create Membership");
    System.out.println("4. List My Memberships");
    System.out.println("5. Create Event");
    System.out.println("6. List All Events");
    System.out.println("7. List My Events");
    System.out.println("8. Invite Attendee to Event");
    System.out.println("9. Update RSVP Status");
    System.out.println("10. View Event Attendees");
    System.out.println("0. Exit");
    System.out.print("Choose an option: ");
  }

  private void createOrganization() {
    System.out.println("\n=== Create Organization ===");
    System.out.print("Organization name: ");
    String name = scanner.nextLine().trim();
    
    try {
      Organization org = apiClient.createOrganization(name, currentUserId);
      System.out.println("Organization created successfully!");
      System.out.println("ID: " + org.getId());
      System.out.println("Name: " + org.getName());
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
        for (Organization org : orgs) {
          System.out.println("ID: " + org.getId() + ", Name: " + org.getName());
        }
      }
    } catch (Exception e) {
      System.err.println("Error listing organizations: " + e.getMessage());
    }
  }

  private void createMembership() {
    System.out.println("\n=== Create Membership ===");
    System.out.print("Organization ID: ");
    String orgId = scanner.nextLine().trim();
    System.out.print("User ID: ");
    String userId = scanner.nextLine().trim();
    System.out.print("Status (ACTIVE/INVITED/SUSPENDED, default: INVITED): ");
    String status = scanner.nextLine().trim();
    if (status.isEmpty()) {
      status = "INVITED";
    }
    
    try {
      Membership membership = apiClient.createMembership(orgId, userId, status);
      System.out.println("Membership created successfully!");
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
        for (Membership m : memberships) {
          System.out.println("Organization ID: " + m.getOrgId() + ", Status: " + m.getStatus());
        }
      }
    } catch (Exception e) {
      System.err.println("Error listing memberships: " + e.getMessage());
    }
  }

  private void createEvent() {
    System.out.println("\n=== Create Event ===");
    
    // First, show available organizations
    System.out.println("Available organizations:");
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
    
    System.out.print("\nOrganization ID: ");
    String orgId = scanner.nextLine().trim();
    
    if (orgId.isEmpty()) {
      System.err.println("Error: Organization ID cannot be empty.");
      return;
    }
    
    System.out.print("Event title: ");
    String title = scanner.nextLine().trim();
    
    System.out.print("Description (optional): ");
    String description = scanner.nextLine().trim();
    
    System.out.print("Start time (yyyy-MM-dd HH:mm, e.g., 2025-12-01 10:00): ");
    String startStr = scanner.nextLine().trim();
    LocalDateTime startAt = parseDateTime(startStr);
    
    System.out.print("End time (yyyy-MM-dd HH:mm, e.g., 2025-12-01 11:00): ");
    String endStr = scanner.nextLine().trim();
    LocalDateTime endAt = parseDateTime(endStr);
    
    System.out.print("Capacity (optional, press Enter for unlimited): ");
    String capacityStr = scanner.nextLine().trim();
    Integer capacity = capacityStr.isEmpty() ? null : Integer.parseInt(capacityStr);
    
    try {
      Event event = apiClient.createEvent(orgId, currentUserId, title, description, 
          startAt, endAt, capacity);
      System.out.println("Event created successfully!");
      System.out.println("ID: " + event.getId());
      System.out.println("Title: " + event.getTitle());
      System.out.println("Start: " + event.getStartAt());
      System.out.println("End: " + event.getEndAt());
    } catch (Exception e) {
      String errorMsg = e.getMessage();
      if (errorMsg != null && errorMsg.contains("does not exist")) {
        System.err.println("Error: The organization with the provided ID does not exist.");
        System.err.println("Please make sure:");
        System.err.println("  1. You have created an organization first (option 1)");
        System.err.println("  2. You are using the correct Organization ID");
        System.err.println("  3. The service has not been restarted (which clears the database)");
      } else {
        System.err.println("Error creating event: " + errorMsg);
      }
    }
  }

  private void listAllEvents() {
    System.out.println("\n=== All Events ===");
    try {
      List<Event> events = apiClient.getAllEvents();
      if (events.isEmpty()) {
        System.out.println("No events found.");
      } else {
        for (Event event : events) {
          System.out.println("ID: " + event.getId() + ", Title: " + event.getTitle() + 
              ", Start: " + event.getStartAt() + ", End: " + event.getEndAt());
        }
      }
    } catch (Exception e) {
      System.err.println("Error listing events: " + e.getMessage());
    }
  }

  private void listMyEvents() {
    System.out.println("\n=== My Events ===");
    try {
      List<Event> events = apiClient.getEventsByUser(currentUserId);
      if (events.isEmpty()) {
        System.out.println("No events found.");
      } else {
        for (Event event : events) {
          System.out.println("ID: " + event.getId() + ", Title: " + event.getTitle() + 
              ", Start: " + event.getStartAt() + ", End: " + event.getEndAt());
        }
      }
    } catch (Exception e) {
      System.err.println("Error listing my events: " + e.getMessage());
    }
  }

  private void inviteAttendee() {
    System.out.println("\n=== Invite Attendee ===");
    
    // Show available events
    System.out.println("Available events:");
    try {
      List<Event> events = apiClient.getAllEvents();
      if (events.isEmpty()) {
        System.out.println("  (No events found. Please create an event first!)");
      } else {
        for (Event event : events) {
          System.out.println("  ID: " + event.getId() + ", Title: " + event.getTitle());
        }
      }
    } catch (Exception e) {
      System.err.println("Warning: Could not list events: " + e.getMessage());
    }
    
    System.out.print("\nEvent ID: ");
    String eventId = scanner.nextLine().trim();
    
    if (eventId.isEmpty()) {
      System.err.println("Error: Event ID cannot be empty.");
      return;
    }
    
    // Show available users
    System.out.println("\nAvailable users:");
    try {
      List<User> users = apiClient.getAllUsers();
      if (users.isEmpty()) {
        System.out.println("  (No users found. Please register users first!)");
      } else {
        for (User user : users) {
          System.out.println("  ID: " + user.getId() + ", Email: " + user.getEmail() + 
              ", Name: " + user.getDisplayName());
        }
      }
    } catch (Exception e) {
      System.err.println("Warning: Could not list users: " + e.getMessage());
    }
    
    System.out.print("\nUser ID to invite: ");
    String userId = scanner.nextLine().trim();
    
    if (userId.isEmpty()) {
      System.err.println("Error: User ID cannot be empty.");
      return;
    }
    
    try {
      Attendee attendee = apiClient.createAttendee(eventId, userId, "pending");
      System.out.println("Invitation sent successfully!");
      System.out.println("Event ID: " + attendee.getEventId());
      System.out.println("User ID: " + attendee.getUserId());
      System.out.println("RSVP Status: " + attendee.getRsvpStatus());
    } catch (Exception e) {
      String errorMsg = e.getMessage();
      if (errorMsg != null && errorMsg.contains("User not found")) {
        System.err.println("Error: The user with the provided ID does not exist.");
        System.err.println("Please make sure:");
        System.err.println("  1. The user has been registered (they need to login/register first)");
        System.err.println("  2. You are using the correct User ID");
        System.err.println("  3. The service has not been restarted (which clears the database)");
      } else if (errorMsg != null && errorMsg.contains("Event not found")) {
        System.err.println("Error: The event with the provided ID does not exist.");
        System.err.println("Please make sure you are using the correct Event ID.");
      } else {
        System.err.println("Error inviting attendee: " + errorMsg);
      }
    }
  }

  private void updateRsvp() {
    System.out.println("\n=== Update RSVP Status ===");
    System.out.print("Event ID: ");
    String eventId = scanner.nextLine().trim();
    System.out.print("User ID: ");
    String userId = scanner.nextLine().trim();
    System.out.print("RSVP Status (pending/yes/no): ");
    String rsvpStatus = scanner.nextLine().trim().toLowerCase();
    
    if (!rsvpStatus.equals("pending") && !rsvpStatus.equals("yes") && !rsvpStatus.equals("no")) {
      System.err.println("Invalid RSVP status. Must be: pending, yes, or no");
      return;
    }
    
    try {
      apiClient.updateRsvpStatus(eventId, userId, rsvpStatus, currentUserId);
      System.out.println("RSVP status updated successfully!");
    } catch (Exception e) {
      System.err.println("Error updating RSVP status: " + e.getMessage());
    }
  }

  private void viewEventAttendees() {
    System.out.println("\n=== Event Attendees ===");
    System.out.print("Event ID: ");
    String eventId = scanner.nextLine().trim();
    
    try {
      List<Attendee> attendees = apiClient.getAttendeesByEvent(eventId);
      if (attendees.isEmpty()) {
        System.out.println("No attendees found for this event.");
      } else {
        System.out.println("Attendees:");
        for (Attendee attendee : attendees) {
          System.out.println("  User ID: " + attendee.getUserId() + ", RSVP: " + attendee.getRsvpStatus());
        }
      }
    } catch (Exception e) {
      System.err.println("Error viewing attendees: " + e.getMessage());
    }
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
