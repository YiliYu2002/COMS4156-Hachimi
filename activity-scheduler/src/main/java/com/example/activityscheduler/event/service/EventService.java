package com.example.activityscheduler.event.service;

import com.example.activityscheduler.event.model.Event;
import com.example.activityscheduler.event.repository.EventRepository;
import com.example.activityscheduler.organization.service.OrganizationService;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing Event entities and providing business logic for event operations.
 * Events are associated with organizations and validated against organization existence.
 */
@Service
@Transactional
public class EventService {

  private static final Logger logger = Logger.getLogger(EventService.class.getName());
  private final EventRepository eventRepository;
  private final OrganizationService organizationService;

  /**
   * Constructs an EventService with the given repository and organization service.
   *
   * @param eventRepository the event repository
   * @param organizationService the organization service
   */
  public EventService(EventRepository eventRepository, OrganizationService organizationService) {
    this.eventRepository = eventRepository;
    this.organizationService = organizationService;
  }

  /**
   * Creates a new event after validating organization exists.
   *
   * @param event the event to create
   * @return the created event
   * @throws IllegalArgumentException if organization does not exist or invalid data
   */
  public Event createEvent(Event event) {
    logger.info("Creating new event: " + (event != null ? event.getTitle() : "null"));
    validateEvent(event);
    if (event != null) {
      validateOrganizationExists(event.getOrgId());
      Event savedEvent = eventRepository.save(event);
      logger.info("Successfully created event with ID: " + savedEvent.getId());
      return savedEvent;
    }
    throw new IllegalArgumentException("Event cannot be null");
  }

  /**
   * Updates an existing event after validating organization exists.
   *
   * @param eventId the ID of the event to update
   * @param updatedEvent the updated event data
   * @return the updated event
   * @throws IllegalArgumentException if organization does not exist or invalid data
   */
  public Event updateEvent(String eventId, Event updatedEvent) {
    logger.info("Updating event with ID: " + eventId);
    Event existingEvent =
        eventRepository
            .findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));

    validateEvent(updatedEvent);
    validateOrganizationExists(updatedEvent.getOrgId());

    // Update the existing event with new data
    existingEvent.setTitle(updatedEvent.getTitle());
    existingEvent.setDescription(updatedEvent.getDescription());
    existingEvent.setStartAt(updatedEvent.getStartAt());
    existingEvent.setEndAt(updatedEvent.getEndAt());
    existingEvent.setCapacity(updatedEvent.getCapacity());
    existingEvent.setLocation(updatedEvent.getLocation());
    existingEvent.setOrgId(updatedEvent.getOrgId());
    existingEvent.setCreatedBy(updatedEvent.getCreatedBy());

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
   * Retrieves all events belonging to a specific organization.
   *
   * @param orgId the organization ID
   * @return a list of events belonging to the organization
   * @throws IllegalArgumentException if organization does not exist
   */
  @Transactional(readOnly = true)
  public List<Event> getEventsByOrganization(String orgId) {
    logger.info("Retrieving events for organization: " + orgId);
    validateOrganizationExists(orgId);
    List<Event> events = eventRepository.findByOrgId(orgId);
    logger.info("Retrieved " + events.size() + " events for organization: " + orgId);
    return events;
  }

  /**
   * Retrieves all events created by a specific user.
   *
   * @param userId the user ID
   * @return a list of events created by the user
   */
  @Transactional(readOnly = true)
  public List<Event> getEventsByUser(String userId) {
    logger.info("Retrieving events created by user: " + userId);
    List<Event> events = eventRepository.findByCreatedBy(userId);
    logger.info("Retrieved " + events.size() + " events created by user: " + userId);
    return events;
  }

  /**
   * Retrieves all events belonging to a specific organization and created by a specific user.
   *
   * @param orgId the organization ID
   * @param userId the user ID
   * @return a list of events matching the criteria
   * @throws IllegalArgumentException if organization does not exist
   */
  @Transactional(readOnly = true)
  public List<Event> getEventsByOrganizationAndUser(String orgId, String userId) {
    logger.info("Retrieving events for organization: " + orgId + " and user: " + userId);
    validateOrganizationExists(orgId);
    List<Event> events = eventRepository.findByOrgIdAndCreatedBy(orgId, userId);
    logger.info(
        "Retrieved "
            + events.size()
            + " events for organization: "
            + orgId
            + " and user: "
            + userId);
    return events;
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

    if (event.getOrgId() == null || event.getOrgId().trim().isEmpty()) {
      logger.severe("Event validation failed: organization ID is null or empty");
      throw new IllegalArgumentException("Organization ID is required");
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

  /**
   * Validates that an organization exists.
   *
   * @param orgId the organization ID to validate
   * @throws IllegalArgumentException if the organization does not exist
   */
  private void validateOrganizationExists(String orgId) {
    logger.fine("Validating organization exists: " + orgId);
    if (orgId == null || orgId.trim().isEmpty()) {
      logger.severe("Organization validation failed: organization ID is null or empty");
      throw new IllegalArgumentException("Organization ID cannot be null or empty");
    }

    if (organizationService.getOrganizationById(orgId).isEmpty()) {
      logger.severe(
          "Organization validation failed: organization with ID '" + orgId + "' does not exist");
      throw new IllegalArgumentException("Organization with ID '" + orgId + "' does not exist");
    }

    logger.fine("Organization validation passed for: " + orgId);
  }
}
