package com.preview.mousemacroapp.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MacroPoint 도메인 검증 테스트.
 *
 * @since 0.3
 */
class MacroPointTest {

    @Test
    @DisplayName("name이 null이면 예외")
    void nullName_shouldThrow() {
        assertThrows(NullPointerException.class,
                () -> new MacroPoint(null,
                        new ScreenPoint(1, 2),
                        new ExactPositionPolicy()));
    }

    @Test
    @DisplayName("name이 blank이면 예외")
    void blankName_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> new MacroPoint("  ",
                        new ScreenPoint(1, 2),
                        new ExactPositionPolicy()));
    }

    @Test
    @DisplayName("base가 null이면 예외")
    void nullBase_shouldThrow() {
        assertThrows(NullPointerException.class,
                () -> new MacroPoint("A",
                        null,
                        new ExactPositionPolicy()));
    }

    @Test
    @DisplayName("positionPolicy가 null이면 예외")
    void nullPolicy_shouldThrow() {
        assertThrows(NullPointerException.class,
                () -> new MacroPoint("A",
                        new ScreenPoint(1, 2),
                        null));
    }
}
