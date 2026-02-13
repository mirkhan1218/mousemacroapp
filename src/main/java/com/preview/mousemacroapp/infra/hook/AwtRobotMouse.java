package com.preview.mousemacroapp.infra.hook;

import com.preview.mousemacroapp.domain.action.MouseButton;
import com.preview.mousemacroapp.domain.point.ScreenPoint;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.Objects;

/**
 * AWT {@link Robot} 기반의 실제 마우스 제어 구현.
 *
 * <p><b>정책</b></p>
 * <ul>
 *   <li>Windows 11 환경에서 기본 입력 제어를 제공한다.</li>
 *   <li>Headless 환경 또는 권한 이슈로 {@link Robot} 생성이 실패할 수 있으므로 생성 시점에 예외를 명확히 노출한다.</li>
 * </ul>
 *
 * @since 0.6
 */
public final class AwtRobotMouse implements RobotMouse {

    private final Robot robot;

    /**
     * 기본 {@link Robot} 인스턴스를 생성하여 마우스 제어를 준비한다.
     *
     * @throws IllegalStateException Robot 생성이 불가능한 환경인 경우
     */
    public AwtRobotMouse() {
        try {
            this.robot = new Robot();
        } catch (AWTException e) {
            throw new IllegalStateException("AWT Robot 생성에 실패했습니다. (headless/권한/환경 설정 확인 필요)", e);
        }
    }

    /**
     * 커서를 지정 좌표로 이동시킨다.
     *
     * @param point 이동할 좌표
     * @throws NullPointerException point가 null인 경우
     */
    @Override
    public void move(ScreenPoint point) {
        Objects.requireNonNull(point, "point");
        robot.mouseMove(point.x(), point.y());
    }

    /**
     * 지정 버튼을 누른다(press).
     *
     * @param button 마우스 버튼
     * @throws NullPointerException button이 null인 경우
     */
    @Override
    public void press(MouseButton button) {
        Objects.requireNonNull(button, "button");
        robot.mousePress(toMask(button));
    }

    /**
     * 지정 버튼을 뗀다(release).
     *
     * @param button 마우스 버튼
     * @throws NullPointerException button이 null인 경우
     */
    @Override
    public void release(MouseButton button) {
        Objects.requireNonNull(button, "button");
        robot.mouseRelease(toMask(button));
    }

    private int toMask(MouseButton button) {
        // 역할: 도메인 버튼 의미를 OS 입력 마스크로 변환하는 단일 지점
        return switch (button) {
            case LEFT -> InputEvent.BUTTON1_DOWN_MASK;
            case MIDDLE -> InputEvent.BUTTON2_DOWN_MASK;
            case RIGHT -> InputEvent.BUTTON3_DOWN_MASK;
        };
    }
}
