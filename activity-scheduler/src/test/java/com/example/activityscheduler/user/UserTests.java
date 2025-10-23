package com.example.activityscheduler.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.activityscheduler.user.model.User;
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
    user.setDisplayName("Bob");
    user.setActive(false);

    assertThat(user.getDisplayName()).isEqualTo("Bob");
    assertThat(user.isActive()).isFalse();
  }
}
