package com.preview.mousemacroapp.infra.hook;

import com.preview.mousemacroapp.domain.action.ClickAction;
import com.preview.mousemacroapp.domain.action.MouseButton;
import com.preview.mousemacroapp.domain.point.ScreenPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RobotClickExecutor 동작 순서 및 파라미터 전달 회귀 방지 테스트.
 *
 * <p><b>테스트 대상</b></p>
 * - {@link RobotClickExecutor}
 *
 * <p><b>검증 목적</b></p>
 * - 클릭 실행 순서(move → press/release)가 규칙대로 고정되는지 검증한다.
 *
 * <p><b>검증 범위</b></p>
 * - 단일 클릭(clickCount=1, holdMillis=0)
 * - 연속 클릭(clickCount>1, holdMillis=0)
 * - 홀드 클릭(holdMillis>0)
 *
 * <p><b>회귀 방지 이유</b></p>
 * - OS 입력은 부작용이 크므로, 실행 규칙이 깨지면 오작동이 즉시 발생한다.
 *
 * @since 0.6
 */
class RobotClickExecutorTest {

    /*
     * 시나리오: 단일 클릭은 move 후 press/release 1회가 수행되어야 한다
     *
     * 입력(Given):
     * - action = new ClickAction(LEFT, 1, 0)
     * - point = new ScreenPoint(10, 20)
     *
     * 예상 결과(Then):
     * - 호출 순서: move(10,20) → press(LEFT) → release(LEFT)
     */
    @Test
    @DisplayName("단일 클릭: move → press → release")
    void singleClick_orderShouldBeFixed() {
        FakeRobotMouse fake = new FakeRobotMouse();
        RobotClickExecutor executor = new RobotClickExecutor(fake);

        executor.execute(new ClickAction(MouseButton.LEFT, 1, 0), new ScreenPoint(10, 20));

        assertEquals(List.of(
                "move(10,20)",
                "press(LEFT)",
                "release(LEFT)"
        ), fake.calls);
    }

    /*
     * 시나리오: 연속 클릭은 move 후 press/release가 clickCount만큼 반복되어야 한다
     *
     * 입력(Given):
     * - action = new ClickAction(RIGHT, 2, 0)
     * - point = new ScreenPoint(1, 2)
     *
     * 예상 결과(Then):
     * - press/release가 2회 반복된다
     */
    @Test
    @DisplayName("연속 클릭: clickCount만큼 press/release 반복")
    void multiClick_shouldRepeatPressRelease() {
        FakeRobotMouse fake = new FakeRobotMouse();
        RobotClickExecutor executor = new RobotClickExecutor(fake);

        executor.execute(new ClickAction(MouseButton.RIGHT, 2, 0), new ScreenPoint(1, 2));

        assertEquals(List.of(
                "move(1,2)",
                "press(RIGHT)",
                "release(RIGHT)",
                "press(RIGHT)",
                "release(RIGHT)"
        ), fake.calls);
    }

    /*
     * 시나리오: 홀드 클릭은 move 후 press → (sleep) → release 순서가 유지되어야 한다
     *
     * 입력(Given):
     * - action = new ClickAction(MIDDLE, 1, 50)
     * - point = new ScreenPoint(7, 8)
     *
     * 예상 결과(Then):
     * - 호출 순서: move → press → release
     * - (sleep 자체는 시간 기반이라 직접 검증하지 않고, press/release 순서를 고정한다)
     */
    @Test
    @DisplayName("홀드 클릭: move → press → release 순서 고정")
    void holdClick_shouldPressThenRelease() {
        FakeRobotMouse fake = new FakeRobotMouse();
        RobotClickExecutor executor = new RobotClickExecutor(fake);

        executor.execute(new ClickAction(MouseButton.MIDDLE, 1, 50), new ScreenPoint(7, 8));

        assertEquals(List.of(
                "move(7,8)",
                "press(MIDDLE)",
                "release(MIDDLE)"
        ), fake.calls);
    }

    private static final class FakeRobotMouse implements RobotMouse {

        private final List<String> calls = new ArrayList<>();

        @Override
        public void move(ScreenPoint point) {
            calls.add("move(" + point.x() + "," + point.y() + ")");
        }

        @Override
        public void press(MouseButton button) {
            calls.add("press(" + button + ")");
        }

        @Override
        public void release(MouseButton button) {
            calls.add("release(" + button + ")");
        }
    }
}
