package com.example.activityscheduler.membership;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.activityscheduler.membership.controller.MembershipController;
import com.example.activityscheduler.membership.controller.MembershipController.MembershipRequest;
import com.example.activityscheduler.membership.controller.MembershipController.StatusUpdateRequest;
import com.example.activityscheduler.membership.model.Membership;
import com.example.activityscheduler.membership.model.MembershipStatus;
import com.example.activityscheduler.membership.service.MembershipService;
import com.example.activityscheduler.organization.repository.OrganizationRepository;
import com.example.activityscheduler.user.repository.UserRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class MembershipControllerTests {

  private MembershipService mockService;
  private UserRepository mockUserRepository;
  private OrganizationRepository mockOrgRepository;
  private MembershipController controller;

  @BeforeEach
  void setUp() {
    mockService = mock(MembershipService.class);
    mockUserRepository = mock(UserRepository.class);
    mockOrgRepository = mock(OrganizationRepository.class);
    controller = new MembershipController(mockService, mockUserRepository, mockOrgRepository);
  }

  @Test
  void getAllMemberships_returnsAllMemberships() {
    List<Membership> memberships =
        Arrays.asList(
            new Membership("org1", "user1", MembershipStatus.ACTIVE),
            new Membership("org2", "user2", MembershipStatus.INVITED));
    when(mockService.getAllMemberships()).thenReturn(memberships);

    List<Membership> result = controller.getAllMemberships();

    assertThat(result).hasSize(2);
    assertThat(result).containsExactlyElementsOf(memberships);
  }

  @Test
  void getMembership_existingMembership_returnsMembership() {
    String orgId = "org-123";
    String userId = "user-456";
    Membership membership = new Membership(orgId, userId, MembershipStatus.ACTIVE);
    when(mockService.getMembership(orgId, userId)).thenReturn(Optional.of(membership));

    Optional<Membership> result = controller.getMembership(orgId, userId);

    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(membership);
  }

  @Test
  void getMembership_nonExistentMembership_returnsEmpty() {
    String orgId = "org-123";
    String userId = "user-456";
    when(mockService.getMembership(orgId, userId)).thenReturn(Optional.empty());

    Optional<Membership> result = controller.getMembership(orgId, userId);

    assertThat(result).isEmpty();
  }

  @Test
  void getMembershipsByOrganization_returnsMemberships() {
    String orgId = "org-123";
    List<Membership> memberships =
        Arrays.asList(
            new Membership(orgId, "user1", MembershipStatus.ACTIVE),
            new Membership(orgId, "user2", MembershipStatus.INVITED));
    when(mockService.getMembershipsByOrganization(orgId)).thenReturn(memberships);

    List<Membership> result = controller.getMembershipsByOrganization(orgId);

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
    when(mockService.getMembershipsByUser(userId)).thenReturn(memberships);

    List<Membership> result = controller.getMembershipsByUser(userId);

    assertThat(result).hasSize(2);
    assertThat(result).containsExactlyElementsOf(memberships);
  }

  @Test
  void getMembershipsByStatus_returnsMemberships() {
    MembershipStatus status = MembershipStatus.ACTIVE;
    List<Membership> memberships =
        Arrays.asList(
            new Membership("org1", "user1", status), new Membership("org2", "user2", status));
    when(mockService.getMembershipsByStatus(status)).thenReturn(memberships);

    List<Membership> result = controller.getMembershipsByStatus(status);

    assertThat(result).hasSize(2);
    assertThat(result).containsExactlyElementsOf(memberships);
  }

  @Test
  void createMembership_validRequest_createsMembership() {
    String orgId = "org-123";
    String userId = "user-456";
    MembershipStatus status = MembershipStatus.ACTIVE;
    Membership membership = new Membership(orgId, userId, status);

    MembershipRequest request = new MembershipRequest();
    request.setOrgId(orgId);
    request.setUserId(userId);
    request.setStatus(status);

    when(mockService.createMembership(orgId, userId, status)).thenReturn(membership);

    Membership result = controller.createMembership(request);

    assertThat(result).isEqualTo(membership);
    verify(mockService).createMembership(orgId, userId, status);
  }

  @Test
  void createMembership_nullRequest_throwsException() {
    assertThat(
            org.junit.jupiter.api.Assertions.assertThrows(
                ResponseStatusException.class, () -> controller.createMembership(null)))
        .isNotNull();
  }

  @Test
  void createMembership_nullOrgId_throwsException() {
    MembershipRequest request = new MembershipRequest();
    request.setOrgId(null);
    request.setUserId("user-456");

    assertThat(
            org.junit.jupiter.api.Assertions.assertThrows(
                ResponseStatusException.class, () -> controller.createMembership(request)))
        .isNotNull();
  }

  @Test
  void createMembership_nullUserId_throwsException() {
    MembershipRequest request = new MembershipRequest();
    request.setOrgId("org-123");
    request.setUserId(null);

    assertThat(
            org.junit.jupiter.api.Assertions.assertThrows(
                ResponseStatusException.class, () -> controller.createMembership(request)))
        .isNotNull();
  }

  @Test
  void updateMembershipStatus_validRequest_updatesStatus() {
    String orgId = "org-123";
    String userId = "user-456";
    MembershipStatus newStatus = MembershipStatus.SUSPENDED;
    Membership updatedMembership = new Membership(orgId, userId, newStatus);

    StatusUpdateRequest request = new StatusUpdateRequest();
    request.setStatus(newStatus);

    // Mock the repository calls that the controller makes
    when(mockUserRepository.findById(userId))
        .thenReturn(
            Optional.of(
                new com.example.activityscheduler.user.model.User(
                    "test@example.com", "Test User")));
    when(mockOrgRepository.findById(orgId))
        .thenReturn(
            Optional.of(
                new com.example.activityscheduler.organization.model.Organization(
                    "Test Org", "user-123")));
    when(mockService.updateMembershipStatus(orgId, userId, newStatus))
        .thenReturn(updatedMembership);

    Membership result = controller.updateMembershipStatus(orgId, userId, request);

    assertThat(result).isEqualTo(updatedMembership);
    verify(mockService).updateMembershipStatus(orgId, userId, newStatus);
  }

  @Test
  void updateMembershipStatus_nullRequest_throwsException() {
    String orgId = "org-123";
    String userId = "user-456";

    assertThat(
            org.junit.jupiter.api.Assertions.assertThrows(
                ResponseStatusException.class,
                () -> controller.updateMembershipStatus(orgId, userId, null)))
        .isNotNull();
  }

  @Test
  void updateMembershipStatus_nullStatus_throwsException() {
    String orgId = "org-123";
    String userId = "user-456";
    StatusUpdateRequest request = new StatusUpdateRequest();
    request.setStatus(null);

    assertThat(
            org.junit.jupiter.api.Assertions.assertThrows(
                ResponseStatusException.class,
                () -> controller.updateMembershipStatus(orgId, userId, request)))
        .isNotNull();
  }

  @Test
  void updateMembershipStatus_userNotFound_throwsException() {
    String orgId = "org-123";
    String userId = "nonexistent-user";
    MembershipStatus newStatus = MembershipStatus.SUSPENDED;
    StatusUpdateRequest request = new StatusUpdateRequest();
    request.setStatus(newStatus);

    // Mock user not found
    when(mockUserRepository.findById(userId)).thenReturn(Optional.empty());

    assertThat(
            org.junit.jupiter.api.Assertions.assertThrows(
                ResponseStatusException.class,
                () -> controller.updateMembershipStatus(orgId, userId, request)))
        .isNotNull();
  }

  @Test
  void updateMembershipStatus_organizationNotFound_throwsException() {
    String orgId = "nonexistent-org";
    String userId = "user-456";
    MembershipStatus newStatus = MembershipStatus.SUSPENDED;
    StatusUpdateRequest request = new StatusUpdateRequest();
    request.setStatus(newStatus);

    // Mock user exists but organization not found
    when(mockUserRepository.findById(userId))
        .thenReturn(
            Optional.of(
                new com.example.activityscheduler.user.model.User(
                    "test@example.com", "Test User")));
    when(mockOrgRepository.findById(orgId)).thenReturn(Optional.empty());

    assertThat(
            org.junit.jupiter.api.Assertions.assertThrows(
                ResponseStatusException.class,
                () -> controller.updateMembershipStatus(orgId, userId, request)))
        .isNotNull();
  }

  @Test
  void updateMembershipStatus_invalidStatus_throwsException() {
    String orgId = "org-123";
    String userId = "user-456";
    MembershipStatus invalidStatus = MembershipStatus.INVITED; // INVITED is not allowed for updates
    StatusUpdateRequest request = new StatusUpdateRequest();
    request.setStatus(invalidStatus);

    // Mock both user and organization exist
    when(mockUserRepository.findById(userId))
        .thenReturn(
            Optional.of(
                new com.example.activityscheduler.user.model.User(
                    "test@example.com", "Test User")));
    when(mockOrgRepository.findById(orgId))
        .thenReturn(
            Optional.of(
                new com.example.activityscheduler.organization.model.Organization(
                    "Test Org", "user-123")));

    assertThat(
            org.junit.jupiter.api.Assertions.assertThrows(
                ResponseStatusException.class,
                () -> controller.updateMembershipStatus(orgId, userId, request)))
        .isNotNull();
  }

  @Test
  void updateMembershipStatus_serviceThrowsIllegalArgumentException_throwsBadRequest() {
    String orgId = "org-123";
    String userId = "user-456";
    MembershipStatus newStatus = MembershipStatus.SUSPENDED;
    StatusUpdateRequest request = new StatusUpdateRequest();
    request.setStatus(newStatus);

    // Mock both user and organization exist
    when(mockUserRepository.findById(userId))
        .thenReturn(
            Optional.of(
                new com.example.activityscheduler.user.model.User(
                    "test@example.com", "Test User")));
    when(mockOrgRepository.findById(orgId))
        .thenReturn(
            Optional.of(
                new com.example.activityscheduler.organization.model.Organization(
                    "Test Org", "user-123")));

    // Mock service throws IllegalArgumentException
    when(mockService.updateMembershipStatus(orgId, userId, newStatus))
        .thenThrow(new IllegalArgumentException("Invalid membership data"));

    assertThat(
            org.junit.jupiter.api.Assertions.assertThrows(
                ResponseStatusException.class,
                () -> controller.updateMembershipStatus(orgId, userId, request)))
        .isNotNull();
  }

  @Test
  void updateMembershipStatus_serviceThrowsIllegalStateException_throwsNotFound() {
    String orgId = "org-123";
    String userId = "user-456";
    MembershipStatus newStatus = MembershipStatus.SUSPENDED;
    StatusUpdateRequest request = new StatusUpdateRequest();
    request.setStatus(newStatus);

    // Mock both user and organization exist
    when(mockUserRepository.findById(userId))
        .thenReturn(
            Optional.of(
                new com.example.activityscheduler.user.model.User(
                    "test@example.com", "Test User")));
    when(mockOrgRepository.findById(orgId))
        .thenReturn(
            Optional.of(
                new com.example.activityscheduler.organization.model.Organization(
                    "Test Org", "user-123")));

    // Mock service throws IllegalStateException
    when(mockService.updateMembershipStatus(orgId, userId, newStatus))
        .thenThrow(new IllegalStateException("Membership not found"));

    assertThat(
            org.junit.jupiter.api.Assertions.assertThrows(
                ResponseStatusException.class,
                () -> controller.updateMembershipStatus(orgId, userId, request)))
        .isNotNull();
  }

  @Test
  void deleteMembership_validData_deletesMembership() {
    String orgId = "org-123";
    String userId = "user-456";

    controller.deleteMembership(orgId, userId);

    verify(mockService).deleteMembership(orgId, userId);
  }

  @Test
  void existsMembership_existingMembership_returnsTrue() {
    String orgId = "org-123";
    String userId = "user-456";

    when(mockService.existsMembership(orgId, userId)).thenReturn(true);

    boolean result = controller.existsMembership(orgId, userId);

    assertThat(result).isTrue();
  }

  @Test
  void existsMembership_nonExistentMembership_returnsFalse() {
    String orgId = "org-123";
    String userId = "user-456";

    when(mockService.existsMembership(orgId, userId)).thenReturn(false);

    boolean result = controller.existsMembership(orgId, userId);

    assertThat(result).isFalse();
  }

  @Test
  void countActiveMembers_returnsCount() {
    String orgId = "org-123";
    long expectedCount = 5L;

    when(mockService.countActiveMembers(orgId)).thenReturn(expectedCount);

    long result = controller.countActiveMembers(orgId);

    assertThat(result).isEqualTo(expectedCount);
  }

  @Test
  void countUserMemberships_returnsCount() {
    String userId = "user-456";
    long expectedCount = 3L;

    when(mockService.countUserMemberships(userId)).thenReturn(expectedCount);

    long result = controller.countUserMemberships(userId);

    assertThat(result).isEqualTo(expectedCount);
  }

  @Test
  void createMembership_serviceThrowsIllegalArgumentException_throwsBadRequest() {
    String orgId = "org-123";
    String userId = "user-456";
    MembershipStatus status = MembershipStatus.ACTIVE;

    MembershipRequest request = new MembershipRequest();
    request.setOrgId(orgId);
    request.setUserId(userId);
    request.setStatus(status);

    // Mock service throws IllegalArgumentException
    when(mockService.createMembership(orgId, userId, status))
        .thenThrow(new IllegalArgumentException("Invalid membership data"));

    assertThat(
            org.junit.jupiter.api.Assertions.assertThrows(
                ResponseStatusException.class, () -> controller.createMembership(request)))
        .isNotNull();
  }

  @Test
  void createMembership_serviceThrowsIllegalStateException_throwsConflict() {
    String orgId = "org-123";
    String userId = "user-456";
    MembershipStatus status = MembershipStatus.ACTIVE;

    MembershipRequest request = new MembershipRequest();
    request.setOrgId(orgId);
    request.setUserId(userId);
    request.setStatus(status);

    // Mock service throws IllegalStateException
    when(mockService.createMembership(orgId, userId, status))
        .thenThrow(new IllegalStateException("Membership already exists"));

    assertThat(
            org.junit.jupiter.api.Assertions.assertThrows(
                ResponseStatusException.class, () -> controller.createMembership(request)))
        .isNotNull();
  }
}
