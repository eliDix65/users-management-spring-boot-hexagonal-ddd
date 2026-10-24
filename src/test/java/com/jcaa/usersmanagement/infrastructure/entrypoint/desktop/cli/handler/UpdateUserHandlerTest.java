package com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jcaa.usersmanagement.domain.exception.UserNotFoundException;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.io.ConsoleIO;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.io.UserResponsePrinter;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.controller.UserController;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.dto.UpdateUserRequest;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.dto.UserResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for UpdateUserHandler.
 *
 * <p>Covers: reading every field from the console, the blank-password branch (mapped to null so the
 * current password is kept), the non-blank password branch, the success message and the
 * UserNotFound error path.
 */
@DisplayName("UpdateUserHandler")
@ExtendWith(MockitoExtension.class)
class UpdateUserHandlerTest {

  @Mock private UserController userController;

  private ByteArrayOutputStream output;

  @BeforeEach
  void setUp() {
    output = new ByteArrayOutputStream();
  }

  private UpdateUserHandler handlerReading(final String input) {
    final ConsoleIO console =
        new ConsoleIO(
            new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))),
            new PrintStream(output, true, StandardCharsets.UTF_8));
    return new UpdateUserHandler(userController, console, new UserResponsePrinter(console));
  }

  private String capturedOutput() {
    return output.toString(StandardCharsets.UTF_8);
  }

  @Test
  @DisplayName("handle() maps a blank password to null and delegates the update request")
  void handle_mapsBlankPasswordToNull_whenPasswordIsLeftBlank() {
    // Arrange
    final UpdateUserHandler handler =
        handlerReading("u-001\nAlice Smith\nalice@example.com\n\nADMIN\nACTIVE\n");
    final UserResponse updated =
        new UserResponse("u-001", "Alice Smith", "alice@example.com", "ADMIN", "ACTIVE");
    final ArgumentCaptor<UpdateUserRequest> captor =
        ArgumentCaptor.forClass(UpdateUserRequest.class);
    when(userController.updateUser(captor.capture())).thenReturn(updated);

    // Act
    handler.handle();

    // Assert
    assertAll(
        "update request mapping with blank password",
        () -> assertThat(captor.getValue().id()).isEqualTo("u-001"),
        () -> assertThat(captor.getValue().name()).isEqualTo("Alice Smith"),
        () -> assertThat(captor.getValue().password()).isNull(),
        () -> assertThat(captor.getValue().role()).isEqualTo("ADMIN"),
        () -> assertThat(captor.getValue().status()).isEqualTo("ACTIVE"),
        () -> assertThat(capturedOutput()).contains("User updated successfully."));
  }

  @Test
  @DisplayName("handle() sends the new password when it is provided")
  void handle_sendsNewPassword_whenPasswordIsProvided() {
    // Arrange
    final UpdateUserHandler handler =
        handlerReading("u-001\nAlice Smith\nalice@example.com\nNewPass123\nADMIN\nACTIVE\n");
    final UserResponse updated =
        new UserResponse("u-001", "Alice Smith", "alice@example.com", "ADMIN", "ACTIVE");
    final ArgumentCaptor<UpdateUserRequest> captor =
        ArgumentCaptor.forClass(UpdateUserRequest.class);
    when(userController.updateUser(captor.capture())).thenReturn(updated);

    // Act
    handler.handle();

    // Assert
    assertThat(captor.getValue().password()).isEqualTo("NewPass123");
  }

  @Test
  @DisplayName("handle() prints the error message when the user does not exist")
  void handle_printsError_whenUserDoesNotExist() {
    // Arrange
    final UpdateUserHandler handler =
        handlerReading("u-999\nGhost User\nghost@example.com\nPass9999\nMEMBER\nINACTIVE\n");
    when(userController.updateUser(any(UpdateUserRequest.class)))
        .thenThrow(UserNotFoundException.becauseIdWasNotFound("u-999"));

    // Act
    handler.handle();

    // Assert
    assertThat(capturedOutput()).contains("Not found: The user with id 'u-999' was not found.");
    verify(userController).updateUser(any(UpdateUserRequest.class));
  }
}
