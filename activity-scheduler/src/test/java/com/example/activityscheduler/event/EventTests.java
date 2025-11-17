package com.example.activityscheduler.event;

import static org.junit.jupiter.api.Assertions.*;

import com.example.activityscheduler.event.model.Event;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
            "org-123",
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
    assertEquals("org-123", event.getOrgId());
    assertEquals("user-789", event.getCreatedBy());
    assertNotNull(event.getCreatedAt());
  }

  @Test
  void testDefaultConstructor() {
    Event defaultEvent = new Event();
    assertNotNull(defaultEvent.getId());
    assertNotNull(defaultEvent.getCreatedAt());
  }

  @Test
  void testValidTimeRange() {
    assertTrue(event.isValidTimeRange());

    Event invalidEvent = new Event();
    invalidEvent.setStartAt(endTime);
    invalidEvent.setEndAt(startTime);
    assertFalse(invalidEvent.isValidTimeRange());
  }

  @Test
  void testSettersAndGetters() {
    event.setTitle("Updated Event");
    event.setDescription("Updated Description");
    event.setCapacity(20);
    event.setLocation("New Location");
    event.setOrgId("new-org");
    event.setCreatedBy("new-user");

    assertEquals("Updated Event", event.getTitle());
    assertEquals("Updated Description", event.getDescription());
    assertEquals(20, event.getCapacity());
    assertEquals("New Location", event.getLocation());
    assertEquals("new-org", event.getOrgId());
    assertEquals("new-user", event.getCreatedBy());
  }

  @Test
  void testToString() {
    String eventString = event.toString();
    assertTrue(eventString.contains("Test Event"));
    assertTrue(eventString.contains("Test Description"));
    assertTrue(eventString.contains("Conference Room A"));
    assertTrue(eventString.contains("user-789"));
  }
}
