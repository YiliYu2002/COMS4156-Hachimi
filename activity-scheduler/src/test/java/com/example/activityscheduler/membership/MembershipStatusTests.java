package com.example.activityscheduler.membership;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.activityscheduler.membership.model.MembershipStatus;
import org.junit.jupiter.api.Test;

class MembershipStatusTests {

  @Test
  void getValue_returnsCorrectDatabaseValue() {
    assertThat(MembershipStatus.ACTIVE.getValue()).isEqualTo("ACTIVE");
    assertThat(MembershipStatus.INVITED.getValue()).isEqualTo("INVITED");
    assertThat(MembershipStatus.SUSPENDED.getValue()).isEqualTo("SUSPENDED");
  }

  @Test
  void fromString_validValues_returnsCorrectStatus() {
    assertThat(MembershipStatus.fromString("active")).isEqualTo(MembershipStatus.ACTIVE);
    assertThat(MembershipStatus.fromString("invited")).isEqualTo(MembershipStatus.INVITED);
    assertThat(MembershipStatus.fromString("suspended")).isEqualTo(MembershipStatus.SUSPENDED);
    assertThat(MembershipStatus.fromString("ACTIVE")).isEqualTo(MembershipStatus.ACTIVE);
    assertThat(MembershipStatus.fromString("INVITED")).isEqualTo(MembershipStatus.INVITED);
    assertThat(MembershipStatus.fromString("SUSPENDED")).isEqualTo(MembershipStatus.SUSPENDED);
  }

  @Test
  void fromString_invalidValue_throwsException() {
    assertThatThrownBy(() -> MembershipStatus.fromString("invalid"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Unknown membership status: invalid");
  }

  @Test
  void fromString_nullValue_returnsNull() {
    assertThat(MembershipStatus.fromString(null)).isNull();
  }

  @Test
  void fromString_emptyValue_throwsException() {
    assertThatThrownBy(() -> MembershipStatus.fromString(""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Unknown membership status: ");
  }

  @Test
  void fromString_caseInsensitive_works() {
    assertThat(MembershipStatus.fromString("Active")).isEqualTo(MembershipStatus.ACTIVE);
    assertThat(MembershipStatus.fromString("INVITED")).isEqualTo(MembershipStatus.INVITED);
    assertThat(MembershipStatus.fromString("suspended")).isEqualTo(MembershipStatus.SUSPENDED);
  }
}
