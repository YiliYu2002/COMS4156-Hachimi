package com.example.activityscheduler.organization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.activityscheduler.membership.model.MembershipStatus;
import com.example.activityscheduler.membership.service.MembershipService;
import com.example.activityscheduler.organization.model.Organization;
import com.example.activityscheduler.organization.repository.OrganizationRepository;
import com.example.activityscheduler.organization.service.OrganizationService;
import com.example.activityscheduler.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for Organization entity and service layer. */
@ExtendWith(MockitoExtension.class)
class OrganizationTests {

  @Mock private OrganizationRepository organizationRepository;
  @Mock private MembershipService membershipService;
  @Mock private UserRepository userRepository;

  @InjectMocks private OrganizationService organizationService;

  private Organization testOrganization;

  @BeforeEach
  void setUp() {
    testOrganization = new Organization("user123", "Test Organization");
  }

  @Test
  void testOrganizationConstructor() {
    // Test constructor with createdBy and name
    Organization orgWithName = new Organization("user123", "Test Org");
    assertNotNull(orgWithName.getId());
    assertNotNull(orgWithName.getCreatedAt());
    assertEquals("Test Org", orgWithName.getName());
    assertEquals("user123", orgWithName.getCreatedBy());
  }

  @Test
  void testOrganizationGettersAndSetters() {
    Organization org = new Organization("user123", "Test Organization");
    String testName = "Updated Organization";
    String testCreatedBy = "user456";
    LocalDateTime testTime = LocalDateTime.now();

    org.setName(testName);
    org.setCreatedBy(testCreatedBy);
    org.setCreatedAt(testTime);

    assertEquals(testName, org.getName());
    assertEquals(testCreatedBy, org.getCreatedBy());
    assertEquals(testTime, org.getCreatedAt());
  }

  @Test
  void testOrganizationToString() {
    Organization org = new Organization("user123", "Test Organization");
    String toString = org.toString();

    assertTrue(toString.contains("Test Organization"));
    assertTrue(toString.contains("user123"));
    assertTrue(toString.contains(org.getId()));
  }

  @Test
  void testGetAllOrganizations() {
    // Given
    List<Organization> organizations =
        Arrays.asList(new Organization("user1", "Org 1"), new Organization("user2", "Org 2"));
    when(organizationRepository.findAll()).thenReturn(organizations);

    // When
    List<Organization> result = organizationService.getAllOrganizations();

    // Then
    assertEquals(2, result.size());
    verify(organizationRepository).findAll();
  }

  @Test
  void testGetOrganizationById() {
    // Given
    String orgId = "test-id";
    when(organizationRepository.findById(orgId)).thenReturn(Optional.of(testOrganization));

    // When
    Optional<Organization> result = organizationService.getOrganizationById(orgId);

    // Then
    assertTrue(result.isPresent());
    assertEquals(testOrganization, result.get());
    verify(organizationRepository).findById(orgId);
  }

  @Test
  void testGetOrganizationByIdNotFound() {
    // Given
    String orgId = "non-existent-id";
    when(organizationRepository.findById(orgId)).thenReturn(Optional.empty());

    // When
    Optional<Organization> result = organizationService.getOrganizationById(orgId);

    // Then
    assertFalse(result.isPresent());
    verify(organizationRepository).findById(orgId);
  }

  @Test
  void testGetOrganizationByName() {
    // Given
    String orgName = "Test Organization";
    when(organizationRepository.findByName(orgName)).thenReturn(Optional.of(testOrganization));

    // When
    Optional<Organization> result = organizationService.getOrganizationByName(orgName);

    // Then
    assertTrue(result.isPresent());
    assertEquals(testOrganization, result.get());
    verify(organizationRepository).findByName(orgName);
  }

  @Test
  void testExistsByName() {
    // Given
    String orgName = "Test Organization";
    when(organizationRepository.existsByName(orgName)).thenReturn(true);

    // When
    boolean result = organizationService.existsByName(orgName);

    // Then
    assertTrue(result);
    verify(organizationRepository).existsByName(orgName);
  }

  @Test
  void testCreateOrganizationSuccess() {
    // Given
    when(organizationRepository.existsByName(anyString())).thenReturn(false);
    when(userRepository.existsById(testOrganization.getCreatedBy())).thenReturn(true);
    when(organizationRepository.save(any(Organization.class))).thenReturn(testOrganization);

    // When
    Organization result = organizationService.createOrganization(testOrganization);

    // Then
    assertEquals(testOrganization, result);
    verify(organizationRepository).existsByName(testOrganization.getName());
    verify(userRepository).existsById(testOrganization.getCreatedBy());
    verify(organizationRepository).save(testOrganization);
    // Verify that a membership is automatically created for the organization creator
    verify(membershipService)
        .createMembership(
            testOrganization.getId(), testOrganization.getCreatedBy(), MembershipStatus.ACTIVE);
  }

  @Test
  void testCreateOrganizationWithNullName() {
    // Given
    Organization orgWithNullName = new Organization("user123", "Test Org");
    orgWithNullName.setName(null);

    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          organizationService.createOrganization(orgWithNullName);
        });
  }

  @Test
  void testCreateOrganizationWithEmptyName() {
    // Given
    Organization orgWithEmptyName = new Organization("user123", "Test Org");
    orgWithEmptyName.setName("   ");

    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          organizationService.createOrganization(orgWithEmptyName);
        });
  }

  @Test
  void testCreateOrganizationWithNullCreatedBy() {
    // Given
    Organization orgWithNullCreatedBy = new Organization("user123", "Test Org");
    orgWithNullCreatedBy.setCreatedBy(null);

    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          organizationService.createOrganization(orgWithNullCreatedBy);
        });
  }

  @Test
  void testCreateOrganizationWithEmptyCreatedBy() {
    // Given
    Organization orgWithEmptyCreatedBy = new Organization("user123", "Test Org");
    orgWithEmptyCreatedBy.setCreatedBy("   ");

    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          organizationService.createOrganization(orgWithEmptyCreatedBy);
        });
  }

  @Test
  void testCreateOrganizationWithExistingName() {
    // Given
    when(userRepository.existsById(testOrganization.getCreatedBy())).thenReturn(true);
    when(organizationRepository.existsByName(testOrganization.getName())).thenReturn(true);

    // When & Then
    assertThrows(
        IllegalStateException.class,
        () -> {
          organizationService.createOrganization(testOrganization);
        });
  }

  @Test
  void testCreateOrganizationWithNonExistentUser() {
    // Given
    when(userRepository.existsById(testOrganization.getCreatedBy())).thenReturn(false);

    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          organizationService.createOrganization(testOrganization);
        });

    // Verify that user existence was checked but organization was not saved
    verify(userRepository).existsById(testOrganization.getCreatedBy());
    verify(organizationRepository, never()).existsByName(anyString());
    verify(organizationRepository, never()).save(any(Organization.class));
    verify(membershipService, never())
        .createMembership(anyString(), anyString(), any(MembershipStatus.class));
  }

  @Test
  void testCreateOrganizationWithMembershipCreationFailure() {
    // Given
    when(organizationRepository.existsByName(anyString())).thenReturn(false);
    when(userRepository.existsById(testOrganization.getCreatedBy())).thenReturn(true);
    when(organizationRepository.save(any(Organization.class))).thenReturn(testOrganization);
    when(membershipService.createMembership(anyString(), anyString(), any(MembershipStatus.class)))
        .thenThrow(new IllegalStateException("Membership creation failed"));

    // When & Then
    assertThrows(
        IllegalStateException.class,
        () -> {
          organizationService.createOrganization(testOrganization);
        });

    // Verify that organization was saved but membership creation failed
    verify(organizationRepository).save(testOrganization);
    verify(membershipService)
        .createMembership(
            testOrganization.getId(), testOrganization.getCreatedBy(), MembershipStatus.ACTIVE);
  }

  @Test
  void testUpdateOrganizationSuccess() {
    // Given
    String orgId = "test-id";
    Organization updatedOrg = new Organization("user456", "Updated Organization");
    when(organizationRepository.findById(orgId)).thenReturn(Optional.of(testOrganization));
    when(organizationRepository.findByName(updatedOrg.getName())).thenReturn(Optional.empty());
    when(organizationRepository.save(any(Organization.class))).thenReturn(updatedOrg);

    // When
    Organization result = organizationService.updateOrganization(orgId, updatedOrg);

    // Then
    assertEquals(updatedOrg.getName(), result.getName());
    verify(organizationRepository).findById(orgId);
    verify(organizationRepository).findByName(updatedOrg.getName());
    verify(organizationRepository).save(any(Organization.class));
  }

  @Test
  void testUpdateOrganizationNotFound() {
    // Given
    String orgId = "non-existent-id";
    when(organizationRepository.findById(orgId)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(
        IllegalStateException.class,
        () -> {
          organizationService.updateOrganization(orgId, testOrganization);
        });
  }

  @Test
  void testDeleteOrganizationSuccess() {
    // Given
    String orgId = "test-id";
    when(organizationRepository.existsById(orgId)).thenReturn(true);

    // When
    organizationService.deleteOrganization(orgId);

    // Then
    verify(organizationRepository).existsById(orgId);
    verify(organizationRepository).deleteById(orgId);
  }

  @Test
  void testDeleteOrganizationNotFound() {
    // Given
    String orgId = "non-existent-id";
    when(organizationRepository.existsById(orgId)).thenReturn(false);

    // When & Then
    assertThrows(
        IllegalStateException.class,
        () -> {
          organizationService.deleteOrganization(orgId);
        });
  }

  @Test
  void testGetOrganizationCount() {
    // Given
    long expectedCount = 5L;
    when(organizationRepository.count()).thenReturn(expectedCount);

    // When
    long result = organizationService.getOrganizationCount();

    // Then
    assertEquals(expectedCount, result);
    verify(organizationRepository).count();
  }
}
