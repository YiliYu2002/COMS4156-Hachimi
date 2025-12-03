package com.example.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RsvpUpdateRequest {
  @JsonProperty("rsvpStatus")
  private String rsvpStatus;

  public RsvpUpdateRequest() {}

  public RsvpUpdateRequest(String rsvpStatus) {
    this.rsvpStatus = rsvpStatus;
  }

  public String getRsvpStatus() {
    return rsvpStatus;
  }

  public void setRsvpStatus(String rsvpStatus) {
    this.rsvpStatus = rsvpStatus;
  }
}
