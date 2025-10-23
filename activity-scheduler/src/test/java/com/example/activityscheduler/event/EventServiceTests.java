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

/** Unit tests for the EventService class. */
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
  void testCreateEvent_WithConflicts() {
    Event conflictingEvent =
        new Event(
            "Conflicting Event",
            "Conflicting Description",
            LocalDateTime.of(2024, 1, 15, 10, 30),
            LocalDateTime.of(2024, 1, 15, 11, 30),
            5,
            "Room B",
            "user-999");

    when(eventRepository.findConflictingEvents(
            any(LocalDateTime.class), any(LocalDateTime.class), isNull()))
        .thenReturn(Arrays.asList(conflictingEvent));

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> eventService.createEvent(testEvent));

    assertTrue(exception.getMessage().contains("Time conflict detected"));
  }

  @Test
  void testUpdateEvent_Success() {
    Event existingEvent =
        new Event(
            "Original Event", "Original Description", startTime, endTime, 10, "Room A", "user-789");
    when(eventRepository.findById("event-123")).thenReturn(Optional.of(existingEvent));
    when(eventRepository.findConflictingEvents(
            any(LocalDateTime.class), any(LocalDateTime.class), anyString()))
        .thenReturn(Collections.emptyList());
    when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

    Event result = eventService.updateEvent("event-123", testEvent);

    assertNotNull(result);
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
  void testGetEventById_Found() {
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
  void testGetAllEvents() {
    List<Event> events = Arrays.asList(testEvent);
    when(eventRepository.findAll()).thenReturn(events);

    List<Event> result = eventService.getAllEvents();

    assertEquals(1, result.size());
    assertEquals(testEvent.getId(), result.get(0).getId());
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
  void testValidateEvent_NullEvent() {
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> eventService.createEvent(null));

    assertTrue(exception.getMessage().contains("Event cannot be null"));
  }

  @Test
  void testValidateEvent_MissingTitle() {
    testEvent.setTitle(null);

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> eventService.createEvent(testEvent));

    assertTrue(exception.getMessage().contains("Event title is required"));
  }

  @Test
  void testValidateEvent_InvalidTimeRange() {
    testEvent.setStartAt(endTime);
    testEvent.setEndAt(startTime);

    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> eventService.createEvent(testEvent));

    assertTrue(exception.getMessage().contains("Start time must be before end time"));
  }
}
