package com.example.activityscheduler.event.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * DTO for event update requests. Contains only the fields that can be updated by the client. The
 * event ID, creation timestamp, and creator are not included as they should not be changed.
 */
@Schema(description = "Event update request")
public class EventUpdateRequest {

  @NotBlank(message = "Event title is required")
  @Schema(
      description = "Event title",
      example = "Team Meeting",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String title;

  @Schema(description = "Event description", example = "Weekly team sync meeting")
  private String description;

  @NotNull(message = "Start time is required")
  @Schema(
      description = "Event start time",
      example = "2025-11-25T10:00:00",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private LocalDateTime startAt;

  @NotNull(message = "End time is required")
  @Schema(
      description = "Event end time (must be after start time)",
      example = "2025-11-25T11:00:00",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private LocalDateTime endAt;

  @Min(value = 0, message = "Capacity must be non-negative")
  @Schema(description = "Event capacity (maximum number of attendees)", example = "50")
  private Integer capacity;

  @NotBlank(message = "Organization ID is required")
  @Schema(
      description = "Organization ID this event belongs to",
      example = "org-123",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String orgId;

  /** Default constructor. */
  public EventUpdateRequest() {}

  /**
   * Constructs an EventUpdateRequest with the specified parameters.
   *
   * @param title the event title
   * @param description the event description
   * @param startAt the event start time
   * @param endAt the event end time
   * @param capacity the event capacity
   * @param orgId the organization ID
   */
  public EventUpdateRequest(
      String title,
      String description,
      LocalDateTime startAt,
      LocalDateTime endAt,
      Integer capacity,
      String orgId) {
    this.title = title;
    this.description = description;
    this.startAt = startAt;
    this.endAt = endAt;
    this.capacity = capacity;
    this.orgId = orgId;
  }

  // Getters and setters
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

  public String getOrgId() {
    return orgId;
  }

  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }

  @Override
  public String toString() {
    return "EventUpdateRequest{"
        + "title='"
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
        + ", orgId='"
        + orgId
        + '\''
        + '}';
  }
}
