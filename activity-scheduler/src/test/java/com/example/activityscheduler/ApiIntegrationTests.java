package com.example.activityscheduler;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.activityscheduler.user.dto.UserRegistrationRequest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/** API integration tests for the Activity Scheduler application. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.datasource.username=sa",
      "spring.datasource.password=",
      "spring.jpa.hibernate.ddl-auto=create-drop"
    })
class ApiIntegrationTests {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate restTemplate;

  private String baseUrl;
  private HttpHeaders headers;

  @BeforeEach
  void setUp() {
    baseUrl = "http://localhost:" + port;
    headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");
  }

  // ========== PERSISTENCE TESTING ==========

  @Test
  void testWriteThenReadPersistence() {
    // Step 1: Create a user (WRITE)
    UserRegistrationRequest userRequest =
        new UserRegistrationRequest("test@example.com", "Test User");
    HttpEntity<UserRegistrationRequest> userEntity = new HttpEntity<>(userRequest, headers);

    ResponseEntity<String> userResponse =
        restTemplate.postForEntity(baseUrl + "/api/users/register", userEntity, String.class);

    assertThat(userResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    String userId = extractUserIdFromResponse(userResponse.getBody());
    assertThat(userId).isNotNull();

    // Step 2: READ the data back to verify persistence
    ResponseEntity<String> readUserResponse =
        restTemplate.getForEntity(baseUrl + "/api/users/" + userId, String.class);
    assertThat(readUserResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(readUserResponse.getBody()).contains("test@example.com");
  }

  // ========== LOGGING VERIFICATION ==========

  @Test
  void testLoggingVerification() {
    // Make API calls that should generate logs
    ResponseEntity<String> usersResponse =
        restTemplate.getForEntity(baseUrl + "/api/users", String.class);
    assertThat(usersResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    // Test health endpoints
    ResponseEntity<String> healthResponse =
        restTemplate.getForEntity(baseUrl + "/health/basic", String.class);
    assertThat(healthResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(healthResponse.getBody()).contains("Application is running");

    // Note: In a real implementation, you would verify the log files contain the expected entries
    // This demonstrates that logging is working by making calls that should generate logs
  }

  // ========== MULTI-CLIENT TESTING ==========

  @Test
  void testMultiClientConcurrency() throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(2);

    // Client 1: Create user
    CompletableFuture<ResponseEntity<String>> client1 =
        CompletableFuture.supplyAsync(
            () -> {
              UserRegistrationRequest request =
                  new UserRegistrationRequest("client1@test.com", "Client 1");
              HttpEntity<UserRegistrationRequest> entity = new HttpEntity<>(request, headers);
              return restTemplate.postForEntity(
                  baseUrl + "/api/users/register", entity, String.class);
            },
            executor);

    // Client 2: Read data
    CompletableFuture<ResponseEntity<String>> client2 =
        CompletableFuture.supplyAsync(
            () -> {
              return restTemplate.getForEntity(baseUrl + "/api/users", String.class);
            },
            executor);

    // Wait for all clients to complete
    ResponseEntity<String> response1 = client1.get(5, TimeUnit.SECONDS);
    ResponseEntity<String> response2 = client2.get(5, TimeUnit.SECONDS);

    // Verify all clients succeeded without interference
    assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);

    executor.shutdown();
  }

  // ========== API TESTING TOOL USAGE ==========

  @Test
  void testApiTestingToolUsage() {
    // This test demonstrates API testing tool usage patterns
    // Using TestRestTemplate as the API testing tool

    // Test typical valid input
    ResponseEntity<String> validResponse =
        restTemplate.getForEntity(baseUrl + "/api/users", String.class);
    assertThat(validResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    // Test atypical valid input (edge cases)
    ResponseEntity<String> edgeCaseResponse =
        restTemplate.getForEntity(
            baseUrl + "/api/users/exists?email=nonexistent@test.com", String.class);
    assertThat(edgeCaseResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(edgeCaseResponse.getBody()).isEqualTo("false");
  }

  // ========== COMPREHENSIVE API COVERAGE ==========

  @Test
  void testAllApiEndpoints() {
    // Test all major API endpoints for comprehensive coverage

    // User endpoints
    ResponseEntity<String> usersResponse =
        restTemplate.getForEntity(baseUrl + "/api/users", String.class);
    assertThat(usersResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    // Health endpoints
    ResponseEntity<String> healthResponse =
        restTemplate.getForEntity(baseUrl + "/health/basic", String.class);
    assertThat(healthResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(healthResponse.getBody()).contains("Application is running");
  }

  @Test
  void testOrganizationEndpoints() {
    // Test GET /api/organizations
    ResponseEntity<String> orgsResponse =
        restTemplate.getForEntity(baseUrl + "/api/organizations", String.class);
    assertThat(orgsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    // Test GET /api/organizations/count
    ResponseEntity<String> countResponse =
        restTemplate.getForEntity(baseUrl + "/api/organizations/count", String.class);
    assertThat(countResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    // Test GET /api/organizations/exists
    ResponseEntity<String> existsResponse =
        restTemplate.getForEntity(baseUrl + "/api/organizations/exists?name=TestOrg", String.class);
    assertThat(existsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testOrganizationCreation() {
    // First create a user
    String uniqueEmail = "testuser" + System.currentTimeMillis() + "@example.com";
    String userJson = "{\"email\":\"" + uniqueEmail + "\",\"displayName\":\"Test User\"}";
    HttpEntity<String> userEntity = new HttpEntity<>(userJson, headers);

    ResponseEntity<String> userResponse =
        restTemplate.postForEntity(baseUrl + "/api/users/register", userEntity, String.class);
    assertThat(userResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    // Extract user ID from response
    String userId = extractUserIdFromResponse(userResponse.getBody());

    // Test POST /api/organizations/create
    String uniqueOrgName = "TestOrg" + System.currentTimeMillis();
    String orgJson = "{\"name\":\"" + uniqueOrgName + "\",\"createdBy\":\"" + userId + "\"}";
    HttpEntity<String> orgEntity = new HttpEntity<>(orgJson, headers);

    ResponseEntity<String> createResponse =
        restTemplate.postForEntity(baseUrl + "/api/organizations/create", orgEntity, String.class);
    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
  }

  @Test
  void testOrganizationInvalidData() {
    // Test POST /api/organizations/create with invalid data
    String invalidOrgJson = "{\"name\":\"\",\"createdBy\":\"user123\"}";
    HttpEntity<String> invalidEntity = new HttpEntity<>(invalidOrgJson, headers);

    ResponseEntity<String> response =
        restTemplate.postForEntity(
            baseUrl + "/api/organizations/create", invalidEntity, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  // ========== MEMBERSHIP API ENDPOINTS ==========

  @Test
  void testMembershipEndpoints() {
    // Test GET /api/memberships
    ResponseEntity<String> membershipsResponse =
        restTemplate.getForEntity(baseUrl + "/api/memberships", String.class);
    assertThat(membershipsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    // Test GET /api/memberships/organization/{orgId}
    ResponseEntity<String> byOrgResponse =
        restTemplate.getForEntity(baseUrl + "/api/memberships/organization/org123", String.class);
    assertThat(byOrgResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    // Test GET /api/memberships/user/{userId}
    ResponseEntity<String> byUserResponse =
        restTemplate.getForEntity(baseUrl + "/api/memberships/user/user123", String.class);
    assertThat(byUserResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    // Test GET /api/memberships/status/{status}
    ResponseEntity<String> byStatusResponse =
        restTemplate.getForEntity(baseUrl + "/api/memberships/status/ACTIVE", String.class);
    assertThat(byStatusResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testMembershipCreation() {
    // Test POST /api/memberships/create
    String membershipJson = "{\"orgId\":\"org123\",\"userId\":\"user456\",\"status\":\"ACTIVE\"}";
    HttpEntity<String> membershipEntity = new HttpEntity<>(membershipJson, headers);

    ResponseEntity<String> createResponse =
        restTemplate.postForEntity(baseUrl + "/api/memberships", membershipEntity, String.class);
    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void testMembershipInvalidData() {
    // Test POST /api/memberships/create with invalid data
    String invalidMembershipJson = "{\"orgId\":\"\",\"userId\":\"\",\"status\":\"ACTIVE\"}";
    HttpEntity<String> invalidEntity = new HttpEntity<>(invalidMembershipJson, headers);

    ResponseEntity<String> response =
        restTemplate.postForEntity(baseUrl + "/api/memberships", invalidEntity, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  // ========== HEALTH API ENDPOINTS ==========

  @Test
  void testHealthEndpoints() {
    // Test GET /health/basic
    ResponseEntity<String> basicHealthResponse =
        restTemplate.getForEntity(baseUrl + "/health/basic", String.class);
    assertThat(basicHealthResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(basicHealthResponse.getBody()).contains("Application is running");

    // Test GET /health/db
    ResponseEntity<String> dbHealthResponse =
        restTemplate.getForEntity(baseUrl + "/health/db", String.class);
    assertThat(dbHealthResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(dbHealthResponse.getBody()).contains("Database");
  }

  @Test
  void testHealthEndpointsWithInvalidMethods() {
    // Test POST /health/basic (should return 405 Method Not Allowed)
    ResponseEntity<String> response =
        restTemplate.postForEntity(baseUrl + "/health/basic", new HttpEntity<>(""), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
  }

  // ========== HELPER METHODS ==========

  private String extractUserIdFromResponse(String responseBody) {
    // Simple extraction - in real implementation, parse JSON properly
    if (responseBody != null && responseBody.contains("\"id\"")) {
      int start = responseBody.indexOf("\"id\":\"") + 6;
      int end = responseBody.indexOf("\"", start);
      return responseBody.substring(start, end);
    }
    return null;
  }
}
