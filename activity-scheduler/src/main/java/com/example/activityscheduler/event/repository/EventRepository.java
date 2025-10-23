package com.example.activityscheduler.event.repository;

import com.example.activityscheduler.event.model.Event;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Event entity operations. Provides data access methods for Event entities
 * using Spring Data JPA with custom queries for conflict detection and availability checking.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, String> {

  /**
   * Finds all events created by a specific user.
   *
   * @param createdBy the user who created the events
   * @return a list of events created by the user
   */
  List<Event> findByCreatedBy(String createdBy);

  /**
   * Finds events that conflict with a given time range. This is used for conflict detection when
   * creating or updating events.
   *
   * @param startTime the start time to check for conflicts
   * @param endTime the end time to check for conflicts
   * @param excludeEventId optional event ID to exclude from conflict check (for updates)
   * @return a list of conflicting events
   */
  @Query(
      "SELECT e FROM Event e WHERE "
          + "((e.startAt < :endTime AND e.endAt > :startTime))"
          + "AND (:excludeEventId IS NULL OR e.id != :excludeEventId)")
  List<Event> findConflictingEvents(
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime,
      @Param("excludeEventId") String excludeEventId);

  /**
   * Finds events that start within a specific time range.
   *
   * @param startTime the start of the time range
   * @param endTime the end of the time range
   * @return a list of events starting within the time range
   */
  @Query("SELECT e FROM Event e WHERE " + "e.startAt >= :startTime AND e.startAt < :endTime")
  List<Event> findEventsStartingInRange(
      @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

  /**
   * Finds events that end within a specific time range.
   *
   * @param startTime the start of the time range
   * @param endTime the end of the time range
   * @return a list of events ending within the time range
   */
  @Query("SELECT e FROM Event e WHERE " + "e.endAt > :startTime AND e.endAt <= :endTime")
  List<Event> findEventsEndingInRange(
      @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

  /**
   * Counts the number of events in a specific time range. Useful for capacity checking and
   * availability analysis.
   *
   * @param startTime the start of the time range
   * @param endTime the end of the time range
   * @return the count of events in the time range
   */
  @Query(
      "SELECT COUNT(e) FROM Event e WHERE "
          + "((e.startAt >= :startTime AND e.startAt < :endTime) OR "
          + "(e.endAt > :startTime AND e.endAt <= :endTime) OR "
          + "(e.startAt <= :startTime AND e.endAt >= :endTime))")
  long countEventsInTimeRange(
      @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

  /**
   * Finds events with capacity information for resource availability checking.
   *
   * @param startTime the start time to check
   * @param endTime the end time to check
   * @return a list of events with capacity information
   */
  @Query(
      "SELECT e FROM Event e WHERE "
          + "((e.startAt < :endTime AND e.endAt > :startTime)) AND "
          + "e.capacity IS NOT NULL")
  List<Event> findEventsWithCapacityInRange(
      @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
