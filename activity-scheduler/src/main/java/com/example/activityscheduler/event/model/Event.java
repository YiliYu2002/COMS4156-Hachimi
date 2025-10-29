package com.example.activityscheduler.event.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Event entity representing a scheduled event in the activity scheduler system. Maps to the
 * 'events' table in the database.
 */
@Entity
@Table(name = "events")
public class Event {

  @Id
  @Column(columnDefinition = "CHAR(36)")
  private String id;

  @NotBlank
  @Column(name = "title", nullable = false, length = 255)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  @NotNull
  @Column(name = "start_at", nullable = false)
  private LocalDateTime startAt;

  @NotNull
  @Column(name = "end_at", nullable = false)
  private LocalDateTime endAt;

  @Min(value = 0, message = "Capacity must be non-negative")
  @Column
  private Integer capacity;

  @Column(name = "location", length = 255)
  private String location;

  @Column(name = "created_by", length = 255)
  private String createdBy;

  @NotNull
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  /** Default constructor. Generates a new UUID for the event ID. */
  public Event() {
    this.id = UUID.randomUUID().toString();
  }

  /**
   * Constructs an Event with the specified parameters.
   *
   * @param title the event title
   * @param description the event description
   * @param startAt the start time
   * @param endAt the end time
   * @param capacity the event capacity (optional)
   * @param location the event location (optional)
   * @param createdBy the user who created the event
   */
  public Event(
      String title,
      String description,
      LocalDateTime startAt,
      LocalDateTime endAt,
      Integer capacity,
      String location,
      String createdBy) {
    this();
    this.title = title;
    this.description = description;
    this.startAt = startAt;
    this.endAt = endAt;
    this.capacity = capacity;
    this.location = location;
    this.createdBy = createdBy;
  }

  // Getters and setters
  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public LocalDateTime getStartAt() {
    return startAt;
  }

  public void setStartAt(LocalDateTime startAt) {
    this.startAt = startAt;
  }

  public LocalDateTime getEndAt() {
    return endAt;
  }

  public void setEndAt(LocalDateTime endAt) {
    this.endAt = endAt;
  }

  public Integer getCapacity() {
    return capacity;
  }

  public void setCapacity(Integer capacity) {
    this.capacity = capacity;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  /**
   * Validates that the event times are logical (start before end).
   *
   * @return true if the event times are valid
   */
  public boolean isValidTimeRange() {
    return startAt != null && endAt != null && startAt.isBefore(endAt);
  }

  /**
   * Checks if this event conflicts with another event.
   *
   * @param other the other event to check for conflicts
   * @return true if there is a time conflict
   */
  public boolean conflictsWith(Event other) {
    if (other == null) {
      return false;
    }

    return !(this.endAt.isBefore(other.startAt) || this.startAt.isAfter(other.endAt));
  }

  @Override
  public String toString() {
    return "Event{"
        + "id='"
        + id
        + '\''
        + ", title='"
        + title
        + '\''
        + ", description='"
        + description
        + '\''
        + ", startAt="
        + startAt
        + ", endAt="
        + endAt
        + ", capacity="
        + capacity
        + ", location='"
        + location
        + '\''
        + ", createdBy='"
        + createdBy
        + '\''
        + ", createdAt="
        + createdAt
        + '}';
  }
}
