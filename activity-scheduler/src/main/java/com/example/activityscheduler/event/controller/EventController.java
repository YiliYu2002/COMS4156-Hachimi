package com.example.activityscheduler.event.controller;

import com.example.activityscheduler.event.model.Event;
import com.example.activityscheduler.event.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST controller for managing Event entities. Provides HTTP endpoints for event operations
 * including CRUD operations, conflict detection, and availability checking.
 */
@RestController
@RequestMapping("/api/events")
@Tag(name = "Event Management", description = "APIs for managing events and scheduling")
public class EventController {

  private final EventService eventService;

  /**
   * Constructs an EventController with the given service.
   *
   * @param eventService the event service
   */
  public EventController(EventService eventService) {
    this.eventService = eventService;
  }

  /**
   * Creates a new event.
   *
   * @param event the event to create
   * @return the created event
   */
  @Operation(summary = "Create a new event", description = "Creates a new event in the system")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Event created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid event data or time conflicts"),
        @ApiResponse(responseCode = "409", description = "Time conflict detected")
      })
  @PostMapping
  public ResponseEntity<Event> createEvent(@Valid @RequestBody Event event) {
    try {
      Event createdEvent = eventService.createEvent(event);
      return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  /**
   * Retrieves an event by its ID.
   *
   * @param id the event ID
   * @return the event if found
   */
  @Operation(
      summary = "Get event by ID",
      description = "Retrieves a specific event by its unique identifier")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Event found successfully"),
        @ApiResponse(responseCode = "404", description = "Event not found")
      })
  @GetMapping("/{id}")
  public ResponseEntity<Event> getEventById(
      @Parameter(description = "Event ID") @PathVariable String id) {
    Optional<Event> event = eventService.getEventById(id);
    return event.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  /**
   * Updates an existing event.
   *
   * @param id the event ID
   * @param event the updated event data
   * @return the updated event
   */
  @Operation(summary = "Update an event", description = "Updates an existing event with new data")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Event updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid event data or time conflicts"),
        @ApiResponse(responseCode = "404", description = "Event not found")
      })
  @PutMapping("/{id}")
  public ResponseEntity<Event> updateEvent(
      @Parameter(description = "Event ID") @PathVariable String id,
      @Valid @RequestBody Event event) {
    try {
      Event updatedEvent = eventService.updateEvent(id, event);
      return ResponseEntity.ok(updatedEvent);
    } catch (IllegalArgumentException e) {
      if (e.getMessage().contains("not found")) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
      }
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  /**
   * Deletes an event by its ID.
   *
   * @param id the event ID
   * @return no content if successful
   */
  @Operation(summary = "Delete an event", description = "Deletes an event from the system")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Event deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Event not found")
      })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteEvent(
      @Parameter(description = "Event ID") @PathVariable String id) {
    try {
      eventService.deleteEvent(id);
      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    }
  }

  /**
   * Retrieves all events in the system.
   *
   * @return a list of all events
   */
  @Operation(summary = "Get all events", description = "Retrieves all events in the system")
  @ApiResponses(
      value = {@ApiResponse(responseCode = "200", description = "Events retrieved successfully")})
  @GetMapping
  public List<Event> getAllEvents() {
    return eventService.getAllEvents();
  }

  /**
   * Checks for time conflicts in a specific time range.
   *
   * @param startTime the start time
   * @param endTime the end time
   * @return a list of conflicting events
   */
  @Operation(
      summary = "Check for time conflicts",
      description = "Finds all events that conflict with a given time range")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Conflict check completed successfully")
      })
  @GetMapping("/conflicts")
  public List<Event> checkForConflicts(
      @Parameter(description = "Start time (ISO format)")
          @RequestParam
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime startTime,
      @Parameter(description = "End time (ISO format)")
          @RequestParam
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime endTime) {
    return eventService.findConflictingEvents(startTime, endTime);
  }
}
