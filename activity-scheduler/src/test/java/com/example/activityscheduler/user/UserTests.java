package com.example.activityscheduler.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.activityscheduler.user.model.User;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserTests {

  @Test
  void defaultConstructor_generatesId_setsDefaults() {
    User user = new User();

    assertThat(user.getId()).isNotNull().isNotBlank();
    assertThat(user.isActive()).isTrue();
    assertThat(user.getCreatedAt()).isNotNull();

    // validate UUID format (36 chars with hyphens)
    assertThat(user.getId().length()).isEqualTo(36);
    assertThat(UUID.fromString(user.getId())).isNotNull();
  }

  @Test
  void parameterizedConstructor_setsEmailAndDisplayName() {
    User user = new User("alice@example.com", "Alice");

    assertThat(user.getEmail()).isEqualTo("alice@example.com");
    assertThat(user.getDisplayName()).isEqualTo("Alice");
    assertThat(user.getId()).isNotBlank();
  }

  @Test
  void setters_updateFields() {
    User user = new User();
    user.setEmail("bob@example.com");
    user.setDisplayName("Bob");
    user.setActive(false);

    assertThat(user.getEmail()).isEqualTo("bob@example.com");
    assertThat(user.getDisplayName()).isEqualTo("Bob");
    assertThat(user.isActive()).isFalse();
  }

  @Test
  void createdAt_canBeOverridden() {
    User user = new User();
    LocalDateTime ts = LocalDateTime.of(2024, 1, 2, 3, 4, 5);
    user.setCreatedAt(ts);
    assertThat(user.getCreatedAt()).isEqualTo(ts);
  }
}
