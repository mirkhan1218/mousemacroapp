package com.preview.mousemacroapp.domain.point;

import com.preview.mousemacroapp.domain.action.policy.ExactPositionPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MacroPoint 생성 유효성 검증 테스트.
 *
 * <p><b>테스트 대상</b></p>
 * - MacroPoint 생성자 제약
 *
 * <p><b>검증 목적</b></p>
 * - UI/설정 로딩 입력이 도메인을 오염시키지 않도록 생성 시점 검증을 고정한다.
 *
 * <p><b>검증 범위</b></p>
 * - name/base/policy null 방어
 * - name blank 방어
 * - 정상 입력 생성 성공
 *
 * <p><b>회귀 방지 이유</b></p>
 * - 잘못된 포인트 정의가 누적되면 실행 단계에서 예측 불가능한 동작을 만든다.
 *
 * @since 0.3
 */
class MacroPointTest {

    /*
     * 시나리오: name이 null이면 생성이 실패해야 한다
     *
     * 입력(Given):
     * - name = null
     *
     * 예상 결과(Then):
     * - NullPointerException 발생
     */
    @Test
    @DisplayName("name이 null이면 예외")
    void nullName_shouldThrow() {
        assertThrows(NullPointerException.class,
                () -> new MacroPoint(null,
                        new ScreenPoint(1, 2),
                        new ExactPositionPolicy()));
    }

    /*
     * 시나리오: name이 공백(blank)이면 생성이 실패해야 한다
     *
     * 입력(Given):
     * - name = "  "
     *
     * 예상 결과(Then):
     * - IllegalArgumentException 발생
     */
    @Test
    @DisplayName("name이 blank이면 예외")
    void blankName_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> new MacroPoint("  ",
                        new ScreenPoint(1, 2),
                        new ExactPositionPolicy()));
    }

    /*
     * 시나리오: base가 null이면 생성이 실패해야 한다
     *
     * 입력(Given):
     * - base = null
     *
     * 예상 결과(Then):
     * - NullPointerException 발생
     */
    @Test
    @DisplayName("base가 null이면 예외")
    void nullBase_shouldThrow() {
        assertThrows(NullPointerException.class,
                () -> new MacroPoint("A",
                        null,
                        new ExactPositionPolicy()));
    }

    /*
     * 시나리오: positionPolicy가 null이면 생성이 실패해야 한다
     *
     * 입력(Given):
     * - positionPolicy = null
     *
     * 예상 결과(Then):
     * - NullPointerException 발생
     */
    @Test
    @DisplayName("positionPolicy가 null이면 예외")
    void nullPolicy_shouldThrow() {
        assertThrows(NullPointerException.class,
                () -> new MacroPoint("A",
                        new ScreenPoint(1, 2),
                        null));
    }
}
