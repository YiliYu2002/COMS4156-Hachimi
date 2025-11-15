package com.example.activityscheduler.event.service;

import com.example.activityscheduler.event.model.Event;
import com.example.activityscheduler.event.repository.EventRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing Event entities and providing business logic for event operations,
 * including conflict detection and availability checking.
 */
@Service
@Transactional
public class EventService {

  private static final Logger logger = Logger.getLogger(EventService.class.getName());
  private final EventRepository eventRepository;

  /**
   * Constructs an EventService with the given repository.
   *
   * @param eventRepository the event repository
   */
  public EventService(EventRepository eventRepository) {
    this.eventRepository = eventRepository;
  }

  /**
   * Creates a new event after validating for conflicts.
   *
   * @param event the event to create
   * @return the created event
   * @throws IllegalArgumentException if there are time conflicts or invalid data
   */
  public Event createEvent(Event event) {
    logger.info("Creating new event: " + (event != null ? event.getTitle() : "null"));
    validateEvent(event);
    checkForConflicts(event, null);
    Event savedEvent = eventRepository.save(event);
    logger.info("Successfully created event with ID: " + savedEvent.getId());
    return savedEvent;
  }

  /**
   * Updates an existing event after validating for conflicts.
   *
   * @param eventId the ID of the event to update
   * @param updatedEvent the updated event data
   * @return the updated event
   * @throws IllegalArgumentException if there are time conflicts or invalid data
   */
  public Event updateEvent(String eventId, Event updatedEvent) {
    logger.info("Updating event with ID: " + eventId);
    Event existingEvent =
        eventRepository
            .findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));

    validateEvent(updatedEvent);
    checkForConflicts(updatedEvent, eventId);

    // Update the existing event with new data
    existingEvent.setTitle(updatedEvent.getTitle());
    existingEvent.setDescription(updatedEvent.getDescription());
    existingEvent.setStartAt(updatedEvent.getStartAt());
    existingEvent.setEndAt(updatedEvent.getEndAt());
    existingEvent.setCapacity(updatedEvent.getCapacity());
    existingEvent.setLocation(updatedEvent.getLocation());

    Event savedEvent = eventRepository.save(existingEvent);
    logger.info("Successfully updated event with ID: " + savedEvent.getId());
    return savedEvent;
  }

  /**
   * Retrieves an event by its ID.
   *
   * @param eventId the event ID
   * @return an Optional containing the event if found
   */
  @Transactional(readOnly = true)
  public Optional<Event> getEventById(String eventId) {
    logger.fine("Retrieving event with ID: " + eventId);
    Optional<Event> event = eventRepository.findById(eventId);
    if (event.isPresent()) {
      logger.fine("Found event: " + event.get().getTitle());
    } else {
      logger.fine("Event not found with ID: " + eventId);
    }
    return event;
  }

  /**
   * Checks for time conflicts when creating or updating an event.
   *
   * @param event the event to check for conflicts
   * @param excludeEventId optional event ID to exclude from conflict check (for updates)
   * @throws IllegalArgumentException if conflicts are found
   */
  public void checkForConflicts(Event event, String excludeEventId) {
    logger.fine(
        "Checking for conflicts for event: "
            + event.getTitle()
            + " from "
            + event.getStartAt()
            + " to "
            + event.getEndAt());
    List<Event> conflictingEvents =
        eventRepository.findConflictingEvents(event.getStartAt(), event.getEndAt(), excludeEventId);

    if (!conflictingEvents.isEmpty()) {
      logger.warning(
          "Time conflict detected for event: "
              + event.getTitle()
              + " with "
              + conflictingEvents.size()
              + " conflicting events");
      StringBuilder conflictMessage =
          new StringBuilder("Time conflict detected with existing events: ");
      for (Event conflict : conflictingEvents) {
        conflictMessage
            .append(conflict.getId())
            .append(" (")
            .append(conflict.getTitle())
            .append("), ");
      }
      throw new IllegalArgumentException(conflictMessage.toString());
    } else {
      logger.fine("No conflicts found for event: " + event.getTitle());
    }
  }

  /**
   * Finds all conflicting events for a given time range.
   *
   * @param startTime the start time to check
   * @param endTime the end time to check
   * @return a list of conflicting events
   */
  @Transactional(readOnly = true)
  public List<Event> findConflictingEvents(LocalDateTime startTime, LocalDateTime endTime) {
    logger.fine("Finding conflicting events from " + startTime + " to " + endTime);
    List<Event> conflicts = eventRepository.findConflictingEvents(startTime, endTime, null);
    logger.fine("Found " + conflicts.size() + " conflicting events");
    return conflicts;
  }

  /**
   * Deletes an event by its ID.
   *
   * @param eventId the event ID to delete
   * @throws IllegalArgumentException if the event is not found
   */
  public void deleteEvent(String eventId) {
    logger.info("Deleting event with ID: " + eventId);
    if (!eventRepository.existsById(eventId)) {
      logger.warning("Attempted to delete non-existent event with ID: " + eventId);
      throw new IllegalArgumentException("Event not found with ID: " + eventId);
    }
    eventRepository.deleteById(eventId);
    logger.info("Successfully deleted event with ID: " + eventId);
  }

  /**
   * Validates an event for basic business rules.
   *
   * @param event the event to validate
   * @throws IllegalArgumentException if the event is invalid
   */
  private void validateEvent(Event event) {
    logger.fine("Validating event: " + (event != null ? event.getTitle() : "null"));
    if (event == null) {
      logger.severe("Event validation failed: event is null");
      throw new IllegalArgumentException("Event cannot be null");
    }

    if (event.getTitle() == null || event.getTitle().trim().isEmpty()) {
      logger.severe("Event validation failed: title is null or empty");
      throw new IllegalArgumentException("Event title is required");
    }

    if (event.getStartAt() == null || event.getEndAt() == null) {
      logger.severe("Event validation failed: start or end time is null");
      throw new IllegalArgumentException("Start and end times are required");
    }

    if (!event.isValidTimeRange()) {
      logger.severe(
          "Event validation failed: invalid time range - start: "
              + event.getStartAt()
              + ", end: "
              + event.getEndAt());
      throw new IllegalArgumentException("Start time must be before end time");
    }

    if (event.getCapacity() != null && event.getCapacity() < 0) {
      logger.severe("Event validation failed: negative capacity: " + event.getCapacity());
      throw new IllegalArgumentException("Capacity must be non-negative");
    }

    logger.fine("Event validation passed for: " + event.getTitle());
  }
}
