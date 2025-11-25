package com.example.activityscheduler.conflict.controller;

import com.example.activityscheduler.conflict.service.ConflictService;
import com.example.activityscheduler.event.model.Event;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.logging.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST controller for managing event time conflicts. Provides HTTP endpoints for checking time
 * conflicts between events.
 */
@RestController
@RequestMapping("/api/conflicts")
@Tag(
    name = "Event Conflict Management",
    description = "APIs for checking time conflicts between events")
public class ConflictController {

  private final ConflictService conflictService;
  private static final Logger logger = Logger.getLogger(ConflictController.class.getName());

  /**
   * Constructs a ConflictController with the given service.
   *
   * @param conflictService the conflict service
   */
  public ConflictController(ConflictService conflictService) {
    this.conflictService = conflictService;
  }

  /**
   * Finds all time conflicts among events that a user has accepted (RSVP status = accepted/yes).
   *
   * @param userId the user ID
   * @param status the RSVP status filter (should be "accepted" or "yes")
   * @return a list of events that have time conflicts
   */
  @Operation(
      summary = "Find conflicts among accepted events",
      description =
          "Returns all events that a user has accepted (RSVP status = accepted/yes) that have time conflicts with each other")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Conflicts found successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid user ID or status parameter")
      })
  @GetMapping("/checkconflicts/{userId}/status")
  public List<Event> findConflictsAmongAcceptedEvents(
      @Parameter(description = "User ID") @PathVariable String userId,
      @Parameter(description = "RSVP status filter (should be 'accepted' or 'yes')")
          @RequestParam(name = "status", defaultValue = "accepted")
          String status) {

    logger.info(
        "Received request to find conflicts among accepted events for user: "
            + userId
            + " with status: "
            + status);

    // Validate status parameter (accept both "accepted" and "yes")
    if (!status.equalsIgnoreCase("accepted") && !status.equalsIgnoreCase("yes")) {
      logger.warning("Invalid status parameter: " + status);
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Status parameter must be 'accepted' or 'yes'. Got: " + status);
    }

    try {
      List<Event> conflicts = conflictService.findConflictsAmongAcceptedEvents(userId);
      logger.info("Found " + conflicts.size() + " conflicting events for user: " + userId);
      return conflicts;
    } catch (IllegalArgumentException e) {
      logger.warning("Bad request for finding conflicts: " + e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }

  /**
   * Finds all accepted events that conflict with a pending event.
   *
   * @param userId the user ID
   * @param pendingEventId the pending event ID to check for conflicts
   * @return a list of accepted events that conflict with the pending event, empty list if no
   *     conflicts
   */
  @Operation(
      summary = "Find conflicts with pending event",
      description =
          "Returns all accepted events that conflict with a pending event. Returns empty list if no conflicts exist.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Conflicts checked successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid user ID or pending event ID"),
        @ApiResponse(responseCode = "404", description = "Pending event not found")
      })
  @GetMapping("/checkconflicts/{userId}/{pendingEventId}")
  public List<Event> findConflictsWithPendingEvent(
      @Parameter(description = "User ID") @PathVariable String userId,
      @Parameter(description = "Pending event ID to check for conflicts") @PathVariable
          String pendingEventId) {

    logger.info(
        "Received request to find conflicts between pending event: "
            + pendingEventId
            + " and accepted events for user: "
            + userId);

    try {
      List<Event> conflicts = conflictService.findConflictsWithPendingEvent(userId, pendingEventId);
      logger.info(
          "Found "
              + conflicts.size()
              + " accepted events conflicting with pending event: "
              + pendingEventId
              + " for user: "
              + userId);
      return conflicts;
    } catch (IllegalArgumentException e) {
      logger.warning("Bad request for finding conflicts: " + e.getMessage());
      if (e.getMessage().contains("not found")) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
      }
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }
  }
}
