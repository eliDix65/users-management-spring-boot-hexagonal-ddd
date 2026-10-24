package com.jcaa.usersmanagement.infrastructure.adapter.email;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import com.jcaa.usersmanagement.domain.exception.EmailSenderException;
import com.jcaa.usersmanagement.domain.model.EmailDestinationModel;
import java.lang.reflect.Field;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * Tests for JavaMailEmailSenderAdapter.
 *
 * <p>Covers: successful message dispatch (happy path) and EmailSenderException wrapping when
 * Transport.send() raises MessagingException (exceptional branch). Transport.send() is a static
 * method — mocked with MockedStatic to prevent any real SMTP connection.
 */
@DisplayName("JavaMailEmailSenderAdapter")
class JavaMailEmailSenderAdapterTest {

  private static final String HOST = "smtp.example.com";
  private static final int PORT = 587;
  private static final int IMPLICIT_TLS_PORT = 465;
  private static final String USERNAME = "user@example.com";
  private static final String PASSWORD = "secret";
  private static final String FROM_ADDRESS = "noreply@example.com";
  private static final String FROM_NAME = "App Notifications";
  private static final String DEST_EMAIL = "john@example.com";
  private static final String DEST_NAME = "John Doe";
  private static final String SUBJECT = "Account created";
  private static final String BODY = "<html>Welcome</html>";

  private JavaMailEmailSenderAdapter adapter;
  private EmailDestinationModel destination;

  @BeforeEach
  void setUp() {
    final SmtpConfig config =
        new SmtpConfig(HOST, PORT, USERNAME, PASSWORD, FROM_ADDRESS, FROM_NAME);
    adapter = new JavaMailEmailSenderAdapter(config);
    destination = new EmailDestinationModel(DEST_EMAIL, DEST_NAME, SUBJECT, BODY);
  }

  // ── send() — happy path

  @Test
  @DisplayName("send() calls Transport.send() exactly once when SMTP succeeds")
  void shouldDispatchMessageWhenSmtpSucceeds() {
    // Arrange
    try (final MockedStatic<Transport> mockedTransport = mockStatic(Transport.class)) {
      // Transport.send(Message) is void — default mock behaviour is a no-op

      // Act
      adapter.send(destination);

      // Assert
      mockedTransport.verify(() -> Transport.send(any(Message.class)));
    }
  }

  // ── send() — MessagingException → EmailSenderException

  @Test
  @DisplayName("send() wraps MessagingException into EmailSenderException with destination email")
  void shouldThrowEmailSenderExceptionWhenTransportFails() {
    // Declare the cause before the static mock to avoid intercepting
    // any static calls MessagingException's constructor may trigger
    final MessagingException smtpError = new MessagingException("Connection refused");

    // Arrange
    try (final MockedStatic<Transport> mockedTransport = mockStatic(Transport.class)) {
      mockedTransport.when(() -> Transport.send(any(Message.class))).thenThrow(smtpError);

      // Act & Assert
      final EmailSenderException exception =
          assertThrows(EmailSenderException.class, () -> adapter.send(destination));
      assertTrue(
          exception.getMessage().contains(DEST_EMAIL),
          "exception message must identify the recipient email");
    }
  }

  // ── Authenticator — getPasswordAuthentication() credentials branch

  @Test
  @SuppressWarnings("java:S3011") // reflection required to access private mailSession in test scope
  @DisplayName("Authenticator returns PasswordAuthentication with credentials from SmtpConfig")
  void shouldProvideConfiguredCredentialsWhenAuthenticatorIsInvoked() throws Exception {
    // Arrange — retrieve the private mailSession field to reach the stored Authenticator
    final Field sessionField = JavaMailEmailSenderAdapter.class.getDeclaredField("mailSession");
    sessionField.setAccessible(true);
    final Session mailSession = (Session) sessionField.get(adapter);

    // Act — Session.requestPasswordAuthentication() internally invokes the stored
    // Authenticator's getPasswordAuthentication(), covering that branch
    final PasswordAuthentication auth =
        mailSession.requestPasswordAuthentication(null, PORT, "smtp", "Login", USERNAME);

    // Assert
    assertAll(
        "credentials must match SmtpConfig",
        () -> assertEquals(USERNAME, auth.getUserName()),
        () -> assertEquals(PASSWORD, auth.getPassword()));
  }

  // ── TLS — puerto 465 usa TLS implicito en lugar de STARTTLS

  @Test
  @SuppressWarnings("java:S3011") // reflection required to access private mailSession in test scope
  @DisplayName("Puerto 465 habilita SSL implicito y desactiva STARTTLS")
  void shouldEnableImplicitTlsForPort465() throws Exception {
    // Arrange — el adaptador se construye con el puerto 465
    final JavaMailEmailSenderAdapter sslAdapter =
        new JavaMailEmailSenderAdapter(
            new SmtpConfig(HOST, IMPLICIT_TLS_PORT, USERNAME, PASSWORD, FROM_ADDRESS, FROM_NAME));
    final Field sessionField = JavaMailEmailSenderAdapter.class.getDeclaredField("mailSession");
    sessionField.setAccessible(true);
    final Properties properties = ((Session) sessionField.get(sslAdapter)).getProperties();

    // Act + Assert
    assertAll(
        "implicit TLS properties",
        () -> assertEquals("true", properties.get("mail.smtp.ssl.enable")),
        () -> assertEquals("false", properties.get("mail.smtp.starttls.enable")),
        () -> assertEquals(HOST, properties.get("mail.smtp.ssl.trust")),
        () -> assertEquals(String.valueOf(IMPLICIT_TLS_PORT), properties.get("mail.smtp.port")));
  }

  @Test
  @SuppressWarnings("java:S3011") // reflection required to access private mailSession in test scope
  @DisplayName("Puerto distinto de 465 mantiene STARTTLS y no habilita SSL implicito")
  void shouldKeepStartTlsForOtherPorts() throws Exception {
    // Arrange — el adaptador bajo prueba usa PORT (587)
    final Field sessionField = JavaMailEmailSenderAdapter.class.getDeclaredField("mailSession");
    sessionField.setAccessible(true);
    final Properties properties = ((Session) sessionField.get(adapter)).getProperties();

    // Act + Assert
    assertAll(
        "STARTTLS properties",
        () -> assertEquals("true", properties.get("mail.smtp.starttls.enable")),
        () -> assertNull(properties.get("mail.smtp.ssl.enable")));
  }
}
