package com.example.activityscheduler.membership.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing the possible status values for a membership. Maps to the 'status' column in the
 * memberships table.
 */
public enum MembershipStatus {

  /** Active membership - the user is an active member of the organization. */
  ACTIVE("ACTIVE"),

  /** Invited membership - the user has been invited but hasn't accepted yet. */
  INVITED("INVITED"),

  /** Suspended membership - the user's membership has been suspended. */
  SUSPENDED("SUSPENDED");

  private final String value;

  /**
   * Constructs a MembershipStatus with the specified database value.
   *
   * @param value the database value
   */
  MembershipStatus(String value) {
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
   * Creates a MembershipStatus from a string value (case-insensitive).
   *
   * @param value the string value
   * @return the corresponding MembershipStatus
   * @throws IllegalArgumentException if the value is not recognized
   */
  @JsonCreator
  public static MembershipStatus fromString(String value) {
    if (value == null) {
      return null;
    }
    for (MembershipStatus status : values()) {
      if (status.value.equalsIgnoreCase(value)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown membership status: " + value);
  }
}
