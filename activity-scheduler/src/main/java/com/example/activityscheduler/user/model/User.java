package com.example.activityscheduler.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User entity representing a user in the activity scheduler system. Maps to the 'users' table in
 * the database.
 */
@Entity
@Table(name = "users") // existing table name
public class User {

  @Id
  @Column(columnDefinition = "CHAR(36)")
  private String id; // matches CHAR(36)

  @Column(nullable = false, unique = true, length = 320)
  private String email;

  @Column(name = "display_name", nullable = false, length = 255)
  private String displayName;

  @Column(name = "is_active", nullable = false)
  private boolean isActive = true;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  /** Default constructor. Generates a new UUID for the user ID. */
  public User() {
    this.id = UUID.randomUUID().toString();
  }

  /**
   * Constructs a User with the specified email, display name.
   *
   * @param email the user's email address
   * @param displayName the user's display name
   */
  public User(String email, String displayName) {
    this();
    this.email = email;
    this.displayName = displayName;
  }

  // getters & setters
  public String getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean active) {
    isActive = active;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
