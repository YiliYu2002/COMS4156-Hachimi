package com.example.activityscheduler.membership.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for Membership entity. Represents the combination of organization ID and
 * user ID.
 */
public class MembershipId implements Serializable {

  private String orgId;
  private String userId;

  /** Default constructor. */
  public MembershipId() {}

  /**
   * Constructs a MembershipId with the specified organization ID and user ID.
   *
   * @param orgId the organization ID
   * @param userId the user ID
   */
  public MembershipId(String orgId, String userId) {
    this.orgId = orgId;
    this.userId = userId;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MembershipId that = (MembershipId) o;
    return Objects.equals(orgId, that.orgId) && Objects.equals(userId, that.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(orgId, userId);
  }

  @Override
  public String toString() {
    return "MembershipId{" + "orgId='" + orgId + '\'' + ", userId='" + userId + '\'' + '}';
  }
}
