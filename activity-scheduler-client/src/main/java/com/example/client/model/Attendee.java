package com.example.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Attendee {
  @JsonProperty("eventId")
  private String eventId;

  @JsonProperty("userId")
  private String userId;

  @JsonProperty("rsvpStatus")
  private String rsvpStatus;

  public Attendee() {}

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
