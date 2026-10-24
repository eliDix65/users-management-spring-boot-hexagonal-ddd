package com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.io.ConsoleIO;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.io.UserResponsePrinter;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.controller.UserController;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.dto.UserResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for ListUsersHandler.
 *
 * <p>Covers: printing the user list and the empty-list message.
 */
@DisplayName("ListUsersHandler")
@ExtendWith(MockitoExtension.class)
class ListUsersHandlerTest {

  @Mock private UserController userController;

  private ByteArrayOutputStream output;

  @BeforeEach
  void setUp() {
    output = new ByteArrayOutputStream();
  }

  private ListUsersHandler handler() {
    final ConsoleIO console =
        new ConsoleIO(
            new Scanner(new ByteArrayInputStream(new byte[0])),
            new PrintStream(output, true, StandardCharsets.UTF_8));
    return new ListUsersHandler(userController, new UserResponsePrinter(console));
  }

  private String capturedOutput() {
    return output.toString(StandardCharsets.UTF_8);
  }

  @Test
  @DisplayName("handle() prints every user returned by the controller")
  void handle_printsAllUsers_whenUsersExist() {
    // Arrange
    when(userController.listAllUsers())
        .thenReturn(
            List.of(
                new UserResponse("u-001", "Alice Smith", "alice@example.com", "ADMIN", "ACTIVE"),
                new UserResponse("u-002", "Bob Jones", "bob@example.com", "MEMBER", "ACTIVE")));

    // Act
    handler().handle();

    // Assert
    verify(userController).listAllUsers();
    assertThat(capturedOutput())
        .contains("Total: 2 user(s)")
        .contains("u-001")
        .contains("bob@example.com");
  }

  @Test
  @DisplayName("handle() prints the empty message when there are no users")
  void handle_printsEmptyMessage_whenNoUsersExist() {
    // Arrange
    when(userController.listAllUsers()).thenReturn(List.of());

    // Act
    handler().handle();

    // Assert
    assertThat(capturedOutput()).contains("No users found.");
  }
}
