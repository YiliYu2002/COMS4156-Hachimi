package com.example.activityscheduler.membership;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.activityscheduler.membership.model.Membership;
import com.example.activityscheduler.membership.model.MembershipId;
import com.example.activityscheduler.membership.model.MembershipStatus;
import com.example.activityscheduler.membership.repository.MembershipRepository;
import com.example.activityscheduler.membership.service.MembershipService;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MembershipServiceTests {

  private MembershipRepository mockRepository;
  private MembershipService membershipService;

  @BeforeEach
  void setUp() {
    mockRepository = mock(MembershipRepository.class);
    membershipService = new MembershipService(mockRepository);
  }

  @Test
  void getAllMemberships_returnsAllMemberships() {
    List<Membership> memberships =
        Arrays.asList(
            new Membership("org1", "user1", MembershipStatus.ACTIVE),
            new Membership("org2", "user2", MembershipStatus.INVITED));
    when(mockRepository.findAll()).thenReturn(memberships);

    List<Membership> result = membershipService.getAllMemberships();

    assertThat(result).hasSize(2);
    assertThat(result).containsExactlyElementsOf(memberships);
  }

  @Test
  void getMembership_existingMembership_returnsMembership() {
    String orgId = "org-123";
    String userId = "user-456";
    Membership membership = new Membership(orgId, userId, MembershipStatus.ACTIVE);
    when(mockRepository.findByOrgIdAndUserId(orgId, userId)).thenReturn(Optional.of(membership));

    Optional<Membership> result = membershipService.getMembership(orgId, userId);

    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(membership);
  }

  @Test
  void getMembership_nonExistentMembership_returnsEmpty() {
    String orgId = "org-123";
    String userId = "user-456";
    when(mockRepository.findByOrgIdAndUserId(orgId, userId)).thenReturn(Optional.empty());

    Optional<Membership> result = membershipService.getMembership(orgId, userId);

    assertThat(result).isEmpty();
  }

  @Test
  void getMembershipsByOrganization_returnsMemberships() {
    String orgId = "org-123";
    List<Membership> memberships =
        Arrays.asList(
            new Membership(orgId, "user1", MembershipStatus.ACTIVE),
            new Membership(orgId, "user2", MembershipStatus.INVITED));
    when(mockRepository.findByOrgId(orgId)).thenReturn(memberships);

    List<Membership> result = membershipService.getMembershipsByOrganization(orgId);

    assertThat(result).hasSize(2);
    assertThat(result).containsExactlyElementsOf(memberships);
  }

  @Test
  void getMembershipsByUser_returnsMemberships() {
    String userId = "user-456";
    List<Membership> memberships =
        Arrays.asList(
            new Membership("org1", userId, MembershipStatus.ACTIVE),
            new Membership("org2", userId, MembershipStatus.SUSPENDED));
    when(mockRepository.findByUserId(userId)).thenReturn(memberships);

    List<Membership> result = membershipService.getMembershipsByUser(userId);

    assertThat(result).hasSize(2);
    assertThat(result).containsExactlyElementsOf(memberships);
  }

  @Test
  void getMembershipsByStatus_returnsMemberships() {
    MembershipStatus status = MembershipStatus.ACTIVE;
    List<Membership> memberships =
        Arrays.asList(
            new Membership("org1", "user1", status), new Membership("org2", "user2", status));
    when(mockRepository.findByStatus(status)).thenReturn(memberships);

    List<Membership> result = membershipService.getMembershipsByStatus(status);

    assertThat(result).hasSize(2);
    assertThat(result).containsExactlyElementsOf(memberships);
  }

  @Test
  void createMembership_validData_createsMembership() {
    String orgId = "org-123";
    String userId = "user-456";
    MembershipStatus status = MembershipStatus.ACTIVE;
    Membership membership = new Membership(orgId, userId, status);

    when(mockRepository.existsByOrgIdAndUserId(orgId, userId)).thenReturn(false);
    when(mockRepository.save(any(Membership.class))).thenReturn(membership);

    Membership result = membershipService.createMembership(orgId, userId, status);

    assertThat(result).isEqualTo(membership);
    verify(mockRepository).save(any(Membership.class));
  }

  @Test
  void createMembership_nullOrgId_throwsException() {
    assertThatThrownBy(
            () -> membershipService.createMembership(null, "user-456", MembershipStatus.ACTIVE))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Organization ID cannot be null or empty");
  }

  @Test
  void createMembership_emptyOrgId_throwsException() {
    assertThatThrownBy(
            () -> membershipService.createMembership("", "user-456", MembershipStatus.ACTIVE))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Organization ID cannot be null or empty");
  }

  @Test
  void createMembership_nullUserId_throwsException() {
    assertThatThrownBy(
            () -> membershipService.createMembership("org-123", null, MembershipStatus.ACTIVE))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("User ID cannot be null or empty");
  }

  @Test
  void createMembership_existingMembership_throwsException() {
    String orgId = "org-123";
    String userId = "user-456";
    when(mockRepository.existsByOrgIdAndUserId(orgId, userId)).thenReturn(true);

    assertThatThrownBy(
            () -> membershipService.createMembership(orgId, userId, MembershipStatus.ACTIVE))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Membership already exists for organization " + orgId + " and user " + userId);
  }

  @Test
  void createMembership_nullStatus_usesDefaultStatus() {
    String orgId = "org-123";
    String userId = "user-456";
    Membership membership = new Membership(orgId, userId, MembershipStatus.ACTIVE);

    when(mockRepository.existsByOrgIdAndUserId(orgId, userId)).thenReturn(false);
    when(mockRepository.save(any(Membership.class))).thenReturn(membership);

    Membership result = membershipService.createMembership(orgId, userId, null);

    assertThat(result).isEqualTo(membership);
    verify(mockRepository).save(any(Membership.class));
  }

  @Test
  void updateMembershipStatus_validData_updatesStatus() {
    String orgId = "org-123";
    String userId = "user-456";
    MembershipStatus newStatus = MembershipStatus.SUSPENDED;
    Membership membership = new Membership(orgId, userId, MembershipStatus.ACTIVE);
    Membership updatedMembership = new Membership(orgId, userId, newStatus);

    when(mockRepository.findByOrgIdAndUserId(orgId, userId)).thenReturn(Optional.of(membership));
    when(mockRepository.save(any(Membership.class))).thenReturn(updatedMembership);

    Membership result = membershipService.updateMembershipStatus(orgId, userId, newStatus);

    assertThat(result.getStatus()).isEqualTo(newStatus);
    verify(mockRepository).save(membership);
  }

  @Test
  void updateMembershipStatus_nonExistentMembership_throwsException() {
    String orgId = "org-123";
    String userId = "user-456";
    MembershipStatus newStatus = MembershipStatus.SUSPENDED;

    when(mockRepository.findByOrgIdAndUserId(orgId, userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> membershipService.updateMembershipStatus(orgId, userId, newStatus))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Membership not found for organization " + orgId + " and user " + userId);
  }

  @Test
  void deleteMembership_validData_deletesMembership() {
    String orgId = "org-123";
    String userId = "user-456";
    MembershipId membershipId = new MembershipId(orgId, userId);

    when(mockRepository.existsById(membershipId)).thenReturn(true);

    membershipService.deleteMembership(orgId, userId);

    verify(mockRepository).deleteById(membershipId);
  }

  @Test
  void deleteMembership_nonExistentMembership_throwsException() {
    String orgId = "org-123";
    String userId = "user-456";
    MembershipId membershipId = new MembershipId(orgId, userId);

    when(mockRepository.existsById(membershipId)).thenReturn(false);

    assertThatThrownBy(() -> membershipService.deleteMembership(orgId, userId))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Membership not found for organization " + orgId + " and user " + userId);
  }

  @Test
  void existsMembership_existingMembership_returnsTrue() {
    String orgId = "org-123";
    String userId = "user-456";

    when(mockRepository.existsByOrgIdAndUserId(orgId, userId)).thenReturn(true);

    boolean result = membershipService.existsMembership(orgId, userId);

    assertThat(result).isTrue();
  }

  @Test
  void existsMembership_nonExistentMembership_returnsFalse() {
    String orgId = "org-123";
    String userId = "user-456";

    when(mockRepository.existsByOrgIdAndUserId(orgId, userId)).thenReturn(false);

    boolean result = membershipService.existsMembership(orgId, userId);

    assertThat(result).isFalse();
  }

  @Test
  void countActiveMembers_returnsCount() {
    String orgId = "org-123";
    long expectedCount = 5L;

    when(mockRepository.countActiveMembersByOrgId(orgId)).thenReturn(expectedCount);

    long result = membershipService.countActiveMembers(orgId);

    assertThat(result).isEqualTo(expectedCount);
  }

  @Test
  void countUserMemberships_returnsCount() {
    String userId = "user-456";
    long expectedCount = 3L;

    when(mockRepository.countMembershipsByUserId(userId)).thenReturn(expectedCount);

    long result = membershipService.countUserMemberships(userId);

    assertThat(result).isEqualTo(expectedCount);
  }
}
