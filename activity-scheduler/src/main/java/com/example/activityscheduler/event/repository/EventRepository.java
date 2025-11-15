package com.example.activityscheduler.event.repository;

import com.example.activityscheduler.event.model.Event;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/** Repository interface for Event entities. Provides data access methods for event operations. */
@Repository
public interface EventRepository extends JpaRepository<Event, String> {

  /**
   * Finds events created by a specific user.
   *
   * @param createdBy the user ID who created the events
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
}
