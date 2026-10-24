package com.jcaa.usersmanagement.infrastructure.entrypoint.desktop.cli.menu;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** Tests for the MenuOption enum: number/description getters and lookup by number. */
@DisplayName("MenuOption")
class MenuOptionTest {

  @ParameterizedTest
  @EnumSource(MenuOption.class)
  @DisplayName("fromNumber() returns the option that matches its number")
  void fromNumber_returnsMatchingOption(final MenuOption option) {
    assertThat(MenuOption.fromNumber(option.getNumber())).contains(option);
  }

  @Test
  @DisplayName("fromNumber() returns an empty Optional for an unknown number")
  void fromNumber_returnsEmpty_whenNumberIsUnknown() {
    assertThat(MenuOption.fromNumber(99)).isEmpty();
    assertThat(MenuOption.fromNumber(-1)).isEmpty();
  }

  @Test
  @DisplayName("getNumber() and getDescription() expose the declared values")
  void getters_exposeDeclaredValues() {
    assertThat(MenuOption.LIST_USERS.getNumber()).isEqualTo(1);
    assertThat(MenuOption.LIST_USERS.getDescription()).isEqualTo("List all users");
    assertThat(MenuOption.EXIT.getNumber()).isEqualTo(0);
    assertThat(MenuOption.EXIT.getDescription()).isEqualTo("Exit");
  }
}
