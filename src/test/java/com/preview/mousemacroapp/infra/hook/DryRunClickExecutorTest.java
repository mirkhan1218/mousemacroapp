package com.preview.mousemacroapp.infra.hook;

import com.preview.mousemacroapp.domain.action.ClickAction;
import com.preview.mousemacroapp.domain.point.ScreenPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * DryRunClickExecutor 입력 계약 검증 테스트.
 *
 * <p><b>테스트 대상</b></p>
 * - {@link com.preview.mousemacroapp.infra.hook.DryRunClickExecutor}
 *
 * <p><b>검증 목적</b></p>
 * - 실행기 생성 및 실행 시 필수 입력이 누락되면
 *   즉시 예외가 발생하는지 검증한다.
 *
 * <p><b>검증 범위</b></p>
 * - 생성 시 clock = null
 * - execute 호출 시 action = null
 * - execute 호출 시 point = null
 *
 * <p><b>회귀 방지 이유</b></p>
 * - Service 계층은 실행기 구현체를 신뢰하고 호출한다.
 * - 실행기에서 입력 방어가 제거되면,
 *   NullPointerException이 내부 깊은 위치에서 발생하거나
 *   부정확한 로그/실행 상태를 유발할 수 있다.
 * - 따라서 Infra 계층은 명시적 입력 계약을 유지해야 한다.
 *
 * @since 0.8
 */
class DryRunClickExecutorTest {

    /*
     * 시나리오: clock이 null이면 생성이 거부되어야 한다.
     *
     * 입력(Given):
     * - clock = null
     *
     * 기대(Then):
     * - NullPointerException 발생
     */
    @Test
    @DisplayName("생성 제약: clock이 null이면 예외")
    void ctor_nullClock_shouldThrow() {
        assertThrows(NullPointerException.class, () -> new DryRunClickExecutor(null));
    }

    /*
     * 시나리오: action이 null이면 실행이 거부되어야 한다.
     *
     * 입력(Given):
     * - action = null
     * - point = (300, 300)
     *
     * 기대(Then):
     * - NullPointerException 발생
     */
    @Test
    @DisplayName("실행 제약: action이 null이면 예외")
    void execute_nullAction_shouldThrow() {
        DryRunClickExecutor executor = new DryRunClickExecutor(Clock.systemUTC());
        ScreenPoint point = new ScreenPoint(300, 300);

        assertThrows(NullPointerException.class, () -> executor.execute(null, point));
    }

    /*
     * 시나리오: point가 null이면 실행이 거부되어야 한다.
     *
     * 입력(Given):
     * - action = LEFT 단일 클릭
     * - point = null
     *
     * 기대(Then):
     * - NullPointerException 발생
     */
    @Test
    @DisplayName("실행 제약: point가 null이면 예외")
    void execute_nullPoint_shouldThrow() {
        DryRunClickExecutor executor = new DryRunClickExecutor(Clock.systemUTC());
        ClickAction action = ClickAction.singleLeft();

        assertThrows(NullPointerException.class, () -> executor.execute(action, null));
    }
}
