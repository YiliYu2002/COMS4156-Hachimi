package com.example.activityscheduler.membership.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Membership entity representing the relationship between users and organizations. Maps to the
 * 'memberships' table in the database with composite primary key.
 */
@Entity
@Table(name = "memberships")
@IdClass(MembershipId.class)
public class Membership {

  @Id
  @Column(name = "org_id", length = 36, nullable = false)
  private String orgId;

  @Id
  @Column(name = "user_id", length = 36, nullable = false)
  private String userId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private MembershipStatus status = MembershipStatus.ACTIVE;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  /** Default constructor. */
  public Membership() {}

  /**
   * Constructs a Membership with the specified organization ID, user ID, and status.
   *
   * @param orgId the organization ID
   * @param userId the user ID
   * @param status the membership status
   */
  public Membership(String orgId, String userId, MembershipStatus status) {
    this.orgId = orgId;
    this.userId = userId;
    this.status = status;
  }

  // Getters and Setters
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

  public MembershipStatus getStatus() {
    return status;
  }

  public void setStatus(MembershipStatus status) {
    this.status = status;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
