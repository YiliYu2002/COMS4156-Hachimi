package com.example.activityscheduler.membership.repository;

import com.example.activityscheduler.membership.model.Membership;
import com.example.activityscheduler.membership.model.MembershipId;
import com.example.activityscheduler.membership.model.MembershipStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Membership entity operations. Provides data access methods for
 * Membership entities using Spring Data JPA with composite primary key support.
 */
@Repository
public interface MembershipRepository extends JpaRepository<Membership, MembershipId> {

  /**
   * Finds all memberships for a specific organization.
   *
   * @param orgId the organization ID
   * @return a list of memberships for the organization
   */
  List<Membership> findByOrgId(String orgId);

  /**
   * Finds all memberships for a specific user.
   *
   * @param userId the user ID
   * @return a list of memberships for the user
   */
  List<Membership> findByUserId(String userId);

  /**
   * Finds a specific membership by organization ID and user ID.
   *
   * @param orgId the organization ID
   * @param userId the user ID
   * @return an Optional containing the membership if found, empty otherwise
   */
  Optional<Membership> findByOrgIdAndUserId(String orgId, String userId);

  /**
   * Finds all memberships with a specific status.
   *
   * @param status the membership status
   * @return a list of memberships with the specified status
   */
  List<Membership> findByStatus(MembershipStatus status);

  /**
   * Finds all memberships for an organization with a specific status.
   *
   * @param orgId the organization ID
   * @param status the membership status
   * @return a list of memberships for the organization with the specified status
   */
  List<Membership> findByOrgIdAndStatus(String orgId, MembershipStatus status);

  /**
   * Finds all memberships for a user with a specific status.
   *
   * @param userId the user ID
   * @param status the membership status
   * @return a list of memberships for the user with the specified status
   */
  List<Membership> findByUserIdAndStatus(String userId, MembershipStatus status);

  /**
   * Checks if a membership exists for the given organization and user.
   *
   * @param orgId the organization ID
   * @param userId the user ID
   * @return true if a membership exists, false otherwise
   */
  boolean existsByOrgIdAndUserId(String orgId, String userId);

  /**
   * Counts the number of active memberships for an organization.
   *
   * @param orgId the organization ID
   * @return the count of active memberships
   */
  @Query("SELECT COUNT(m) FROM Membership m WHERE m.orgId = :orgId AND m.status = 'ACTIVE'")
  long countActiveMembersByOrgId(@Param("orgId") String orgId);

  /**
   * Counts the number of memberships for a user across all organizations.
   *
   * @param userId the user ID
   * @return the count of memberships for the user
   */
  @Query("SELECT COUNT(m) FROM Membership m WHERE m.userId = :userId")
  long countMembershipsByUserId(@Param("userId") String userId);
}
