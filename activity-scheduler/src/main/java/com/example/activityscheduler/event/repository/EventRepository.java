package com.example.activityscheduler.event.repository;

import com.example.activityscheduler.event.model.Event;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
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
   * Finds events belonging to a specific organization.
   *
   * @param orgId the organization ID
   * @return a list of events belonging to the organization
   */
  List<Event> findByOrgId(String orgId);

  /**
   * Finds events belonging to a specific organization and created by a specific user.
   *
   * @param orgId the organization ID
   * @param createdBy the user ID who created the events
   * @return a list of events matching the criteria
   */
  List<Event> findByOrgIdAndCreatedBy(String orgId, String createdBy);
}
