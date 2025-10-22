package com.example.activityscheduler.organization.service;

import com.example.activityscheduler.membership.model.MembershipStatus;
import com.example.activityscheduler.membership.service.MembershipService;
import com.example.activityscheduler.organization.model.Organization;
import com.example.activityscheduler.organization.repository.OrganizationRepository;
import com.example.activityscheduler.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
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
  private static final Logger logger = Logger.getLogger(OrganizationService.class.getName());

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
    logger.info("Retrieving all organizations");
    List<Organization> organizations = organizationRepository.findAll();
    logger.info("Retrieved " + organizations.size() + " organizations");
    return organizations;
  }

  /**
   * Retrieves an organization by its ID.
   *
   * @param id the organization ID
   * @return an Optional containing the organization if found, empty otherwise
   */
  @Transactional(readOnly = true)
  public Optional<Organization> getOrganizationById(String id) {
    logger.info("Retrieving organization with ID: " + id);
    Optional<Organization> organization = organizationRepository.findById(id);
    if (organization.isPresent()) {
      logger.info("Organization found: " + organization.get().getName());
    } else {
      logger.info("Organization not found with ID: " + id);
    }
    return organization;
  }

  /**
   * Retrieves an organization by its name.
   *
   * @param name the organization name
   * @return an Optional containing the organization if found, empty otherwise
   */
  @Transactional(readOnly = true)
  public Optional<Organization> getOrganizationByName(String name) {
    logger.info("Retrieving organization with name: " + name);
    Optional<Organization> organization = organizationRepository.findByName(name);
    if (organization.isPresent()) {
      logger.info("Organization found: " + organization.get().getName());
    } else {
      logger.info("Organization not found with name: " + name);
    }
    return organization;
  }

  /**
   * Checks if an organization exists with the given name.
   *
   * @param name the organization name to check
   * @return true if an organization exists with this name, false otherwise
   */
  @Transactional(readOnly = true)
  public boolean existsByName(String name) {
    logger.info("Checking if organization exists with name: " + name);
    boolean exists = organizationRepository.existsByName(name);
    logger.info("Organization exists: " + exists);
    return exists;
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
    logger.info("Saving organization: " + organization.getName());
    Organization savedOrganization = organizationRepository.save(organization);
    logger.info("Organization saved: " + savedOrganization.getName());

    // Automatically create a membership for the organization creator
    try {
      logger.info(
          "Creating membership for organization creator: " + savedOrganization.getCreatedBy());
      membershipService.createMembership(
          savedOrganization.getId(), savedOrganization.getCreatedBy(), MembershipStatus.ACTIVE);
      logger.info(
          "Membership created for organization creator: " + savedOrganization.getCreatedBy());
    } catch (Exception e) {
      logger.warning("Failed to create membership for organization creator: " + e.getMessage());
      // If membership creation fails, we should rollback the organization creation
      // This will be handled by the @Transactional annotation
      throw new IllegalStateException(
          "Failed to create membership for organization creator: " + e.getMessage(), e);
    }
    logger.info(
        "Organization created and membership created for organization creator: "
            + savedOrganization.getCreatedBy());
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
      logger.warning("Organization ID cannot be null or empty");
      throw new IllegalArgumentException("Organization ID cannot be null or empty");
    }

    if (organization == null) {
      logger.warning("Organization cannot be null");
      throw new IllegalArgumentException("Organization cannot be null");
    }

    if (organization.getName() == null || organization.getName().trim().isEmpty()) {
      logger.warning("Organization name cannot be null or empty");
      throw new IllegalArgumentException("Organization name cannot be null or empty");
    }

    if (organization.getCreatedBy() == null || organization.getCreatedBy().trim().isEmpty()) {
      logger.warning("Created by cannot be null or empty");
      throw new IllegalArgumentException("Created by cannot be null or empty");
    }

    // Check if another organization with the same name exists (excluding current one)
    Optional<Organization> existingWithName =
        organizationRepository.findByName(organization.getName());
    if (existingWithName.isPresent() && !existingWithName.get().getId().equals(id)) {
      logger.warning("Organization with name '" + organization.getName() + "' already exists");
      throw new IllegalStateException(
          "Organization with name '" + organization.getName() + "' already exists");
    }

    logger.info("Updating organization: " + id);
    Organization existingOrganization =
        organizationRepository
            .findById(id)
            .orElseThrow(
                () -> new IllegalStateException("Organization with ID '" + id + "' not found"));

    existingOrganization.setName(organization.getName());
    existingOrganization.setCreatedBy(organization.getCreatedBy());
    logger.info("Updating organization: " + existingOrganization.getName());
    Organization updatedOrganization = organizationRepository.save(existingOrganization);
    logger.info("Organization updated: " + updatedOrganization.getName());
    return updatedOrganization;
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
      logger.warning("Organization ID cannot be null or empty");
      throw new IllegalArgumentException("Organization ID cannot be null or empty");
    }

    if (!organizationRepository.existsById(id)) {
      logger.warning("Organization with ID '" + id + "' not found");
      throw new IllegalStateException("Organization with ID '" + id + "' not found");
    }

    organizationRepository.deleteById(id);
    logger.info("Organization deleted: " + id);
  }

  /**
   * Gets the total count of organizations.
   *
   * @return the total number of organizations
   */
  @Transactional(readOnly = true)
  public long getOrganizationCount() {
    logger.info("Getting organization count");
    long count = organizationRepository.count();
    logger.info("Organization count: " + count);
    return count;
  }
}
