package com.example.activityscheduler.attendee;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.activityscheduler.attendee.model.AttendeeId;
import org.junit.jupiter.api.Test;

class AttendeeIdTests {

  @Test
  void defaultConstructor_createsEmptyId() {
    AttendeeId id = new AttendeeId();

    assertThat(id.getEventId()).isNull();
    assertThat(id.getUserId()).isNull();
  }

  @Test
  void parameterizedConstructor_setsFields() {
    String eventId = "event-123";
    String userId = "user-456";

    AttendeeId id = new AttendeeId(eventId, userId);

    assertThat(id.getEventId()).isEqualTo(eventId);
    assertThat(id.getUserId()).isEqualTo(userId);
  }

  @Test
  void equals_sameValues_returnsTrue() {
    AttendeeId id1 = new AttendeeId("event-123", "user-456");
    AttendeeId id2 = new AttendeeId("event-123", "user-456");

    assertThat(id1).isEqualTo(id2);
  }

  @Test
  void equals_differentValues_returnsFalse() {
    AttendeeId id1 = new AttendeeId("event-123", "user-456");
    AttendeeId id2 = new AttendeeId("event-789", "user-456");
    AttendeeId id3 = new AttendeeId("event-123", "user-789");

    assertThat(id1).isNotEqualTo(id2);
    assertThat(id1).isNotEqualTo(id3);
  }

  @Test
  void equals_sameInstance_returnsTrue() {
    AttendeeId id = new AttendeeId("event-123", "user-456");

    assertThat(id).isEqualTo(id);
  }

  @Test
  void equals_null_returnsFalse() {
    AttendeeId id = new AttendeeId("event-123", "user-456");

    assertThat(id).isNotEqualTo(null);
  }

  @Test
  void equals_differentClass_returnsFalse() {
    AttendeeId id = new AttendeeId("event-123", "user-456");
    String other = "not-an-attendee-id";

    assertThat(id).isNotEqualTo(other);
  }

  @Test
  void hashCode_sameValues_returnsSameHash() {
    AttendeeId id1 = new AttendeeId("event-123", "user-456");
    AttendeeId id2 = new AttendeeId("event-123", "user-456");

    assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
  }

  @Test
  void hashCode_differentValues_returnsDifferentHash() {
    AttendeeId id1 = new AttendeeId("event-123", "user-456");
    AttendeeId id2 = new AttendeeId("event-789", "user-456");

    assertThat(id1.hashCode()).isNotEqualTo(id2.hashCode());
  }

  @Test
  void toString_containsBothIds() {
    AttendeeId id = new AttendeeId("event-123", "user-456");
    String toString = id.toString();

    assertThat(toString).contains("event-123");
    assertThat(toString).contains("user-456");
  }
}
