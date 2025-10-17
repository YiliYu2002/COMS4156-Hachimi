package com.example.activityscheduler.membership.controller;

import com.example.activityscheduler.membership.model.Membership;
import com.example.activityscheduler.membership.model.MembershipStatus;
import com.example.activityscheduler.membership.service.MembershipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
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

  /**
   * Constructs a MembershipController with the given service.
   *
   * @param membershipService the membership service
   */
  public MembershipController(MembershipService membershipService) {
    this.membershipService = membershipService;
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
    return membershipService.getAllMemberships();
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
    return membershipService.getMembership(orgId, userId);
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
    return membershipService.getMembershipsByOrganization(orgId);
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
    return membershipService.getMembershipsByUser(userId);
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
    return membershipService.getMembershipsByStatus(status);
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
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid membership data");
    }

    try {
      return membershipService.createMembership(
          membershipRequest.getOrgId(),
          membershipRequest.getUserId(),
          membershipRequest.getStatus());
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    } catch (IllegalStateException e) {
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
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status data");
    }

    try {
      return membershipService.updateMembershipStatus(orgId, userId, statusUpdate.getStatus());
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    } catch (IllegalStateException e) {
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
    try {
      membershipService.deleteMembership(orgId, userId);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    } catch (IllegalStateException e) {
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
    return membershipService.existsMembership(orgId, userId);
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
    return membershipService.countActiveMembers(orgId);
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
    return membershipService.countUserMemberships(userId);
  }

  /** Request DTO for creating a membership. */
  public static class MembershipRequest {
    private String orgId;
    private String userId;
    private MembershipStatus status;

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
