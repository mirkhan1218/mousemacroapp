package com.preview.mousemacroapp.infra.hook;

import com.preview.mousemacroapp.domain.action.ClickAction;
import com.preview.mousemacroapp.domain.point.ScreenPoint;
import com.preview.mousemacroapp.service.ClickExecutor;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

/**
 * 실제 마우스를 제어하지 않고, 클릭 요청을 로그로만 출력하는 실행기.
 *
 * <p>
 * 역할:
 * - 개발 초기 단계에서 사용자의 마우스 제어를 방해하지 않기 위해 "대체 실행"을 제공한다.
 * - Service/Domain 로직 검증(상태 전이, 루프 제어, 딜레이 등)은 유지한다.
 * </p>
 */
public final class DryRunClickExecutor implements ClickExecutor {

    private final Clock clock;

    public DryRunClickExecutor(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public void execute(ClickAction action, ScreenPoint point) {
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(point, "point");

        Instant now = Instant.now(clock);
        System.out.printf("[DRY-RUN] %s action=%s point=%s%n", now, action, point);
    }
}
