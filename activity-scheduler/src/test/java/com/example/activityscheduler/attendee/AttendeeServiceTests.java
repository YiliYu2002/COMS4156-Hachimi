package com.example.activityscheduler.attendee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.activityscheduler.attendee.model.Attendee;
import com.example.activityscheduler.attendee.model.AttendeeId;
import com.example.activityscheduler.attendee.model.RsvpStatus;
import com.example.activityscheduler.attendee.repository.AttendeeRepository;
import com.example.activityscheduler.attendee.service.AttendeeService;
import com.example.activityscheduler.event.model.Event;
import com.example.activityscheduler.event.repository.EventRepository;
import com.example.activityscheduler.membership.service.MembershipService;
import com.example.activityscheduler.user.model.User;
import com.example.activityscheduler.user.repository.UserRepository;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AttendeeServiceTests {

  private AttendeeRepository mockAttendeeRepository;
  private UserRepository mockUserRepository;
  private EventRepository mockEventRepository;
  private MembershipService mockMembershipService;
  private AttendeeService attendeeService;
  private Event testEvent;
  private User testUser;

  @BeforeEach
  void setUp() {
    mockAttendeeRepository = mock(AttendeeRepository.class);
    mockUserRepository = mock(UserRepository.class);
    mockEventRepository = mock(EventRepository.class);
    mockMembershipService = mock(MembershipService.class);
    attendeeService =
        new AttendeeService(
            mockAttendeeRepository, mockUserRepository, mockEventRepository, mockMembershipService);

    testEvent = new Event();
    testEvent.setId("event-123");
    testEvent.setTitle("Test Event");
    testEvent.setOrgId("org-123");
    testEvent.setCreatedBy("creator-123");
    testEvent.setStartAt(LocalDateTime.now().plusDays(1));
    testEvent.setEndAt(LocalDateTime.now().plusDays(2));

    testUser = new User("test@example.com", "Test User");
    try {
      Field idField = User.class.getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(testUser, "user-456");
    } catch (Exception e) {
      throw new RuntimeException("Failed to set User ID", e);
    }
  }

  @Test
  void getAttendee_existingAttendee_returnsAttendee() {
    String eventId = "event-123";
    String userId = "user-456";
    Attendee attendee = new Attendee(eventId, userId, RsvpStatus.YES);
    when(mockAttendeeRepository.findByEventIdAndUserId(eventId, userId))
        .thenReturn(Optional.of(attendee));

    Optional<Attendee> result = attendeeService.getAttendee(eventId, userId);

    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(attendee);
  }

  @Test
  void getAttendee_nonExistentAttendee_returnsEmpty() {
    String eventId = "event-123";
    String userId = "user-456";
    when(mockAttendeeRepository.findByEventIdAndUserId(eventId, userId))
        .thenReturn(Optional.empty());

    Optional<Attendee> result = attendeeService.getAttendee(eventId, userId);

    assertThat(result).isEmpty();
  }

  @Test
  void getAttendeesByEvent_returnsAttendees() {
    String eventId = "event-123";
    List<Attendee> attendees =
        Arrays.asList(
            new Attendee(eventId, "user1", RsvpStatus.YES),
            new Attendee(eventId, "user2", RsvpStatus.PENDING));
    when(mockAttendeeRepository.findByEventId(eventId)).thenReturn(attendees);

    List<Attendee> result = attendeeService.getAttendeesByEvent(eventId);

    assertThat(result).hasSize(2);
    assertThat(result).containsExactlyElementsOf(attendees);
  }

  @Test
  void createAttendee_validData_createsAttendee() {
    String eventId = "event-123";
    String userId = "user-456";
    RsvpStatus rsvpStatus = RsvpStatus.YES;
    Attendee attendee = new Attendee(eventId, userId, rsvpStatus);

    when(mockUserRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(mockEventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));
    when(mockMembershipService.existsMembership("org-123", userId)).thenReturn(true);
    when(mockAttendeeRepository.existsByEventIdAndUserId(eventId, userId)).thenReturn(false);
    when(mockAttendeeRepository.save(any(Attendee.class))).thenReturn(attendee);

    Attendee result = attendeeService.createAttendee(eventId, userId, rsvpStatus);

    assertThat(result).isEqualTo(attendee);
    verify(mockAttendeeRepository).save(any(Attendee.class));
  }

  @Test
  void createAttendee_nullUserId_throwsException() {
    assertThatThrownBy(() -> attendeeService.createAttendee("event-123", null, RsvpStatus.YES))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("User ID cannot be null or empty");
  }

  @Test
  void createAttendee_emptyUserId_throwsException() {
    assertThatThrownBy(() -> attendeeService.createAttendee("event-123", "", RsvpStatus.YES))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("User ID cannot be null or empty");
  }

  @Test
  void createAttendee_userNotFound_throwsException() {
    String eventId = "event-123";
    String userId = "user-456";

    when(mockUserRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> attendeeService.createAttendee(eventId, userId, RsvpStatus.YES))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("User not found");
  }

  @Test
  void createAttendee_nullEventId_throwsException() {
    String userId = "user-456";

    when(mockUserRepository.findById(userId)).thenReturn(Optional.of(testUser));

    assertThatThrownBy(() -> attendeeService.createAttendee(null, userId, RsvpStatus.YES))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Event ID cannot be null or empty");
  }

  @Test
  void createAttendee_eventNotFound_throwsException() {
    String eventId = "event-123";
    String userId = "user-456";

    when(mockUserRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(mockEventRepository.findById(eventId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> attendeeService.createAttendee(eventId, userId, RsvpStatus.YES))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Event not found");
  }

  @Test
  void createAttendee_userNotMemberOfOrganization_throwsException() {
    String eventId = "event-123";
    String userId = "user-456";

    when(mockUserRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(mockEventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));
    when(mockMembershipService.existsMembership("org-123", userId)).thenReturn(false);

    assertThatThrownBy(() -> attendeeService.createAttendee(eventId, userId, RsvpStatus.YES))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("is not a member of organization");
  }

  @Test
  void createAttendee_existingAttendee_throwsException() {
    String eventId = "event-123";
    String userId = "user-456";

    when(mockUserRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(mockEventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));
    when(mockMembershipService.existsMembership("org-123", userId)).thenReturn(true);
    when(mockAttendeeRepository.existsByEventIdAndUserId(eventId, userId)).thenReturn(true);

    assertThatThrownBy(() -> attendeeService.createAttendee(eventId, userId, RsvpStatus.YES))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Attendee already exists for event " + eventId + " and user " + userId);
  }

  @Test
  void createAttendee_nullRsvpStatus_usesPending() {
    String eventId = "event-123";
    String userId = "user-456";
    Attendee attendee = new Attendee(eventId, userId, RsvpStatus.PENDING);

    when(mockUserRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(mockEventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));
    when(mockMembershipService.existsMembership("org-123", userId)).thenReturn(true);
    when(mockAttendeeRepository.existsByEventIdAndUserId(eventId, userId)).thenReturn(false);
    when(mockAttendeeRepository.save(any(Attendee.class))).thenReturn(attendee);

    Attendee result = attendeeService.createAttendee(eventId, userId, null);

    assertThat(result.getRsvpStatus()).isEqualTo(RsvpStatus.PENDING);
    verify(mockAttendeeRepository).save(any(Attendee.class));
  }

  @Test
  void createAttendee_withoutRsvpStatus_usesPending() {
    String eventId = "event-123";
    String userId = "user-456";
    Attendee attendee = new Attendee(eventId, userId, RsvpStatus.PENDING);

    when(mockUserRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(mockEventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));
    when(mockMembershipService.existsMembership("org-123", userId)).thenReturn(true);
    when(mockAttendeeRepository.existsByEventIdAndUserId(eventId, userId)).thenReturn(false);
    when(mockAttendeeRepository.save(any(Attendee.class))).thenReturn(attendee);

    Attendee result = attendeeService.createAttendee(eventId, userId);

    assertThat(result.getRsvpStatus()).isEqualTo(RsvpStatus.PENDING);
    verify(mockAttendeeRepository).save(any(Attendee.class));
  }

  @Test
  void updateRsvpStatus_validData_updatesStatus() {
    String eventId = "event-123";
    String userId = "user-456";
    String requestUserId = "user-456";
    RsvpStatus newStatus = RsvpStatus.YES;
    Attendee attendee = new Attendee(eventId, userId, RsvpStatus.PENDING);
    Attendee updatedAttendee = new Attendee(eventId, userId, newStatus);

    when(mockAttendeeRepository.findByEventIdAndUserId(eventId, userId))
        .thenReturn(Optional.of(attendee));
    when(mockAttendeeRepository.save(any(Attendee.class))).thenReturn(updatedAttendee);

    Attendee result = attendeeService.updateRsvpStatus(eventId, userId, requestUserId, newStatus);

    assertThat(result.getRsvpStatus()).isEqualTo(newStatus);
    verify(mockAttendeeRepository).save(attendee);
  }

  @Test
  void updateRsvpStatus_nullEventId_throwsException() {
    assertThatThrownBy(
            () -> attendeeService.updateRsvpStatus(null, "user-456", "user-456", RsvpStatus.YES))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Event ID cannot be null or empty");
  }

  @Test
  void updateRsvpStatus_nullUserId_throwsException() {
    assertThatThrownBy(
            () -> attendeeService.updateRsvpStatus("event-123", null, "user-456", RsvpStatus.YES))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("User ID cannot be null or empty");
  }

  @Test
  void updateRsvpStatus_nullRequestUserId_throwsException() {
    assertThatThrownBy(
            () -> attendeeService.updateRsvpStatus("event-123", "user-456", null, RsvpStatus.YES))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Request user ID cannot be null or empty");
  }

  @Test
  void updateRsvpStatus_requestUserIdMismatch_throwsException() {
    assertThatThrownBy(
            () ->
                attendeeService.updateRsvpStatus(
                    "event-123", "user-456", "user-789", RsvpStatus.YES))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Request user ID does not match user ID");
  }

  @Test
  void updateRsvpStatus_nullRsvpStatus_throwsException() {
    assertThatThrownBy(
            () -> attendeeService.updateRsvpStatus("event-123", "user-456", "user-456", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("RSVP status cannot be null");
  }

  @Test
  void updateRsvpStatus_nonExistentAttendee_throwsException() {
    String eventId = "event-123";
    String userId = "user-456";

    when(mockAttendeeRepository.findByEventIdAndUserId(eventId, userId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> attendeeService.updateRsvpStatus(eventId, userId, userId, RsvpStatus.YES))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Attendee not found for event " + eventId + " and user " + userId);
  }

  @Test
  void deleteAttendee_validData_deletesAttendee() {
    String eventId = "event-123";
    String userId = "user-456";
    String requestUserId = "creator-123";
    AttendeeId attendeeId = new AttendeeId(eventId, userId);

    when(mockEventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));
    when(mockAttendeeRepository.existsById(attendeeId)).thenReturn(true);

    attendeeService.deleteAttendee(eventId, userId, requestUserId);

    verify(mockAttendeeRepository).deleteById(attendeeId);
  }

  @Test
  void deleteAttendee_nullEventId_throwsException() {
    assertThatThrownBy(() -> attendeeService.deleteAttendee(null, "user-456", "creator-123"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Event ID cannot be null or empty");
  }

  @Test
  void deleteAttendee_nullRequestUserId_throwsException() {
    assertThatThrownBy(() -> attendeeService.deleteAttendee("event-123", "user-456", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Request user ID cannot be null or empty");
  }

  @Test
  void deleteAttendee_eventNotFound_throwsException() {
    String eventId = "event-123";
    String userId = "user-456";
    String requestUserId = "creator-123";

    when(mockEventRepository.findById(eventId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> attendeeService.deleteAttendee(eventId, userId, requestUserId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Event not found");
  }

  @Test
  void deleteAttendee_eventHasNoCreator_throwsException() {
    String eventId = "event-123";
    String userId = "user-456";
    String requestUserId = "creator-123";
    Event eventWithoutCreator = new Event();
    eventWithoutCreator.setId(eventId);
    eventWithoutCreator.setCreatedBy(null);

    when(mockEventRepository.findById(eventId)).thenReturn(Optional.of(eventWithoutCreator));

    assertThatThrownBy(() -> attendeeService.deleteAttendee(eventId, userId, requestUserId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Event has no creator");
  }

  @Test
  void deleteAttendee_notEventCreator_throwsException() {
    String eventId = "event-123";
    String userId = "user-456";
    String requestUserId = "not-creator";

    when(mockEventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));

    assertThatThrownBy(() -> attendeeService.deleteAttendee(eventId, userId, requestUserId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Only the event creator can delete attendees");
  }

  @Test
  void deleteAttendee_nonExistentAttendee_throwsException() {
    String eventId = "event-123";
    String userId = "user-456";
    String requestUserId = "creator-123";
    AttendeeId attendeeId = new AttendeeId(eventId, userId);

    when(mockEventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));
    when(mockAttendeeRepository.existsById(attendeeId)).thenReturn(false);

    assertThatThrownBy(() -> attendeeService.deleteAttendee(eventId, userId, requestUserId))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Attendee not found for event " + eventId + " and user " + userId);
  }

  @Test
  void existsAttendee_existingAttendee_returnsTrue() {
    String eventId = "event-123";
    String userId = "user-456";

    when(mockAttendeeRepository.existsByEventIdAndUserId(eventId, userId)).thenReturn(true);

    boolean result = attendeeService.existsAttendee(eventId, userId);

    assertThat(result).isTrue();
  }

  @Test
  void existsAttendee_nonExistentAttendee_returnsFalse() {
    String eventId = "event-123";
    String userId = "user-456";

    when(mockAttendeeRepository.existsByEventIdAndUserId(eventId, userId)).thenReturn(false);

    boolean result = attendeeService.existsAttendee(eventId, userId);

    assertThat(result).isFalse();
  }

  @Test
  void countAttendeesByEvent_returnsCount() {
    String eventId = "event-123";
    long expectedCount = 5L;

    when(mockAttendeeRepository.countByEventId(eventId)).thenReturn(expectedCount);

    long result = attendeeService.countAttendeesByEvent(eventId);

    assertThat(result).isEqualTo(expectedCount);
  }

  @Test
  void countAttendeesByEventAndStatus_returnsCount() {
    String eventId = "event-123";
    RsvpStatus rsvpStatus = RsvpStatus.YES;
    long expectedCount = 3L;

    when(mockAttendeeRepository.countByEventIdAndRsvpStatus(eventId, rsvpStatus))
        .thenReturn(expectedCount);

    long result = attendeeService.countAttendeesByEventAndStatus(eventId, rsvpStatus);

    assertThat(result).isEqualTo(expectedCount);
  }
}
