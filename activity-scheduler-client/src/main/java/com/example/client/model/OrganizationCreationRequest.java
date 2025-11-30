package com.example.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrganizationCreationRequest {
  @JsonProperty("name")
  private String name;

  @JsonProperty("createdBy")
  private String createdBy;

  public OrganizationCreationRequest() {}

  public OrganizationCreationRequest(String name, String createdBy) {
    this.name = name;
    this.createdBy = createdBy;
  }

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
}
