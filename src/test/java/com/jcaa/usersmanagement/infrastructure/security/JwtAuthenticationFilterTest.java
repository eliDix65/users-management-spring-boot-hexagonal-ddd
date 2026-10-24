package com.jcaa.usersmanagement.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Tests for JwtAuthenticationFilter.
 *
 * <p>Covers: continuing the filter chain without authenticating when there is no bearer header, and
 * clearing the security context (instead of propagating) when the token cannot be parsed.
 */
@DisplayName("JwtAuthenticationFilter")
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

  @Mock private JwtTokenService jwtTokenService;

  private JwtAuthenticationFilter filter;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
    filter = new JwtAuthenticationFilter(jwtTokenService);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("continues the chain without authenticating when there is no Authorization header")
  void continuesChainWithoutAuthenticating_whenHeaderIsMissing()
      throws ServletException, IOException {
    // Arrange
    final MockHttpServletRequest request = new MockHttpServletRequest();
    final MockHttpServletResponse response = new MockHttpServletResponse();
    final MockFilterChain chain = new MockFilterChain();

    // Act
    filter.doFilterInternal(request, response, chain);

    // Assert
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    assertThat(chain.getRequest()).isSameAs(request);
  }

  @Test
  @DisplayName("clears the security context and continues the chain when the token is malformed")
  void clearsContextAndContinuesChain_whenTokenIsMalformed() throws ServletException, IOException {
    // Arrange
    final MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer broken-token");
    final MockHttpServletResponse response = new MockHttpServletResponse();
    final MockFilterChain chain = new MockFilterChain();
    when(jwtTokenService.parse("broken-token")).thenThrow(new MalformedJwtException("malformed"));

    // Act
    filter.doFilterInternal(request, response, chain);

    // Assert
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    assertThat(chain.getRequest()).isSameAs(request);
    verify(jwtTokenService).parse("broken-token");
  }
}
