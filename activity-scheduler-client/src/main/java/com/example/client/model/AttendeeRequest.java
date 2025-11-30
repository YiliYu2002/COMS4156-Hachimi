package com.example.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AttendeeRequest {
  @JsonProperty("eventId")
  private String eventId;

  @JsonProperty("userId")
  private String userId;

  @JsonProperty("rsvpStatus")
  private String rsvpStatus;

  public AttendeeRequest() {}

  public AttendeeRequest(String eventId, String userId, String rsvpStatus) {
    this.eventId = eventId;
    this.userId = userId;
    this.rsvpStatus = rsvpStatus;
  }

  public String getEventId() {
    return eventId;
  }

  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getRsvpStatus() {
    return rsvpStatus;
  }

  public void setRsvpStatus(String rsvpStatus) {
    this.rsvpStatus = rsvpStatus;
  }
}
