package com.example.activityscheduler.attendee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.activityscheduler.attendee.controller.AttendeeController;
import com.example.activityscheduler.attendee.controller.AttendeeController.AttendeeRequest;
import com.example.activityscheduler.attendee.controller.AttendeeController.RsvpUpdateRequest;
import com.example.activityscheduler.attendee.model.Attendee;
import com.example.activityscheduler.attendee.model.RsvpStatus;
import com.example.activityscheduler.attendee.service.AttendeeService;
import com.example.activityscheduler.user.model.User;
import com.example.activityscheduler.user.repository.UserRepository;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class AttendeeControllerTests {

  private AttendeeService mockService;
  private UserRepository mockUserRepository;
  private AttendeeController controller;
  private User testUser;

  @BeforeEach
  void setUp() {
    mockService = mock(AttendeeService.class);
    mockUserRepository = mock(UserRepository.class);
    controller = new AttendeeController(mockService, mockUserRepository);

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
    when(mockService.getAttendee(eventId, userId)).thenReturn(Optional.of(attendee));

    Optional<Attendee> result = controller.getAttendee(eventId, userId);

    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(attendee);
  }

  @Test
  void getAttendee_nonExistentAttendee_returnsEmpty() {
    String eventId = "event-123";
    String userId = "user-456";
    when(mockService.getAttendee(eventId, userId)).thenReturn(Optional.empty());

    Optional<Attendee> result = controller.getAttendee(eventId, userId);

    assertThat(result).isEmpty();
  }

  @Test
  void getAttendeesByEvent_returnsAttendees() {
    String eventId = "event-123";
    List<Attendee> attendees =
        Arrays.asList(
            new Attendee(eventId, "user1", RsvpStatus.YES),
            new Attendee(eventId, "user2", RsvpStatus.PENDING));
    when(mockService.getAttendeesByEvent(eventId)).thenReturn(attendees);

    List<Attendee> result = controller.getAttendeesByEvent(eventId);

    assertThat(result).hasSize(2);
    assertThat(result).containsExactlyElementsOf(attendees);
  }

  @Test
  void createAttendee_validRequest_createsAttendee() {
    String eventId = "event-123";
    String userId = "user-456";
    RsvpStatus rsvpStatus = RsvpStatus.PENDING;

    AttendeeRequest request = new AttendeeRequest();
    request.setEventId(eventId);
    request.setUserId(userId);
    request.setRsvpStatus(rsvpStatus);

    Attendee attendee = new Attendee(eventId, userId, rsvpStatus);
    when(mockUserRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(mockService.createAttendee(eventId, userId, rsvpStatus)).thenReturn(attendee);

    Attendee result = controller.createAttendee(request);

    assertThat(result).isEqualTo(attendee);
    verify(mockService).createAttendee(eventId, userId, rsvpStatus);
  }

  @Test
  void createAttendee_nullRequest_throwsException() {
    assertThrows(ResponseStatusException.class, () -> controller.createAttendee(null));
  }

  @Test
  void createAttendee_nullEventId_throwsException() {
    AttendeeRequest request = new AttendeeRequest();
    request.setEventId(null);
    request.setUserId("user-456");

    assertThrows(ResponseStatusException.class, () -> controller.createAttendee(request));
  }

  @Test
  void createAttendee_nullUserId_throwsException() {
    AttendeeRequest request = new AttendeeRequest();
    request.setEventId("event-123");
    request.setUserId(null);

    assertThrows(ResponseStatusException.class, () -> controller.createAttendee(request));
  }

  @Test
  void createAttendee_userNotFound_throwsException() {
    String eventId = "event-123";
    String userId = "nonexistent-user";

    AttendeeRequest request = new AttendeeRequest();
    request.setEventId(eventId);
    request.setUserId(userId);

    when(mockUserRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(ResponseStatusException.class, () -> controller.createAttendee(request));
  }

  @Test
  void createAttendee_illegalArgumentException_throwsBadRequest() {
    String eventId = "event-123";
    String userId = "user-456";

    AttendeeRequest request = new AttendeeRequest();
    request.setEventId(eventId);
    request.setUserId(userId);

    when(mockUserRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(mockService.createAttendee(anyString(), anyString(), any(RsvpStatus.class)))
        .thenThrow(new IllegalArgumentException("Invalid data"));

    assertThrows(ResponseStatusException.class, () -> controller.createAttendee(request));
  }

  @Test
  void createAttendee_illegalStateException_throwsConflict() {
    String eventId = "event-123";
    String userId = "user-456";

    AttendeeRequest request = new AttendeeRequest();
    request.setEventId(eventId);
    request.setUserId(userId);

    when(mockUserRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(mockService.createAttendee(anyString(), anyString(), any(RsvpStatus.class)))
        .thenThrow(new IllegalStateException("Already exists"));

    assertThrows(ResponseStatusException.class, () -> controller.createAttendee(request));
  }

  @Test
  void updateRsvpStatus_validRequest_updatesStatus() {
    String eventId = "event-123";
    String userId = "user-456";
    String requestUserId = "user-456";
    RsvpStatus newStatus = RsvpStatus.YES;

    RsvpUpdateRequest request = new RsvpUpdateRequest();
    request.setRsvpStatus(newStatus);

    Attendee updatedAttendee = new Attendee(eventId, userId, newStatus);
    when(mockUserRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(mockUserRepository.findById(requestUserId)).thenReturn(Optional.of(testUser));
    when(mockService.updateRsvpStatus(eventId, userId, requestUserId, newStatus))
        .thenReturn(updatedAttendee);

    Attendee result = controller.updateRsvpStatus(eventId, userId, requestUserId, request);

    assertThat(result).isEqualTo(updatedAttendee);
    verify(mockService).updateRsvpStatus(eventId, userId, requestUserId, newStatus);
  }

  @Test
  void updateRsvpStatus_nullRequest_throwsException() {
    String eventId = "event-123";
    String userId = "user-456";
    String requestUserId = "user-456";

    assertThrows(
        ResponseStatusException.class,
        () -> controller.updateRsvpStatus(eventId, userId, requestUserId, null));
  }

  @Test
  void updateRsvpStatus_nullRsvpStatus_throwsException() {
    String eventId = "event-123";
    String userId = "user-456";
    String requestUserId = "user-456";
    RsvpUpdateRequest request = new RsvpUpdateRequest();
    request.setRsvpStatus(null);

    assertThrows(
        ResponseStatusException.class,
        () -> controller.updateRsvpStatus(eventId, userId, requestUserId, request));
  }

  @Test
  void updateRsvpStatus_missingHeader_throwsException() {
    String eventId = "event-123";
    String userId = "user-456";
    String requestUserId = "";
    RsvpUpdateRequest request = new RsvpUpdateRequest();
    request.setRsvpStatus(RsvpStatus.YES);

    assertThrows(
        ResponseStatusException.class,
        () -> controller.updateRsvpStatus(eventId, userId, requestUserId, request));
  }

  @Test
  void updateRsvpStatus_userNotFound_throwsException() {
    String eventId = "event-123";
    String userId = "nonexistent-user";
    String requestUserId = "nonexistent-user";
    RsvpUpdateRequest request = new RsvpUpdateRequest();
    request.setRsvpStatus(RsvpStatus.YES);

    when(mockUserRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(
        ResponseStatusException.class,
        () -> controller.updateRsvpStatus(eventId, userId, requestUserId, request));
  }

  @Test
  void updateRsvpStatus_requestUserNotFound_throwsException() {
    String eventId = "event-123";
    String userId = "user-456";
    String requestUserId = "nonexistent-user";
    RsvpUpdateRequest request = new RsvpUpdateRequest();
    request.setRsvpStatus(RsvpStatus.YES);

    when(mockUserRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(mockUserRepository.findById(requestUserId)).thenReturn(Optional.empty());

    assertThrows(
        ResponseStatusException.class,
        () -> controller.updateRsvpStatus(eventId, userId, requestUserId, request));
  }

  @Test
  void updateRsvpStatus_illegalArgumentException_throwsBadRequest() {
    String eventId = "event-123";
    String userId = "user-456";
    String requestUserId = "user-456";
    RsvpUpdateRequest request = new RsvpUpdateRequest();
    request.setRsvpStatus(RsvpStatus.YES);

    when(mockUserRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(mockUserRepository.findById(requestUserId)).thenReturn(Optional.of(testUser));
    when(mockService.updateRsvpStatus(anyString(), anyString(), anyString(), any(RsvpStatus.class)))
        .thenThrow(new IllegalArgumentException("Invalid data"));

    assertThrows(
        ResponseStatusException.class,
        () -> controller.updateRsvpStatus(eventId, userId, requestUserId, request));
  }

  @Test
  void updateRsvpStatus_illegalStateException_throwsNotFound() {
    String eventId = "event-123";
    String userId = "user-456";
    String requestUserId = "user-456";
    RsvpUpdateRequest request = new RsvpUpdateRequest();
    request.setRsvpStatus(RsvpStatus.YES);

    when(mockUserRepository.findById(userId)).thenReturn(Optional.of(testUser));
    when(mockUserRepository.findById(requestUserId)).thenReturn(Optional.of(testUser));
    when(mockService.updateRsvpStatus(anyString(), anyString(), anyString(), any(RsvpStatus.class)))
        .thenThrow(new IllegalStateException("Not found"));

    assertThrows(
        ResponseStatusException.class,
        () -> controller.updateRsvpStatus(eventId, userId, requestUserId, request));
  }

  @Test
  void deleteAttendee_validRequest_deletesAttendee() {
    String eventId = "event-123";
    String userId = "user-456";
    String requestUserId = "creator-123";

    controller.deleteAttendee(eventId, userId, requestUserId);

    verify(mockService).deleteAttendee(eventId, userId, requestUserId);
  }

  @Test
  void deleteAttendee_missingHeader_throwsException() {
    String eventId = "event-123";
    String userId = "user-456";
    String requestUserId = "";

    assertThrows(
        ResponseStatusException.class,
        () -> controller.deleteAttendee(eventId, userId, requestUserId));
  }

  @Test
  void deleteAttendee_illegalArgumentException_throwsBadRequest() {
    String eventId = "event-123";
    String userId = "user-456";
    String requestUserId = "creator-123";

    doThrow(new IllegalArgumentException("Invalid data"))
        .when(mockService)
        .deleteAttendee(eventId, userId, requestUserId);

    assertThrows(
        ResponseStatusException.class,
        () -> controller.deleteAttendee(eventId, userId, requestUserId));
  }

  @Test
  void deleteAttendee_illegalStateException_throwsNotFound() {
    String eventId = "event-123";
    String userId = "user-456";
    String requestUserId = "creator-123";

    doThrow(new IllegalStateException("Not found"))
        .when(mockService)
        .deleteAttendee(eventId, userId, requestUserId);

    assertThrows(
        ResponseStatusException.class,
        () -> controller.deleteAttendee(eventId, userId, requestUserId));
  }

  @Test
  void existsAttendee_existingAttendee_returnsTrue() {
    String eventId = "event-123";
    String userId = "user-456";

    when(mockService.existsAttendee(eventId, userId)).thenReturn(true);

    boolean result = controller.existsAttendee(eventId, userId);

    assertThat(result).isTrue();
  }

  @Test
  void existsAttendee_nonExistentAttendee_returnsFalse() {
    String eventId = "event-123";
    String userId = "user-456";

    when(mockService.existsAttendee(eventId, userId)).thenReturn(false);

    boolean result = controller.existsAttendee(eventId, userId);

    assertThat(result).isFalse();
  }

  @Test
  void countAttendeesByEvent_returnsCount() {
    String eventId = "event-123";
    long expectedCount = 5L;

    when(mockService.countAttendeesByEvent(eventId)).thenReturn(expectedCount);

    long result = controller.countAttendeesByEvent(eventId);

    assertThat(result).isEqualTo(expectedCount);
  }

  @Test
  void countAttendeesByEventAndStatus_returnsCount() {
    String eventId = "event-123";
    RsvpStatus rsvpStatus = RsvpStatus.YES;
    long expectedCount = 3L;

    when(mockService.countAttendeesByEventAndStatus(eventId, rsvpStatus)).thenReturn(expectedCount);

    long result = controller.countAttendeesByEventAndStatus(eventId, rsvpStatus);

    assertThat(result).isEqualTo(expectedCount);
  }
}
