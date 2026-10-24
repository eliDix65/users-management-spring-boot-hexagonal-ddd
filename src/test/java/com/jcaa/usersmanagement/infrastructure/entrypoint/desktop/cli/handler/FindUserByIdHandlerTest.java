package com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jcaa.usersmanagement.domain.exception.UserNotFoundException;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.io.ConsoleIO;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.io.UserResponsePrinter;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.controller.UserController;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for FindUserByIdHandler.
 *
 * <p>Covers: reading the id, printing the found user and the UserNotFound error path.
 */
@DisplayName("FindUserByIdHandler")
@ExtendWith(MockitoExtension.class)
class FindUserByIdHandlerTest {

  @Mock private UserController userController;

  private ByteArrayOutputStream output;

  @BeforeEach
  void setUp() {
    output = new ByteArrayOutputStream();
  }

  private FindUserByIdHandler handlerReading(final String input) {
    final ConsoleIO console =
        new ConsoleIO(
            new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))),
            new PrintStream(output, true, StandardCharsets.UTF_8));
    return new FindUserByIdHandler(userController, console, new UserResponsePrinter(console));
  }

  private String capturedOutput() {
    return output.toString(StandardCharsets.UTF_8);
  }

  @Test
  @DisplayName("handle() prints the user returned by the controller")
  void handle_printsUser_whenUserExists() {
    // Arrange
    final FindUserByIdHandler handler = handlerReading("u-001\n");
    when(userController.findUserById("u-001"))
        .thenReturn(new UserResponse("u-001", "Alice Smith", "alice@example.com", "ADMIN", "ACTIVE"));

    // Act
    handler.handle();

    // Assert
    verify(userController).findUserById("u-001");
    assertThat(capturedOutput()).contains("u-001").contains("alice@example.com");
  }

  @Test
  @DisplayName("handle() prints the error message when the user does not exist")
  void handle_printsError_whenUserDoesNotExist() {
    // Arrange
    final FindUserByIdHandler handler = handlerReading("u-999\n");
    when(userController.findUserById("u-999"))
        .thenThrow(UserNotFoundException.becauseIdWasNotFound("u-999"));

    // Act
    handler.handle();

    // Assert
    assertThat(capturedOutput()).contains("Not found: The user with id 'u-999' was not found.");
  }
}
