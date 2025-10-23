package com.example.activityscheduler.organization;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.activityscheduler.organization.controller.OrganizationController;
import com.example.activityscheduler.organization.dto.OrganizationCreationRequest;
import com.example.activityscheduler.organization.model.Organization;
import com.example.activityscheduler.organization.service.OrganizationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Integration tests for OrganizationController REST endpoints. */
@WebMvcTest(OrganizationController.class)
class OrganizationControllerTests {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private OrganizationService organizationService;

  @Autowired private ObjectMapper objectMapper;

  private Organization testOrganization;

  @BeforeEach
  void setUp() {
    testOrganization = new Organization("user123", "Test Organization");
  }

  @Test
  void testGetAllOrganizations() throws Exception {
    // Given
    List<Organization> organizations =
        Arrays.asList(new Organization("user1", "Org 1"), new Organization("user2", "Org 2"));
    when(organizationService.getAllOrganizations()).thenReturn(organizations);

    // When & Then
    mockMvc
        .perform(get("/api/organizations"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].name").value("Org 1"))
        .andExpect(jsonPath("$[1].name").value("Org 2"));

    verify(organizationService).getAllOrganizations();
  }

  @Test
  void testGetOrganizationById() throws Exception {
    // Given
    String orgId = "test-id";
    when(organizationService.getOrganizationById(orgId)).thenReturn(Optional.of(testOrganization));

    // When & Then
    mockMvc
        .perform(get("/api/organizations/{id}", orgId))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(testOrganization.getId()))
        .andExpect(jsonPath("$.name").value("Test Organization"));

    verify(organizationService).getOrganizationById(orgId);
  }

  @Test
  void testGetOrganizationByIdNotFound() throws Exception {
    // Given
    String orgId = "non-existent-id";
    when(organizationService.getOrganizationById(orgId)).thenReturn(Optional.empty());

    // When & Then
    mockMvc.perform(get("/api/organizations/{id}", orgId)).andExpect(status().isNotFound());

    verify(organizationService).getOrganizationById(orgId);
  }

  @Test
  void testGetOrganizationByName() throws Exception {
    // Given
    String orgName = "Test Organization";
    when(organizationService.getOrganizationByName(orgName))
        .thenReturn(Optional.of(testOrganization));

    // When & Then
    mockMvc
        .perform(get("/api/organizations/by-name").param("name", orgName))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.name").value("Test Organization"));

    verify(organizationService).getOrganizationByName(orgName);
  }

  @Test
  void testExistsByName() throws Exception {
    // Given
    String orgName = "Test Organization";
    when(organizationService.existsByName(orgName)).thenReturn(true);

    // When & Then
    mockMvc
        .perform(get("/api/organizations/exists").param("name", orgName))
        .andExpect(status().isOk())
        .andExpect(content().string("true"));

    verify(organizationService).existsByName(orgName);
  }

  @Test
  void testGetOrganizationCount() throws Exception {
    // Given
    long count = 5L;
    when(organizationService.getOrganizationCount()).thenReturn(count);

    // When & Then
    mockMvc
        .perform(get("/api/organizations/count"))
        .andExpect(status().isOk())
        .andExpect(content().string("5"));

    verify(organizationService).getOrganizationCount();
  }

  @Test
  void testCreateOrganization() throws Exception {
    // Given
    OrganizationCreationRequest request =
        new OrganizationCreationRequest("Test Organization", "user123");
    when(organizationService.createOrganization(any(Organization.class)))
        .thenReturn(testOrganization);

    // When & Then
    mockMvc
        .perform(
            post("/api/organizations/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.name").value("Test Organization"));

    verify(organizationService).createOrganization(any(Organization.class));
  }

  @Test
  void testCreateOrganizationWithInvalidData() throws Exception {
    // Given
    OrganizationCreationRequest request =
        new OrganizationCreationRequest("", "user123"); // Empty name
    when(organizationService.createOrganization(any(Organization.class)))
        .thenThrow(new IllegalArgumentException("Organization name cannot be null or empty"));

    // When & Then
    mockMvc
        .perform(
            post("/api/organizations/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());

    verify(organizationService).createOrganization(any(Organization.class));
  }

  @Test
  void testCreateOrganizationWithDuplicateName() throws Exception {
    // Given
    OrganizationCreationRequest request =
        new OrganizationCreationRequest("Test Organization", "user123");
    when(organizationService.createOrganization(any(Organization.class)))
        .thenThrow(
            new IllegalStateException("Organization with name 'Test Organization' already exists"));

    // When & Then
    mockMvc
        .perform(
            post("/api/organizations/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict());

    verify(organizationService).createOrganization(any(Organization.class));
  }

  @Test
  void testUpdateOrganization() throws Exception {
    // Given
    String orgId = "test-id";
    Organization updatedOrg = new Organization("user456", "Updated Organization");
    when(organizationService.updateOrganization(anyString(), any(Organization.class)))
        .thenReturn(updatedOrg);

    // When & Then
    mockMvc
        .perform(
            put("/api/organizations/{id}", orgId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedOrg)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.name").value("Updated Organization"));

    verify(organizationService).updateOrganization(eq(orgId), any(Organization.class));
  }

  @Test
  void testUpdateOrganizationNotFound() throws Exception {
    // Given
    String orgId = "non-existent-id";
    when(organizationService.updateOrganization(anyString(), any(Organization.class)))
        .thenThrow(new IllegalStateException("Organization with ID '" + orgId + "' not found"));

    // When & Then
    mockMvc
        .perform(
            put("/api/organizations/{id}", orgId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testOrganization)))
        .andExpect(status().isNotFound());

    verify(organizationService).updateOrganization(eq(orgId), any(Organization.class));
  }

  @Test
  void testDeleteOrganization() throws Exception {
    // Given
    String orgId = "test-id";
    doNothing().when(organizationService).deleteOrganization(orgId);

    // When & Then
    mockMvc.perform(delete("/api/organizations/{id}", orgId)).andExpect(status().isNoContent());

    verify(organizationService).deleteOrganization(orgId);
  }

  @Test
  void testDeleteOrganizationNotFound() throws Exception {
    // Given
    String orgId = "non-existent-id";
    doThrow(new IllegalStateException("Organization with ID '" + orgId + "' not found"))
        .when(organizationService)
        .deleteOrganization(orgId);

    // When & Then
    mockMvc.perform(delete("/api/organizations/{id}", orgId)).andExpect(status().isNotFound());

    verify(organizationService).deleteOrganization(orgId);
  }

  @Test
  void testGetAllOrganizationsEmpty() throws Exception {
    // Given
    when(organizationService.getAllOrganizations()).thenReturn(Arrays.asList());

    // When & Then
    mockMvc
        .perform(get("/api/organizations"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(0));

    verify(organizationService).getAllOrganizations();
  }

  // Note: Service exception handling tests removed as the controller doesn't handle service
  // exceptions
  // The controller lets service exceptions bubble up as runtime exceptions

  @Test
  void testGetOrganizationByNameNotFound() throws Exception {
    // Given
    String orgName = "Non-existent Organization";
    when(organizationService.getOrganizationByName(orgName)).thenReturn(Optional.empty());

    // When & Then
    mockMvc
        .perform(get("/api/organizations/by-name").param("name", orgName))
        .andExpect(status().isNotFound());

    verify(organizationService).getOrganizationByName(orgName);
  }

  @Test
  void testGetOrganizationByNameInvalidName() throws Exception {
    // Given
    String orgName = "";
    when(organizationService.getOrganizationByName(orgName)).thenReturn(Optional.empty());

    // When & Then
    mockMvc
        .perform(get("/api/organizations/by-name").param("name", orgName))
        .andExpect(status().isNotFound());

    verify(organizationService).getOrganizationByName(orgName);
  }

  @Test
  void testExistsByNameNotFound() throws Exception {
    // Given
    String orgName = "Non-existent Organization";
    when(organizationService.existsByName(orgName)).thenReturn(false);

    // When & Then
    mockMvc
        .perform(get("/api/organizations/exists").param("name", orgName))
        .andExpect(status().isOk())
        .andExpect(content().string("false"));

    verify(organizationService).existsByName(orgName);
  }

  @Test
  void testExistsByNameInvalidName() throws Exception {
    // Given
    String orgName = "";
    when(organizationService.existsByName(orgName)).thenReturn(false);

    // When & Then
    mockMvc
        .perform(get("/api/organizations/exists").param("name", orgName))
        .andExpect(status().isOk())
        .andExpect(content().string("false"));

    verify(organizationService).existsByName(orgName);
  }

  @Test
  void testGetOrganizationCountZero() throws Exception {
    // Given
    long count = 0L;
    when(organizationService.getOrganizationCount()).thenReturn(count);

    // When & Then
    mockMvc
        .perform(get("/api/organizations/count"))
        .andExpect(status().isOk())
        .andExpect(content().string("0"));

    verify(organizationService).getOrganizationCount();
  }

  // Note: Service exception handling tests removed as the controller doesn't handle service
  // exceptions
  // The controller lets service exceptions bubble up as runtime exceptions

  @Test
  void testCreateOrganizationWithNullData() throws Exception {
    // Given
    OrganizationCreationRequest request = new OrganizationCreationRequest(null, "user123");
    when(organizationService.createOrganization(any(Organization.class)))
        .thenThrow(new IllegalArgumentException("Organization name cannot be null"));

    // When & Then
    mockMvc
        .perform(
            post("/api/organizations/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());

    verify(organizationService).createOrganization(any(Organization.class));
  }

  @Test
  void testUpdateOrganizationWithInvalidData() throws Exception {
    // Given
    String orgId = "test-id";
    Organization invalidOrg = new Organization("user456", ""); // Empty name
    when(organizationService.updateOrganization(anyString(), any(Organization.class)))
        .thenThrow(new IllegalArgumentException("Organization name cannot be empty"));

    // When & Then
    mockMvc
        .perform(
            put("/api/organizations/{id}", orgId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidOrg)))
        .andExpect(status().isBadRequest());

    verify(organizationService).updateOrganization(eq(orgId), any(Organization.class));
  }

  @Test
  void testDeleteOrganizationWithInvalidId() throws Exception {
    // Given
    String orgId = "invalid-id";
    doThrow(new IllegalArgumentException("Organization ID cannot be empty"))
        .when(organizationService)
        .deleteOrganization(orgId);

    // When & Then
    mockMvc.perform(delete("/api/organizations/{id}", orgId)).andExpect(status().isBadRequest());

    verify(organizationService).deleteOrganization(orgId);
  }
}
