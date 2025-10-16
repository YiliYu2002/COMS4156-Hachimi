package com.example.activityscheduler.organization.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Organization entity representing an organization in the activity scheduler system. Maps to the
 * 'organizations' table in the database.
 */
@Entity
@Table(name = "organizations")
public class Organization {

  @Id
  @Column(columnDefinition = "CHAR(36)")
  private String id;

  @Column(nullable = false, unique = true, length = 255)
  private String name;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "created_by", nullable = false)
  private String createdBy;

  /** Default constructor. Generates a new UUID for the organization ID. */
  public Organization(String createdBy, String name) {
    this.id = UUID.randomUUID().toString();
    this.createdBy = createdBy;
    this.name = name;
  }

  // getters & setters
  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  @Override
  public String toString() {
    return "Organization{"
        + "id='"
        + id
        + '\''
        + ", name='"
        + name
        + '\''
        + ", createdAt="
        + createdAt
        + ", createdBy='"
        + createdBy
        + '\''
        + '}';
  }
}
