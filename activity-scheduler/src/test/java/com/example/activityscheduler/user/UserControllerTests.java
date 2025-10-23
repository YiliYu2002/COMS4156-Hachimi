package com.example.activityscheduler.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.activityscheduler.user.controller.UserController;
import com.example.activityscheduler.user.dto.UserRegistrationRequest;
import com.example.activityscheduler.user.model.User;
import com.example.activityscheduler.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;

class UserControllerTests {

  private UserRepository mockRepo;
  private UserController controller;

  @BeforeEach
  void setUp() {
    mockRepo = Mockito.mock(UserRepository.class);
    controller = new UserController(mockRepo);
  }

  @Test
  void testGetAllUsers() {
    Mockito.when(mockRepo.findAll()).thenReturn(List.of(new User("a@b.com", "Alice")));

    List<User> users = controller.getAll();

    assertThat(users).hasSize(1);
    assertThat(users.get(0).getEmail()).isEqualTo("a@b.com");
  }

  @Test
  void testGetUserById() {
    Mockito.when(mockRepo.findById("1")).thenReturn(Optional.of(new User("a@b.com", "Alice")));

    Optional<User> user = controller.getById("1");

    assertThat(user).isPresent();
    assertThat(user.get().getEmail()).isEqualTo("a@b.com");
  }

  @Test
  void testExistsByEmail() {
    Mockito.when(mockRepo.existsByEmail("a@b.com")).thenReturn(true);

    boolean exists = controller.existsByEmail("a@b.com");
    assertThat(exists).isTrue();
  }

  @Test
  void testRegisterUser() {
    Mockito.when(mockRepo.existsByEmail("a@b.com")).thenReturn(false);
    Mockito.when(mockRepo.save(Mockito.any(User.class)))
        .thenAnswer(inv -> inv.getArgument(0, User.class));

    UserRegistrationRequest request = new UserRegistrationRequest("a@b.com", "Alice");
    User user = controller.register(request);
    assertThat(user).isNotNull();
    assertThat(user.getEmail()).isEqualTo("a@b.com");
  }

  @Test
  void testRegisterUserAlreadyExists() {
    Mockito.when(mockRepo.existsByEmail("a@b.com")).thenReturn(true);

    UserRegistrationRequest request = new UserRegistrationRequest("a@b.com", "Alice");
    assertThrows(ResponseStatusException.class, () -> controller.register(request));
  }

  @Test
  void testRegisterUserInvalidEmail() {
    UserRegistrationRequest request = new UserRegistrationRequest("", "Alice");
    assertThrows(ResponseStatusException.class, () -> controller.register(request));
  }

  @Test
  void testRegisterUserInvalidDisplayName() {
    UserRegistrationRequest request = new UserRegistrationRequest("a@b.com", "");
    assertThrows(ResponseStatusException.class, () -> controller.register(request));
  }

  @Test
  void testRegisterNullUser() {
    assertThrows(ResponseStatusException.class, () -> controller.register(null));
  }

  @Test
  void testRegisterNullEmail() {
    UserRegistrationRequest request = new UserRegistrationRequest(null, "Alice");
    assertThrows(ResponseStatusException.class, () -> controller.register(request));
  }

  @Test
  void testRegisterNullDisplayName() {
    UserRegistrationRequest request = new UserRegistrationRequest("a@b.com", null);
    assertThrows(ResponseStatusException.class, () -> controller.register(request));
  }

  @Test
  void testGetUserByIdNotFound() {
    Mockito.when(mockRepo.findById("nonexistent")).thenReturn(Optional.empty());

    Optional<User> user = controller.getById("nonexistent");

    assertThat(user).isEmpty();
  }

  @Test
  void testGetUserByIdInvalidId() {
    Mockito.when(mockRepo.findById("")).thenReturn(Optional.empty());

    Optional<User> user = controller.getById("");

    assertThat(user).isEmpty();
  }

  @Test
  void testExistsByEmailNotFound() {
    Mockito.when(mockRepo.existsByEmail("nonexistent@example.com")).thenReturn(false);

    boolean exists = controller.existsByEmail("nonexistent@example.com");
    assertThat(exists).isFalse();
  }

  @Test
  void testExistsByEmailInvalidEmail() {
    Mockito.when(mockRepo.existsByEmail("")).thenReturn(false);

    boolean exists = controller.existsByEmail("");
    assertThat(exists).isFalse();
  }

  @Test
  void testGetAllUsersEmpty() {
    Mockito.when(mockRepo.findAll()).thenReturn(List.of());

    List<User> users = controller.getAll();

    assertThat(users).isEmpty();
  }

  @Test
  void testGetAllUsersServiceException() {
    Mockito.when(mockRepo.findAll()).thenThrow(new RuntimeException("Database error"));

    assertThrows(RuntimeException.class, () -> controller.getAll());
  }

  @Test
  void testUpdateUsername() {
    UserRepository mockRepo = Mockito.mock(UserRepository.class);
    User testUser = new User("a@b.com", "Alice");
    Mockito.when(mockRepo.findById("1")).thenReturn(Optional.of(testUser));
    Mockito.when(mockRepo.save(Mockito.any(User.class)))
        .thenAnswer(inv -> inv.getArgument(0, User.class));
    UserController ctrl = new UserController(mockRepo);

    User user = ctrl.updateUsername("1", "Bob");
    assertThat(user).isNotNull();
    assertThat(user.getDisplayName()).isEqualTo("Bob");
  }

  @Test
  void testUpdateUsernameNotFound() {
    UserRepository mockRepo = Mockito.mock(UserRepository.class);
    UserController ctrl = new UserController(mockRepo);
    assertThrows(ResponseStatusException.class, () -> ctrl.updateUsername("1", "Bob"));
  }

  @Test
  void testUpdateUsernameInvalidDisplayName() {
    UserRepository mockRepo = Mockito.mock(UserRepository.class);
    User testUser = new User("a@b.com", "Alice");
    Mockito.when(mockRepo.findById("1")).thenReturn(Optional.of(testUser));
    UserController ctrl = new UserController(mockRepo);

    assertThrows(ResponseStatusException.class, () -> ctrl.updateUsername("1", null));

    assertThrows(ResponseStatusException.class, () -> ctrl.updateUsername("1", ""));

    assertThrows(ResponseStatusException.class, () -> ctrl.updateUsername("1", "   "));
  }

  @Test
  void testRegisterWithInvalidEmail() {
    UserRepository mockRepo = Mockito.mock(UserRepository.class);
    UserController ctrl = new UserController(mockRepo);
    UserRegistrationRequest request = new UserRegistrationRequest("invalid-email", "Alice");
    assertThrows(ResponseStatusException.class, () -> ctrl.register(request));
  }
}
