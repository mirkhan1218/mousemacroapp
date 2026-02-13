package com.preview.mousemacroapp.service;

import com.preview.mousemacroapp.domain.action.ClickAction;
import com.preview.mousemacroapp.domain.point.ScreenPoint;

/**
 * 클릭 실행 포트(Port).
 *
 * <p>
 * Service는 실제 클릭 구현(AWT Robot, 네이티브 훅 등)에 의존하지 않는다.
 * 외부 실행(Infra)은 이 인터페이스를 구현하여 주입한다.
 * </p>
 *
 * @since 0.6
 */
public interface ClickExecutor {

    /**
     * 지정된 좌표에 클릭 동작을 수행한다.
     *
     * @param action 클릭 동작(버튼/횟수/다운시간 등)
     * @param point  실제 클릭 좌표
     */
    void execute(ClickAction action, ScreenPoint point);
}
