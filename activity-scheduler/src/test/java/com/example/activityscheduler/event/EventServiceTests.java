package com.example.activityscheduler.event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.activityscheduler.event.model.Event;
import com.example.activityscheduler.event.repository.EventRepository;
import com.example.activityscheduler.event.service.EventService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventServiceTests {

  @Mock private EventRepository eventRepository;
  private EventService eventService;
  private Event testEvent;
  private LocalDateTime startTime;
  private LocalDateTime endTime;

  @BeforeEach
  void setUp() {
    eventService = new EventService(eventRepository);
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
  void testCreateEvent_Success() {
    when(eventRepository.findConflictingEvents(
            any(LocalDateTime.class), any(LocalDateTime.class), isNull()))
        .thenReturn(Collections.emptyList());
    when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

    Event result = eventService.createEvent(testEvent);

    assertNotNull(result);
    assertEquals(testEvent.getId(), result.getId());
    verify(eventRepository).save(testEvent);
  }

  @Test
  void testCreateEvent_TimeConflict() {
    Event conflictingEvent = new Event();
    conflictingEvent.setId("conflict-123");
    conflictingEvent.setTitle("Conflicting Event");

    when(eventRepository.findConflictingEvents(
            any(LocalDateTime.class), any(LocalDateTime.class), isNull()))
        .thenReturn(Arrays.asList(conflictingEvent));

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> eventService.createEvent(testEvent));

    assertTrue(exception.getMessage().contains("Time conflict detected"));
    verify(eventRepository, never()).save(any(Event.class));
  }

  @Test
  void testCreateEvent_InvalidTimeRange() {
    Event invalidEvent = new Event();
    invalidEvent.setTitle("Invalid Event");
    invalidEvent.setStartAt(endTime);
    invalidEvent.setEndAt(startTime);

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> eventService.createEvent(invalidEvent));

    assertTrue(exception.getMessage().contains("Start time must be before end time"));
  }

  @Test
  void testCreateEvent_NullTitle() {
    Event invalidEvent = new Event();
    invalidEvent.setTitle(null);
    invalidEvent.setStartAt(startTime);
    invalidEvent.setEndAt(endTime);

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> eventService.createEvent(invalidEvent));

    assertTrue(exception.getMessage().contains("Event title is required"));
  }

  @Test
  void testCreateEvent_NullEvent() {
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> eventService.createEvent(null));

    assertTrue(exception.getMessage().contains("Event cannot be null"));
  }

  @Test
  void testUpdateEvent_Success() {
    Event existingEvent = new Event();
    existingEvent.setId("event-123");
    existingEvent.setTitle("Original Event");

    when(eventRepository.findById("event-123")).thenReturn(Optional.of(existingEvent));
    when(eventRepository.findConflictingEvents(
            any(LocalDateTime.class), any(LocalDateTime.class), eq("event-123")))
        .thenReturn(Collections.emptyList());
    when(eventRepository.save(any(Event.class))).thenReturn(existingEvent);

    Event result = eventService.updateEvent("event-123", testEvent);

    assertNotNull(result);
    assertEquals("event-123", result.getId());
    verify(eventRepository).save(existingEvent);
  }

  @Test
  void testUpdateEvent_NotFound() {
    when(eventRepository.findById("nonexistent")).thenReturn(Optional.empty());

    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> eventService.updateEvent("nonexistent", testEvent));

    assertTrue(exception.getMessage().contains("Event not found"));
  }

  @Test
  void testGetEventById_Success() {
    when(eventRepository.findById("event-123")).thenReturn(Optional.of(testEvent));

    Optional<Event> result = eventService.getEventById("event-123");

    assertTrue(result.isPresent());
    assertEquals(testEvent.getId(), result.get().getId());
  }

  @Test
  void testGetEventById_NotFound() {
    when(eventRepository.findById("nonexistent")).thenReturn(Optional.empty());

    Optional<Event> result = eventService.getEventById("nonexistent");

    assertFalse(result.isPresent());
  }

  @Test
  void testDeleteEvent_Success() {
    when(eventRepository.existsById("event-123")).thenReturn(true);

    eventService.deleteEvent("event-123");

    verify(eventRepository).deleteById("event-123");
  }

  @Test
  void testDeleteEvent_NotFound() {
    when(eventRepository.existsById("nonexistent")).thenReturn(false);

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> eventService.deleteEvent("nonexistent"));

    assertTrue(exception.getMessage().contains("Event not found"));
  }

  @Test
  void testFindConflictingEvents() {
    List<Event> conflicts = Arrays.asList(testEvent);
    when(eventRepository.findConflictingEvents(startTime, endTime, null)).thenReturn(conflicts);

    List<Event> result = eventService.findConflictingEvents(startTime, endTime);

    assertEquals(1, result.size());
    assertEquals(testEvent.getId(), result.get(0).getId());
  }
}
