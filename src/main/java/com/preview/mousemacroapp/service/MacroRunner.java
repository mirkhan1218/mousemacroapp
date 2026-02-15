package com.preview.mousemacroapp.service;

import com.preview.mousemacroapp.debug.DebugLog;
import com.preview.mousemacroapp.domain.action.ClickAction;
import com.preview.mousemacroapp.domain.action.policy.ClickPositionPolicy;
import com.preview.mousemacroapp.domain.point.MacroPoint;
import com.preview.mousemacroapp.domain.point.ScreenPoint;
import com.preview.mousemacroapp.domain.schedule.ExecutionSchedule;
import com.preview.mousemacroapp.domain.timing.DelayPolicy;

import java.time.Clock;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Random;

/**
 * 실행 루프 담당(내부 실행 엔진).
 *
 * <p>
 * Service는 상태 전이와 수명 주기를 제어하고,
 * Runner는 반복 실행(딜레이/스케줄/좌표 정책 적용)을 담당한다.
 * </p>
 *
 * @since 0.6
 */
final class MacroRunner {

    private final ClickExecutor clickExecutor;
    private final Clock clock;

    private Thread worker;

    private volatile boolean stopRequested;
    private volatile boolean paused;

    MacroRunner(ClickExecutor clickExecutor, Clock clock) {
        this.clickExecutor = Objects.requireNonNull(clickExecutor, "clickExecutor");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    void start(MacroPoint macroPoint,
               ClickAction clickAction,
               ClickPositionPolicy positionPolicy,
               DelayPolicy delayPolicy,
               ExecutionSchedule schedule,
               Random random,
               int repeatCount,
               Runnable onCompleted) {

        Objects.requireNonNull(macroPoint, "macroPoint");
        Objects.requireNonNull(clickAction, "clickAction");
        Objects.requireNonNull(positionPolicy, "positionPolicy");
        Objects.requireNonNull(delayPolicy, "delayPolicy");
        Objects.requireNonNull(schedule, "schedule");
        Objects.requireNonNull(random, "random");
        Objects.requireNonNull(onCompleted, "onCompleted");

        if (repeatCount < 0) {
            throw new IllegalArgumentException("repeatCount는 0 이상이어야 한다. repeatCount=" + repeatCount);
        }

        stopRequested = false;
        paused = false;

        // 역할: 실행 스레드 생명주기는 Runner 내부에서만 생성/시작한다(외부 직접 제어 금지).
        worker = new Thread(() -> {
            try {
                runLoop(macroPoint, clickAction, positionPolicy, delayPolicy, schedule, random, repeatCount);
            } finally {
                // 역할: 정상 종료/stop 요청/예외 종료 모두 “종료 완료”를 Service에 알린다.
                onCompleted.run();
            }
        }, "macro-runner");

        // 역할: UI 종료 시 백그라운드 스레드가 프로세스 종료를 막지 않도록 daemon 처리한다.
        worker.setDaemon(true);

        worker.start();
    }

    void requestStop() {
        stopRequested = true;
        Thread t = worker;
        if (t != null) {
            // 역할: stop 요청은 sleep 대기 중인 루프를 즉시 탈출시키기 위해 interrupt를 사용한다.
            t.interrupt();
        }
    }

    void pause() {
        paused = true;
    }

    void resume() {
        paused = false;
        Thread t = worker;
        if (t != null) {
            // 역할: resume 시 sleep을 끊어 즉시 다음 루프로 복귀시키기 위해 interrupt를 사용한다.
            t.interrupt();
        }
    }

    private void runLoop(MacroPoint macroPoint,
                         ClickAction clickAction,
                         ClickPositionPolicy positionPolicy,
                         DelayPolicy delayPolicy,
                         ExecutionSchedule schedule,
                         Random random,
                         int repeatCount) {

        ScreenPoint base = macroPoint.base();

        int executed = 0;
        DebugLog.log("RUNNER", () -> "run start repeat=" + repeatCount);

        // 역할: stopRequested는 루프 종료 여부를 판단하는 단일 플래그다.
        while (!stopRequested) {
            // 역할: PAUSED 상태에서는 클릭을 수행하지 않는다.
            if (paused) {
                sleepSilently(50);
                continue;
            }

            // ExecutionSchedule.Always(Null Object)로 통일하여 항상 정책 판단 수행
            LocalTime now = LocalTime.now(clock);
            if (!schedule.isAllowed(now)) {
                sleepSilently(200);
                continue;
            }

            ScreenPoint resolved = positionPolicy.resolve(base, random);
            clickExecutor.execute(clickAction, resolved);

            executed++;
            if (repeatCount > 0) {
                final int current = executed;   // ← 이 줄 추가
                DebugLog.log("RUNNER", () -> "tick=" + current + "/" + repeatCount);

                if (current >= repeatCount) {
                    DebugLog.log("RUNNER", () -> "finished -> stop");
                    break;
                }
            }

            long delayMillis = delayPolicy.resolveDelayMillis(random);
            sleepSilently(delayMillis);
        }
    }

    private void sleepSilently(long millis) {
        try {
            Thread.sleep(Math.max(1L, millis));
        } catch (InterruptedException ignored) {
            // 역할: stop/resume 등 상태 변화 시 sleep을 끊기 위한 인터럽트는 정상 흐름
        }
    }
}
