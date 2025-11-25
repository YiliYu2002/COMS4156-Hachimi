package com.example.activityscheduler.attendee.repository;

import com.example.activityscheduler.attendee.model.Attendee;
import com.example.activityscheduler.attendee.model.AttendeeId;
import com.example.activityscheduler.attendee.model.RsvpStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Attendee entity operations. Provides data access methods for Attendee
 * entities using Spring Data JPA with composite primary key support.
 */
@Repository
public interface AttendeeRepository extends JpaRepository<Attendee, AttendeeId> {

  /**
   * Finds all attendees for a specific event.
   *
   * @param eventId the event ID
   * @return a list of attendees for the event
   */
  List<Attendee> findByEventId(String eventId);

  /**
   * Finds all attendees for a specific user.
   *
   * @param userId the user ID
   * @return a list of attendees for the user
   */
  List<Attendee> findByUserId(String userId);

  /**
   * Finds a specific attendee by event ID and user ID.
   *
   * @param eventId the event ID
   * @param userId the user ID
   * @return an Optional containing the attendee if found, empty otherwise
   */
  Optional<Attendee> findByEventIdAndUserId(String eventId, String userId);

  /**
   * Finds all attendees with a specific RSVP status.
   *
   * @param rsvpStatus the RSVP status
   * @return a list of attendees with the specified status
   */
  List<Attendee> findByRsvpStatus(RsvpStatus rsvpStatus);

  /**
   * Finds all attendees for an event with a specific RSVP status.
   *
   * @param eventId the event ID
   * @param rsvpStatus the RSVP status
   * @return a list of attendees for the event with the specified status
   */
  List<Attendee> findByEventIdAndRsvpStatus(String eventId, RsvpStatus rsvpStatus);

  /**
   * Finds all attendees for a user with a specific RSVP status.
   *
   * @param userId the user ID
   * @param rsvpStatus the RSVP status
   * @return a list of attendees for the user with the specified status
   */
  List<Attendee> findByUserIdAndRsvpStatus(String userId, RsvpStatus rsvpStatus);

  /**
   * Checks if an attendee exists for the given event and user.
   *
   * @param eventId the event ID
   * @param userId the user ID
   * @return true if an attendee exists, false otherwise
   */
  boolean existsByEventIdAndUserId(String eventId, String userId);

  /**
   * Counts the number of attendees for an event.
   *
   * @param eventId the event ID
   * @return the count of attendees
   */
  @Query("SELECT COUNT(a) FROM Attendee a WHERE a.eventId = :eventId")
  long countByEventId(@Param("eventId") String eventId);

  /**
   * Counts the number of attendees for an event with a specific RSVP status.
   *
   * @param eventId the event ID
   * @param rsvpStatus the RSVP status
   * @return the count of attendees with the specified status
   */
  @Query(
      "SELECT COUNT(a) FROM Attendee a WHERE a.eventId = :eventId AND a.rsvpStatus = :rsvpStatus")
  long countByEventIdAndRsvpStatus(
      @Param("eventId") String eventId, @Param("rsvpStatus") RsvpStatus rsvpStatus);

  /**
   * Counts the number of attendees for a user across all events.
   *
   * @param userId the user ID
   * @return the count of attendees for the user
   */
  @Query("SELECT COUNT(a) FROM Attendee a WHERE a.userId = :userId")
  long countByUserId(@Param("userId") String userId);
}
