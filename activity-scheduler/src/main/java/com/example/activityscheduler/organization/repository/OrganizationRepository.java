package com.example.activityscheduler.organization.repository;

import com.example.activityscheduler.organization.model.Organization;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Organization entity operations. Provides data access methods for
 * Organization entities using Spring Data JPA.
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, String> {

  /**
   * Finds an organization by its name.
   *
   * @param name the organization name to search for
   * @return an Optional containing the organization if found, empty otherwise
   */
  Optional<Organization> findByName(String name);

  /**
   * Checks if an organization exists with the given name.
   *
   * @param name the organization name to check
   * @return true if an organization exists with this name, false otherwise
   */
  boolean existsByName(String name);
}
