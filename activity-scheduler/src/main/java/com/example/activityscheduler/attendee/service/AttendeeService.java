package com.example.activityscheduler.attendee.service;

import com.example.activityscheduler.attendee.model.Attendee;
import com.example.activityscheduler.attendee.model.AttendeeId;
import com.example.activityscheduler.attendee.model.RsvpStatus;
import com.example.activityscheduler.attendee.repository.AttendeeRepository;
import com.example.activityscheduler.user.repository.UserRepository;
import com.example.activityscheduler.organization.repository.OrganizationRepository;
// import com.example.activityscheduler.event.repository.EventRepository;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing Attendee entities. Provides business logic for attendee operations
 * including CRUD operations, RSVP status management, and validation.
 */
@Service
@Transactional
public class AttendeeService {

  private final AttendeeRepository attendeeRepository;
  private final UserRepository userRepository;
  private final OrganizationRepository organizationRepository;
  // private final EventRepository eventRepository;
  private static final Logger logger = Logger.getLogger(AttendeeService.class.getName());

  /**
   * Constructs an AttendeeService with the given repository.
   *
   * @param attendeeRepository the attendee repository
   * @param userRepository the user repository
   * @param organizationRepository the organization repository
   */
  public AttendeeService(AttendeeRepository attendeeRepository, UserRepository userRepository, OrganizationRepository organizationRepository) { //TODO: Added EventRepository to parameter list
    this.attendeeRepository = attendeeRepository;
    this.userRepository = userRepository;
    this.organizationRepository = organizationRepository;
    // this.eventRepository = eventRepository
  }

  /**
   * Retrieves an attendee by event ID and user ID.
   *
   * @param eventId the event ID
   * @param userId the user ID
   * @return an Optional containing the attendee if found, empty otherwise
   */
  @Transactional(readOnly = true)
  public Optional<Attendee> getAttendee(String eventId, String userId) {
    logger.info("Retrieving attendee for event: " + eventId + " and user: " + userId);
    Optional<Attendee> attendee = attendeeRepository.findByEventIdAndUserId(eventId, userId);
    if (attendee.isPresent()) {
      logger.info(
          "Attendee found: event: "
              + attendee.get().getEventId()
              + " and user: "
              + attendee.get().getUserId());
    } else {
      logger.info("Attendee not found for event: " + eventId + " and user: " + userId);
    }
    return attendee;
  }

  /**
   * Retrieves all attendees for a specific event.
   *
   * @param eventId the event ID
   * @return a list of attendees for the event
   */
  @Transactional(readOnly = true)
  public List<Attendee> getAttendeesByEvent(String eventId) {
    logger.info("Retrieving attendees for event: " + eventId);
    List<Attendee> attendees = attendeeRepository.findByEventId(eventId);
    logger.info("Retrieved " + attendees.size() + " attendees for event: " + eventId);
    return attendees;
  }

  /**
   * Creates a new attendee (event invitation).
   *
   * @param eventId the event ID
   * @param userId the user ID
   * @param rsvpStatus the RSVP status (defaults to PENDING if null)
   * @return the created attendee
   * @throws IllegalArgumentException if event ID or user ID is null or empty
   * @throws IllegalStateException if attendee already exists
   */
  public Attendee createAttendee(String eventId, String userId, RsvpStatus rsvpStatus) {

    if (userId == null || userId.trim().isEmpty()) {
      logger.warning("User ID cannot be null or empty");
      throw new IllegalArgumentException("User ID cannot be null or empty");
    }

    if (userRepository.findById(userId).isEmpty()) {
      logger.warning("User not found for attendee creation: " + userId);
      throw new IllegalArgumentException("User not found");
    }

    // retrieve the org_id of the event, then check if user_id in this org at this line

    if (eventId == null || eventId.trim().isEmpty()) {
      logger.warning("Event ID cannot be null or empty");
      throw new IllegalArgumentException("Event ID cannot be null or empty");
    }

    if (attendeeRepository.existsByEventIdAndUserId(eventId, userId)) {
      logger.warning("Attendee already exists for event " + eventId + " and user " + userId);
      throw new IllegalStateException(
          "Attendee already exists for event " + eventId + " and user " + userId);
    }

    RsvpStatus status = (rsvpStatus != null) ? rsvpStatus : RsvpStatus.PENDING;
    Attendee attendee = new Attendee(eventId, userId, status);
    logger.info(
        "Inviting attendee for event: "
            + eventId
            + " and user: "
            + userId
            + " with RSVP status: "
            + status);
    return attendeeRepository.save(attendee);
  }

  /**
   * Creates a new attendee with PENDING RSVP status (invitation).
   *
   * @param eventId the event ID
   * @param userId the user ID
   * @return the created attendee
   */
  public Attendee createAttendee(String eventId, String userId) {
    logger.info("Creating attendee for event: " + eventId + " and user: " + userId);
    return createAttendee(eventId, userId, RsvpStatus.PENDING);
  }

  /**
   * Updates the RSVP status of an existing attendee.
   *
   * @param eventId the event ID
   * @param userId the user ID
   * @param newRsvpStatus the new RSVP status
   * @param requestUserId the user ID of the user making the request
   * @return the updated attendee
   * @throws IllegalArgumentException if event ID, user ID, or RSVP status is null
   * @throws IllegalStateException if attendee is not found
   */
  public Attendee updateRsvpStatus(String eventId, String userId, String requestUserId, RsvpStatus newRsvpStatus) {
    if (eventId == null || eventId.trim().isEmpty()) {
      logger.warning("Event ID cannot be null or empty");
      throw new IllegalArgumentException("Event ID cannot be null or empty");
    }

    if (userId == null || userId.trim().isEmpty()) {
      logger.warning("User ID cannot be null or empty");
      throw new IllegalArgumentException("User ID cannot be null or empty");
    }

    if (requestUserId == null || requestUserId.trim().isEmpty()) {
      logger.warning("Request user ID cannot be null or empty");
      throw new IllegalArgumentException("Request user ID cannot be null or empty");
    }

    if (!requestUserId.equals(userId)) {
      logger.warning("Request user ID does not match user ID");
      throw new IllegalArgumentException("Request user ID does not match user ID");
    }

    if (newRsvpStatus == null) {
      logger.warning("RSVP status cannot be null");
      throw new IllegalArgumentException("RSVP status cannot be null");
    }

    Attendee attendee =
        attendeeRepository
            .findByEventIdAndUserId(eventId, userId)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Attendee not found for event " + eventId + " and user " + userId));

    attendee.setRsvpStatus(newRsvpStatus);
    logger.info(
        "Updating RSVP status for event: "
            + eventId
            + " and user: "
            + userId
            + " to: "
            + newRsvpStatus);
    return attendeeRepository.save(attendee);
  }

  /**
   * Deletes an attendee (removes invitation).
   *
   * @param eventId the event ID
   * @param userId the user ID
   * @throws IllegalArgumentException if event ID or user ID is null or empty
   * @throws IllegalStateException if attendee is not found
   */
  public void deleteAttendee(String eventId, String userId, String requestUserId) {
    if (eventId == null || eventId.trim().isEmpty()) {
      logger.warning("Event ID cannot be null or empty");
      throw new IllegalArgumentException("Event ID cannot be null or empty");
    }

    // String eventCreatorId = eventRepository.findById(eventId).get().getCreatorId();

    // if (!eventCreatorId.equals(requestUserId)) {
    //   logger.warning("Only the event creator can delete an attendee");
    //   throw new IllegalArgumentException("Request user ID does not match event creator ID");
    // }

    if (userId == null || userId.trim().isEmpty()) {
      logger.warning("User ID cannot be null or empty");
      throw new IllegalArgumentException("User ID cannot be null or empty");
    }

    AttendeeId attendeeId = new AttendeeId(eventId, userId);
    if (!attendeeRepository.existsById(attendeeId)) {
      logger.warning("Attendee not found for event " + eventId + " and user " + userId);
      throw new IllegalStateException(
          "Attendee not found for event " + eventId + " and user " + userId);
    }

    attendeeRepository.deleteById(attendeeId);
    logger.info("Deleted attendee for event: " + eventId + " and user: " + userId);
  }

  /**
   * Checks if an attendee exists for the given event and user.
   *
   * @param eventId the event ID
   * @param userId the user ID
   * @return true if attendee exists, false otherwise
   */
  @Transactional(readOnly = true)
  public boolean existsAttendee(String eventId, String userId) {
    logger.info("Checking if attendee exists for event: " + eventId + " and user: " + userId);
    boolean exists = attendeeRepository.existsByEventIdAndUserId(eventId, userId);
    logger.info("Attendee exists: " + exists);
    return exists;
  }

  /**
   * Counts the number of attendees for an event.
   *
   * @param eventId the event ID
   * @return the count of attendees
   */
  @Transactional(readOnly = true)
  public long countAttendeesByEvent(String eventId) {
    logger.info("Counting attendees for event: " + eventId);
    long count = attendeeRepository.countByEventId(eventId);
    logger.info("Attendee count for event " + eventId + ": " + count);
    return count;
  }

  /**
   * Counts the number of attendees for an event with a specific RSVP status.
   *
   * @param eventId the event ID
   * @param rsvpStatus the RSVP status
   * @return the count of attendees with the specified status
   */
  @Transactional(readOnly = true)
  public long countAttendeesByEventAndStatus(String eventId, RsvpStatus rsvpStatus) {
    logger.info("Counting attendees for event: " + eventId + " with RSVP status: " + rsvpStatus);
    long count = attendeeRepository.countByEventIdAndRsvpStatus(eventId, rsvpStatus);
    logger.info(
        "Attendee count for event " + eventId + " with RSVP status " + rsvpStatus + ": " + count);
    return count;
  }


}
