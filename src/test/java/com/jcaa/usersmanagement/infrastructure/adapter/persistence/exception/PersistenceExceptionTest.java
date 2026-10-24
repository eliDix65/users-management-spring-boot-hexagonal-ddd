package com.jcaa.usersmanagement.infrastructure.adapter.persistence.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests for PersistenceException: every factory method builds the expected message and cause. */
@DisplayName("PersistenceException")
class PersistenceExceptionTest {

  private final SQLException cause = new SQLException("connection reset");

  @Test
  @DisplayName("becauseSaveFailed() builds the save message")
  void becauseSaveFailed_buildsMessage() {
    final PersistenceException exception = PersistenceException.becauseSaveFailed("u-1", cause);

    org.junit.jupiter.api.Assertions.assertAll(
        "save failure",
        () -> assertThat(exception).hasMessage("Failed to save user with ID: 'u-1'."),
        () -> assertThat(exception).hasCause(cause));
  }

  @Test
  @DisplayName("becauseUpdateFailed() builds the update message")
  void becauseUpdateFailed_buildsMessage() {
    final PersistenceException exception = PersistenceException.becauseUpdateFailed("u-2", cause);

    assertThat(exception).hasMessage("Failed to update user with ID: 'u-2'.");
  }

  @Test
  @DisplayName("becauseFindByIdFailed() builds the find-by-id message")
  void becauseFindByIdFailed_buildsMessage() {
    final PersistenceException exception =
        PersistenceException.becauseFindByIdFailed("u-3", cause);

    assertThat(exception).hasMessage("Failed to find user with ID: 'u-3'.");
  }

  @Test
  @DisplayName("becauseFindByEmailFailed() builds the find-by-email message")
  void becauseFindByEmailFailed_buildsMessage() {
    final PersistenceException exception =
        PersistenceException.becauseFindByEmailFailed("a@example.com", cause);

    assertThat(exception).hasMessage("Failed to find user with email: 'a@example.com'.");
  }

  @Test
  @DisplayName("becauseFindAllFailed() builds the find-all message")
  void becauseFindAllFailed_buildsMessage() {
    final PersistenceException exception = PersistenceException.becauseFindAllFailed(cause);

    assertThat(exception).hasMessage("Failed to retrieve all users.");
  }

  @Test
  @DisplayName("becauseDeleteFailed() builds the delete message")
  void becauseDeleteFailed_buildsMessage() {
    final PersistenceException exception = PersistenceException.becauseDeleteFailed("u-4", cause);

    assertThat(exception).hasMessage("Failed to delete user with ID: 'u-4'.");
  }

  @Test
  @DisplayName("becauseConnectionFailed() builds the connection message")
  void becauseConnectionFailed_buildsMessage() {
    final PersistenceException exception = PersistenceException.becauseConnectionFailed(cause);

    org.junit.jupiter.api.Assertions.assertAll(
        "connection failure",
        () -> assertThat(exception).hasMessage("Could not establish database connection."),
        () -> assertThat(exception).hasCause(cause));
  }
}
