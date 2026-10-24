package com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.io.ConsoleIO;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.controller.UserController;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.dto.CreateUserRequest;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.dto.LoginRequest;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.dto.UpdateUserRequest;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.dto.UserResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for UserManagementCli.
 *
 * <p>Covers: the main loop (invalid option, every menu option, exit), handler dispatch for all six
 * operations, the ConstraintViolationException branch and the generic RuntimeException branch. The
 * CLI is driven end-to-end with a scripted ByteArrayInputStream; only the controller is mocked.
 */
@DisplayName("UserManagementCli")
@ExtendWith(MockitoExtension.class)
class UserManagementCliTest {

  @Mock private UserController userController;

  private ByteArrayOutputStream output;
  private UserManagementCli cli;

  @BeforeEach
  void setUp() {
    output = new ByteArrayOutputStream();
  }

  private void cliReading(final String input) {
    final ConsoleIO console =
        new ConsoleIO(
            new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))),
            new PrintStream(output, true, StandardCharsets.UTF_8));
    cli = new UserManagementCli(userController, console);
  }

  private String capturedOutput() {
    return output.toString(StandardCharsets.UTF_8);
  }

  private static UserResponse sampleResponse() {
    return new UserResponse("u-001", "Alice Smith", "alice@example.com", "ADMIN", "ACTIVE");
  }

  @Test
  @DisplayName(
      "start() walks through every menu option, reports an invalid option and says goodbye on exit")
  void start_executesEveryOption_andExitsGracefully() {
    // Arrange
    final String input =
        String.join(
            "\n",
            "9",
            "1",
            "2",
            "u-001",
            "3",
            "u-001",
            "Alice Smith",
            "alice@example.com",
            "Pass1234",
            "ADMIN",
            "4",
            "u-001",
            "Alice Smith",
            "alice@example.com",
            "",
            "ADMIN",
            "ACTIVE",
            "5",
            "u-001",
            "6",
            "alice@example.com",
            "Pass1234",
            "0")
            + "\n";
    cliReading(input);
    when(userController.listAllUsers()).thenReturn(List.of(sampleResponse()));
    when(userController.findUserById("u-001")).thenReturn(sampleResponse());
    when(userController.createUser(any(CreateUserRequest.class))).thenReturn(sampleResponse());
    when(userController.updateUser(any(UpdateUserRequest.class))).thenReturn(sampleResponse());
    when(userController.login(any(LoginRequest.class))).thenReturn(sampleResponse());

    // Act
    cli.start();

    // Assert
    final String printed = capturedOutput();
    assertAll(
        "full journey output",
        () -> assertThat(printed).contains("Users Management System"),
        () -> assertThat(printed).contains("Main Menu"),
        () -> assertThat(printed).contains("Invalid option. Please try again."),
        () -> assertThat(printed).contains("Total: 1 user(s)"),
        () -> assertThat(printed).contains("User created successfully."),
        () -> assertThat(printed).contains("User updated successfully."),
        () -> assertThat(printed).contains("User deleted successfully."),
        () -> assertThat(printed).contains("Login successful. Welcome!"),
        () -> assertThat(printed).contains("Goodbye!"));
  }

  @Test
  @DisplayName("start() prints every validation violation when a handler raises ConstraintViolationException")
  void start_printsValidationErrors_whenHandlerRaisesConstraintViolation() {
    // Arrange
    cliReading("2\nu-001\n0\n");
    final ConstraintViolation<UserResponse> violation = mock(ConstraintViolation.class);
    when(violation.getMessage()).thenReturn("id must not be blank");
    final ConstraintViolationException violationException =
        new ConstraintViolationException(Set.of(violation));
    when(userController.findUserById("u-001")).thenThrow(violationException);

    // Act
    cli.start();

    // Assert
    final String printed = capturedOutput();
    assertAll(
        "validation error output",
        () -> assertThat(printed).contains("Validation errors:"),
        () -> assertThat(printed).contains("- id must not be blank"),
        () -> assertThat(printed).contains("Goodbye!"));
  }

  @Test
  @DisplayName("start() reports an unexpected error and keeps the loop running")
  void start_reportsUnexpectedError_whenHandlerRaisesRuntimeException() {
    // Arrange
    cliReading("2\nu-001\n0\n");
    when(userController.findUserById("u-001")).thenThrow(new RuntimeException("boom"));

    // Act
    cli.start();

    // Assert
    final String printed = capturedOutput();
    assertAll(
        "unexpected error output",
        () -> assertThat(printed).contains("Unexpected error: boom"),
        () -> assertThat(printed).contains("Goodbye!"));
  }
}
