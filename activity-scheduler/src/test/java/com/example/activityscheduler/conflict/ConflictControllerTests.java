package com.example.activityscheduler.conflict;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.activityscheduler.conflict.controller.ConflictController;
import com.example.activityscheduler.conflict.service.ConflictService;
import com.example.activityscheduler.event.model.Event;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ConflictControllerTests {

  private MockMvc mockMvc;

  @Mock private ConflictService conflictService;

  private Event testEvent1;
  private Event testEvent2;
  private LocalDateTime startTime1;
  private LocalDateTime endTime1;
  private LocalDateTime startTime2;
  private LocalDateTime endTime2;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(new ConflictController(conflictService)).build();
    startTime1 = LocalDateTime.of(2024, 1, 15, 10, 0);
    endTime1 = LocalDateTime.of(2024, 1, 15, 11, 0);
    startTime2 = LocalDateTime.of(2024, 1, 15, 10, 30);
    endTime2 = LocalDateTime.of(2024, 1, 15, 11, 30);

    testEvent1 =
        new Event(
            "Test Event 1", "Test Description 1", startTime1, endTime1, 10, "org-123", "user-789");
    testEvent2 =
        new Event(
            "Test Event 2", "Test Description 2", startTime2, endTime2, 20, "org-123", "user-789");
  }

  @Test
  void testFindConflictsAmongAcceptedEvents_Success_WithAcceptedStatus() throws Exception {
    List<Event> conflicts = Arrays.asList(testEvent1, testEvent2);
    when(conflictService.findConflictsAmongAcceptedEvents("user-123")).thenReturn(conflicts);

    mockMvc
        .perform(get("/api/conflicts/checkconflicts/user-123/status").param("status", "accepted"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].title").value("Test Event 1"))
        .andExpect(jsonPath("$[1].title").value("Test Event 2"));

    verify(conflictService).findConflictsAmongAcceptedEvents("user-123");
  }

  @Test
  void testFindConflictsAmongAcceptedEvents_Success_WithYesStatus() throws Exception {
    List<Event> conflicts = Arrays.asList(testEvent1, testEvent2);
    when(conflictService.findConflictsAmongAcceptedEvents("user-123")).thenReturn(conflicts);

    mockMvc
        .perform(get("/api/conflicts/checkconflicts/user-123/status").param("status", "yes"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].title").value("Test Event 1"));

    verify(conflictService).findConflictsAmongAcceptedEvents("user-123");
  }

  @Test
  void testFindConflictsAmongAcceptedEvents_Success_WithDefaultStatus() throws Exception {
    List<Event> conflicts = Arrays.asList(testEvent1);
    when(conflictService.findConflictsAmongAcceptedEvents("user-123")).thenReturn(conflicts);

    mockMvc
        .perform(get("/api/conflicts/checkconflicts/user-123/status"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].title").value("Test Event 1"));

    verify(conflictService).findConflictsAmongAcceptedEvents("user-123");
  }

  @Test
  void testFindConflictsAmongAcceptedEvents_EmptyList() throws Exception {
    when(conflictService.findConflictsAmongAcceptedEvents("user-123")).thenReturn(Arrays.asList());

    mockMvc
        .perform(get("/api/conflicts/checkconflicts/user-123/status").param("status", "accepted"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());

    verify(conflictService).findConflictsAmongAcceptedEvents("user-123");
  }

  @Test
  void testFindConflictsAmongAcceptedEvents_InvalidStatus() throws Exception {
    mockMvc
        .perform(get("/api/conflicts/checkconflicts/user-123/status").param("status", "invalid"))
        .andExpect(status().isBadRequest());

    verify(conflictService, never()).findConflictsAmongAcceptedEvents(anyString());
  }

  @Test
  void testFindConflictsAmongAcceptedEvents_IllegalArgumentException() throws Exception {
    when(conflictService.findConflictsAmongAcceptedEvents("user-123"))
        .thenThrow(new IllegalArgumentException("User not found with ID: user-123"));

    mockMvc
        .perform(get("/api/conflicts/checkconflicts/user-123/status").param("status", "accepted"))
        .andExpect(status().isBadRequest());

    verify(conflictService).findConflictsAmongAcceptedEvents("user-123");
  }

  @Test
  void testFindConflictsWithPendingEvent_Success() throws Exception {
    List<Event> conflicts = Arrays.asList(testEvent1);
    when(conflictService.findConflictsWithPendingEvent("user-123", "pending-event-456"))
        .thenReturn(conflicts);

    mockMvc
        .perform(get("/api/conflicts/checkconflicts/user-123/pending-event-456"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].title").value("Test Event 1"));

    verify(conflictService).findConflictsWithPendingEvent("user-123", "pending-event-456");
  }

  @Test
  void testFindConflictsWithPendingEvent_NoConflicts() throws Exception {
    when(conflictService.findConflictsWithPendingEvent("user-123", "pending-event-456"))
        .thenReturn(Arrays.asList());

    mockMvc
        .perform(get("/api/conflicts/checkconflicts/user-123/pending-event-456"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$").isEmpty());

    verify(conflictService).findConflictsWithPendingEvent("user-123", "pending-event-456");
  }

  @Test
  void testFindConflictsWithPendingEvent_EventNotFound() throws Exception {
    when(conflictService.findConflictsWithPendingEvent("user-123", "nonexistent-event"))
        .thenThrow(new IllegalArgumentException("Pending event not found: nonexistent-event"));

    mockMvc
        .perform(get("/api/conflicts/checkconflicts/user-123/nonexistent-event"))
        .andExpect(status().isNotFound());

    verify(conflictService).findConflictsWithPendingEvent("user-123", "nonexistent-event");
  }

  @Test
  void testFindConflictsWithPendingEvent_UserNotFound() throws Exception {
    when(conflictService.findConflictsWithPendingEvent("nonexistent-user", "pending-event-456"))
        .thenThrow(new IllegalArgumentException("User not found with ID: nonexistent-user"));

    mockMvc
        .perform(get("/api/conflicts/checkconflicts/nonexistent-user/pending-event-456"))
        .andExpect(status().isNotFound());

    verify(conflictService).findConflictsWithPendingEvent("nonexistent-user", "pending-event-456");
  }
}
