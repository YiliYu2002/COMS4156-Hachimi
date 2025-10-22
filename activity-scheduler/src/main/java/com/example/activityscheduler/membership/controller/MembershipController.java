package com.example.activityscheduler.membership.controller;

import com.example.activityscheduler.membership.model.Membership;
import com.example.activityscheduler.membership.model.MembershipStatus;
import com.example.activityscheduler.membership.service.MembershipService;
import com.example.activityscheduler.organization.repository.OrganizationRepository;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST controller for managing Membership entities. Provides HTTP endpoints for membership
 * operations including CRUD operations and status management.
 */
@RestController
@RequestMapping("/api/memberships")
@Tag(name = "Membership Management", description = "APIs for managing organization memberships")
public class MembershipController {

  private final MembershipService membershipService;
  private final UserRepository userRepository;
  private final OrganizationRepository organizationRepository;
  private static final Logger logger = Logger.getLogger(MembershipController.class.getName());

  /**
   * Constructs a MembershipController with the given service.
   *
   * @param membershipService the membership service
   * @param userRepository the user repository
   * @param organizationRepository the organization repository
   */
  public MembershipController(
      MembershipService membershipService,
      UserRepository userRepository,
      OrganizationRepository organizationRepository) {
    this.membershipService = membershipService;
    this.userRepository = userRepository;
    this.organizationRepository = organizationRepository;
  }

  /**
   * Retrieves all memberships.
   *
   * @return a list of all memberships
   */
  @Operation(
      summary = "Get all memberships",
      description = "Retrieves a list of all memberships in the system")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all memberships")
      })
  @GetMapping
  public List<Membership> getAllMemberships() {
    logger.info("Retrieving all memberships");
    List<Membership> memberships = membershipService.getAllMemberships();
    logger.info("Retrieved " + memberships.size() + " memberships");
    return memberships;
  }

  /**
   * Retrieves a specific membership by organization ID and user ID.
   *
   * @param orgId the organization ID
   * @param userId the user ID
   * @return an Optional containing the membership if found, empty otherwise
   */
  @Operation(
      summary = "Get membership by organization and user",
      description = "Retrieves a specific membership by organization ID and user ID")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Membership found successfully"),
        @ApiResponse(responseCode = "404", description = "Membership not found")
      })
  @GetMapping("/{orgId}/{userId}")
  public Optional<Membership> getMembership(
      @Parameter(description = "Organization ID") @PathVariable String orgId,
      @Parameter(description = "User ID") @PathVariable String userId) {
    logger.info("Retrieving membership for organization: " + orgId + " and user: " + userId);
    Optional<Membership> membership = membershipService.getMembership(orgId, userId);
    if (membership.isPresent()) {
      logger.info(
          "Membership found: organization: "
              + membership.get().getOrgId()
              + " and user: "
              + membership.get().getUserId());
    } else {
      logger.info("Membership not found for organization: " + orgId + " and user: " + userId);
    }
    return membership;
  }

  /**
   * Retrieves all memberships for a specific organization.
   *
   * @param orgId the organization ID
   * @return a list of memberships for the organization
   */
  @Operation(
      summary = "Get memberships by organization",
      description = "Retrieves all memberships for a specific organization")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved organization memberships")
      })
  @GetMapping("/organization/{orgId}")
  public List<Membership> getMembershipsByOrganization(
      @Parameter(description = "Organization ID") @PathVariable String orgId) {
    logger.info("Retrieving memberships for organization: " + orgId);
    List<Membership> memberships = membershipService.getMembershipsByOrganization(orgId);
    logger.info("Retrieved " + memberships.size() + " memberships for organization: " + orgId);
    return memberships;
  }

  /**
   * Retrieves all memberships for a specific user.
   *
   * @param userId the user ID
   * @return a list of memberships for the user
   */
  @Operation(
      summary = "Get memberships by user",
      description = "Retrieves all memberships for a specific user")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved user memberships")
      })
  @GetMapping("/user/{userId}")
  public List<Membership> getMembershipsByUser(
      @Parameter(description = "User ID") @PathVariable String userId) {
    logger.info("Retrieving memberships for user: " + userId);
    List<Membership> memberships = membershipService.getMembershipsByUser(userId);
    logger.info("Retrieved " + memberships.size() + " memberships for user: " + userId);
    return memberships;
  }

  /**
   * Retrieves all memberships with a specific status.
   *
   * @param status the membership status
   * @return a list of memberships with the specified status
   */
  @Operation(
      summary = "Get memberships by status",
      description = "Retrieves all memberships with a specific status")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved memberships by status")
      })
  @GetMapping("/status/{status}")
  public List<Membership> getMembershipsByStatus(
      @Parameter(description = "Membership status") @PathVariable MembershipStatus status) {
    logger.info("Retrieving memberships by status: " + status);
    List<Membership> memberships = membershipService.getMembershipsByStatus(status);
    logger.info("Retrieved " + memberships.size() + " memberships by status: " + status);
    return memberships;
  }

  /**
   * Creates a new membership.
   *
   * @param membershipRequest the membership creation request
   * @return the created membership
   */
  @Operation(
      summary = "Create a new membership",
      description = "Creates a new membership between an organization and a user")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Membership created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid membership data"),
        @ApiResponse(responseCode = "409", description = "Membership already exists")
      })
  @PostMapping
  public Membership createMembership(@RequestBody MembershipRequest membershipRequest) {
    if (membershipRequest == null
        || membershipRequest.getOrgId() == null
        || membershipRequest.getUserId() == null) {
      logger.warning("Invalid membership creation request: missing required fields");
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid membership data");
    }

    logger.info(
        "Creating membership for organization: "
            + membershipRequest.getOrgId()
            + " and user: "
            + membershipRequest.getUserId()
            + " with status: "
            + membershipRequest.getStatus());

    try {
      Membership createdMembership =
          membershipService.createMembership(
              membershipRequest.getOrgId(),
              membershipRequest.getUserId(),
              membershipRequest.getStatus());
      logger.info(
          "Successfully created membership for organization: "
              + membershipRequest.getOrgId()
              + " and user: "
              + membershipRequest.getUserId());
      return createdMembership;
    } catch (IllegalArgumentException e) {
      logger.warning("Bad request for membership creation: " + e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    } catch (IllegalStateException e) {
      logger.warning("Conflict during membership creation: " + e.getMessage());
      throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
    }
  }

  /**
   * Updates the status of an existing membership.
   *
   * @param orgId the organization ID
   * @param userId the user ID
   * @param statusUpdate the status update request
   * @return the updated membership
   */
  @Operation(
      summary = "Update membership status",
      description = "Updates the status of an existing membership")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Membership status updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status data"),
        @ApiResponse(responseCode = "404", description = "Membership not found")
      })
  @PutMapping("/{orgId}/{userId}/status")
  public Membership updateMembershipStatus(
      @Parameter(description = "Organization ID") @PathVariable String orgId,
      @Parameter(description = "User ID") @PathVariable String userId,
      @RequestBody StatusUpdateRequest statusUpdate) {
    if (statusUpdate == null || statusUpdate.getStatus() == null) {
      logger.warning("Invalid status update request: status is null");
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status data");
    }

    logger.info(
        "Updating membership status for organization: "
            + orgId
            + " and user: "
            + userId
            + " to status: "
            + statusUpdate.getStatus());
    if (userRepository.findById(userId).isEmpty()) {
      logger.warning("User not found for membership status update: " + userId);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
    }
    if (organizationRepository.findById(orgId).isEmpty()) {
      logger.warning("Organization not found for membership status update: " + orgId);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found");
    }
    if (!statusUpdate.getStatus().equals(MembershipStatus.ACTIVE)
        && !statusUpdate.getStatus().equals(MembershipStatus.SUSPENDED)) {
      logger.warning("Invalid status for membership update: " + statusUpdate.getStatus());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status data");
    }

    try {
      Membership updatedMembership =
          membershipService.updateMembershipStatus(orgId, userId, statusUpdate.getStatus());
      logger.info(
          "Successfully updated membership status for organization: "
              + orgId
              + " and user: "
              + userId);
      return updatedMembership;
    } catch (IllegalArgumentException e) {
      logger.warning("Bad request for membership status update: " + e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    } catch (IllegalStateException e) {
      logger.warning("Membership not found for status update: " + e.getMessage());
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    }
  }

  /**
   * Deletes a membership.
   *
   * @param orgId the organization ID
   * @param userId the user ID
   */
  @Operation(
      summary = "Delete membership",
      description = "Deletes a membership between an organization and a user")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Membership deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Membership not found")
      })
  @DeleteMapping("/{orgId}/{userId}")
  public void deleteMembership(
      @Parameter(description = "Organization ID") @PathVariable String orgId,
      @Parameter(description = "User ID") @PathVariable String userId) {
    logger.info("Deleting membership for organization: " + orgId + " and user: " + userId);
    try {
      membershipService.deleteMembership(orgId, userId);
      logger.info(
          "Successfully deleted membership for organization: " + orgId + " and user: " + userId);
    } catch (IllegalArgumentException e) {
      logger.warning("Bad request for membership deletion: " + e.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    } catch (IllegalStateException e) {
      logger.warning("Membership not found for deletion: " + e.getMessage());
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    }
  }

  /**
   * Checks if a membership exists for the given organization and user.
   *
   * @param orgId the organization ID
   * @param userId the user ID
   * @return true if membership exists, false otherwise
   */
  @Operation(
      summary = "Check membership existence",
      description = "Checks if a membership exists for the given organization and user")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Membership existence checked successfully")
      })
  @GetMapping("/{orgId}/{userId}/exists")
  public boolean existsMembership(
      @Parameter(description = "Organization ID") @PathVariable String orgId,
      @Parameter(description = "User ID") @PathVariable String userId) {
    logger.info(
        "Checking if membership exists for organization: " + orgId + " and user: " + userId);
    boolean exists = membershipService.existsMembership(orgId, userId);
    logger.info("Membership exists check result: " + exists);
    return exists;
  }

  /**
   * Counts the number of active members for an organization.
   *
   * @param orgId the organization ID
   * @return the count of active members
   */
  @Operation(
      summary = "Count active members",
      description = "Counts the number of active members for an organization")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Active member count retrieved successfully")
      })
  @GetMapping("/organization/{orgId}/active-count")
  public long countActiveMembers(
      @Parameter(description = "Organization ID") @PathVariable String orgId) {
    logger.info("Counting active members for organization: " + orgId);
    long count = membershipService.countActiveMembers(orgId);
    logger.info("Active member count for organization " + orgId + ": " + count);
    return count;
  }

  /**
   * Counts the number of memberships for a user.
   *
   * @param userId the user ID
   * @return the count of memberships
   */
  @Operation(
      summary = "Count user memberships",
      description = "Counts the number of memberships for a user")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "User membership count retrieved successfully")
      })
  @GetMapping("/user/{userId}/count")
  public long countUserMemberships(
      @Parameter(description = "User ID") @PathVariable String userId) {
    logger.info("Counting memberships for user: " + userId);
    long count = membershipService.countUserMemberships(userId);
    logger.info("Membership count for user " + userId + ": " + count);
    return count;
  }

  /** Request DTO for creating a membership. */
  public static class MembershipRequest {
    @Schema(description = "Organization ID", required = true)
    private String orgId;

    @Schema(description = "User ID", required = true)
    private String userId;

    @Schema(description = "Membership status", example = "INVITED", defaultValue = "INVITED")
    private MembershipStatus status = MembershipStatus.INVITED; // Default to INVITED

    public String getOrgId() {
      return orgId;
    }

    public void setOrgId(String orgId) {
      this.orgId = orgId;
    }

    public String getUserId() {
      return userId;
    }

    public void setUserId(String userId) {
      this.userId = userId;
    }

    public MembershipStatus getStatus() {
      return status;
    }

    public void setStatus(MembershipStatus status) {
      this.status = status;
    }
  }

  /** Request DTO for updating membership status. */
  public static class StatusUpdateRequest {
    private MembershipStatus status;

    public MembershipStatus getStatus() {
      return status;
    }

    public void setStatus(MembershipStatus status) {
      this.status = status;
    }
  }
}
