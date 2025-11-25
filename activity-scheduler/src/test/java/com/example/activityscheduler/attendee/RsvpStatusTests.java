package com.example.activityscheduler.attendee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.activityscheduler.attendee.model.RsvpStatus;
import org.junit.jupiter.api.Test;

class RsvpStatusTests {

  @Test
  void getValue_returnsCorrectDatabaseValue() {
    assertThat(RsvpStatus.PENDING.getValue()).isEqualTo("pending");
    assertThat(RsvpStatus.YES.getValue()).isEqualTo("yes");
    assertThat(RsvpStatus.NO.getValue()).isEqualTo("no");
  }

  @Test
  void fromString_validValues_returnsCorrectStatus() {
    assertThat(RsvpStatus.fromString("pending")).isEqualTo(RsvpStatus.PENDING);
    assertThat(RsvpStatus.fromString("yes")).isEqualTo(RsvpStatus.YES);
    assertThat(RsvpStatus.fromString("no")).isEqualTo(RsvpStatus.NO);
    assertThat(RsvpStatus.fromString("PENDING")).isEqualTo(RsvpStatus.PENDING);
    assertThat(RsvpStatus.fromString("YES")).isEqualTo(RsvpStatus.YES);
    assertThat(RsvpStatus.fromString("NO")).isEqualTo(RsvpStatus.NO);
  }

  @Test
  void fromString_invalidValue_throwsException() {
    assertThatThrownBy(() -> RsvpStatus.fromString("invalid"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Unknown RSVP status: invalid");
  }

  @Test
  void fromString_nullValue_returnsNull() {
    assertThat(RsvpStatus.fromString(null)).isNull();
  }

  @Test
  void fromString_emptyValue_throwsException() {
    assertThatThrownBy(() -> RsvpStatus.fromString(""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Unknown RSVP status: ");
  }

  @Test
  void fromString_caseInsensitive_works() {
    assertThat(RsvpStatus.fromString("Pending")).isEqualTo(RsvpStatus.PENDING);
    assertThat(RsvpStatus.fromString("YES")).isEqualTo(RsvpStatus.YES);
    assertThat(RsvpStatus.fromString("No")).isEqualTo(RsvpStatus.NO);
  }
}
