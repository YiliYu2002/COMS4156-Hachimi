package com.example.activityscheduler.conflict.service;

import com.example.activityscheduler.attendee.model.RsvpStatus;
import com.example.activityscheduler.attendee.repository.AttendeeRepository;
import com.example.activityscheduler.event.model.Event;
import com.example.activityscheduler.event.repository.EventRepository;
import com.example.activityscheduler.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing event time conflicts. Provides business logic for detecting conflicts
 * between events.
 */
@Service
@Transactional
public class ConflictService {

  private final AttendeeRepository attendeeRepository;
  private final EventRepository eventRepository;
  private final UserRepository userRepository;
  private static final Logger logger = Logger.getLogger(ConflictService.class.getName());

  /**
   * Constructs a ConflictService with the given repositories.
   *
   * @param attendeeRepository the attendee repository
   * @param eventRepository the event repository
   * @param userRepository the user repository
   */
  public ConflictService(
      AttendeeRepository attendeeRepository,
      EventRepository eventRepository,
      UserRepository userRepository) {
    this.attendeeRepository = attendeeRepository;
    this.eventRepository = eventRepository;
    this.userRepository = userRepository;
  }

  /**
   * Finds all time conflicts among events that a user has accepted (RSVP status = YES).
   *
   * @param userId the user ID
   * @return a list of events that have time conflicts with other accepted events
   */
  @Transactional(readOnly = true)
  public List<Event> findConflictsAmongAcceptedEvents(String userId) {
    logger.info("Finding conflicts among accepted events for user: " + userId);

    if (userId == null || userId.trim().isEmpty()) {
      logger.warning("User ID cannot be null or empty");
      throw new IllegalArgumentException("User ID cannot be null or empty");
    }

    // Validate user exists
    if (userRepository.findById(userId).isEmpty()) {
      logger.warning("User not found: " + userId);
      throw new IllegalArgumentException("User not found with ID: " + userId);
    }

    // Get all events the user has accepted
    List<com.example.activityscheduler.attendee.model.Attendee> acceptedAttendees =
        attendeeRepository.findByUserIdAndRsvpStatus(userId, RsvpStatus.YES);

    if (acceptedAttendees.isEmpty()) {
      logger.info("No accepted events found for user: " + userId);
      return new ArrayList<>();
    }

    // Get all event IDs
    List<String> eventIds =
        acceptedAttendees.stream()
            .map(com.example.activityscheduler.attendee.model.Attendee::getEventId)
            .toList();

    // Fetch all events
    List<Event> acceptedEvents = new ArrayList<>();
    for (String eventId : eventIds) {
      Optional<Event> event = eventRepository.findById(eventId);
      event.ifPresent(acceptedEvents::add);
    }

    // Find conflicts
    List<Event> conflictingEvents = new ArrayList<>();
    for (int i = 0; i < acceptedEvents.size(); i++) {
      Event event1 = acceptedEvents.get(i);
      for (int j = i + 1; j < acceptedEvents.size(); j++) {
        Event event2 = acceptedEvents.get(j);
        if (event1.hasTimeConflict(event2)) {
          // Add both events if not already in the list
          if (!conflictingEvents.contains(event1)) {
            conflictingEvents.add(event1);
          }
          if (!conflictingEvents.contains(event2)) {
            conflictingEvents.add(event2);
          }
        }
      }
    }

    logger.info("Found " + conflictingEvents.size() + " conflicting events for user: " + userId);
    return conflictingEvents;
  }

  /**
   * Finds all accepted events that conflict with a pending event.
   *
   * @param userId the user ID
   * @param pendingEventId the pending event ID to check for conflicts
   * @return a list of accepted events that conflict with the pending event, empty list if no
   *     conflicts
   */
  @Transactional(readOnly = true)
  public List<Event> findConflictsWithPendingEvent(String userId, String pendingEventId) {
    logger.info(
        "Finding conflicts between pending event: "
            + pendingEventId
            + " and accepted events for user: "
            + userId);

    if (userId == null || userId.trim().isEmpty()) {
      logger.warning("User ID cannot be null or empty");
      throw new IllegalArgumentException("User ID cannot be null or empty");
    }

    // Validate user exists
    if (userRepository.findById(userId).isEmpty()) {
      logger.warning("User not found: " + userId);
      throw new IllegalArgumentException("User not found with ID: " + userId);
    }

    if (pendingEventId == null || pendingEventId.trim().isEmpty()) {
      logger.warning("Pending event ID cannot be null or empty");
      throw new IllegalArgumentException("Pending event ID cannot be null or empty");
    }

    // Get the pending event
    Optional<Event> pendingEventOpt = eventRepository.findById(pendingEventId);
    if (pendingEventOpt.isEmpty()) {
      logger.warning("Pending event not found: " + pendingEventId);
      throw new IllegalArgumentException("Pending event not found: " + pendingEventId);
    }

    Event pendingEvent = pendingEventOpt.get();

    // Get all events the user has accepted
    List<com.example.activityscheduler.attendee.model.Attendee> acceptedAttendees =
        attendeeRepository.findByUserIdAndRsvpStatus(userId, RsvpStatus.YES);

    if (acceptedAttendees.isEmpty()) {
      logger.info("No accepted events found for user: " + userId + ", no conflicts possible");
      return new ArrayList<>();
    }

    // Check for conflicts (exclude the pending event itself from the check)
    List<Event> conflictingEvents = new ArrayList<>();
    for (com.example.activityscheduler.attendee.model.Attendee attendee : acceptedAttendees) {
      // Skip if this is the pending event itself (creator was auto-added as attendee)
      if (attendee.getEventId().equals(pendingEventId)) {
        continue;
      }
      Optional<Event> acceptedEvent = eventRepository.findById(attendee.getEventId());
      if (acceptedEvent.isPresent() && pendingEvent.hasTimeConflict(acceptedEvent.get())) {
        conflictingEvents.add(acceptedEvent.get());
      }
    }

    logger.info(
        "Found "
            + conflictingEvents.size()
            + " accepted events conflicting with pending event: "
            + pendingEventId
            + " for user: "
            + userId);
    return conflictingEvents;
  }
}
