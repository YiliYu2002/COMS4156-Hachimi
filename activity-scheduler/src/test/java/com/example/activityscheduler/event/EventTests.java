package com.example.activityscheduler.event;

import static org.junit.jupiter.api.Assertions.*;

import com.example.activityscheduler.event.model.Event;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for the Event entity. */
class EventTests {

  private Event event;
  private LocalDateTime startTime;
  private LocalDateTime endTime;

  @BeforeEach
  void setUp() {
    startTime = LocalDateTime.of(2024, 1, 15, 10, 0);
    endTime = LocalDateTime.of(2024, 1, 15, 11, 0);
    event =
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
  void testEventCreation() {
    assertNotNull(event.getId());
    assertEquals("Test Event", event.getTitle());
    assertEquals("Test Description", event.getDescription());
    assertEquals(startTime, event.getStartAt());
    assertEquals(endTime, event.getEndAt());
    assertEquals(10, event.getCapacity());
    assertEquals("Conference Room A", event.getLocation());
    assertEquals("user-789", event.getCreatedBy());
    assertNotNull(event.getCreatedAt());
  }

  @Test
  void testValidTimeRange() {
    assertTrue(event.isValidTimeRange());

    // Test invalid time range
    event.setStartAt(endTime);
    event.setEndAt(startTime);
    assertFalse(event.isValidTimeRange());
  }

  @Test
  void testConflictDetection() {
    Event otherEvent =
        new Event(
            "Other Event",
            "Other Description",
            LocalDateTime.of(2024, 1, 15, 10, 30),
            LocalDateTime.of(2024, 1, 15, 11, 30),
            5,
            "Room B",
            "user-999");

    // Events should conflict (overlapping times)
    assertTrue(event.conflictsWith(otherEvent));

    // Non-overlapping events should not conflict
    otherEvent.setStartAt(LocalDateTime.of(2024, 1, 15, 12, 0));
    otherEvent.setEndAt(LocalDateTime.of(2024, 1, 15, 13, 0));
    assertFalse(event.conflictsWith(otherEvent));
  }

  @Test
  void testSettersAndGetters() {
    event.setTitle("Updated Event");
    event.setDescription("Updated Description");
    event.setCapacity(20);
    event.setLocation("New Location");
    event.setCreatedBy("new-user");

    assertEquals("Updated Event", event.getTitle());
    assertEquals("Updated Description", event.getDescription());
    assertEquals(20, event.getCapacity());
    assertEquals("New Location", event.getLocation());
    assertEquals("new-user", event.getCreatedBy());
  }

  @Test
  void testToString() {
    String eventString = event.toString();
    assertTrue(eventString.contains("Event{"));
    assertTrue(eventString.contains("title='Test Event'"));
    assertTrue(eventString.contains("description='Test Description'"));
  }
}
