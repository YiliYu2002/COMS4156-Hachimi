package com.example.activityscheduler.membership.service;

import com.example.activityscheduler.membership.model.Membership;
import com.example.activityscheduler.membership.model.MembershipId;
import com.example.activityscheduler.membership.model.MembershipStatus;
import com.example.activityscheduler.membership.repository.MembershipRepository;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing Membership entities. Provides business logic for membership operations
 * including CRUD operations, status management, and validation.
 */
@Service
@Transactional
public class MembershipService {

  private final MembershipRepository membershipRepository;
  private static final Logger logger = Logger.getLogger(MembershipService.class.getName());

  /**
   * Constructs a MembershipService with the given repository.
   *
   * @param membershipRepository the membership repository
   */
  public MembershipService(MembershipRepository membershipRepository) {
    this.membershipRepository = membershipRepository;
  }

  /**
   * Retrieves all memberships.
   *
   * @return a list of all memberships
   */
  @Transactional(readOnly = true)
  public List<Membership> getAllMemberships() {
    logger.info("Retrieving all memberships");
    List<Membership> memberships = membershipRepository.findAll();
    logger.info("Retrieved " + memberships.size() + " memberships");
    return membershipRepository.findAll();
  }

  /**
   * Retrieves a membership by organization ID and user ID.
   *
   * @param orgId the organization ID
   * @param userId the user ID
   * @return an Optional containing the membership if found, empty otherwise
   */
  @Transactional(readOnly = true)
  public Optional<Membership> getMembership(String orgId, String userId) {
    logger.info("Retrieving membership for organization: " + orgId + " and user: " + userId);
    Optional<Membership> membership = membershipRepository.findByOrgIdAndUserId(orgId, userId);
    if (membership.isPresent()) {
      logger.info(
          "Membership found: organization: "
              + membership.get().getOrgId()
              + " and user: "
              + membership.get().getUserId());
    } else {
      logger.info("Membership not found for organization: " + orgId + " and user: " + userId);
    }
    return membershipRepository.findByOrgIdAndUserId(orgId, userId);
  }

  /**
   * Retrieves all memberships for a specific organization.
   *
   * @param orgId the organization ID
   * @return a list of memberships for the organization
   */
  @Transactional(readOnly = true)
  public List<Membership> getMembershipsByOrganization(String orgId) {
    logger.info("Retrieving memberships for organization: " + orgId);
    List<Membership> memberships = membershipRepository.findByOrgId(orgId);
    logger.info("Retrieved " + memberships.size() + " memberships for organization: " + orgId);
    return membershipRepository.findByOrgId(orgId);
  }

  /**
   * Retrieves all memberships for a specific user.
   *
   * @param userId the user ID
   * @return a list of memberships for the user
   */
  @Transactional(readOnly = true)
  public List<Membership> getMembershipsByUser(String userId) {
    logger.info("Retrieving memberships for user: " + userId);
    List<Membership> memberships = membershipRepository.findByUserId(userId);
    logger.info("Retrieved " + memberships.size() + " memberships for user: " + userId);
    return membershipRepository.findByUserId(userId);
  }

  /**
   * Retrieves all memberships with a specific status.
   *
   * @param status the membership status
   * @return a list of memberships with the specified status
   */
  @Transactional(readOnly = true)
  public List<Membership> getMembershipsByStatus(MembershipStatus status) {
    logger.info("Retrieving memberships with status: " + status);
    List<Membership> memberships = membershipRepository.findByStatus(status);
    logger.info("Retrieved " + memberships.size() + " memberships with status: " + status);
    return membershipRepository.findByStatus(status);
  }

  /**
   * Creates a new membership.
   *
   * @param orgId the organization ID
   * @param userId the user ID
   * @param status the membership status (defaults to ACTIVE if null)
   * @return the created membership
   * @throws IllegalArgumentException if organization ID or user ID is null or empty
   * @throws IllegalStateException if membership already exists
   */
  public Membership createMembership(String orgId, String userId, MembershipStatus status) {
    if (orgId == null || orgId.trim().isEmpty()) {
      logger.warning("Organization ID cannot be null or empty");
      throw new IllegalArgumentException("Organization ID cannot be null or empty");
    }

    if (userId == null || userId.trim().isEmpty()) {
      logger.warning("User ID cannot be null or empty");
      throw new IllegalArgumentException("User ID cannot be null or empty");
    }

    if (membershipRepository.existsByOrgIdAndUserId(orgId, userId)) {
      logger.warning("Membership already exists for organization " + orgId + " and user " + userId);
      throw new IllegalStateException(
          "Membership already exists for organization " + orgId + " and user " + userId);
    }

    MembershipStatus membershipStatus = (status != null) ? status : MembershipStatus.ACTIVE;
    Membership membership = new Membership(orgId, userId, membershipStatus);
    logger.info(
        "Creating membership for organization: "
            + orgId
            + " and user: "
            + userId
            + " with status: "
            + membershipStatus);
    return membershipRepository.save(membership);
  }

  /**
   * Creates a new membership with ACTIVE status.
   *
   * @param orgId the organization ID
   * @param userId the user ID
   * @return the created membership
   */
  public Membership createMembership(String orgId, String userId) {
    logger.info("Creating membership for organization: " + orgId + " and user: " + userId);
    return createMembership(orgId, userId, MembershipStatus.ACTIVE);
  }

  /**
   * Updates the status of an existing membership.
   *
   * @param orgId the organization ID
   * @param userId the user ID
   * @param newStatus the new membership status
   * @return the updated membership
   * @throws IllegalArgumentException if organization ID, user ID, or status is null
   * @throws IllegalStateException if membership is not found
   */
  public Membership updateMembershipStatus(
      String orgId, String userId, MembershipStatus newStatus) {
    if (orgId == null || orgId.trim().isEmpty()) {
      logger.warning("Organization ID cannot be null or empty");
      throw new IllegalArgumentException("Organization ID cannot be null or empty");
    }

    if (userId == null || userId.trim().isEmpty()) {
      logger.warning("User ID cannot be null or empty");
      throw new IllegalArgumentException("User ID cannot be null or empty");
    }

    if (newStatus == null) {
      logger.warning("Status cannot be null");
      throw new IllegalArgumentException("Status cannot be null");
    }

    Membership membership =
        membershipRepository
            .findByOrgIdAndUserId(orgId, userId)
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Membership not found for organization " + orgId + " and user " + userId));

    membership.setStatus(newStatus);
    logger.info(
        "Updating membership status for organization: "
            + orgId
            + " and user: "
            + userId
            + " to: "
            + newStatus);
    return membershipRepository.save(membership);
  }

  /**
   * Deletes a membership.
   *
   * @param orgId the organization ID
   * @param userId the user ID
   * @throws IllegalArgumentException if organization ID or user ID is null or empty
   * @throws IllegalStateException if membership is not found
   */
  public void deleteMembership(String orgId, String userId) {
    if (orgId == null || orgId.trim().isEmpty()) {
      logger.warning("Organization ID cannot be null or empty");
      throw new IllegalArgumentException("Organization ID cannot be null or empty");
    }

    if (userId == null || userId.trim().isEmpty()) {
      logger.warning("User ID cannot be null or empty");
      throw new IllegalArgumentException("User ID cannot be null or empty");
    }

    MembershipId membershipId = new MembershipId(orgId, userId);
    if (!membershipRepository.existsById(membershipId)) {
      logger.warning("Membership not found for organization " + orgId + " and user " + userId);
      throw new IllegalStateException(
          "Membership not found for organization " + orgId + " and user " + userId);
    }

    membershipRepository.deleteById(membershipId);
    logger.info("Deleted membership for organization: " + orgId + " and user: " + userId);
  }

  /**
   * Checks if a membership exists for the given organization and user.
   *
   * @param orgId the organization ID
   * @param userId the user ID
   * @return true if membership exists, false otherwise
   */
  @Transactional(readOnly = true)
  public boolean existsMembership(String orgId, String userId) {
    logger.info(
        "Checking if membership exists for organization: " + orgId + " and user: " + userId);
    boolean exists = membershipRepository.existsByOrgIdAndUserId(orgId, userId);
    logger.info("Membership exists: " + exists);
    return membershipRepository.existsByOrgIdAndUserId(orgId, userId);
  }

  /**
   * Counts the number of active members for an organization.
   *
   * @param orgId the organization ID
   * @return the count of active members
   */
  @Transactional(readOnly = true)
  public long countActiveMembers(String orgId) {
    logger.info("Counting active members for organization: " + orgId);
    long count = membershipRepository.countActiveMembersByOrgId(orgId);
    logger.info("Active member count for organization " + orgId + ": " + count);
    return membershipRepository.countActiveMembersByOrgId(orgId);
  }

  /**
   * Counts the number of memberships for a user.
   *
   * @param userId the user ID
   * @return the count of memberships
   */
  @Transactional(readOnly = true)
  public long countUserMemberships(String userId) {
    logger.info("Counting memberships for user: " + userId);
    long count = membershipRepository.countMembershipsByUserId(userId);
    logger.info("Membership count for user " + userId + ": " + count);
    return count;
  }
}
