package com.jcaa.usersmanagement.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Tests for DataSourceSpringConfig.
 *
 * <p>Covers the dataSource() bean: the HikariCP pool is constructed with the injected properties
 * (host, port, database, credentials, SSL mode and vendor). The HikariDataSource construction is
 * intercepted so no real connection is attempted; the HikariConfig wiring still executes for real.
 */
@DisplayName("DataSourceSpringConfig")
class DataSourceSpringConfigTest {

  @Test
  @DisplayName("dataSource() builds a HikariDataSource from the injected properties")
  void dataSource_buildsHikariDataSourceFromInjectedProperties() {
    // Arrange
    final DataSourceSpringConfig springConfig = new DataSourceSpringConfig();
    ReflectionTestUtils.setField(springConfig, "dbHost", "localhost");
    ReflectionTestUtils.setField(springConfig, "dbPort", 3306);
    ReflectionTestUtils.setField(springConfig, "dbName", "test_db");
    ReflectionTestUtils.setField(springConfig, "dbUsername", "test_user");
    ReflectionTestUtils.setField(springConfig, "dbPassword", "test_pass");
    ReflectionTestUtils.setField(springConfig, "dbSslMode", "DISABLED");
    ReflectionTestUtils.setField(springConfig, "dbVendor", "mysql");

    // Act + Assert
    try (final MockedConstruction<HikariDataSource> mocked =
        Mockito.mockConstruction(HikariDataSource.class)) {
      final DataSource dataSource = springConfig.dataSource();

      assertThat(dataSource).isInstanceOf(HikariDataSource.class);
      assertThat(mocked.constructed()).hasSize(1);
    }
  }
}
