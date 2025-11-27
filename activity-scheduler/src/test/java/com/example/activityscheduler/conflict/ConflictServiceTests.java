package com.example.activityscheduler.conflict;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.example.activityscheduler.attendee.model.Attendee;
import com.example.activityscheduler.attendee.model.RsvpStatus;
import com.example.activityscheduler.attendee.repository.AttendeeRepository;
import com.example.activityscheduler.conflict.service.ConflictService;
import com.example.activityscheduler.event.model.Event;
import com.example.activityscheduler.event.repository.EventRepository;
import com.example.activityscheduler.user.model.User;
import com.example.activityscheduler.user.repository.UserRepository;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConflictServiceTests {

  @Mock private AttendeeRepository attendeeRepository;
  @Mock private EventRepository eventRepository;
  @Mock private UserRepository userRepository;

  private ConflictService conflictService;
  private User testUser;
  private Event event1;
  private Event event2;
  private Event event3;
  private Event pendingEvent;
  private LocalDateTime startTime1;
  private LocalDateTime endTime1;
  private LocalDateTime startTime2;
  private LocalDateTime endTime2;
  private LocalDateTime startTime3;
  private LocalDateTime endTime3;
  private LocalDateTime pendingStartTime;
  private LocalDateTime pendingEndTime;

  @BeforeEach
  void setUp() {
    conflictService = new ConflictService(attendeeRepository, eventRepository, userRepository);

    testUser = new User("test@example.com", "Test User");
    try {
      Field idField = User.class.getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(testUser, "user-123");
    } catch (Exception e) {
      throw new RuntimeException("Failed to set User ID", e);
    }

    // Event 1: 10:00 - 11:00
    startTime1 = LocalDateTime.of(2024, 1, 15, 10, 0);
    endTime1 = LocalDateTime.of(2024, 1, 15, 11, 0);
    event1 = new Event("Event 1", "Description 1", startTime1, endTime1, 10, "org-123", "user-789");
    event1.setId("event-1");

    // Event 2: 10:30 - 11:30 (conflicts with Event 1)
    startTime2 = LocalDateTime.of(2024, 1, 15, 10, 30);
    endTime2 = LocalDateTime.of(2024, 1, 15, 11, 30);
    event2 = new Event("Event 2", "Description 2", startTime2, endTime2, 20, "org-123", "user-789");
    event2.setId("event-2");

    // Event 3: 14:00 - 15:00 (no conflict)
    startTime3 = LocalDateTime.of(2024, 1, 15, 14, 0);
    endTime3 = LocalDateTime.of(2024, 1, 15, 15, 0);
    event3 = new Event("Event 3", "Description 3", startTime3, endTime3, 30, "org-123", "user-789");
    event3.setId("event-3");

    // Pending Event: 10:45 - 11:45 (conflicts with Event 1 and Event 2)
    pendingStartTime = LocalDateTime.of(2024, 1, 15, 10, 45);
    pendingEndTime = LocalDateTime.of(2024, 1, 15, 11, 45);
    pendingEvent =
        new Event(
            "Pending Event",
            "Pending Description",
            pendingStartTime,
            pendingEndTime,
            40,
            "org-123",
            "user-789");
    pendingEvent.setId("pending-event-456");
  }

  @Test
  void testFindConflictsAmongAcceptedEvents_Success_WithConflicts() {
    String userId = "user-123";
    Attendee attendee1 = new Attendee("event-1", userId, RsvpStatus.YES);
    Attendee attendee2 = new Attendee("event-2", userId, RsvpStatus.YES);
    Attendee attendee3 = new Attendee("event-3", userId, RsvpStatus.YES);
    List<Attendee> acceptedAttendees = Arrays.asList(attendee1, attendee2, attendee3);

    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(attendeeRepository.findByUserIdAndRsvpStatus(userId, RsvpStatus.YES))
        .thenReturn(acceptedAttendees);
    when(eventRepository.findById("event-1")).thenReturn(Optional.of(event1));
    when(eventRepository.findById("event-2")).thenReturn(Optional.of(event2));
    when(eventRepository.findById("event-3")).thenReturn(Optional.of(event3));

    List<Event> result = conflictService.findConflictsAmongAcceptedEvents(userId);

    assertThat(result).hasSize(2);
    assertThat(result).contains(event1, event2);
    assertThat(result).doesNotContain(event3);
    verify(userRepository).findById(userId);
    verify(attendeeRepository).findByUserIdAndRsvpStatus(userId, RsvpStatus.YES);
  }

  @Test
  void testFindConflictsAmongAcceptedEvents_Success_NoConflicts() {
    String userId = "user-123";
    Attendee attendee1 = new Attendee("event-1", userId, RsvpStatus.YES);
    Attendee attendee3 = new Attendee("event-3", userId, RsvpStatus.YES);
    List<Attendee> acceptedAttendees = Arrays.asList(attendee1, attendee3);

    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(attendeeRepository.findByUserIdAndRsvpStatus(userId, RsvpStatus.YES))
        .thenReturn(acceptedAttendees);
    when(eventRepository.findById("event-1")).thenReturn(Optional.of(event1));
    when(eventRepository.findById("event-3")).thenReturn(Optional.of(event3));

    List<Event> result = conflictService.findConflictsAmongAcceptedEvents(userId);

    assertThat(result).isEmpty();
    verify(userRepository).findById(userId);
    verify(attendeeRepository).findByUserIdAndRsvpStatus(userId, RsvpStatus.YES);
  }

  @Test
  void testFindConflictsAmongAcceptedEvents_NoAcceptedEvents() {
    String userId = "user-123";
    List<Attendee> acceptedAttendees = Arrays.asList();

    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(attendeeRepository.findByUserIdAndRsvpStatus(userId, RsvpStatus.YES))
        .thenReturn(acceptedAttendees);

    List<Event> result = conflictService.findConflictsAmongAcceptedEvents(userId);

    assertThat(result).isEmpty();
    verify(userRepository).findById(userId);
    verify(attendeeRepository).findByUserIdAndRsvpStatus(userId, RsvpStatus.YES);
    verify(eventRepository, never()).findById(anyString());
  }

  @Test
  void testFindConflictsAmongAcceptedEvents_NullUserId() {
    assertThatThrownBy(() -> conflictService.findConflictsAmongAcceptedEvents(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("User ID cannot be null or empty");

    verify(userRepository, never()).findById(anyString());
    verify(attendeeRepository, never()).findByUserIdAndRsvpStatus(anyString(), any());
  }

  @Test
  void testFindConflictsAmongAcceptedEvents_EmptyUserId() {
    assertThatThrownBy(() -> conflictService.findConflictsAmongAcceptedEvents(""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("User ID cannot be null or empty");

    verify(userRepository, never()).findById(anyString());
    verify(attendeeRepository, never()).findByUserIdAndRsvpStatus(anyString(), any());
  }

  @Test
  void testFindConflictsAmongAcceptedEvents_WhitespaceUserId() {
    assertThatThrownBy(() -> conflictService.findConflictsAmongAcceptedEvents("   "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("User ID cannot be null or empty");

    verify(userRepository, never()).findById(anyString());
    verify(attendeeRepository, never()).findByUserIdAndRsvpStatus(anyString(), any());
  }

  @Test
  void testFindConflictsAmongAcceptedEvents_UserNotFound() {
    String userId = "nonexistent-user";

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> conflictService.findConflictsAmongAcceptedEvents(userId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("User not found with ID: " + userId);

    verify(userRepository).findById(userId);
    verify(attendeeRepository, never()).findByUserIdAndRsvpStatus(anyString(), any());
  }

  @Test
  void testFindConflictsWithPendingEvent_Success_WithConflicts() {
    String userId = "user-123";
    String pendingEventId = "pending-event-456";
    Attendee attendee1 = new Attendee("event-1", userId, RsvpStatus.YES);
    Attendee attendee2 = new Attendee("event-2", userId, RsvpStatus.YES);
    Attendee attendee3 = new Attendee("event-3", userId, RsvpStatus.YES);
    List<Attendee> acceptedAttendees = Arrays.asList(attendee1, attendee2, attendee3);

    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(eventRepository.findById(pendingEventId)).thenReturn(Optional.of(pendingEvent));
    when(attendeeRepository.findByUserIdAndRsvpStatus(userId, RsvpStatus.YES))
        .thenReturn(acceptedAttendees);
    when(eventRepository.findById("event-1")).thenReturn(Optional.of(event1));
    when(eventRepository.findById("event-2")).thenReturn(Optional.of(event2));
    when(eventRepository.findById("event-3")).thenReturn(Optional.of(event3));

    List<Event> result = conflictService.findConflictsWithPendingEvent(userId, pendingEventId);

    assertThat(result).hasSize(2);
    assertThat(result).contains(event1, event2);
    assertThat(result).doesNotContain(event3);
    verify(userRepository).findById(userId);
    verify(eventRepository).findById(pendingEventId);
    verify(attendeeRepository).findByUserIdAndRsvpStatus(userId, RsvpStatus.YES);
  }

  @Test
  void testFindConflictsWithPendingEvent_Success_NoConflicts() {
    String userId = "user-123";
    String pendingEventId = "pending-event-456";
    Attendee attendee3 = new Attendee("event-3", userId, RsvpStatus.YES);
    List<Attendee> acceptedAttendees = Arrays.asList(attendee3);

    // Create a pending event that doesn't conflict with event3
    Event nonConflictingPendingEvent =
        new Event(
            "Non-Conflicting Pending Event",
            "Description",
            LocalDateTime.of(2024, 1, 15, 16, 0),
            LocalDateTime.of(2024, 1, 15, 17, 0),
            50,
            "org-123",
            "user-789");
    nonConflictingPendingEvent.setId(pendingEventId);

    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(eventRepository.findById(pendingEventId))
        .thenReturn(Optional.of(nonConflictingPendingEvent));
    when(attendeeRepository.findByUserIdAndRsvpStatus(userId, RsvpStatus.YES))
        .thenReturn(acceptedAttendees);
    when(eventRepository.findById("event-3")).thenReturn(Optional.of(event3));

    List<Event> result = conflictService.findConflictsWithPendingEvent(userId, pendingEventId);

    assertThat(result).isEmpty();
    verify(userRepository).findById(userId);
    verify(eventRepository).findById(pendingEventId);
    verify(attendeeRepository).findByUserIdAndRsvpStatus(userId, RsvpStatus.YES);
  }

  @Test
  void testFindConflictsWithPendingEvent_NoAcceptedEvents() {
    String userId = "user-123";
    String pendingEventId = "pending-event-456";
    List<Attendee> acceptedAttendees = Arrays.asList();

    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(eventRepository.findById(pendingEventId)).thenReturn(Optional.of(pendingEvent));
    when(attendeeRepository.findByUserIdAndRsvpStatus(userId, RsvpStatus.YES))
        .thenReturn(acceptedAttendees);

    List<Event> result = conflictService.findConflictsWithPendingEvent(userId, pendingEventId);

    assertThat(result).isEmpty();
    verify(userRepository).findById(userId);
    verify(eventRepository).findById(pendingEventId);
    verify(attendeeRepository).findByUserIdAndRsvpStatus(userId, RsvpStatus.YES);
  }

  @Test
  void testFindConflictsWithPendingEvent_NullUserId() {
    assertThatThrownBy(
            () -> conflictService.findConflictsWithPendingEvent(null, "pending-event-456"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("User ID cannot be null or empty");

    verify(userRepository, never()).findById(anyString());
    verify(eventRepository, never()).findById(anyString());
  }

  @Test
  void testFindConflictsWithPendingEvent_EmptyUserId() {
    assertThatThrownBy(() -> conflictService.findConflictsWithPendingEvent("", "pending-event-456"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("User ID cannot be null or empty");

    verify(userRepository, never()).findById(anyString());
    verify(eventRepository, never()).findById(anyString());
  }

  @Test
  void testFindConflictsWithPendingEvent_NullPendingEventId() {
    when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));

    assertThatThrownBy(() -> conflictService.findConflictsWithPendingEvent("user-123", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Pending event ID cannot be null or empty");

    verify(userRepository).findById("user-123");
    verify(eventRepository, never()).findById(anyString());
  }

  @Test
  void testFindConflictsWithPendingEvent_EmptyPendingEventId() {
    when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));

    assertThatThrownBy(() -> conflictService.findConflictsWithPendingEvent("user-123", ""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Pending event ID cannot be null or empty");

    verify(userRepository).findById("user-123");
    verify(eventRepository, never()).findById(anyString());
  }

  @Test
  void testFindConflictsWithPendingEvent_UserNotFound() {
    String userId = "nonexistent-user";
    String pendingEventId = "pending-event-456";

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> conflictService.findConflictsWithPendingEvent(userId, pendingEventId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("User not found with ID: " + userId);

    verify(userRepository).findById(userId);
    verify(eventRepository, never()).findById(anyString());
  }

  @Test
  void testFindConflictsWithPendingEvent_PendingEventNotFound() {
    String userId = "user-123";
    String pendingEventId = "nonexistent-event";

    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(eventRepository.findById(pendingEventId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> conflictService.findConflictsWithPendingEvent(userId, pendingEventId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Pending event not found: " + pendingEventId);

    verify(userRepository).findById(userId);
    verify(eventRepository).findById(pendingEventId);
    verify(attendeeRepository, never()).findByUserIdAndRsvpStatus(anyString(), any());
  }

  @Test
  void testFindConflictsWithPendingEvent_AcceptedEventNotFound() {
    String userId = "user-123";
    String pendingEventId = "pending-event-456";
    Attendee attendee1 = new Attendee("event-1", userId, RsvpStatus.YES);
    Attendee attendee2 = new Attendee("nonexistent-event", userId, RsvpStatus.YES);
    List<Attendee> acceptedAttendees = Arrays.asList(attendee1, attendee2);

    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(eventRepository.findById(pendingEventId)).thenReturn(Optional.of(pendingEvent));
    when(attendeeRepository.findByUserIdAndRsvpStatus(userId, RsvpStatus.YES))
        .thenReturn(acceptedAttendees);
    when(eventRepository.findById("event-1")).thenReturn(Optional.of(event1));
    when(eventRepository.findById("nonexistent-event")).thenReturn(Optional.empty());

    List<Event> result = conflictService.findConflictsWithPendingEvent(userId, pendingEventId);

    // Should only return event1, not the nonexistent event
    assertThat(result).hasSize(1);
    assertThat(result).contains(event1);
    verify(userRepository).findById(userId);
    verify(eventRepository).findById(pendingEventId);
    verify(attendeeRepository).findByUserIdAndRsvpStatus(userId, RsvpStatus.YES);
  }
}
