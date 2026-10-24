package com.jcaa.usersmanagement.infrastructure.adapter.persistence.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DatabaseConfig")
class DatabaseConfigTest {

  private static final String HOST = "mysql.example.com";
  private static final int PORT = 15425;
  private static final String DATABASE = "crud_usuarios";
  private static final String USERNAME = "avnadmin";
  private static final String PASSWORD = "secret";
  private static final String SSL_MODE = "REQUIRED";

  @Test
  @DisplayName("buildJdbcUrl() usa la plantilla de MySQL cuando no se indica vendedor")
  void shouldBuildJdbcUrlWithConfiguredSslMode() {
    // Arrange
    final DatabaseConfig config =
        new DatabaseConfig(HOST, PORT, DATABASE, USERNAME, PASSWORD, SSL_MODE);

    // Act
    final String jdbcUrl = config.buildJdbcUrl();

    // Assert
    assertThat(jdbcUrl)
        .isEqualTo(
            "jdbc:mysql://mysql.example.com:15425/crud_usuarios"
                + "?sslMode=REQUIRED&serverTimezone=UTC&allowPublicKeyRetrieval=true");
  }

  @Test
  @DisplayName("buildJdbcUrl() usa la plantilla de PostgreSQL cuando vendor=postgresql")
  void shouldBuildPostgreSqlJdbcUrl() {
    // Arrange
    final DatabaseConfig config =
        new DatabaseConfig(
            HOST,
            PORT,
            DATABASE,
            USERNAME,
            PASSWORD,
            SSL_MODE,
            DatabaseConfig.VENDOR_POSTGRESQL);

    // Act
    final String jdbcUrl = config.buildJdbcUrl();

    // Assert
    assertThat(jdbcUrl)
        .isEqualTo(
            "jdbc:postgresql://mysql.example.com:15425/crud_usuarios?sslmode=require");
  }

  @Test
  @DisplayName("driverClassName() devuelve el driver que corresponde a cada motor")
  void shouldResolveDriverClassNamePerVendor() {
    // Arrange
    final DatabaseConfig mysqlConfig =
        new DatabaseConfig(HOST, PORT, DATABASE, USERNAME, PASSWORD, SSL_MODE);
    final DatabaseConfig postgresConfig =
        new DatabaseConfig(
            HOST, PORT, DATABASE, USERNAME, PASSWORD, SSL_MODE, DatabaseConfig.VENDOR_POSTGRESQL);

    // Act + Assert
    assertThat(mysqlConfig.driverClassName()).isEqualTo("com.mysql.cj.jdbc.Driver");
    assertThat(postgresConfig.driverClassName()).isEqualTo("org.postgresql.Driver");
  }

  @Test
  @DisplayName("buildJdbcUrl() traduce el modo SSL de MySQL al valor propio de PostgreSQL")
  void shouldTranslateSslModeForPostgreSql() {
    // Arrange
    final DatabaseConfig disabled =
        new DatabaseConfig(
            HOST,
            PORT,
            DATABASE,
            USERNAME,
            PASSWORD,
            "DISABLED",
            DatabaseConfig.VENDOR_POSTGRESQL);
    final DatabaseConfig verifyFull =
        new DatabaseConfig(
            HOST,
            PORT,
            DATABASE,
            USERNAME,
            PASSWORD,
            "VERIFY_FULL",
            DatabaseConfig.VENDOR_POSTGRESQL);

    // Act + Assert
    assertThat(disabled.buildJdbcUrl()).endsWith("?sslmode=disable");
    assertThat(verifyFull.buildJdbcUrl()).endsWith("?sslmode=verify-full");
  }

  @Test
  @DisplayName("buildJdbcUrl() usa MySQL cuando el vendedor es null o vacío")
  void shouldDefaultToMySqlWhenVendorIsNullOrBlank() {
    // Arrange
    final DatabaseConfig nullVendor =
        new DatabaseConfig(HOST, PORT, DATABASE, USERNAME, PASSWORD, SSL_MODE, null);
    final DatabaseConfig blankVendor =
        new DatabaseConfig(HOST, PORT, DATABASE, USERNAME, PASSWORD, SSL_MODE, "   ");

    // Act + Assert
    assertThat(nullVendor.buildJdbcUrl()).startsWith("jdbc:mysql://");
    assertThat(blankVendor.buildJdbcUrl()).startsWith("jdbc:mysql://");
    assertThat(nullVendor.driverClassName()).isEqualTo("com.mysql.cj.jdbc.Driver");
    assertThat(blankVendor.driverClassName()).isEqualTo("com.mysql.cj.jdbc.Driver");
  }

  @Test
  @DisplayName("buildJdbcUrl() usa sslmode=disable cuando PostgreSQL no recibe modo SSL")
  void shouldDefaultToDisableSslForPostgreSqlWhenSslModeIsBlank() {
    // Arrange
    final DatabaseConfig nullSslMode =
        new DatabaseConfig(HOST, PORT, DATABASE, USERNAME, PASSWORD, null, "PostgreSQL");
    final DatabaseConfig blankSslMode =
        new DatabaseConfig(HOST, PORT, DATABASE, USERNAME, PASSWORD, "  ", "PostgreSQL");

    // Act + Assert
    assertThat(nullSslMode.buildJdbcUrl()).endsWith("?sslmode=disable");
    assertThat(blankSslMode.buildJdbcUrl()).endsWith("?sslmode=disable");
  }

  @Test
  @DisplayName("buildJdbcUrl() traduce el resto de modos SSL de PostgreSQL")
  void shouldTranslateRemainingSslModesForPostgreSql() {
    // Arrange
    final DatabaseConfig disable =
        new DatabaseConfig(
            HOST, PORT, DATABASE, USERNAME, PASSWORD, "DISABLE", DatabaseConfig.VENDOR_POSTGRESQL);
    final DatabaseConfig verifyCa =
        new DatabaseConfig(
            HOST, PORT, DATABASE, USERNAME, PASSWORD, "VERIFY_CA", DatabaseConfig.VENDOR_POSTGRESQL);
    final DatabaseConfig unknown =
        new DatabaseConfig(
            HOST, PORT, DATABASE, USERNAME, PASSWORD, "SOMETHING_ELSE",
            DatabaseConfig.VENDOR_POSTGRESQL);

    // Act + Assert
    assertThat(disable.buildJdbcUrl()).endsWith("?sslmode=disable");
    assertThat(verifyCa.buildJdbcUrl()).endsWith("?sslmode=verify-ca");
    assertThat(unknown.buildJdbcUrl()).endsWith("?sslmode=require");
  }
}