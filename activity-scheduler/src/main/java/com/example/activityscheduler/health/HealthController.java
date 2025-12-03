package com.example.activityscheduler.health;

import java.sql.Connection;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller for health checks. */
@RestController
public class HealthController {

  private static final Logger logger = Logger.getLogger(HealthController.class.getName());
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
    logger.fine("Basic health check requested");
    return "Application is running";
  }

  /**
   * Database health check.
   *
   * @return a string indicating that the database is connected
   */
  @GetMapping("/health/db")
  public String dbHealth() {
    logger.fine("Database health check requested");
    try (Connection conn = dataSource.getConnection()) {
      boolean isValid = conn.isValid(2);
      if (isValid) {
        logger.fine("Database connection is valid");
        return "Database connected";
      } else {
        logger.warning("Database connection is invalid");
        return "Database not connected";
      }
    } catch (Exception e) {
      logger.severe("Database connection failed: " + e.getMessage());
      return "Database connection failed: " + e.getMessage();
    }
  }
}
