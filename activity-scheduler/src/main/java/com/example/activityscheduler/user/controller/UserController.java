package com.example.activityscheduler.user.controller;

import com.example.activityscheduler.user.dto.UserRegistrationRequest;
import com.example.activityscheduler.user.model.User;
import com.example.activityscheduler.user.repository.UserRepository;
import com.example.activityscheduler.user.utils.EmailValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** REST controller for managing User entities. Provides HTTP endpoints for user operations. */
@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {

  private final UserRepository repo;
  private static final Logger logger = Logger.getLogger(UserController.class.getName());

  /**
   * Constructs a UserController with the given repository.
   *
   * @param repo the user repository
   */
  public UserController(UserRepository repo) {
    this.repo = repo;
  }

  /**
   * Retrieves all users.
   *
   * @return a list of all users
   */
  @Operation(summary = "Get all users", description = "Retrieves a list of all users in the system")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all users")
      })
  @GetMapping
  public List<User> getAll() {
    logger.info("Retrieving all users");
    List<User> users = repo.findAll();
    logger.info("Retrieved " + users.size() + " users");
    return users;
  }

  /**
   * Retrieves a user by their ID.
   *
   * @param id the user ID
   * @return an Optional containing the user if found, empty otherwise
   */
  @Operation(
      summary = "Get user by ID",
      description = "Retrieves a specific user by their unique identifier")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "User found successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
      })
  @GetMapping("/{id}")
  public Optional<User> getById(@Parameter(description = "User ID") @PathVariable String id) {
    logger.info("Retrieving user with ID: " + id);
    Optional<User> user = repo.findById(id);
    if (user.isPresent()) {
      logger.info("User found: " + user.get().getEmail());
    } else {
      logger.info("User not found with ID: " + id);
    }
    return user;
  }

  /**
   * Checks if a user exists with the given email.
   *
   * @param email the email to check
   * @return true if a user exists with this email, false otherwise
   */
  @Operation(
      summary = "Check if user exists by email",
      description = "Checks whether a user with the given email address exists")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Email check completed successfully")
      })
  @GetMapping("/exists")
  public boolean existsByEmail(
      @Parameter(description = "Email address to check") @RequestParam String email) {
    logger.info("Checking if user exists with email: " + email);
    boolean exists = repo.existsByEmail(email);
    logger.info("User exists: " + exists);
    return exists;
  }

  /**
   * Allows a new user to be registered by providing an email address and display name.
   *
   * @param request the user registration request
   * @return the created user
   */
  @Operation(
      summary = "Create a new user by email",
      description =
          "Creates a new user in the system by providing an email address and display name")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid user data"),
        @ApiResponse(responseCode = "409", description = "User already exists")
      })
  @PostMapping("/register")
  public User register(@RequestBody UserRegistrationRequest request) {
    // Basic validation to ensure required fields are present
    if (request == null
        || request.getEmail() == null
        || request.getEmail().isBlank()
        || request.getDisplayName() == null
        || request.getDisplayName().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user data");
    }
    if (!EmailValidator.isValidEmail(request.getEmail())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email address");
    }
    if (repo.existsByEmail(request.getEmail())) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
    }

    // Create a new User entity with auto-generated ID
    User user = new User(request.getEmail(), request.getDisplayName());
    logger.info("Saving user: " + user.getEmail());
    User savedUser = repo.save(user);
    logger.info("User saved: " + savedUser.getEmail());
    return savedUser;
  }

  /**
   * Updates an existing user's username.
   *
   * @param id the user ID
   * @return the updated user
   */
  @Operation(summary = "Update user username", description = "Updates an existing user's username")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid user data"),
        @ApiResponse(responseCode = "404", description = "User not found")
      })
  @PutMapping("/{id}/username")
  public User updateUsername(
      @Parameter(description = "User ID") @PathVariable String id,
      @RequestBody String displayName) {
    User user =
        repo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    if (displayName == null || displayName.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid display name");
    }
    user.setDisplayName(displayName);
    return repo.save(user);
  }
}
