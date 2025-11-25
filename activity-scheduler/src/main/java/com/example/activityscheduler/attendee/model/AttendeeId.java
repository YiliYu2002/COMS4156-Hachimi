package com.example.activityscheduler.attendee.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for Attendee entity. Represents the combination of event ID and user ID.
 */
public class AttendeeId implements Serializable {

  private String eventId;
  private String userId;

  /** Default constructor. */
  public AttendeeId() {}

  /**
   * Constructs an AttendeeId with the specified event ID and user ID.
   *
   * @param eventId the event ID
   * @param userId the user ID
   */
  public AttendeeId(String eventId, String userId) {
    this.eventId = eventId;
    this.userId = userId;
  }

  public String getEventId() {
    return eventId;
  }

  public String getUserId() {
    return userId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AttendeeId that = (AttendeeId) o;
    return Objects.equals(eventId, that.eventId) && Objects.equals(userId, that.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(eventId, userId);
  }

  @Override
  public String toString() {
    return "AttendeeId{" + "eventId='" + eventId + '\'' + ", userId='" + userId + '\'' + '}';
  }
}
