package com.preview.mousemacroapp.domain.action;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ClickAction 생성 제약(Validation) 검증 테스트.
 *
 * <p><b>테스트 대상</b></p>
 * - ClickAction 생성자 제약 조건
 *
 * <p><b>검증 목적</b></p>
 * - 잘못된 입력을 생성 시점에서 차단한다.
 *
 * <p><b>검증 범위</b></p>
 * - null button
 * - clickCount 최소값
 * - holdMillis 음수
 * - holdMillis > 0 && clickCount != 1
 *
 * <p><b>회귀 방지 이유</b></p>
 * - 잘못된 클릭 조합이 실행 엔진에 전달되는 것을 방지한다.
 *
 * @since 0.4
 */
class ClickActionValidationTest {

    /*
     * 시나리오: button이 null이면 생성이 실패해야 한다
     *
     * 입력(Given):
     * - new ClickAction(null, 1, 0)
     *
     * 예상 결과(Then):
     * - NullPointerException 발생
     */
    @Test
    @DisplayName("button이 null이면 예외")
    void nullButton_shouldThrow() {
        assertThrows(NullPointerException.class,
                () -> new ClickAction(null, 1, 0));
    }

    /*
     * 시나리오: clickCount는 1 이상이어야 한다
     *
     * 입력(Given):
     * - new ClickAction(LEFT, 0, 0)
     *
     * 예상 결과(Then):
     * - IllegalArgumentException 발생
     */
    @Test
    @DisplayName("clickCount는 1 이상이어야 한다")
    void clickCountMustBeAtLeastOne() {
        assertThrows(IllegalArgumentException.class,
                () -> new ClickAction(MouseButton.LEFT, 0, 0));
    }

    /*
     * 시나리오: holdMillis는 0 이상이어야 한다
     *
     * 입력(Given):
     * - new ClickAction(LEFT, 1, -1)
     *
     * 예상 결과(Then):
     * - IllegalArgumentException 발생
     */
    @Test
    @DisplayName("holdMillis는 0 이상이어야 한다")
    void holdMillisMustBeNonNegative() {
        assertThrows(IllegalArgumentException.class,
                () -> new ClickAction(MouseButton.LEFT, 1, -1));
    }

    /*
     * 시나리오: holdMillis>0이면 clickCount는 반드시 1이어야 한다
     *
     * 입력(Given):
     * - new ClickAction(LEFT, 2, 10)
     *
     * 예상 결과(Then):
     * - IllegalArgumentException 발생
     */
    @Test
    @DisplayName("holdMillis>0이면 clickCount는 반드시 1이어야 한다")
    void holdRequiresClickCountOne() {
        assertThrows(IllegalArgumentException.class,
                () -> new ClickAction(MouseButton.LEFT, 2, 10));
    }
}
