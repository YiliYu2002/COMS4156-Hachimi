package com.example.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MembershipRequest {
  @JsonProperty("orgId")
  private String orgId;

  @JsonProperty("userId")
  private String userId;

  @JsonProperty("status")
  private String status;

  public MembershipRequest() {}

  public MembershipRequest(String orgId, String userId, String status) {
    this.orgId = orgId;
    this.userId = userId;
    this.status = status;
  }

  public String getOrgId() {
    return orgId;
  }

  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
