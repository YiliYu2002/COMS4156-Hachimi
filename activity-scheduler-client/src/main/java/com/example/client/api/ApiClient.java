package com.example.client.api;

import com.example.client.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class ApiClient {
  private final String baseUrl;
  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;

  public ApiClient(String baseUrl) {
    this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    this.httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();
    this.objectMapper = new ObjectMapper();
    this.objectMapper.registerModule(new JavaTimeModule());
  }

  public String checkBasicHealth() throws IOException {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/health/basic"))
        .GET()
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
    
    if (response.statusCode() >= 200 && response.statusCode() < 300) {
        return response.body();
    } else {
        throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
    }
  }

  public User registerUser(String email, String displayName) throws IOException {
    UserRegistrationRequest request = new UserRegistrationRequest(email, displayName);
    String json = objectMapper.writeValueAsString(request);

    HttpRequest httpRequest = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/users/register"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(json))
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
    
    if (response.statusCode() == 200 || response.statusCode() == 409) {
      return objectMapper.readValue(response.body(), User.class);
    } else {
      throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
    }
  }

  public User getUserByEmail(String email) throws IOException {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/users/exists?email=" + email))
        .GET()
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
    
    if (response.statusCode() == 200) {
      boolean exists = Boolean.parseBoolean(response.body());
      if (!exists) {
        return null;
      }
      // If exists, we need to get the user - but the API doesn't have getByEmail
      // So we'll need to get all users and find by email
      List<User> users = getAllUsers();
      return users.stream()
          .filter(u -> u.getEmail().equals(email))
          .findFirst()
          .orElse(null);
    } else {
      throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
    }
  }

  public List<User> getAllUsers() throws IOException {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/users"))
        .GET()
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
    
    if (response.statusCode() == 200) {
      User[] users = objectMapper.readValue(response.body(), User[].class);
      return Arrays.asList(users);
    } else {
      throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
    }
  }

  public Organization createOrganization(String name, String createdBy) throws IOException {
    OrganizationCreationRequest request = new OrganizationCreationRequest(name, createdBy);
    String json = objectMapper.writeValueAsString(request);

    HttpRequest httpRequest = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/organizations/create"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(json))
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
    
    if (response.statusCode() == 201 || response.statusCode() == 409) {
      return objectMapper.readValue(response.body(), Organization.class);
    } else {
      throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
    }
  }

  public List<Organization> getAllOrganizations() throws IOException {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/organizations"))
        .GET()
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
    
    if (response.statusCode() == 200) {
      Organization[] orgs = objectMapper.readValue(response.body(), Organization[].class);
      return Arrays.asList(orgs);
    } else {
      throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
    }
  }

  public Membership createMembership(String orgId, String userId, String status) throws IOException {
    MembershipRequest request = new MembershipRequest(orgId, userId, status);
    String json = objectMapper.writeValueAsString(request);

    HttpRequest httpRequest = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/memberships"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(json))
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
    
    if (response.statusCode() == 200 || response.statusCode() == 409) {
      return objectMapper.readValue(response.body(), Membership.class);
    } else {
      throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
    }
  }

  public List<Membership> getMembershipsByUser(String userId) throws IOException {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/memberships/user/" + userId))
        .GET()
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
    
    if (response.statusCode() == 200) {
      Membership[] memberships = objectMapper.readValue(response.body(), Membership[].class);
      return Arrays.asList(memberships);
    } else {
      throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
    }
  }

  public Membership updateMembershipStatus(String orgId, String userId, String status) throws IOException {
    // Create a simple JSON object with just the status
    String json = "{\"status\":\"" + status + "\"}";

    HttpRequest httpRequest = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/memberships/" + orgId + "/" + userId + "/status"))
        .header("Content-Type", "application/json")
        .PUT(HttpRequest.BodyPublishers.ofString(json))
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
    
    if (response.statusCode() == 200) {
      return objectMapper.readValue(response.body(), Membership.class);
    } else {
      throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
    }
  }

  public List<Membership> getMembershipsByOrganization(String orgId) throws IOException {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/memberships/organization/" + orgId))
        .GET()
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
    
    if (response.statusCode() == 200) {
      Membership[] memberships = objectMapper.readValue(response.body(), Membership[].class);
      return Arrays.asList(memberships);
    } else {
      throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
    }
  }

  public Event createEvent(String orgId, String createdBy, String title, String description,
                           LocalDateTime startAt, LocalDateTime endAt, Integer capacity) throws IOException {
    Event event = new Event();
    event.setOrgId(orgId);
    event.setCreatedBy(createdBy);
    event.setTitle(title);
    event.setDescription(description);
    event.setStartAt(startAt);
    event.setEndAt(endAt);
    event.setCapacity(capacity);
    event.setCreatedAt(LocalDateTime.now()); // Required by backend validation

    String json = objectMapper.writeValueAsString(event);

    HttpRequest httpRequest = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/events"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(json))
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
    
    if (response.statusCode() == 201) {
      return objectMapper.readValue(response.body(), Event.class);
    } else {
      throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
    }
  }

  public List<Event> getEventsByUser(String userId) throws IOException {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/events/user/" + userId))
        .GET()
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
    
    if (response.statusCode() == 200) {
      Event[] events = objectMapper.readValue(response.body(), Event[].class);
      return Arrays.asList(events);
    } else {
      throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
    }
  }

  // Note: getAllEvents() removed - backend doesn't have GET /api/events endpoint
  // Use getEventsByOrganization(), getEventsByUser(), or getEventsByOrganizationAndUser() instead

  public Attendee createAttendee(String eventId, String userId, String rsvpStatus) throws IOException {
    AttendeeRequest request = new AttendeeRequest(eventId, userId, rsvpStatus);
    String json = objectMapper.writeValueAsString(request);

    HttpRequest httpRequest = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/attendees"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(json))
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
    
    if (response.statusCode() == 200 || response.statusCode() == 409) {
      return objectMapper.readValue(response.body(), Attendee.class);
    } else {
      throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
    }
  }

  public void updateRsvpStatus(String eventId, String userId, String rsvpStatus, String requestUserId) throws IOException {
    RsvpUpdateRequest request = new RsvpUpdateRequest(rsvpStatus);
    String json = objectMapper.writeValueAsString(request);

    HttpRequest httpRequest = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/attendees/" + eventId + "/" + userId + "/rsvp"))
        .header("Content-Type", "application/json")
        .header("X-User-Id", requestUserId)
        .PUT(HttpRequest.BodyPublishers.ofString(json))
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
    
    if (response.statusCode() != 200) {
      throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
    }
  }

  public List<Attendee> getAttendeesByEvent(String eventId) throws IOException {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/attendees/event/" + eventId))
        .GET()
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
    
    if (response.statusCode() == 200) {
      Attendee[] attendees = objectMapper.readValue(response.body(), Attendee[].class);
      return Arrays.asList(attendees);
    } else {
      throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
    }
  }

  public List<Attendee> getAttendeesByUser(String userId) throws IOException {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/attendees/user/" + userId))
        .GET()
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
    
    if (response.statusCode() == 200) {
      Attendee[] attendees = objectMapper.readValue(response.body(), Attendee[].class);
      return Arrays.asList(attendees);
    } else {
      throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
    }
  }

  // User Management
  public User getUserById(String userId) throws IOException {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/users/" + userId))
        .GET()
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
    
    if (response.statusCode() == 200) {
      return objectMapper.readValue(response.body(), User.class);
    } else if (response.statusCode() == 404) {
      return null;
    } else {
      throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
    }
  }

  public User updateUserDisplayName(String userId, String displayName) throws IOException {
    HttpRequest httpRequest = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/users/" + userId + "/username"))
        .header("Content-Type", "application/json")
        .PUT(HttpRequest.BodyPublishers.ofString("\"" + displayName + "\""))
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
    
    if (response.statusCode() == 200) {
      return objectMapper.readValue(response.body(), User.class);
    } else {
      throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
    }
  }

  public void deleteUser(String userId) throws IOException {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/users/" + userId))
        .DELETE()
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
    
    if (response.statusCode() != 204 && response.statusCode() != 200) {
      throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
    }
  }

  // Organization Management
  public Organization getOrganizationById(String orgId) throws IOException {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/organizations/" + orgId))
        .GET()
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
    
    if (response.statusCode() == 200) {
      return objectMapper.readValue(response.body(), Organization.class);
    } else if (response.statusCode() == 404) {
      return null;
    } else {
      throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
    }
  }

  public Organization updateOrganization(String orgId, String name) throws IOException {
    Organization org = new Organization();
    org.setName(name);
    String json = objectMapper.writeValueAsString(org);

    HttpRequest httpRequest = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/organizations/" + orgId))
        .header("Content-Type", "application/json")
        .PUT(HttpRequest.BodyPublishers.ofString(json))
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
    
    if (response.statusCode() == 200) {
      return objectMapper.readValue(response.body(), Organization.class);
    } else {
      throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
    }
  }

  public void deleteOrganization(String orgId) throws IOException {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/organizations/" + orgId))
        .DELETE()
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
    
    if (response.statusCode() != 204 && response.statusCode() != 200) {
      throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
    }
  }

  // Event Management
  public Event getEventById(String eventId) throws IOException {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/events/" + eventId))
        .GET()
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
    
    if (response.statusCode() == 200) {
      return objectMapper.readValue(response.body(), Event.class);
    } else if (response.statusCode() == 404) {
      return null;
    } else {
      throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
    }
  }

  public Event updateEvent(String eventId, String orgId, String title, String description,
                           LocalDateTime startAt, LocalDateTime endAt, Integer capacity) throws IOException {
    // Get existing event to preserve createdAt
    Event existingEvent = getEventById(eventId);
    if (existingEvent == null) {
      throw new IOException("Event not found with ID: " + eventId);
    }
    
    Event event = new Event();
    event.setOrgId(orgId);
    event.setTitle(title);
    event.setDescription(description);
    event.setStartAt(startAt);
    event.setEndAt(endAt);
    event.setCapacity(capacity);
    event.setCreatedAt(existingEvent.getCreatedAt()); // Preserve original createdAt
    event.setCreatedBy(existingEvent.getCreatedBy()); // Preserve original createdBy

    String json = objectMapper.writeValueAsString(event);

    HttpRequest httpRequest = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/events/" + eventId))
        .header("Content-Type", "application/json")
        .PUT(HttpRequest.BodyPublishers.ofString(json))
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
    
    if (response.statusCode() == 200) {
      return objectMapper.readValue(response.body(), Event.class);
    } else {
      throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
    }
  }

  public void deleteEvent(String eventId, String requestUserId) throws IOException {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/events/" + eventId))
        .header("X-User-Id", requestUserId)
        .DELETE()
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
    
    if (response.statusCode() != 204 && response.statusCode() != 200) {
      throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
    }
  }

  // Event Queries
  public List<Event> getEventsByOrganization(String orgId) throws IOException {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/events/organization/" + orgId))
        .GET()
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
    
    if (response.statusCode() == 200) {
      Event[] events = objectMapper.readValue(response.body(), Event[].class);
      return Arrays.asList(events);
    } else {
      throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
    }
  }

  public List<Event> getEventsByOrganizationAndUser(String orgId, String userId) throws IOException {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/events/organization/" + orgId + "/user/" + userId))
        .GET()
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
    
    if (response.statusCode() == 200) {
      Event[] events = objectMapper.readValue(response.body(), Event[].class);
      return Arrays.asList(events);
    } else {
      throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
    }
  }

  // Attendee Queries
  public long getAttendeeCountByEvent(String eventId) throws IOException {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/attendees/event/" + eventId + "/count"))
        .GET()
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
    
    if (response.statusCode() == 200) {
      return Long.parseLong(response.body());
    } else {
      throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
    }
  }

  public long getAttendeeCountByEventAndStatus(String eventId, String rsvpStatus) throws IOException {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/attendees/event/" + eventId + "/status/" + rsvpStatus.toUpperCase() + "/count"))
        .GET()
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
    
    if (response.statusCode() == 200) {
      return Long.parseLong(response.body());
    } else {
      throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
    }
  }

  // Membership Queries
  public long getActiveMemberCountByOrganization(String orgId) throws IOException {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/memberships/organization/" + orgId + "/active-count"))
        .GET()
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
    
    if (response.statusCode() == 200) {
      return Long.parseLong(response.body());
    } else {
      throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
    }
  }

  public long getMembershipCountByUser(String userId) throws IOException {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/memberships/user/" + userId + "/count"))
        .GET()
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
    
    if (response.statusCode() == 200) {
      return Long.parseLong(response.body());
    } else {
      throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
    }
  }

  // Conflict Detection
  /**
   * Finds conflicts among accepted events for a user.
   * 
   * @param userId the user ID
   * @param rsvpStatus the RSVP status to check (typically "YES" for accepted events)
   * @return list of conflicting events
   */
  public List<Event> findConflictsAmongAcceptedEvents(String userId, String rsvpStatus) throws IOException {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/conflicts/checkconflicts/" + userId + "/status?status=" + rsvpStatus.toUpperCase()))
        .GET()
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
    
    if (response.statusCode() == 200) {
      Event[] events = objectMapper.readValue(response.body(), Event[].class);
      return Arrays.asList(events);
    } else if (response.statusCode() == 404) {
      throw new IOException("CONFLICT_DETECTION_NOT_AVAILABLE: Conflict detection endpoint is not available on this server. The feature may not be implemented yet.");
    } else {
      throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
    }
  }

  /**
   * Finds conflicts between a pending event and accepted events for a user.
   * 
   * @param userId the user ID
   * @param pendingEventId the pending event ID to check for conflicts
   * @return list of conflicting events
   */
  public List<Event> findConflictsWithPendingEvent(String userId, String pendingEventId) throws IOException {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/conflicts/checkconflicts/" + userId + "/" + pendingEventId))
        .GET()
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Request interrupted", e);
    }
    
    if (response.statusCode() == 200) {
      Event[] events = objectMapper.readValue(response.body(), Event[].class);
      return Arrays.asList(events);
    } else if (response.statusCode() == 404) {
      throw new IOException("CONFLICT_DETECTION_NOT_AVAILABLE: Conflict detection endpoint is not available on this server. The feature may not be implemented yet.");
    } else {
      throw new IOException("HTTP " + response.statusCode() + ": " + response.body());
    }
  }
}
