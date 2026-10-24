package com.jcaa.usersmanagement.infrastructure.entrypoint.rest.advice;

import static org.assertj.core.api.Assertions.assertThat;

import com.jcaa.usersmanagement.domain.exception.EmailSenderException;
import com.jcaa.usersmanagement.domain.exception.InvalidCredentialsException;
import com.jcaa.usersmanagement.domain.exception.UserAlreadyExistsException;
import com.jcaa.usersmanagement.domain.exception.UserNotFoundException;
import com.jcaa.usersmanagement.domain.exception.InvalidUserNameException;
import com.jcaa.usersmanagement.infrastructure.adapter.persistence.exception.PersistenceException;
import com.jcaa.usersmanagement.infrastructure.entrypoint.rest.dto.response.ApiErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotBlank;
import java.sql.SQLException;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * Tests for GlobalExceptionHandler.
 *
 * <p>Covers every @ExceptionHandler method: domain exceptions mapped to 404/409/401/400, constraint
 * violations and method-argument validation errors joined into a single message, persistence
 * failures and the generic fallback mapped to 500.
 */
@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  @DisplayName("handleUserNotFound() maps UserNotFoundException to 404")
  void handleUserNotFound_mapsTo404() {
    // Act
    final ApiErrorResponse response =
        handler.handleUserNotFound(UserNotFoundException.becauseIdWasNotFound("u-404"));

    // Assert
    org.junit.jupiter.api.Assertions.assertAll(
        "user not found response",
        () -> assertThat(response.status()).isEqualTo(404),
        () -> assertThat(response.message()).contains("u-404"));
  }

  @Test
  @DisplayName("handleUserAlreadyExists() maps UserAlreadyExistsException to 409")
  void handleUserAlreadyExists_mapsTo409() {
    // Act
    final ApiErrorResponse response =
        handler.handleUserAlreadyExists(
            UserAlreadyExistsException.becauseEmailAlreadyExists("dup@example.com"));

    // Assert
    org.junit.jupiter.api.Assertions.assertAll(
        "user already exists response",
        () -> assertThat(response.status()).isEqualTo(409),
        () -> assertThat(response.message()).contains("dup@example.com"));
  }

  @Test
  @DisplayName("handleInvalidCredentials() maps InvalidCredentialsException to 401")
  void handleInvalidCredentials_mapsTo401() {
    // Act
    final ApiErrorResponse response =
        handler.handleInvalidCredentials(InvalidCredentialsException.becauseCredentialsAreInvalid());

    // Assert
    assertThat(response.status()).isEqualTo(401);
  }

  @Test
  @DisplayName("handleConstraintViolation() joins every violation into a 400 message")
  void handleConstraintViolation_joinsViolationsInto400() {
    // Arrange
    try (final ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      final Set<ConstraintViolation<BlankNamePayload>> violations =
          factory.getValidator().validate(new BlankNamePayload());

      // Act
      final ApiErrorResponse response =
          handler.handleConstraintViolation(new ConstraintViolationException(violations));

      // Assert
      org.junit.jupiter.api.Assertions.assertAll(
          "constraint violation response",
          () -> assertThat(response.status()).isEqualTo(400),
          () -> assertThat(response.message()).contains("name: must not be blank"));
    }
  }

  @Test
  @DisplayName("handleMethodArgumentNotValid() joins the field errors into a 400 message")
  void handleMethodArgumentNotValid_joinsFieldErrors() throws NoSuchMethodException {
    // Arrange
    final MethodParameter parameter =
        new MethodParameter(
            GlobalExceptionHandlerTest.class.getDeclaredMethod("dummyEndpoint", String.class), 0);
    final BeanPropertyBindingResult bindingResult =
        new BeanPropertyBindingResult(new Object(), "request");
    bindingResult.addError(new FieldError("request", "email", "must be a valid email address"));

    // Act
    final ApiErrorResponse response =
        handler.handleMethodArgumentNotValid(
            new MethodArgumentNotValidException(parameter, bindingResult));

    // Assert
    org.junit.jupiter.api.Assertions.assertAll(
        "method argument not valid response",
        () -> assertThat(response.status()).isEqualTo(400),
        () -> assertThat(response.message()).contains("email: must be a valid email address"));
  }

  @Test
  @DisplayName("handleDomain() maps any DomainException to 400")
  void handleDomain_mapsTo400() {
    // Act
    final ApiErrorResponse response =
        handler.handleDomain(EmailSenderException.becauseSendFailed(new IllegalStateException("x")));

    // Assert
    assertThat(response.status()).isEqualTo(400);
  }

  @Test
  @DisplayName("handleDomain() also accepts validation domain exceptions")
  void handleDomain_acceptsValidationDomainExceptions() {
    // Act
    final ApiErrorResponse response =
        handler.handleDomain(InvalidUserNameException.becauseLengthIsTooShort(3));

    // Assert
    org.junit.jupiter.api.Assertions.assertAll(
        "domain validation response",
        () -> assertThat(response.status()).isEqualTo(400),
        () -> assertThat(response.message()).contains("at least 3 characters"));
  }

  @Test
  @DisplayName("handlePersistence() maps PersistenceException to a generic 500 message")
  void handlePersistence_mapsTo500() {
    // Act
    final ApiErrorResponse response =
        handler.handlePersistence(
            PersistenceException.becauseSaveFailed("u-1", new SQLException("connection reset")));

    // Assert
    org.junit.jupiter.api.Assertions.assertAll(
        "persistence response",
        () -> assertThat(response.status()).isEqualTo(500),
        () -> assertThat(response.message()).isEqualTo("Error de persistencia."));
  }

  @Test
  @DisplayName("handleGeneral() maps any exception to a generic 500 message")
  void handleGeneral_mapsTo500() {
    // Act
    final ApiErrorResponse response = handler.handleGeneral(new RuntimeException("algo falló"));

    // Assert
    org.junit.jupiter.api.Assertions.assertAll(
        "general response",
        () -> assertThat(response.status()).isEqualTo(500),
        () -> assertThat(response.message()).isEqualTo("Error interno del servidor."));
  }

  // ── helpers

  @SuppressWarnings("unused")
  private void dummyEndpoint(final String value) {}

  private static final class BlankNamePayload {
    @NotBlank(message = "must not be blank")
    private final String name = "";
  }
}
