package com.example.activityscheduler.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for organization creation requests. Contains only the fields that should be provided by the
 * client.
 */
@Schema(description = "Organization creation request")
public class OrganizationCreationRequest {

  @Schema(description = "Organization name", example = "Acme Corp", required = true)
  private String name;

  @Schema(
      description = "User ID of the organization creator",
      example = "user-123",
      required = true)
  private String createdBy;

  /** Default constructor. */
  public OrganizationCreationRequest() {}

  /**
   * Constructs an OrganizationCreationRequest with the specified name and creator.
   *
   * @param name the organization name
   * @param createdBy the user ID of the organization creator
   */
  public OrganizationCreationRequest(String name, String createdBy) {
    this.name = name;
    this.createdBy = createdBy;
  }

  // Getters and setters
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  @Override
  public String toString() {
    return "OrganizationCreationRequest{"
        + "name='"
        + name
        + '\''
        + ", createdBy='"
        + createdBy
        + '\''
        + '}';
  }
}
