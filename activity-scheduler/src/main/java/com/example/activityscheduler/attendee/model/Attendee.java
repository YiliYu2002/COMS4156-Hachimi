package com.example.activityscheduler.attendee.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * Attendee entity representing the relationship between events and users. Maps to the
 * 'event_attendees' table in the database with composite primary key.
 */
@Entity
@Table(name = "event_attendees")
@IdClass(AttendeeId.class)
public class Attendee {

  @Id
  @Column(name = "event_id", length = 36, nullable = false)
  private String eventId;

  @Id
  @Column(name = "user_id", length = 36, nullable = false)
  private String userId;

  @Enumerated(EnumType.STRING)
  @Column(name = "rsvp_status", nullable = false)
  private RsvpStatus rsvpStatus = RsvpStatus.PENDING;

  /** Default constructor. */
  public Attendee() {}

  /**
   * Constructs an Attendee with the specified event ID, user ID, and RSVP status.
   *
   * @param eventId the event ID
   * @param userId the user ID
   * @param rsvpStatus the RSVP status
   */
  public Attendee(String eventId, String userId, RsvpStatus rsvpStatus) {
    this.eventId = eventId;
    this.userId = userId;
    this.rsvpStatus = rsvpStatus;
  }

  // Getters and Setters
  public String getEventId() {
    return eventId;
  }

  public String getUserId() {
    return userId;
  }

  public RsvpStatus getRsvpStatus() {
    return rsvpStatus;
  }

  public void setRsvpStatus(RsvpStatus rsvpStatus) {
    this.rsvpStatus = rsvpStatus;
  }
}
