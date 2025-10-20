package com.example.activityscheduler.event.service;

import com.example.activityscheduler.event.model.Event;
import com.example.activityscheduler.event.repository.EventRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing Event entities and providing business logic for event operations,
 * including conflict detection and availability checking.
 */
@Service
@Transactional
public class EventService {

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
    validateEvent(event);
    checkForConflicts(event, null);
    return eventRepository.save(event);
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

    return eventRepository.save(existingEvent);
  }

  /**
   * Retrieves an event by its ID.
   *
   * @param eventId the event ID
   * @return an Optional containing the event if found
   */
  @Transactional(readOnly = true)
  public Optional<Event> getEventById(String eventId) {
    return eventRepository.findById(eventId);
  }

  /**
   * Retrieves all events in the system.
   *
   * @return a list of all events
   */
  @Transactional(readOnly = true)
  public List<Event> getAllEvents() {
    return eventRepository.findAll();
  }

  /**
   * Checks for time conflicts when creating or updating an event.
   *
   * @param event the event to check for conflicts
   * @param excludeEventId optional event ID to exclude from conflict check (for updates)
   * @throws IllegalArgumentException if conflicts are found
   */
  public void checkForConflicts(Event event, String excludeEventId) {
    List<Event> conflictingEvents =
        eventRepository.findConflictingEvents(event.getStartAt(), event.getEndAt(), excludeEventId);

    if (!conflictingEvents.isEmpty()) {
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
    return eventRepository.findConflictingEvents(startTime, endTime, null);
  }

  /**
   * Deletes an event by its ID.
   *
   * @param eventId the event ID to delete
   * @throws IllegalArgumentException if the event is not found
   */
  public void deleteEvent(String eventId) {
    if (!eventRepository.existsById(eventId)) {
      throw new IllegalArgumentException("Event not found with ID: " + eventId);
    }
    eventRepository.deleteById(eventId);
  }

  /**
   * Validates an event for basic business rules.
   *
   * @param event the event to validate
   * @throws IllegalArgumentException if the event is invalid
   */
  private void validateEvent(Event event) {
    if (event == null) {
      throw new IllegalArgumentException("Event cannot be null");
    }

    if (event.getTitle() == null || event.getTitle().trim().isEmpty()) {
      throw new IllegalArgumentException("Event title is required");
    }

    if (event.getStartAt() == null || event.getEndAt() == null) {
      throw new IllegalArgumentException("Start and end times are required");
    }

    if (!event.isValidTimeRange()) {
      throw new IllegalArgumentException("Start time must be before end time");
    }

    if (event.getCapacity() != null && event.getCapacity() < 0) {
      throw new IllegalArgumentException("Capacity must be non-negative");
    }
  }
}
