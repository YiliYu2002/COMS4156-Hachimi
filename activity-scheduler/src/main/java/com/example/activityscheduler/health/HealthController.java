package com.example.activityscheduler.health;

import java.sql.Connection;
import javax.sql.DataSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller for health checks. */
@RestController
public class HealthController {

  private final DataSource dataSource;

  /**
   * Constructor for HealthController.
   *
   * @param dataSource the data source to use for database health checks
   */
  public HealthController(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  /**
   * Basic health check.
   *
   * @return a string indicating that the application is running
   */
  @GetMapping("/health/basic")
  public String basicHealth() {
    return "Application is running ✅";
  }

  /**
   * Database health check.
   *
   * @return a string indicating that the database is connected
   */
  @GetMapping("/health/db")
  public String dbHealth() {
    try (Connection conn = dataSource.getConnection()) {
      return conn.isValid(2) ? "Database connected ✅" : "Database not connected ❌";
    } catch (Exception e) {
      return "Database connection failed ❌: " + e.getMessage();
    }
  }
}
