package com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jcaa.usersmanagement.domain.exception.UserAlreadyExistsException;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.io.ConsoleIO;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.io.UserResponsePrinter;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.controller.UserController;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.dto.CreateUserRequest;
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
 * Tests for CreateUserHandler.
 *
 * <p>Covers: reading all five fields from the console, delegating a correctly populated request to
 * the controller, printing the created user, and the UserAlreadyExists error path.
 */
@DisplayName("CreateUserHandler")
@ExtendWith(MockitoExtension.class)
class CreateUserHandlerTest {

  @Mock private UserController userController;

  private ByteArrayOutputStream output;

  @BeforeEach
  void setUp() {
    output = new ByteArrayOutputStream();
  }

  private CreateUserHandler handlerReading(final String input) {
    final ConsoleIO console =
        new ConsoleIO(
            new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))),
            new PrintStream(output, true, StandardCharsets.UTF_8));
    return new CreateUserHandler(userController, console, new UserResponsePrinter(console));
  }

  private String capturedOutput() {
    return output.toString(StandardCharsets.UTF_8);
  }

  @Test
  @DisplayName("handle() delegates a correctly populated CreateUserRequest and prints the result")
  void handle_delegatesCorrectRequest_whenCreationSucceeds() {
    // Arrange
    final CreateUserHandler handler =
        handlerReading("u-001\nAlice Smith\nalice@example.com\nPass1234\nADMIN\n");
    final UserResponse created =
        new UserResponse("u-001", "Alice Smith", "alice@example.com", "ADMIN", "PENDING");
    final ArgumentCaptor<CreateUserRequest> captor = ArgumentCaptor.forClass(CreateUserRequest.class);
    when(userController.createUser(captor.capture())).thenReturn(created);

    // Act
    handler.handle();

    // Assert
    assertAll(
        "create request mapping",
        () -> assertThat(captor.getValue().id()).isEqualTo("u-001"),
        () -> assertThat(captor.getValue().name()).isEqualTo("Alice Smith"),
        () -> assertThat(captor.getValue().email()).isEqualTo("alice@example.com"),
        () -> assertThat(captor.getValue().password()).isEqualTo("Pass1234"),
        () -> assertThat(captor.getValue().role()).isEqualTo("ADMIN"),
        () -> assertThat(capturedOutput()).contains("User created successfully."),
        () -> assertThat(capturedOutput()).contains("alice@example.com"));
  }

  @Test
  @DisplayName("handle() prints the error message when the email already exists")
  void handle_printsError_whenUserAlreadyExists() {
    // Arrange
    final CreateUserHandler handler =
        handlerReading("u-002\nDave Brown\ndave@example.com\nPass5678\nMEMBER\n");
    when(userController.createUser(any(CreateUserRequest.class)))
        .thenThrow(UserAlreadyExistsException.becauseEmailAlreadyExists("dave@example.com"));

    // Act
    handler.handle();

    // Assert
    assertThat(capturedOutput())
        .contains("Error: A user with email 'dave@example.com' already exists.");
    verify(userController).createUser(any(CreateUserRequest.class));
  }
}
