package com.example.activityscheduler.membership;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.activityscheduler.membership.model.MembershipStatus;
import org.junit.jupiter.api.Test;

class MembershipStatusTests {

  @Test
  void getValue_returnsCorrectDatabaseValue() {
    assertThat(MembershipStatus.ACTIVE.getValue()).isEqualTo("active");
    assertThat(MembershipStatus.INVITED.getValue()).isEqualTo("invited");
    assertThat(MembershipStatus.SUSPENDED.getValue()).isEqualTo("suspended");
  }

  @Test
  void fromValue_validValues_returnsCorrectStatus() {
    assertThat(MembershipStatus.fromValue("active")).isEqualTo(MembershipStatus.ACTIVE);
    assertThat(MembershipStatus.fromValue("invited")).isEqualTo(MembershipStatus.INVITED);
    assertThat(MembershipStatus.fromValue("suspended")).isEqualTo(MembershipStatus.SUSPENDED);
  }

  @Test
  void fromValue_invalidValue_throwsException() {
    assertThatThrownBy(() -> MembershipStatus.fromValue("invalid"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Unknown membership status: invalid");
  }

  @Test
  void fromValue_nullValue_throwsException() {
    assertThatThrownBy(() -> MembershipStatus.fromValue(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Unknown membership status: null");
  }

  @Test
  void fromValue_emptyValue_throwsException() {
    assertThatThrownBy(() -> MembershipStatus.fromValue(""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Unknown membership status: ");
  }

  @Test
  void fromValue_caseSensitive_throwsException() {
    assertThatThrownBy(() -> MembershipStatus.fromValue("Active"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Unknown membership status: Active");
  }
}
