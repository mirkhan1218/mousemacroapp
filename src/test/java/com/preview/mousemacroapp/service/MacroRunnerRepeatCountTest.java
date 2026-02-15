package com.preview.mousemacroapp.service;

import com.preview.mousemacroapp.domain.action.ClickAction;
import com.preview.mousemacroapp.domain.action.policy.ExactPositionPolicy;
import com.preview.mousemacroapp.domain.point.MacroPoint;
import com.preview.mousemacroapp.domain.point.ScreenPoint;
import com.preview.mousemacroapp.domain.schedule.ExecutionSchedule;
import com.preview.mousemacroapp.domain.timing.DelayPolicy;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link MacroRunner}의 반복 횟수(repeatCount) 종료 계약을 검증한다.
 *
 * <p>역할:</p>
 * - repeatCount=3이면 클릭이 정확히 3회 수행된 뒤 종료 콜백이 호출된다.
 * - repeatCount=0이면 제한 없이 동작하므로 본 테스트에서는 다루지 않는다(별도 제어 필요).</p>
 */
class MacroRunnerRepeatCountTest {

    @Test
    void run_whenRepeatCountIsThree_thenExecutesExactlyThreeTimesAndCompletes() throws Exception {
        AtomicInteger executed = new AtomicInteger(0);
        CountDownLatch completed = new CountDownLatch(1);

        ClickExecutor fakeExecutor = (action, point) -> executed.incrementAndGet();

        Clock fixedClock = Clock.fixed(Instant.parse("2026-02-14T00:00:00Z"), ZoneId.of("Asia/Seoul"));
        MacroRunner runner = new MacroRunner(fakeExecutor, fixedClock);

        MacroPoint macroPoint = new MacroPoint("t", new ScreenPoint(10, 20), new ExactPositionPolicy());
        ClickAction clickAction = ClickAction.singleLeft();
        DelayPolicy delayPolicy = new DelayPolicy(1, 0, 0);
        ExecutionSchedule schedule = new ExecutionSchedule.Always();

        runner.start(
                macroPoint,
                clickAction,
                macroPoint.positionPolicy(),
                delayPolicy,
                schedule,
                new Random(0),
                3,
                completed::countDown
        );

        assertTrue(completed.await(2, TimeUnit.SECONDS), "repeatCount 종료 콜백이 시간 내 호출되지 않았다.");
        assertEquals(3, executed.get(), "클릭 실행 횟수는 repeatCount와 동일해야 한다.");
    }
}
