package com.example.activityscheduler.attendee.controller;

import com.example.activityscheduler.attendee.model.Attendee;
import com.example.activityscheduler.attendee.model.RsvpStatus;
import com.example.activityscheduler.attendee.service.AttendeeService;
import com.example.activityscheduler.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST controller for managing Attendee entities. Provides HTTP endpoints for event invitation
 * operations including CRUD operations and RSVP status management.
 */
@RestController
@RequestMapping("/api/attendees")
@Tag(
    name = "Event Attendee Management",
    description = "APIs for managing event invitations and RSVP status")
public class AttendeeController {

  private final AttendeeService attendeeService;
  private final UserRepository userRepository;
  private static final Logger logger = Logger.getLogger(AttendeeController.class.getName());

  /**
   * Constructs an AttendeeController with the given service.
   *
   * @param attendeeService the attendee service
   * @param userRepository the user repository
   */
  public AttendeeController(AttendeeService attendeeService, UserRepository userRepository) {
    this.attendeeService = attendeeService;
    this.userRepository = userRepository;
  }

  /**
   * Retrieves a specific attendee by event ID and user ID.
   *
   * @param eventId the event ID
   * @param userId the user ID
   * @return an Optional containing the attendee if found, empty otherwise
   */
  @Operation(
      summary = "Get attendee by event and user",
      description = "Retrieves a specific attendee by event ID and user ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Attendee found successfully"),
        @ApiResponse(responseCode = "404", description = "Attendee not found")
      })
  @GetMapping("/{eventId}/{userId}")
  public Optional<Attendee> getAttendee(
      @Parameter(description = "Event ID") @PathVariable String eventId,
      @Parameter(description = "User ID") @PathVariable String userId) {
    logger.info("Retrieving attendee for event: " + eventId + " and user: " + userId);
    Optional<Attendee> attendee = attendeeService.getAttendee(eventId, userId);
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
  @Operation(
      summary = "Get attendees by event",
      description = "Retrieves all attendees for a specific event")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved event attendees")
      })
  @GetMapping("/event/{eventId}")
  public List<Attendee> getAttendeesByEvent(
      @Parameter(description = "Event ID") @PathVariable String eventId) {
    logger.info("Retrieving attendees for event: " + eventId);
    List<Attendee> attendees = attendeeService.getAttendeesByEvent(eventId);
    logger.info("Retrieved " + attendees.size() + " attendees for event: " + eventId);
    return attendees;
  }

  /**
   * Creates a new attendee (event invitation).
   *
   * @param attendeeRequest the attendee creation request
   * @return the created attendee
   */
  @Operation(
      summary = "Create a new event invitation",
      description = "Creates a new event invitation for a user")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Event invitation created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid invitation data"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "409", description = "Invitation already exists")
      })
  @PostMapping
  public Attendee createAttendee(@RequestBody AttendeeRequest attendeeRequest) {
    if (attendeeRequest == null
        || attendeeRequest.getEventId() == null
        || attendeeRequest.getUserId() == null) {
      logger.warning("Invalid attendee creation request: missing required fields");
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid invitation data");
    }

    logger.info(
        "Creating attendee for event: "
            + attendeeRequest.getEventId()
            + " and user: "
            + attendeeRequest.getUserId()
            + " with RSVP status: "
            + attendeeRequest.getRsvpStatus());

    // Validate user exists
    if (userRepository.findById(attendeeRequest.getUserId()).isEmpty()) {
      logger.warning("User not found for attendee creation: " + attendeeRequest.getUserId());
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
    }

    try {
      Attendee createdAttendee =
          attendeeService.createAttendee(
              attendeeRequest.getEventId(),
              attendeeRequest.getUserId(),
              attendeeRequest.getRsvpStatus());
      logger.info(
          "Successfully created attendee for event: "
              + attendeeRequest.getEventId()
              + " and user: "
              + attendeeRequest.getUserId());
      return createdAttendee;
    } catch (IllegalArgumentException e) {
      logger.warning("Bad request for attendee creation: " + e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    } catch (IllegalStateException e) {
      logger.warning("Conflict during attendee creation: " + e.getMessage());
      throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
    }
  }

  /**
   * Updates the RSVP status of an existing attendee.
   *
   * @param eventId the event ID
   * @param userId the user ID
   * @param rsvpUpdate the RSVP status update request
   * @return the updated attendee
   */
  @Operation(
      summary = "Update RSVP status",
      description = "Updates the RSVP status of an existing event invitation")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "RSVP status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid RSVP status data"),
        @ApiResponse(responseCode = "404", description = "Attendee not found")
      })
  @PutMapping("/{eventId}/{userId}/rsvp")
  public Attendee updateRsvpStatus(
      @Parameter(description = "Event ID") @PathVariable String eventId,
      @Parameter(description = "User ID") @PathVariable String userId,
      @Parameter(description = "User ID of the request sender") @RequestHeader("X-User-Id")
          String requestUserId,
      @RequestBody RsvpUpdateRequest rsvpUpdate) {

    if (rsvpUpdate == null || rsvpUpdate.getRsvpStatus() == null) {
      logger.warning("Invalid RSVP update request: RSVP status is null");
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid RSVP status data");
    }

    if (requestUserId == null || requestUserId.trim().isEmpty()) {
      logger.warning("Request user ID header (X-User-Id) is missing or empty");
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Request user ID header is required");
    }

    logger.info(
        "Updating RSVP status for event: "
            + eventId
            + " and user: "
            + userId
            + " by request user: "
            + requestUserId
            + " to status: "
            + rsvpUpdate.getRsvpStatus());

    // Validate user exists
    if (userRepository.findById(userId).isEmpty()) {
      logger.warning("User not found for RSVP status update: " + userId);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
    }

    // Validate request user exists
    if (userRepository.findById(requestUserId).isEmpty()) {
      logger.warning("Request user not found: " + requestUserId);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Request user not found");
    }

    try {
      Attendee updatedAttendee =
          attendeeService.updateRsvpStatus(
              eventId, userId, requestUserId, rsvpUpdate.getRsvpStatus());
      logger.info(
          "Successfully updated RSVP status for event: " + eventId + " and user: " + userId);
      return updatedAttendee;
    } catch (IllegalArgumentException e) {
      logger.warning("Bad request for RSVP status update: " + e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    } catch (IllegalStateException e) {
      logger.warning("Attendee not found for RSVP status update: " + e.getMessage());
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    }
  }

  /**
   * Deletes an attendee (removes invitation).
   *
   * @param eventId the event ID
   * @param userId the user ID
   */
  @Operation(
      summary = "Delete event invitation",
      description = "Deletes an event invitation for a user")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Event invitation deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Attendee not found")
      })
  @DeleteMapping("/{eventId}/{userId}")
  public void deleteAttendee(
      @Parameter(description = "Event ID") @PathVariable String eventId,
      @Parameter(description = "User ID") @PathVariable String userId,
      @Parameter(description = "Request user ID") @RequestHeader("X-User-Id")
          String requestUserId) {

    if (requestUserId == null || requestUserId.trim().isEmpty()) {
      logger.warning("Request user ID header (X-User-Id) is missing or empty");
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Request user ID header is required");
    }

    logger.info("Deleting attendee for event: " + eventId + " and user: " + userId);
    try {
      attendeeService.deleteAttendee(eventId, userId, requestUserId);
      logger.info("Successfully deleted attendee for event: " + eventId + " and user: " + userId);
    } catch (IllegalArgumentException e) {
      logger.warning("Bad request for attendee deletion: " + e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    } catch (IllegalStateException e) {
      logger.warning("Attendee not found for deletion: " + e.getMessage());
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    }
  }

  /**
   * Checks if an attendee exists for the given event and user.
   *
   * @param eventId the event ID
   * @param userId the user ID
   * @return true if attendee exists, false otherwise
   */
  @Operation(
      summary = "Check attendee existence",
      description = "Checks if an event invitation exists for the given event and user")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Attendee existence checked successfully")
      })
  @GetMapping("/{eventId}/{userId}/exists")
  public boolean existsAttendee(
      @Parameter(description = "Event ID") @PathVariable String eventId,
      @Parameter(description = "User ID") @PathVariable String userId) {
    logger.info("Checking if attendee exists for event: " + eventId + " and user: " + userId);
    boolean exists = attendeeService.existsAttendee(eventId, userId);
    logger.info("Attendee exists check result: " + exists);
    return exists;
  }

  /**
   * Counts the number of attendees for an event.
   *
   * @param eventId the event ID
   * @return the count of attendees
   */
  @Operation(
      summary = "Count event attendees",
      description = "Counts the number of attendees for an event")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Attendee count retrieved successfully")
      })
  @GetMapping("/event/{eventId}/count")
  public long countAttendeesByEvent(
      @Parameter(description = "Event ID") @PathVariable String eventId) {
    logger.info("Counting attendees for event: " + eventId);
    long count = attendeeService.countAttendeesByEvent(eventId);
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
  @Operation(
      summary = "Count event attendees by RSVP status",
      description = "Counts the number of attendees for an event with a specific RSVP status")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Attendee count by RSVP status retrieved successfully")
      })
  @GetMapping("/event/{eventId}/status/{rsvpStatus}/count")
  public long countAttendeesByEventAndStatus(
      @Parameter(description = "Event ID") @PathVariable String eventId,
      @Parameter(description = "RSVP status") @PathVariable RsvpStatus rsvpStatus) {
    logger.info("Counting attendees for event: " + eventId + " with RSVP status: " + rsvpStatus);
    long count = attendeeService.countAttendeesByEventAndStatus(eventId, rsvpStatus);
    logger.info(
        "Attendee count for event " + eventId + " with RSVP status " + rsvpStatus + ": " + count);
    return count;
  }

  /** Request DTO for creating an attendee (event invitation). */
  public static class AttendeeRequest {
    @Schema(description = "Event ID", required = true)
    private String eventId;

    @Schema(description = "User ID", required = true)
    private String userId;

    @Schema(
        description = "RSVP status",
        example = "pending",
        defaultValue = "pending",
        allowableValues = {"pending", "yes", "no"})
    private RsvpStatus rsvpStatus = RsvpStatus.PENDING; // Default to PENDING

    public String getEventId() {
      return eventId;
    }

    public void setEventId(String eventId) {
      this.eventId = eventId;
    }

    public String getUserId() {
      return userId;
    }

    public void setUserId(String userId) {
      this.userId = userId;
    }

    public RsvpStatus getRsvpStatus() {
      return rsvpStatus;
    }

    public void setRsvpStatus(RsvpStatus rsvpStatus) {
      this.rsvpStatus = rsvpStatus;
    }
  }

  /** Request DTO for updating RSVP status. */
  public static class RsvpUpdateRequest {
    @Schema(
        description = "RSVP status",
        example = "yes",
        allowableValues = {"pending", "yes", "no"})
    private RsvpStatus rsvpStatus;

    public RsvpStatus getRsvpStatus() {
      return rsvpStatus;
    }

    public void setRsvpStatus(RsvpStatus rsvpStatus) {
      this.rsvpStatus = rsvpStatus;
    }
  }
}
