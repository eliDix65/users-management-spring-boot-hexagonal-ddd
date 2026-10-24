package com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.io;

import static org.assertj.core.api.Assertions.assertThat;

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

/**
 * Tests for UserResponsePrinter.
 *
 * <p>Covers: single-user rendering with every field, the empty-list message and the non-empty list
 * summary. The printer writes through a real ConsoleIO backed by a captured stream.
 */
@DisplayName("UserResponsePrinter")
class UserResponsePrinterTest {

  private ByteArrayOutputStream output;
  private UserResponsePrinter printer;

  @BeforeEach
  void setUp() {
    output = new ByteArrayOutputStream();
    final ConsoleIO console =
        new ConsoleIO(
            new Scanner(new ByteArrayInputStream(new byte[0])),
            new PrintStream(output, true, StandardCharsets.UTF_8));
    printer = new UserResponsePrinter(console);
  }

  private static UserResponse response(final String id, final String email) {
    return new UserResponse(id, "Alice Smith", email, "ADMIN", "ACTIVE");
  }

  @Test
  @DisplayName("print() renders every field of the user between separators")
  void print_rendersEveryField() {
    // Act
    printer.print(response("u-001", "alice@example.com"));

    // Assert
    final String printed = output.toString(StandardCharsets.UTF_8);
    assertThat(printed)
        .contains("-".repeat(52))
        .contains("u-001")
        .contains("Alice Smith")
        .contains("alice@example.com")
        .contains("ADMIN")
        .contains("ACTIVE");
  }

  @Test
  @DisplayName("printList() prints a friendly message when the list is empty")
  void printList_printsNoUsersFound_whenListIsEmpty() {
    // Act
    printer.printList(List.of());

    // Assert
    assertThat(output.toString(StandardCharsets.UTF_8)).contains("No users found.");
  }

  @Test
  @DisplayName("printList() prints the total and each user when the list is not empty")
  void printList_printsTotalAndEachUser_whenListIsNotEmpty() {
    // Act
    printer.printList(List.of(response("u-001", "alice@example.com"), response("u-002", "bob@example.com")));

    // Assert
    final String printed = output.toString(StandardCharsets.UTF_8);
    assertThat(printed)
        .contains("Total: 2 user(s)")
        .contains("u-001")
        .contains("u-002")
        .contains("bob@example.com");
  }
}
