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

  public List<Event> getAllEvents() throws IOException {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/events"))
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
}
