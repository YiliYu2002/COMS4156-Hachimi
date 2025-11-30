package com.example.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserRegistrationRequest {
  @JsonProperty("email")
  private String email;

  @JsonProperty("displayName")
  private String displayName;

  public UserRegistrationRequest() {}

  public UserRegistrationRequest(String email, String displayName) {
    this.email = email;
    this.displayName = displayName;
  }

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
}
