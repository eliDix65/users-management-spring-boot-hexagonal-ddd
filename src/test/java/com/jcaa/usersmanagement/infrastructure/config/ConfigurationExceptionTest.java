package com.jcaa.usersmanagement.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests for ConfigurationException: factory method, message and cause preservation. */
@DisplayName("ConfigurationException")
class ConfigurationExceptionTest {

  @Test
  @DisplayName("becauseLoadFailed() builds the exception with the fixed message and the cause")
  void becauseLoadFailed_preservesMessageAndCause() {
    // Arrange
    final IllegalStateException cause = new IllegalStateException("boom");

    // Act
    final ConfigurationException exception = ConfigurationException.becauseLoadFailed(cause);

    // Assert
    org.junit.jupiter.api.Assertions.assertAll(
        "configuration exception",
        () -> assertThat(exception).hasMessage("Failed to load the application configuration."),
        () -> assertThat(exception).hasCause(cause));
  }
}
