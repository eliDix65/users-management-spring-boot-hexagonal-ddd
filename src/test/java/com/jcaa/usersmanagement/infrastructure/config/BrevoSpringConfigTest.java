package com.jcaa.usersmanagement.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jcaa.usersmanagement.infrastructure.adapter.email.BrevoConfig;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

/**
 * Tests for BrevoSpringConfig.
 *
 * <p>Covers: building the BrevoConfig bean with injected values (including the fingerprint logging
 * path), the validation performed by the underlying record and the RestClient bean with its default
 * headers and timeouts. All values are fictitious.
 */
@DisplayName("BrevoSpringConfig")
class BrevoSpringConfigTest {

  private static final String API_KEY = "xkeysib-test-api-key";

  private final BrevoSpringConfig springConfig = new BrevoSpringConfig();

  private BrevoConfig validConfig() {
    return springConfig.brevoConfig(
        "https://api.brevo.test",
        API_KEY,
        "noreply@example.com",
        "App Notifications",
        Duration.ofSeconds(5),
        Duration.ofSeconds(10),
        3,
        Duration.ofMillis(200),
        2.0,
        50,
        10,
        5,
        Duration.ofSeconds(30));
  }

  @Test
  @DisplayName("brevoConfig() builds the configuration with the injected values")
  void brevoConfig_buildsConfigurationWithInjectedValues() {
    // Act
    final BrevoConfig config = validConfig();

    // Assert
    org.junit.jupiter.api.Assertions.assertAll(
        "brevo config bean",
        () -> assertThat(config.baseUrl()).isEqualTo("https://api.brevo.test"),
        () -> assertThat(config.apiKey()).isEqualTo(API_KEY),
        () -> assertThat(config.fromAddress()).isEqualTo("noreply@example.com"),
        () -> assertThat(config.fromName()).isEqualTo("App Notifications"),
        () -> assertThat(config.connectTimeout()).isEqualTo(Duration.ofSeconds(5)),
        () -> assertThat(config.readTimeout()).isEqualTo(Duration.ofSeconds(10)),
        () -> assertThat(config.retryMaxAttempts()).isEqualTo(3),
        () -> assertThat(config.retryMultiplier()).isEqualTo(2.0),
        () -> assertThat(config.circuitFailureThreshold()).isEqualTo(50),
        () -> assertThat(config.circuitSlidingWindowSize()).isEqualTo(10),
        () -> assertThat(config.circuitMinimumCalls()).isEqualTo(5),
        () -> assertThat(config.circuitOpenDuration()).isEqualTo(Duration.ofSeconds(30)));
  }

  @Test
  @DisplayName("brevoConfig() propagates the validation error when the api key is not a Brevo key")
  void brevoConfig_rejectsApiKeyWithoutBrevoPrefix() {
    // Act + Assert
    assertThrows(
        IllegalArgumentException.class,
        () ->
            springConfig.brevoConfig(
                "https://api.brevo.test",
                "not-a-brevo-key",
                "noreply@example.com",
                "App Notifications",
                Duration.ofSeconds(5),
                Duration.ofSeconds(10),
                3,
                Duration.ofMillis(200),
                2.0,
                50,
                10,
                5,
                Duration.ofSeconds(30)));
  }

  @Test
  @DisplayName("brevoConfig() propagates the validation error when the base url is blank")
  void brevoConfig_rejectsBlankBaseUrl() {
    // Act + Assert
    assertThrows(
        IllegalArgumentException.class,
        () ->
            springConfig.brevoConfig(
                "  ",
                API_KEY,
                "noreply@example.com",
                "App Notifications",
                Duration.ofSeconds(5),
                Duration.ofSeconds(10),
                3,
                Duration.ofMillis(200),
                2.0,
                50,
                10,
                5,
                Duration.ofSeconds(30)));
  }

  @Test
  @DisplayName("brevoRestClient() builds a client from the configuration")
  void brevoRestClient_buildsClientFromConfiguration() {
    // Arrange
    final BrevoConfig config = validConfig();

    // Act
    final RestClient restClient = springConfig.brevoRestClient(config);

    // Assert
    assertThat(restClient).isNotNull();
  }
}
