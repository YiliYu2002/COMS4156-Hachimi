package com.example.activityscheduler.organization.service;

import com.example.activityscheduler.membership.model.MembershipStatus;
import com.example.activityscheduler.membership.service.MembershipService;
import com.example.activityscheduler.organization.model.Organization;
import com.example.activityscheduler.organization.repository.OrganizationRepository;
import com.example.activityscheduler.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing Organization entities. Provides business logic for organization
 * operations including CRUD operations and validation.
 */
@Service
@Transactional
public class OrganizationService {

  private final OrganizationRepository organizationRepository;
  private final MembershipService membershipService;
  private final UserRepository userRepository;

  /**
   * Constructs an OrganizationService with the given repository, membership service, and user
   * repository.
   *
   * @param organizationRepository the organization repository
   * @param membershipService the membership service
   * @param userRepository the user repository
   */
  public OrganizationService(
      OrganizationRepository organizationRepository,
      MembershipService membershipService,
      UserRepository userRepository) {
    this.organizationRepository = organizationRepository;
    this.membershipService = membershipService;
    this.userRepository = userRepository;
  }

  /**
   * Retrieves all organizations.
   *
   * @return a list of all organizations
   */
  @Transactional(readOnly = true)
  public List<Organization> getAllOrganizations() {
    return organizationRepository.findAll();
  }

  /**
   * Retrieves an organization by its ID.
   *
   * @param id the organization ID
   * @return an Optional containing the organization if found, empty otherwise
   */
  @Transactional(readOnly = true)
  public Optional<Organization> getOrganizationById(String id) {
    return organizationRepository.findById(id);
  }

  /**
   * Retrieves an organization by its name.
   *
   * @param name the organization name
   * @return an Optional containing the organization if found, empty otherwise
   */
  @Transactional(readOnly = true)
  public Optional<Organization> getOrganizationByName(String name) {
    return organizationRepository.findByName(name);
  }

  /**
   * Checks if an organization exists with the given name.
   *
   * @param name the organization name to check
   * @return true if an organization exists with this name, false otherwise
   */
  @Transactional(readOnly = true)
  public boolean existsByName(String name) {
    return organizationRepository.existsByName(name);
  }

  /**
   * Creates a new organization and automatically adds the creator as an active member.
   *
   * @param organization the organization to create
   * @return the created organization
   * @throws IllegalArgumentException if organization name is null or empty
   * @throws IllegalStateException if organization with the same name already exists
   */
  public Organization createOrganization(Organization organization) {
    if (organization == null) {
      throw new IllegalArgumentException("Organization cannot be null");
    }

    if (organization.getName() == null || organization.getName().trim().isEmpty()) {
      throw new IllegalArgumentException("Organization name cannot be null or empty");
    }

    if (organization.getCreatedBy() == null || organization.getCreatedBy().trim().isEmpty()) {
      throw new IllegalArgumentException("Created by cannot be null or empty");
    }

    // Validate that the user exists in the user table
    if (!userRepository.existsById(organization.getCreatedBy())) {
      throw new IllegalArgumentException(
          "User with ID '" + organization.getCreatedBy() + "' does not exist");
    }

    if (existsByName(organization.getName())) {
      throw new IllegalStateException(
          "Organization with name '" + organization.getName() + "' already exists");
    }

    // Save the organization first
    Organization savedOrganization = organizationRepository.save(organization);

    // Automatically create a membership for the organization creator
    try {
      membershipService.createMembership(
          savedOrganization.getId(), savedOrganization.getCreatedBy(), MembershipStatus.ACTIVE);
    } catch (Exception e) {
      // If membership creation fails, we should rollback the organization creation
      // This will be handled by the @Transactional annotation
      throw new IllegalStateException(
          "Failed to create membership for organization creator: " + e.getMessage(), e);
    }

    return savedOrganization;
  }

  /**
   * Updates an existing organization.
   *
   * @param id the organization ID
   * @param organization the updated organization data
   * @return the updated organization
   * @throws IllegalArgumentException if organization ID is null or organization data is invalid
   * @throws IllegalStateException if organization is not found or name conflict exists
   */
  public Organization updateOrganization(String id, Organization organization) {
    if (id == null || id.trim().isEmpty()) {
      throw new IllegalArgumentException("Organization ID cannot be null or empty");
    }

    if (organization == null) {
      throw new IllegalArgumentException("Organization cannot be null");
    }

    if (organization.getName() == null || organization.getName().trim().isEmpty()) {
      throw new IllegalArgumentException("Organization name cannot be null or empty");
    }

    if (organization.getCreatedBy() == null || organization.getCreatedBy().trim().isEmpty()) {
      throw new IllegalArgumentException("Created by cannot be null or empty");
    }

    // Check if another organization with the same name exists (excluding current one)
    Optional<Organization> existingWithName =
        organizationRepository.findByName(organization.getName());
    if (existingWithName.isPresent() && !existingWithName.get().getId().equals(id)) {
      throw new IllegalStateException(
          "Organization with name '" + organization.getName() + "' already exists");
    }

    Organization existingOrganization =
        organizationRepository
            .findById(id)
            .orElseThrow(
                () -> new IllegalStateException("Organization with ID '" + id + "' not found"));

    existingOrganization.setName(organization.getName());
    existingOrganization.setCreatedBy(organization.getCreatedBy());
    return organizationRepository.save(existingOrganization);
  }

  /**
   * Deletes an organization by its ID.
   *
   * @param id the organization ID
   * @throws IllegalArgumentException if organization ID is null or empty
   * @throws IllegalStateException if organization is not found
   */
  public void deleteOrganization(String id) {
    if (id == null || id.trim().isEmpty()) {
      throw new IllegalArgumentException("Organization ID cannot be null or empty");
    }

    if (!organizationRepository.existsById(id)) {
      throw new IllegalStateException("Organization with ID '" + id + "' not found");
    }

    organizationRepository.deleteById(id);
  }

  /**
   * Gets the total count of organizations.
   *
   * @return the total number of organizations
   */
  @Transactional(readOnly = true)
  public long getOrganizationCount() {
    return organizationRepository.count();
  }
}
