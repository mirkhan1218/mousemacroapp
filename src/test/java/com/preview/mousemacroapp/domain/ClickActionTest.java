package com.preview.mousemacroapp.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 클릭 동작 방식(How) 도메인 테스트.
 *
 * @since 0.4
 */
class ClickActionTest {

    @Test
    @DisplayName("팩토리: 단일 좌클릭")
    void singleLeftFactory() {
        ClickAction action = ClickAction.singleLeft();

        assertEquals(MouseButton.LEFT, action.button());
        assertEquals(1, action.clickCount());
        assertEquals(0L, action.holdMillis());
    }

    @Test
    @DisplayName("팩토리: 더블 좌클릭")
    void doubleLeftFactory() {
        ClickAction action = ClickAction.doubleLeft();

        assertEquals(MouseButton.LEFT, action.button());
        assertEquals(2, action.clickCount());
        assertEquals(0L, action.holdMillis());
    }

    @Test
    @DisplayName("팩토리: 우클릭")
    void rightClickFactory() {
        ClickAction action = ClickAction.rightClick();

        assertEquals(MouseButton.RIGHT, action.button());
        assertEquals(1, action.clickCount());
        assertEquals(0L, action.holdMillis());
    }

    @Test
    @DisplayName("팩토리: 홀드 클릭")
    void holdFactory() {
        ClickAction action = ClickAction.hold(MouseButton.LEFT, 150);

        assertEquals(MouseButton.LEFT, action.button());
        assertEquals(1, action.clickCount());
        assertEquals(150L, action.holdMillis());
    }

    @Test
    @DisplayName("생성 제약: button이 null이면 예외")
    void nullButton_shouldThrow() {
        assertThrows(NullPointerException.class,
                () -> new ClickAction(null, 1, 0));
    }

    @Test
    @DisplayName("생성 제약: clickCount는 1 이상")
    void clickCountMustBeAtLeastOne() {
        assertThrows(IllegalArgumentException.class,
                () -> new ClickAction(MouseButton.LEFT, 0, 0));
    }

    @Test
    @DisplayName("생성 제약: holdMillis는 0 이상")
    void holdMillisMustBeNonNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> new ClickAction(MouseButton.LEFT, 1, -1));
    }

    @Test
    @DisplayName("생성 제약: 홀드 동작(holdMillis>0)은 clickCount=1 이어야 한다")
    void holdRequiresClickCountOne() {
        assertThrows(IllegalArgumentException.class,
                () -> new ClickAction(MouseButton.LEFT, 2, 10));
    }

    @Test
    @DisplayName("팩토리 제약: holdMillis가 음수이면 예외")
    void holdFactory_negativeMillis_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> ClickAction.hold(MouseButton.RIGHT, -10));
    }
}
