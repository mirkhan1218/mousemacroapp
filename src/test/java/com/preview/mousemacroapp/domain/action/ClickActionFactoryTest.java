package com.preview.mousemacroapp.domain.action;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ClickAction 팩토리 정책 검증 테스트.
 *
 * <p><b>테스트 대상</b></p>
 * - ClickAction 팩토리 메서드
 *
 * <p><b>검증 목적</b></p>
 * - 단일/더블/우클릭/홀드 팩토리 반환값을 고정한다.
 *
 * <p><b>검증 범위</b></p>
 * - button
 * - clickCount
 * - holdMillis
 *
 * <p><b>회귀 방지 이유</b></p>
 * - 팩토리 로직 변경 시 클릭 동작이 왜곡되는 것을 방지한다.
 *
 * @since 0.4
 */
class ClickActionFactoryTest {

    /*
     * 시나리오: 단일 좌클릭 팩토리가 올바른 ClickAction을 생성해야 한다
     *
     * 입력(Given):
     * - ClickAction.singleLeft() 호출
     *
     * 예상 결과(Then):
     * - button == LEFT
     * - clickCount == 1
     * - holdMillis == 0
     */
    @Test
    @DisplayName("팩토리: 단일 좌클릭")
    void singleLeftFactory() {
        ClickAction action = ClickAction.singleLeft();

        assertEquals(MouseButton.LEFT, action.button());
        assertEquals(1, action.clickCount());
        assertEquals(0L, action.holdMillis());
    }

    /*
     * 시나리오: 더블 좌클릭 팩토리가 올바른 ClickAction을 생성해야 한다
     *
     * 입력(Given):
     * - ClickAction.doubleLeft() 호출
     *
     * 예상 결과(Then):
     * - button == LEFT
     * - clickCount == 2
     * - holdMillis == 0
     */
    @Test
    @DisplayName("팩토리: 더블 좌클릭")
    void doubleLeftFactory() {
        ClickAction action = ClickAction.doubleLeft();

        assertEquals(MouseButton.LEFT, action.button());
        assertEquals(2, action.clickCount());
        assertEquals(0L, action.holdMillis());
    }

    /*
     * 시나리오: 우클릭 팩토리가 올바른 ClickAction을 생성해야 한다
     *
     * 입력(Given):
     * - ClickAction.rightClick() 호출
     *
     * 예상 결과(Then):
     * - button == RIGHT
     * - clickCount == 1
     * - holdMillis == 0
     */
    @Test
    @DisplayName("팩토리: 우클릭")
    void rightClickFactory() {
        ClickAction action = ClickAction.rightClick();

        assertEquals(MouseButton.RIGHT, action.button());
        assertEquals(1, action.clickCount());
        assertEquals(0L, action.holdMillis());
    }

    /*
     * 시나리오: 홀드 팩토리가 올바른 ClickAction을 생성해야 한다
     *
     * 입력(Given):
     * - ClickAction.hold(LEFT, 150) 호출
     *
     * 예상 결과(Then):
     * - button == LEFT
     * - clickCount == 1
     * - holdMillis == 150
     */
    @Test
    @DisplayName("팩토리: 홀드 클릭")
    void holdFactory() {
        ClickAction action = ClickAction.hold(MouseButton.LEFT, 150);

        assertEquals(MouseButton.LEFT, action.button());
        assertEquals(1, action.clickCount());
        assertEquals(150L, action.holdMillis());
    }
}
