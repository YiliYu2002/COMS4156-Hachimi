package com.example.activityscheduler.attendee;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.activityscheduler.attendee.model.Attendee;
import com.example.activityscheduler.attendee.model.RsvpStatus;
import org.junit.jupiter.api.Test;

class AttendeeTests {

  @Test
  void defaultConstructor_setsDefaults() {
    Attendee attendee = new Attendee();

    assertThat(attendee.getEventId()).isNull();
    assertThat(attendee.getUserId()).isNull();
    assertThat(attendee.getRsvpStatus()).isEqualTo(RsvpStatus.PENDING);
  }

  @Test
  void parameterizedConstructor_setsFields() {
    String eventId = "event-123";
    String userId = "user-456";
    RsvpStatus rsvpStatus = RsvpStatus.YES;

    Attendee attendee = new Attendee(eventId, userId, rsvpStatus);

    assertThat(attendee.getEventId()).isEqualTo(eventId);
    assertThat(attendee.getUserId()).isEqualTo(userId);
    assertThat(attendee.getRsvpStatus()).isEqualTo(rsvpStatus);
  }

  @Test
  void setRsvpStatus_updatesStatus() {
    Attendee attendee = new Attendee("event-123", "user-456", RsvpStatus.PENDING);

    attendee.setRsvpStatus(RsvpStatus.YES);

    assertThat(attendee.getRsvpStatus()).isEqualTo(RsvpStatus.YES);
  }

  @Test
  void setRsvpStatus_toNo_updatesStatus() {
    Attendee attendee = new Attendee("event-123", "user-456", RsvpStatus.PENDING);

    attendee.setRsvpStatus(RsvpStatus.NO);

    assertThat(attendee.getRsvpStatus()).isEqualTo(RsvpStatus.NO);
  }

  @Test
  void setRsvpStatus_toPending_updatesStatus() {
    Attendee attendee = new Attendee("event-123", "user-456", RsvpStatus.YES);

    attendee.setRsvpStatus(RsvpStatus.PENDING);

    assertThat(attendee.getRsvpStatus()).isEqualTo(RsvpStatus.PENDING);
  }
}
