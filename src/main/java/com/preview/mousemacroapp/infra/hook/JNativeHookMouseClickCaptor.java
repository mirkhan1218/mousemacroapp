package com.preview.mousemacroapp.infra.hook;

import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;
import com.preview.mousemacroapp.debug.DebugLog;
import com.preview.mousemacroapp.domain.point.ScreenPoint;
import com.preview.mousemacroapp.service.CaptureResult;
import com.preview.mousemacroapp.service.MouseClickCaptor;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * JNativeHook 기반 전역 마우스 클릭(1회) 캡처 구현체.
 *
 * <p><b>역할:</b></p>
 * <ul>
 *     <li>다음 클릭 1회를 캡처하면 즉시 리스너를 해제한다.</li>
 *     <li>취소/타임아웃을 지원한다.</li>
 * </ul>
 */
public final class JNativeHookMouseClickCaptor implements MouseClickCaptor {

    private final JNativeHookFacade facade;
    private final ScheduledExecutorService scheduler;

    private volatile CompletableFuture<CaptureResult<ScreenPoint>> inFlight;
    private volatile NativeMouseListener inFlightListener;

    public JNativeHookMouseClickCaptor(JNativeHookFacade facade) {
        this.facade = Objects.requireNonNull(facade, "facade");
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "mouse-click-captor-timeout");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * 테스트를 위한 스케줄러 주입 생성자.
     *
     * <p>역할:</p>
     * - 타임아웃 동작을 테스트에서 안정적으로 제어한다.
     * - 테스트 종료 시 스케줄러 종료(shutdownNow) 등을 테스트 코드가 책임질 수 있게 한다.</p>
     */
    JNativeHookMouseClickCaptor(JNativeHookFacade facade, ScheduledExecutorService scheduler) {
        this.facade = Objects.requireNonNull(facade, "facade");
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
    }

    @Override
    public CompletableFuture<CaptureResult<ScreenPoint>> captureNextClick(Duration timeout) {
        Objects.requireNonNull(timeout, "timeout");

        if (inFlight != null && !inFlight.isDone()) {
            return CompletableFuture.completedFuture(CaptureResult.failed("이미 캡처가 진행 중이다."));
        }

        CompletableFuture<CaptureResult<ScreenPoint>> future = new CompletableFuture<>();
        inFlight = future;

        // 역할: 캡처는 전역 훅 등록이 전제이므로, 필요 시 여기서 보장한다.
        try {
            facade.register();
        } catch (NativeHookException e) {
            DebugLog.log("CAPTURE", () -> "register failed: " + e.getMessage());
            future.complete(CaptureResult.failed("전역 훅 등록 실패: " + e.getMessage()));
            return future;
        }

        DebugLog.log("CAPTURE", () -> "captureNextClick started timeout=" + timeout);

        final NativeMouseListener listener = new NativeMouseListener() {
            @Override
            public void nativeMouseClicked(NativeMouseEvent e) {
                ScreenPoint point = new ScreenPoint(e.getX(), e.getY());
                DebugLog.log("CAPTURE", () -> "captured point=" + point);

                cleanupListener(this);
                completeIfNotDone(future, CaptureResult.captured(point));
            }
        };

        inFlightListener = listener;
        facade.addMouseListener(listener);

        scheduler.schedule(() -> {
            if (!future.isDone()) {
                DebugLog.log("CAPTURE", () -> "timeout");
                cleanupListener(listener);
                completeIfNotDone(future, CaptureResult.timeout());
            }
        }, timeout.toMillis(), TimeUnit.MILLISECONDS);

        future.whenComplete((r, ex) -> {
            // 역할: 종료 시점에 리스너 누수 방지를 위해 한 번 더 정리한다.
            cleanupListener(listener);

            if (ex != null) {
                DebugLog.log("CAPTURE", () -> "failed ex=" + ex.getClass().getSimpleName() + " msg=" + ex.getMessage());
            }
        });

        return future;
    }

    @Override
    public void cancel() {
        CompletableFuture<CaptureResult<ScreenPoint>> future = inFlight;
        NativeMouseListener listener = inFlightListener;

        if (future == null || future.isDone()) {
            return;
        }

        DebugLog.log("CAPTURE", () -> "cancel requested");
        if (listener != null) {
            cleanupListener(listener);
        }
        completeIfNotDone(future, CaptureResult.cancelled());
    }

    private void completeIfNotDone(CompletableFuture<CaptureResult<ScreenPoint>> future,
                                   CaptureResult<ScreenPoint> result) {
        if (!future.isDone()) {
            future.complete(result);
        }
    }

    private void cleanupListener(NativeMouseListener listener) {
        // 역할: 이미 해제된 리스너에 대해 중복 remove 호출을 방지한다.
        if (inFlightListener != listener) {
            return;
        }

        try {
            facade.removeMouseListener(listener);
        } catch (RuntimeException ex) {
            // 역할: 해제 실패는 캡처 결과 자체를 깨지 않도록 로그로만 남긴다.
            DebugLog.log("CAPTURE", () -> "listener cleanup failed: " + ex.getMessage());
        } finally {
            inFlightListener = null;

        }
    }
}
