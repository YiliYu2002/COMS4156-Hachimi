package com.example.activityscheduler.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for user registration requests. Contains only the fields that should be provided by the
 * client.
 */
@Schema(description = "User registration request")
public class UserRegistrationRequest {

  @Schema(description = "User's email address", example = "user@example.com", required = true)
  private String email;

  @Schema(description = "User's display name", example = "John Doe", required = true)
  private String displayName;

  /** Default constructor. */
  public UserRegistrationRequest() {}

  /**
   * Constructs a UserRegistrationRequest with the specified email and display name.
   *
   * @param email the user's email address
   * @param displayName the user's display name
   */
  public UserRegistrationRequest(String email, String displayName) {
    this.email = email;
    this.displayName = displayName;
  }

  // Getters and setters
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  @Override
  public String toString() {
    return "UserRegistrationRequest{"
        + "email='"
        + email
        + '\''
        + ", displayName='"
        + displayName
        + '\''
        + '}';
  }
}
