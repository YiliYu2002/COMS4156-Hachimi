package com.example.activityscheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Activity Scheduler application. This class is responsible for
 * starting the Spring Boot application.
 */
@SpringBootApplication
public class ActivitySchedulerApplication {

  /**
   * Main method to start the Spring Boot application.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(ActivitySchedulerApplication.class, args);
  }
}
