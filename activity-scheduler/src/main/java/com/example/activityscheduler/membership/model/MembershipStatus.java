package com.example.activityscheduler.membership.model;

/**
 * Enum representing the possible status values for a membership. Maps to the 'status' column in the
 * memberships table.
 */
public enum MembershipStatus {

  /** Active membership - the user is an active member of the organization. */
  ACTIVE("active"),

  /** Invited membership - the user has been invited but hasn't accepted yet. */
  INVITED("invited"),

  /** Suspended membership - the user's membership has been suspended. */
  SUSPENDED("suspended");

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
  public String getValue() {
    return value;
  }

  /**
   * Gets the MembershipStatus from its database value.
   *
   * @param value the database value
   * @return the corresponding MembershipStatus
   * @throws IllegalArgumentException if the value is not recognized
   */
  public static MembershipStatus fromValue(String value) {
    for (MembershipStatus status : values()) {
      if (status.value.equals(value)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown membership status: " + value);
  }
}
