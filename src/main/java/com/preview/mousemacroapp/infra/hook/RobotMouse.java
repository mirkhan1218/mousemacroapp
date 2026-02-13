package com.preview.mousemacroapp.infra.hook;

import com.preview.mousemacroapp.domain.action.MouseButton;
import com.preview.mousemacroapp.domain.point.ScreenPoint;

/**
 * Robot 기반 입력 장치를 추상화한 마우스 제어 인터페이스.
 *
 * <p><b>정책</b></p>
 * <ul>
 *     <li>실제 OS 제어(AWT Robot)에 대한 의존을 격리하여 테스트 가능성을 확보한다.</li>
 *     <li>ClickExecutor는 본 인터페이스만 의존하고, 실제 구현(AWT 등)은 infra에서 교체 가능하도록 한다.</li>
 * </ul>
 *
 * @since 0.6
 */
public interface RobotMouse {

    /**
     * 마우스 커서를 지정 좌표로 이동한다.
     *
     * @param point 이동할 화면 좌표
     * @throws NullPointerException point가 null인 경우
     */
    void move(ScreenPoint point);

    /**
     * 지정 버튼을 누른다(press).
     *
     * @param button 마우스 버튼
     * @throws NullPointerException button이 null인 경우
     */
    void press(MouseButton button);

    /**
     * 지정 버튼을 뗀다(release).
     *
     * @param button 마우스 버튼
     * @throws NullPointerException button이 null인 경우
     */
    void release(MouseButton button);
}
