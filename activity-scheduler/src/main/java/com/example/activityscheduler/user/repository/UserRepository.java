package com.example.activityscheduler.user.repository;

import com.example.activityscheduler.user.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for User entity operations. Provides data access methods for User entities
 * using Spring Data JPA.
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {
  /**
   * Finds a user by their email address.
   *
   * @param email the email address to search for
   * @return an Optional containing the user if found, empty otherwise
   */
  Optional<User> findByEmail(String email);

  /**
   * Checks if a user exists with the given email address.
   *
   * @param email the email address to check
   * @return true if a user exists with this email, false otherwise
   */
  boolean existsByEmail(String email);
}
