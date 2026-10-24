package com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jcaa.usersmanagement.domain.exception.UserNotFoundException;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.io.ConsoleIO;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.controller.UserController;
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
 * Tests for DeleteUserHandler.
 *
 * <p>Covers: reading the id, delegating the deletion and the UserNotFound error path.
 */
@DisplayName("DeleteUserHandler")
@ExtendWith(MockitoExtension.class)
class DeleteUserHandlerTest {

  @Mock private UserController userController;

  private ByteArrayOutputStream output;

  @BeforeEach
  void setUp() {
    output = new ByteArrayOutputStream();
  }

  private DeleteUserHandler handlerReading(final String input) {
    final ConsoleIO console =
        new ConsoleIO(
            new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))),
            new PrintStream(output, true, StandardCharsets.UTF_8));
    return new DeleteUserHandler(userController, console);
  }

  private String capturedOutput() {
    return output.toString(StandardCharsets.UTF_8);
  }

  @Test
  @DisplayName("handle() deletes the user with the entered id")
  void handle_deletesUser_whenUserExists() {
    // Arrange
    final DeleteUserHandler handler = handlerReading("u-001\n");

    // Act
    handler.handle();

    // Assert
    verify(userController).deleteUser("u-001");
    assertThat(capturedOutput()).contains("User deleted successfully.");
  }

  @Test
  @DisplayName("handle() prints the error message when the user does not exist")
  void handle_printsError_whenUserDoesNotExist() {
    // Arrange
    final DeleteUserHandler handler = handlerReading("u-999\n");
    org.mockito.Mockito.doThrow(UserNotFoundException.becauseIdWasNotFound("u-999"))
        .when(userController)
        .deleteUser("u-999");

    // Act
    handler.handle();

    // Assert
    assertThat(capturedOutput()).contains("Not found: The user with id 'u-999' was not found.");
  }
}
