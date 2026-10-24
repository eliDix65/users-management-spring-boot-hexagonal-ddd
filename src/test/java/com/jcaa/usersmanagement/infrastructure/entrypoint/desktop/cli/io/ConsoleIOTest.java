package com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.io;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for ConsoleIO.
 *
 * <p>Covers: trimmed reads, blank-value re-prompting in readRequired(), non-numeric re-prompting in
 * readInt(), optional reads, and the three output methods. All I/O is exercised through a scripted
 * ByteArrayInputStream and a captured ByteArrayOutputStream — no real console involved.
 */
@DisplayName("ConsoleIO")
class ConsoleIOTest {

  private ByteArrayOutputStream output;

  @BeforeEach
  void setUp() {
    output = new ByteArrayOutputStream();
  }

  private ConsoleIO consoleReading(final String input) {
    final ByteArrayInputStream in =
        new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
    return new ConsoleIO(
        new Scanner(in), new PrintStream(output, true, StandardCharsets.UTF_8));
  }

  private String capturedOutput() {
    return output.toString(StandardCharsets.UTF_8);
  }

  // ── readRequired

  @Test
  @DisplayName("readRequired() returns the trimmed value when the input is not blank")
  void readRequired_returnsTrimmedValue_whenInputIsNotBlank() {
    // Arrange
    final ConsoleIO console = consoleReading("  u-001  \n");

    // Act
    final String value = console.readRequired("User ID: ");

    // Assert
    assertThat(value).isEqualTo("u-001");
    assertThat(capturedOutput()).contains("User ID: ");
  }

  @Test
  @DisplayName("readRequired() re-prompts until a non-blank value is entered")
  void readRequired_rePromptsUntilNotBlank_whenInputIsBlank() {
    // Arrange
    final ConsoleIO console = consoleReading("   \n \n  value  \n");

    // Act
    final String value = console.readRequired("Name: ");

    // Assert
    assertThat(value).isEqualTo("value");
    assertThat(capturedOutput()).contains("Value cannot be blank. Please try again.");
  }

  // ── readOptional

  @Test
  @DisplayName("readOptional() returns the trimmed value even when it is blank")
  void readOptional_returnsTrimmedValue_evenWhenBlank() {
    // Arrange
    final ConsoleIO console = consoleReading("   \n");

    // Act
    final String value = console.readOptional("New password: ");

    // Assert
    assertThat(value).isEmpty();
    assertThat(capturedOutput()).contains("New password: ");
  }

  // ── readInt

  @Test
  @DisplayName("readInt() parses a valid number")
  void readInt_parsesValidNumber() {
    // Arrange
    final ConsoleIO console = consoleReading("42\n");

    // Act + Assert
    assertThat(console.readInt("Option: ")).isEqualTo(42);
  }

  @Test
  @DisplayName("readInt() re-prompts until a number is entered")
  void readInt_rePromptsUntilNumber_whenInputIsNotNumeric() {
    // Arrange
    final ConsoleIO console = consoleReading("abc\n 7 \n");

    // Act
    final int value = console.readInt("Option: ");

    // Assert
    assertThat(value).isEqualTo(7);
    assertThat(capturedOutput()).contains("Invalid input. Please enter a number.");
  }

  // ── output

  @Test
  @DisplayName("println(), println() without arguments and printf() write to the output stream")
  void outputMethods_writeToTheStream() {
    // Arrange
    final ConsoleIO console = consoleReading("");

    // Act
    console.println("hello");
    console.println();
    console.printf("  [%d] %s%n", 1, "List all users");

    // Assert
    assertThat(capturedOutput())
        .contains("hello")
        .contains("  [1] List all users")
        .contains(System.lineSeparator());
  }
}
