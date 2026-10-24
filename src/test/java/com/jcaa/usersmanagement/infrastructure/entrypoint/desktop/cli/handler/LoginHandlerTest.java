package com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jcaa.usersmanagement.domain.exception.InvalidCredentialsException;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.io.ConsoleIO;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.io.UserResponsePrinter;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.controller.UserController;
import com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.dto.LoginRequest;
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
 * Tests for LoginHandler.
 *
 * <p>Covers: reading email and password, delegating a correctly populated LoginRequest, printing
 * the welcome message and the InvalidCredentials error path.
 */
@DisplayName("LoginHandler")
@ExtendWith(MockitoExtension.class)
class LoginHandlerTest {

  @Mock private UserController userController;

  private ByteArrayOutputStream output;

  @BeforeEach
  void setUp() {
    output = new ByteArrayOutputStream();
  }

  private LoginHandler handlerReading(final String input) {
    final ConsoleIO console =
        new ConsoleIO(
            new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))),
            new PrintStream(output, true, StandardCharsets.UTF_8));
    return new LoginHandler(userController, console, new UserResponsePrinter(console));
  }

  private String capturedOutput() {
    return output.toString(StandardCharsets.UTF_8);
  }

  @Test
  @DisplayName("handle() delegates a correctly populated LoginRequest and prints the welcome message")
  void handle_delegatesCorrectRequest_whenCredentialsAreValid() {
    // Arrange
    final LoginHandler handler = handlerReading("alice@example.com\nPass1234\n");
    final UserResponse logged =
        new UserResponse("u-001", "Alice Smith", "alice@example.com", "ADMIN", "ACTIVE");
    final ArgumentCaptor<LoginRequest> captor = ArgumentCaptor.forClass(LoginRequest.class);
    when(userController.login(captor.capture())).thenReturn(logged);

    // Act
    handler.handle();

    // Assert
    assertAll(
        "login request mapping",
        () -> assertThat(captor.getValue().email()).isEqualTo("alice@example.com"),
        () -> assertThat(captor.getValue().password()).isEqualTo("Pass1234"),
        () -> assertThat(capturedOutput()).contains("Login successful. Welcome!"),
        () -> assertThat(capturedOutput()).contains("alice@example.com"));
  }

  @Test
  @DisplayName("handle() prints the error message when the credentials are invalid")
  void handle_printsError_whenCredentialsAreInvalid() {
    // Arrange
    final LoginHandler handler = handlerReading("alice@example.com\nWrongPass1\n");
    when(userController.login(any(LoginRequest.class)))
        .thenThrow(InvalidCredentialsException.becauseCredentialsAreInvalid());

    // Act
    handler.handle();

    // Assert
    assertThat(capturedOutput()).contains("Error: ");
    verify(userController).login(any(LoginRequest.class));
  }
}
