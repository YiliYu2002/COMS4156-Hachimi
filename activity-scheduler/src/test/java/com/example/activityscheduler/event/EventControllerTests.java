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

/** Unit tests for the EventController class. */
@ExtendWith(MockitoExtension.class)
class EventControllerTests {

  @Mock private EventService eventService;

  private MockMvc mockMvc;
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
  void testCreateEvent_ValidationError() throws Exception {
    testEvent.setTitle(null); // Invalid data

    mockMvc
        .perform(
            post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testEvent)))
        .andExpect(status().isBadRequest());

    verify(eventService, never()).createEvent(any(Event.class));
  }

  @Test
  void testGetEventById_Found() throws Exception {
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
        .andExpect(jsonPath("$.id").value(testEvent.getId()));

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
    doNothing().when(eventService).deleteEvent("event-123");

    mockMvc.perform(delete("/api/events/event-123")).andExpect(status().isNoContent());

    verify(eventService).deleteEvent("event-123");
  }

  @Test
  void testDeleteEvent_NotFound() throws Exception {
    doThrow(new IllegalArgumentException("Event not found with ID: nonexistent"))
        .when(eventService)
        .deleteEvent("nonexistent");

    mockMvc.perform(delete("/api/events/nonexistent")).andExpect(status().isNotFound());

    verify(eventService).deleteEvent("nonexistent");
  }

  @Test
  void testGetAllEvents() throws Exception {
    List<Event> events = Arrays.asList(testEvent);
    when(eventService.getAllEvents()).thenReturn(events);

    mockMvc
        .perform(get("/api/events"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].id").value(testEvent.getId()));

    verify(eventService).getAllEvents();
  }

  @Test
  void testCheckForConflicts() throws Exception {
    List<Event> conflicts = Arrays.asList(testEvent);
    when(eventService.findConflictingEvents(startTime, endTime)).thenReturn(conflicts);

    mockMvc
        .perform(
            get("/api/events/conflicts")
                .param("startTime", startTime.toString())
                .param("endTime", endTime.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].id").value(testEvent.getId()));

    verify(eventService).findConflictingEvents(startTime, endTime);
  }
}
