package com.example.activityscheduler.event;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.activityscheduler.event.controller.EventController;
import com.example.activityscheduler.event.model.Event;
import com.example.activityscheduler.event.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class EventControllerTests {

  private MockMvc mockMvc;

  @Mock private EventService eventService;

  private ObjectMapper objectMapper;
  private Event testEvent;
  private LocalDateTime startTime;
  private LocalDateTime endTime;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(new EventController(eventService)).build();
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    startTime = LocalDateTime.of(2024, 1, 15, 10, 0);
    endTime = LocalDateTime.of(2024, 1, 15, 11, 0);
    testEvent =
        new Event(
            "Test Event",
            "Test Description",
            startTime,
            endTime,
            10,
            "Conference Room A",
            "org-123",
            "user-789");
  }

  @Test
  void testCreateEvent_Success() throws Exception {
    when(eventService.createEvent(any(Event.class))).thenReturn(testEvent);

    mockMvc
        .perform(
            post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEvent)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(testEvent.getId()))
        .andExpect(jsonPath("$.title").value("Test Event"));

    verify(eventService).createEvent(any(Event.class));
  }

  @Test
  void testCreateEvent_Failure_InvalidTitle() throws Exception {
    Event invalidEvent = new Event();
    invalidEvent.setTitle(null);
    invalidEvent.setOrgId("org-123");
    invalidEvent.setStartAt(startTime);
    invalidEvent.setEndAt(endTime);

    mockMvc
        .perform(
            post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidEvent)))
        .andExpect(status().isBadRequest());

    verify(eventService, never()).createEvent(any(Event.class));
  }

  @Test
  void testGetEventById_Success() throws Exception {
    when(eventService.getEventById("event-123")).thenReturn(Optional.of(testEvent));

    mockMvc
        .perform(get("/api/events/event-123"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(testEvent.getId()))
        .andExpect(jsonPath("$.title").value("Test Event"));

    verify(eventService).getEventById("event-123");
  }

  @Test
  void testGetEventById_NotFound() throws Exception {
    when(eventService.getEventById("nonexistent")).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/events/nonexistent")).andExpect(status().isNotFound());

    verify(eventService).getEventById("nonexistent");
  }

  @Test
  void testUpdateEvent_Success() throws Exception {
    when(eventService.updateEvent(eq("event-123"), any(Event.class))).thenReturn(testEvent);

    mockMvc
        .perform(
            put("/api/events/event-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEvent)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(testEvent.getId()))
        .andExpect(jsonPath("$.title").value("Test Event"));

    verify(eventService).updateEvent(eq("event-123"), any(Event.class));
  }

  @Test
  void testUpdateEvent_NotFound() throws Exception {
    when(eventService.updateEvent(eq("nonexistent"), any(Event.class)))
        .thenThrow(new IllegalArgumentException("Event not found with ID: nonexistent"));

    mockMvc
        .perform(
            put("/api/events/nonexistent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEvent)))
        .andExpect(status().isNotFound());

    verify(eventService).updateEvent(eq("nonexistent"), any(Event.class));
  }

  @Test
  void testDeleteEvent_Success() throws Exception {
    doNothing().when(eventService).deleteEvent("event-123", "user-789");

    mockMvc
        .perform(delete("/api/events/event-123").param("userId", "user-789"))
        .andExpect(status().isNoContent());

    verify(eventService).deleteEvent("event-123", "user-789");
  }

  @Test
  void testDeleteEvent_NotFound() throws Exception {
    doThrow(new IllegalArgumentException("Event not found with ID: nonexistent"))
        .when(eventService)
        .deleteEvent("nonexistent", "user-789");

    mockMvc
        .perform(delete("/api/events/nonexistent").param("userId", "user-789"))
        .andExpect(status().isNotFound());

    verify(eventService).deleteEvent("nonexistent", "user-789");
  }

  @Test
  void testDeleteEvent_Forbidden_NotCreator() throws Exception {
    doThrow(
            new IllegalArgumentException(
                "Only the event creator can delete the event. User 'user-999' is not the creator of this event."))
        .when(eventService)
        .deleteEvent("event-123", "user-999");

    mockMvc
        .perform(delete("/api/events/event-123").param("userId", "user-999"))
        .andExpect(status().isForbidden());

    verify(eventService).deleteEvent("event-123", "user-999");
  }

  @Test
  void testGetEventsByOrganization_Success() throws Exception {
    List<Event> events = Arrays.asList(testEvent);
    when(eventService.getEventsByOrganization("org-123")).thenReturn(events);

    mockMvc
        .perform(get("/api/events/organization/org-123"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].id").value(testEvent.getId()))
        .andExpect(jsonPath("$[0].title").value("Test Event"));

    verify(eventService).getEventsByOrganization("org-123");
  }

  @Test
  void testGetEventsByUser_Success() throws Exception {
    List<Event> events = Arrays.asList(testEvent);
    when(eventService.getEventsByUser("user-789")).thenReturn(events);

    mockMvc
        .perform(get("/api/events/user/user-789"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].id").value(testEvent.getId()))
        .andExpect(jsonPath("$[0].title").value("Test Event"));

    verify(eventService).getEventsByUser("user-789");
  }

  @Test
  void testGetEventsByOrganizationAndUser_Success() throws Exception {
    List<Event> events = Arrays.asList(testEvent);
    when(eventService.getEventsByOrganizationAndUser("org-123", "user-789")).thenReturn(events);

    mockMvc
        .perform(get("/api/events/organization/org-123/user/user-789"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].id").value(testEvent.getId()))
        .andExpect(jsonPath("$[0].title").value("Test Event"));

    verify(eventService).getEventsByOrganizationAndUser("org-123", "user-789");
  }
}
