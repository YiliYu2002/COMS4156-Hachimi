package com.example.activityscheduler.attendee.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing the possible RSVP status values for an event attendee. Maps to the
 * 'rsvp_status' column in the event_attendees table.
 */
public enum RsvpStatus {

  /** Pending RSVP - the user has been invited but hasn't responded yet. */
  PENDING("pending"),

  /** Yes RSVP - the user has accepted the invitation. */
  YES("yes"),

  /** No RSVP - the user has declined the invitation. */
  NO("no");

  private final String value;

  /**
   * Constructs an RsvpStatus with the specified database value.
   *
   * @param value the database value
   */
  RsvpStatus(String value) {
    this.value = value;
  }

  /**
   * Gets the database value for this status.
   *
   * @return the database value
   */
  @JsonValue
  public String getValue() {
    return value;
  }

  /**
   * Creates an RsvpStatus from a string value (case-insensitive).
   *
   * @param value the string value
   * @return the corresponding RsvpStatus
   * @throws IllegalArgumentException if the value is not recognized
   */
  @JsonCreator
  public static RsvpStatus fromString(String value) {
    if (value == null) {
      return null;
    }
    for (RsvpStatus status : values()) {
      if (status.value.equalsIgnoreCase(value)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown RSVP status: " + value);
  }
}
