package com.example.activityscheduler.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.activityscheduler.user.dto.UserRegistrationRequest;
import org.junit.jupiter.api.Test;

/** Unit tests for User DTOs. */
class UserDtoTests {

  @Test
  void testUserRegistrationRequestConstructor() {
    UserRegistrationRequest request = new UserRegistrationRequest("a@b.com", "Alice");
    assertThat(request.getEmail()).isEqualTo("a@b.com");
    assertThat(request.getDisplayName()).isEqualTo("Alice");
  }

  @Test
  void testUserRegistrationRequestDefaultConstructor() {
    UserRegistrationRequest request = new UserRegistrationRequest();
    assertThat(request.getEmail()).isNull();
    assertThat(request.getDisplayName()).isNull();
  }

  @Test
  void testUserRegistrationRequestSetters() {
    UserRegistrationRequest request = new UserRegistrationRequest();
    request.setEmail("test@example.com");
    request.setDisplayName("Test User");

    assertThat(request.getEmail()).isEqualTo("test@example.com");
    assertThat(request.getDisplayName()).isEqualTo("Test User");
  }

  @Test
  void testUserRegistrationRequestToString() {
    UserRegistrationRequest request = new UserRegistrationRequest("user@test.com", "John Doe");
    String toString = request.toString();

    assertThat(toString).contains("UserRegistrationRequest");
    assertThat(toString).contains("user@test.com");
    assertThat(toString).contains("John Doe");
  }

  @Test
  void testUserRegistrationRequestWithNullValues() {
    UserRegistrationRequest request = new UserRegistrationRequest(null, null);
    assertThat(request.getEmail()).isNull();
    assertThat(request.getDisplayName()).isNull();
  }

  @Test
  void testUserRegistrationRequestWithEmptyStrings() {
    UserRegistrationRequest request = new UserRegistrationRequest("", "");
    assertThat(request.getEmail()).isEmpty();
    assertThat(request.getDisplayName()).isEmpty();
  }
}
