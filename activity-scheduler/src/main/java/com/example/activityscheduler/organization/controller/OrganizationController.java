package com.example.activityscheduler.organization.controller;

import com.example.activityscheduler.organization.dto.OrganizationCreationRequest;
import com.example.activityscheduler.organization.model.Organization;
import com.example.activityscheduler.organization.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST controller for managing Organization entities. Provides HTTP endpoints for organization
 * operations with comprehensive API documentation.
 */
@RestController
@RequestMapping("/api/organizations")
@Tag(name = "Organization Management", description = "APIs for managing organizations")
public class OrganizationController {

  private final OrganizationService organizationService;

  /**
   * Constructs an OrganizationController with the given service.
   *
   * @param organizationService the organization service
   */
  public OrganizationController(OrganizationService organizationService) {
    this.organizationService = organizationService;
  }

  /**
   * Retrieves all organizations.
   *
   * @return a list of all organizations
   */
  @Operation(
      summary = "Get all organizations",
      description = "Retrieves a list of all organizations in the system")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all organizations")
      })
  @GetMapping
  public List<Organization> getAllOrganizations() {
    return organizationService.getAllOrganizations();
  }

  /**
   * Retrieves an organization by its ID.
   *
   * @param id the organization ID
   * @return the organization if found
   */
  @Operation(
      summary = "Get organization by ID",
      description = "Retrieves a specific organization by its unique identifier")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Organization found successfully"),
        @ApiResponse(responseCode = "404", description = "Organization not found")
      })
  @GetMapping("/{id}")
  public ResponseEntity<Organization> getOrganizationById(
      @Parameter(description = "Organization ID") @PathVariable String id) {
    Optional<Organization> organization = organizationService.getOrganizationById(id);
    return organization.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  /**
   * Retrieves an organization by its name.
   *
   * @param name the organization name
   * @return the organization if found
   */
  @Operation(
      summary = "Get organization by name",
      description = "Retrieves a specific organization by its name")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Organization found successfully"),
        @ApiResponse(responseCode = "404", description = "Organization not found")
      })
  @GetMapping("/by-name")
  public ResponseEntity<Organization> getOrganizationByName(
      @Parameter(description = "Organization name") @RequestParam String name) {
    Optional<Organization> organization = organizationService.getOrganizationByName(name);
    return organization.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  /**
   * Checks if an organization exists with the given name.
   *
   * @param name the organization name to check
   * @return true if an organization exists with this name, false otherwise
   */
  @Operation(
      summary = "Check if organization exists by name",
      description = "Checks whether an organization with the given name exists")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Name check completed successfully")
      })
  @GetMapping("/exists")
  public boolean existsByName(
      @Parameter(description = "Organization name to check") @RequestParam String name) {
    return organizationService.existsByName(name);
  }

  /**
   * Gets the total count of organizations.
   *
   * @return the total number of organizations
   */
  @Operation(
      summary = "Get organization count",
      description = "Retrieves the total number of organizations in the system")
  @ApiResponses(
      value = {@ApiResponse(responseCode = "200", description = "Count retrieved successfully")})
  @GetMapping("/count")
  public long getOrganizationCount() {
    return organizationService.getOrganizationCount();
  }

  /**
   * Creates a new organization.
   *
   * @param request the organization creation request
   * @return the created organization
   */
  @Operation(
      summary = "Create a new organization",
      description = "Creates a new organization in the system by providing name and creator")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Organization created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid organization data"),
        @ApiResponse(
            responseCode = "409",
            description = "Organization with this name already exists")
      })
  @PostMapping("/create")
  public ResponseEntity<Organization> createOrganization(
      @RequestBody OrganizationCreationRequest request) {
    try {
      // Create Organization entity from request
      Organization organization = new Organization(request.getCreatedBy(), request.getName());
      Organization createdOrganization = organizationService.createOrganization(organization);
      return ResponseEntity.status(HttpStatus.CREATED).body(createdOrganization);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    } catch (IllegalStateException e) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
    }
  }

  /**
   * Updates an existing organization.
   *
   * @param id the organization ID
   * @param organization the updated organization data
   * @return the updated organization
   */
  @Operation(
      summary = "Update organization",
      description = "Updates an existing organization with new data")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Organization updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid organization data"),
        @ApiResponse(responseCode = "404", description = "Organization not found"),
        @ApiResponse(
            responseCode = "409",
            description = "Organization with this name already exists")
      })
  @PutMapping("/{id}")
  public ResponseEntity<Organization> updateOrganization(
      @Parameter(description = "Organization ID") @PathVariable String id,
      @RequestBody Organization organization) {
    try {
      Organization updatedOrganization = organizationService.updateOrganization(id, organization);
      return ResponseEntity.ok(updatedOrganization);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    } catch (IllegalStateException e) {
      if (e.getMessage().contains("not found")) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
      } else {
        throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
      }
    }
  }

  /**
   * Deletes an organization by its ID.
   *
   * @param id the organization ID
   * @return no content if successful
   */
  @Operation(summary = "Delete organization", description = "Deletes an organization by its ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Organization deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Organization not found")
      })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteOrganization(
      @Parameter(description = "Organization ID") @PathVariable String id) {
    try {
      organizationService.deleteOrganization(id);
      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    } catch (IllegalStateException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    }
  }
}
