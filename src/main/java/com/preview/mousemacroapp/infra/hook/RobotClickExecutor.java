package com.preview.mousemacroapp.infra.hook;

import com.preview.mousemacroapp.domain.action.ClickAction;
import com.preview.mousemacroapp.domain.point.ScreenPoint;
import com.preview.mousemacroapp.service.ClickExecutor;

import java.util.Objects;

/**
 * RobotMouse 기반의 클릭 실행기.
 *
 * <p><b>정책</b></p>
 * <ul>
 *     <li>ClickAction(clickCount/holdMillis) 규칙을 실행 레벨에서 그대로 반영한다.</li>
 *     <li>holdMillis &gt; 0 인 경우는 “단일 클릭 + 홀드”로 간주하고, press → 유지 → release 순서를 고정한다.</li>
 *     <li>clickCount &gt; 1 인 경우는 “연속 클릭”으로 처리한다.</li>
 * </ul>
 *
 * @since 0.6
 */
public final class RobotClickExecutor implements ClickExecutor {

    private final RobotMouse robotMouse;

    /**
     * RobotMouse 구현체를 주입받아 클릭 실행기를 생성한다.
     *
     * @param robotMouse 실제 입력 수행 구현체
     * @throws NullPointerException robotMouse가 null인 경우
     */
    public RobotClickExecutor(RobotMouse robotMouse) {
        this.robotMouse = Objects.requireNonNull(robotMouse, "robotMouse");
    }

    /**
     * 지정 좌표로 이동한 뒤, ClickAction 정책에 따라 클릭을 수행한다.
     *
     * @param action 클릭 동작 정책
     * @param point  클릭 대상 좌표
     * @throws NullPointerException action 또는 point가 null인 경우
     */
    @Override
    public void execute(ClickAction action, ScreenPoint point) {
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(point, "point");

        robotMouse.move(point);

        // 역할: holdMillis가 있으면 “단일 클릭 + 홀드” 정책을 강제한다.
        if (action.holdMillis() > 0) {
            robotMouse.press(action.button());
            sleepSilently(action.holdMillis());
            robotMouse.release(action.button());
            return;
        }

        // 역할: 일반 클릭은 clickCount 횟수만큼 press/release를 반복한다.
        for (int i = 0; i < action.clickCount(); i++) {
            robotMouse.press(action.button());
            robotMouse.release(action.button());

            // 역할: 더블/연속 클릭에서 OS 이벤트로 안정 인식되도록 최소 간격을 둔다.
            if (i < action.clickCount() - 1) {
                sleepSilently(20);
            }
        }
    }

    private void sleepSilently(long millis) {
        // 역할: stop 요청/interrupt로 스레드가 깨질 수 있으므로 interrupt 플래그는 복원한다.
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
