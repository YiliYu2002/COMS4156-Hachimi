package com.example.activityscheduler.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.activityscheduler.user.controller.UserController;
import com.example.activityscheduler.user.model.User;
import com.example.activityscheduler.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;

class UserControllerTests {
  @Test
  void testGetAllUsers() {
    UserRepository mockRepo = Mockito.mock(UserRepository.class);
    Mockito.when(mockRepo.findAll()).thenReturn(List.of(new User("a@b.com", "Alice")));
    UserController ctrl = new UserController(mockRepo);

    List<User> users = ctrl.getAll();

    assertThat(users).hasSize(1);
    assertThat(users.get(0).getEmail()).isEqualTo("a@b.com");
  }

  @Test
  void testGetUserById() {
    UserRepository mockRepo = Mockito.mock(UserRepository.class);
    Mockito.when(mockRepo.findById("1")).thenReturn(Optional.of(new User("a@b.com", "Alice")));
    UserController ctrl = new UserController(mockRepo);

    Optional<User> user = ctrl.getById("1");

    assertThat(user).isPresent();
    assertThat(user.get().getEmail()).isEqualTo("a@b.com");
  }

  @Test
  void testExistsByEmail() {
    UserRepository mockRepo = Mockito.mock(UserRepository.class);
    Mockito.when(mockRepo.existsByEmail("a@b.com")).thenReturn(true);
    UserController ctrl = new UserController(mockRepo);

    boolean exists = ctrl.existsByEmail("a@b.com");
    assertThat(exists).isTrue();
  }

  @Test
  void testRegisterUser() {
    UserRepository mockRepo = Mockito.mock(UserRepository.class);
    Mockito.when(mockRepo.existsByEmail("a@b.com")).thenReturn(false);
    Mockito.when(mockRepo.save(Mockito.any(User.class)))
        .thenAnswer(inv -> inv.getArgument(0, User.class));
    UserController ctrl = new UserController(mockRepo);

    User user = ctrl.register(new User("a@b.com", "Alice"));
    assertThat(user).isNotNull();
    assertThat(user.getEmail()).isEqualTo("a@b.com");
  }

  @Test
  void testRegisterUserAlreadyExists() {
    UserRepository mockRepo = Mockito.mock(UserRepository.class);
    Mockito.when(mockRepo.existsByEmail("a@b.com")).thenReturn(true);
    UserController ctrl = new UserController(mockRepo);

    assertThrows(ResponseStatusException.class, () -> ctrl.register(new User("a@b.com", "Alice")));
  }

  @Test
  void testRegisterUserInvalidEmail() {
    UserRepository mockRepo = Mockito.mock(UserRepository.class);
    UserController ctrl = new UserController(mockRepo);
    assertThrows(ResponseStatusException.class, () -> ctrl.register(new User("", "Alice")));
  }

  @Test
  void testRegisterUserInvalidDisplayName() {
    UserRepository mockRepo = Mockito.mock(UserRepository.class);
    UserController ctrl = new UserController(mockRepo);
    assertThrows(ResponseStatusException.class, () -> ctrl.register(new User("a@b.com", "")));
  }

  @Test
  void testRegisterNullUser() {
    UserRepository mockRepo = Mockito.mock(UserRepository.class);
    UserController ctrl = new UserController(mockRepo);
    assertThrows(ResponseStatusException.class, () -> ctrl.register(null));
  }

  @Test
  void testRegisterNullEmail() {
    UserRepository mockRepo = Mockito.mock(UserRepository.class);
    UserController ctrl = new UserController(mockRepo);
    assertThrows(ResponseStatusException.class, () -> ctrl.register(new User(null, "Alice")));
  }

  @Test
  void testRegisterNullDisplayName() {
    UserRepository mockRepo = Mockito.mock(UserRepository.class);
    UserController ctrl = new UserController(mockRepo);
    assertThrows(ResponseStatusException.class, () -> ctrl.register(new User("a@b.com", null)));
  }
}
