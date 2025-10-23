package com.example.activityscheduler.event.service;

import com.example.activityscheduler.event.model.Event;
import com.example.activityscheduler.event.repository.EventRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing Event entities and providing business logic for event operations,
 * including conflict detection and availability checking.
 */
@Service
@Transactional
public class EventService {

  private static final Logger logger = LoggerFactory.getLogger(EventService.class);
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
    logger.info("Creating new event: {}", event != null ? event.getTitle() : "null");
    validateEvent(event);
    checkForConflicts(event, null);
    Event savedEvent = eventRepository.save(event);
    logger.info("Successfully created event with ID: {}", savedEvent.getId());
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
    logger.info("Updating event with ID: {}", eventId);
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
    logger.info("Successfully updated event with ID: {}", savedEvent.getId());
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
    logger.debug("Retrieving event with ID: {}", eventId);
    Optional<Event> event = eventRepository.findById(eventId);
    if (event.isPresent()) {
      logger.debug("Found event: {}", event.get().getTitle());
    } else {
      logger.debug("Event not found with ID: {}", eventId);
    }
    return event;
  }

  /**
   * Retrieves all events in the system.
   *
   * @return a list of all events
   */
  @Transactional(readOnly = true)
  public List<Event> getAllEvents() {
    logger.debug("Retrieving all events");
    List<Event> events = eventRepository.findAll();
    logger.debug("Found {} events", events.size());
    return events;
  }

  /**
   * Checks for time conflicts when creating or updating an event.
   *
   * @param event the event to check for conflicts
   * @param excludeEventId optional event ID to exclude from conflict check (for updates)
   * @throws IllegalArgumentException if conflicts are found
   */
  public void checkForConflicts(Event event, String excludeEventId) {
    logger.debug(
        "Checking for conflicts for event: {} from {} to {}",
        event.getTitle(),
        event.getStartAt(),
        event.getEndAt());
    List<Event> conflictingEvents =
        eventRepository.findConflictingEvents(event.getStartAt(), event.getEndAt(), excludeEventId);

    if (!conflictingEvents.isEmpty()) {
      logger.warn(
          "Time conflict detected for event: {} with {} conflicting events",
          event.getTitle(),
          conflictingEvents.size());
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
      logger.debug("No conflicts found for event: {}", event.getTitle());
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
    logger.debug("Finding conflicting events from {} to {}", startTime, endTime);
    List<Event> conflicts = eventRepository.findConflictingEvents(startTime, endTime, null);
    logger.debug("Found {} conflicting events", conflicts.size());
    return conflicts;
  }

  /**
   * Deletes an event by its ID.
   *
   * @param eventId the event ID to delete
   * @throws IllegalArgumentException if the event is not found
   */
  public void deleteEvent(String eventId) {
    logger.info("Deleting event with ID: {}", eventId);
    if (!eventRepository.existsById(eventId)) {
      logger.warn("Attempted to delete non-existent event with ID: {}", eventId);
      throw new IllegalArgumentException("Event not found with ID: " + eventId);
    }
    eventRepository.deleteById(eventId);
    logger.info("Successfully deleted event with ID: {}", eventId);
  }

  /**
   * Validates an event for basic business rules.
   *
   * @param event the event to validate
   * @throws IllegalArgumentException if the event is invalid
   */
  private void validateEvent(Event event) {
    logger.debug("Validating event: {}", event != null ? event.getTitle() : "null");
    if (event == null) {
      logger.error("Event validation failed: event is null");
      throw new IllegalArgumentException("Event cannot be null");
    }

    if (event.getTitle() == null || event.getTitle().trim().isEmpty()) {
      logger.error("Event validation failed: title is null or empty");
      throw new IllegalArgumentException("Event title is required");
    }

    if (event.getStartAt() == null || event.getEndAt() == null) {
      logger.error("Event validation failed: start or end time is null");
      throw new IllegalArgumentException("Start and end times are required");
    }

    if (!event.isValidTimeRange()) {
      logger.error(
          "Event validation failed: invalid time range - start: {}, end: {}",
          event.getStartAt(),
          event.getEndAt());
      throw new IllegalArgumentException("Start time must be before end time");
    }

    if (event.getCapacity() != null && event.getCapacity() < 0) {
      logger.error("Event validation failed: negative capacity: {}", event.getCapacity());
      throw new IllegalArgumentException("Capacity must be non-negative");
    }

    logger.debug("Event validation passed for: {}", event.getTitle());
  }
}
