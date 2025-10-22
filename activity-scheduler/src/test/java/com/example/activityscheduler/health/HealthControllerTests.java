package com.example.activityscheduler.health;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** Unit tests for HealthController. */
@WebMvcTest(HealthController.class)
class HealthControllerTests {

  private MockMvc mockMvc;

  @MockBean private DataSource dataSource;

  @MockBean private Connection connection;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(new HealthController(dataSource)).build();
  }

  // ========== BASIC HEALTH ENDPOINT TESTS ==========

  @Test
  void testBasicHealth_typicalValidInput_returnsSuccessMessage() throws Exception {
    // When & Then
    mockMvc
        .perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                "/health/basic"))
        .andExpect(
            org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
        .andExpect(
            org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                .string("Application is running"));
  }

  @Test
  void testBasicHealth_multipleCalls_returnsConsistentResponse() throws Exception {
    // When & Then - First call
    mockMvc
        .perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                "/health/basic"))
        .andExpect(
            org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
        .andExpect(
            org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                .string("Application is running"));

    // When & Then - Second call
    mockMvc
        .perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                "/health/basic"))
        .andExpect(
            org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
        .andExpect(
            org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                .string("Application is running"));
  }

  @Test
  void testBasicHealth_withDifferentHttpMethods_returnsMethodNotAllowed() throws Exception {
    // When & Then - POST method should not be allowed
    mockMvc
        .perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(
                "/health/basic"))
        .andExpect(
            org.springframework.test.web.servlet.result.MockMvcResultMatchers.status()
                .isMethodNotAllowed());
  }

  // ========== DATABASE HEALTH ENDPOINT TESTS ==========

  @Test
  void testDbHealth_validConnection_returnsSuccessMessage() throws Exception {
    // Given
    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.isValid(2)).thenReturn(true);

    // When & Then
    mockMvc
        .perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/health/db"))
        .andExpect(
            org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
        .andExpect(
            org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                .string("Database connected"));

    verify(dataSource).getConnection();
    verify(connection).isValid(2);
  }

  @Test
  void testDbHealth_invalidConnection_returnsFailureMessage() throws Exception {
    // Given
    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.isValid(2)).thenReturn(false);

    // When & Then
    mockMvc
        .perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/health/db"))
        .andExpect(
            org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
        .andExpect(
            org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                .string("Database not connected"));

    verify(dataSource).getConnection();
    verify(connection).isValid(2);
  }

  @Test
  void testDbHealth_connectionException_returnsErrorMessage() throws Exception {
    // Given
    when(dataSource.getConnection()).thenThrow(new RuntimeException("Connection failed"));

    // When & Then
    mockMvc
        .perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/health/db"))
        .andExpect(
            org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
        .andExpect(
            org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                .string("Database connection failed: Connection failed"));

    verify(dataSource).getConnection();
  }

  @Test
  void testDbHealth_sqlException_returnsErrorMessage() throws Exception {
    // Given
    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.isValid(2)).thenThrow(new RuntimeException("SQL error"));

    // When & Then
    mockMvc
        .perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/health/db"))
        .andExpect(
            org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
        .andExpect(
            org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                .string("Database connection failed: SQL error"));

    verify(dataSource).getConnection();
    verify(connection).isValid(2);
  }

  @Test
  void testDbHealth_withDifferentHttpMethods_returnsMethodNotAllowed() throws Exception {
    // When & Then - POST method should not be allowed
    mockMvc
        .perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/health/db"))
        .andExpect(
            org.springframework.test.web.servlet.result.MockMvcResultMatchers.status()
                .isMethodNotAllowed());
  }
}
