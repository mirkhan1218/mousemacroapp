package com.preview.mousemacroapp.ui;

import com.preview.mousemacroapp.domain.point.ScreenPoint;
import com.preview.mousemacroapp.service.CaptureResult;
import com.preview.mousemacroapp.service.MacroRequest;
import com.preview.mousemacroapp.service.MacroService;
import com.preview.mousemacroapp.service.MouseClickCaptor;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 좌표 캡처 UI 흐름이 MacroRequest(macroPoint)에 반영되는지 검증한다.
 *
 * <p><b>테스트 대상</b></p>
 * - {@link MacroController}의 capturePoint/start 공개 흐름
 *
 * <p><b>검증 목적</b></p>
 * - 전역 클릭 1회 캡처 결과가 UI(Label)와 내부 선택 좌표에 반영되는지 확인한다.
 * - start 호출 시 {@link MacroRequest#macroPoint()}로 전달되는 좌표가 캡처 좌표인지 확인한다.
 *
 * <p><b>검증 범위</b></p>
 * - captor는 Fake로 대체하며, CAPTURED 결과를 즉시 반환한다.
 * - JavaFX는 Label/Button 수준에서만 사용하고, 전체 UI(Scene/Stage)는 띄우지 않는다.
 *
 * <p><b>회귀 방지 이유</b></p>
 * - UI 작업 확장 시 “캡처 좌표가 start 요청에 반영되지 않는 회귀”를 조기에 차단한다.
 *
 * @since 0.6
 */
class MacroControllerCaptureFlowTest {

    @BeforeAll
    static void initJavaFx() {
        /*
         * 시나리오: JavaFX Platform 1회 초기화
         *
         * 입력(Given):
         * - 없음
         *
         * 예상 결과(Then):
         * - Platform.startup이 정상 완료된다.
         */
        CountDownLatch latch = new CountDownLatch(1);

        // 역할: 테스트 실행 전 JavaFX 런타임을 1회만 기동한다.
        Platform.startup(latch::countDown);

        try {
            assertTrue(latch.await(2, TimeUnit.SECONDS), "JavaFX Platform 초기화에 실패했다.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("JavaFX Platform 초기화 대기 중 인터럽트가 발생했다.");
        }
    }

    @Test
    void capturePoint_whenCaptured_thenUpdatesPointAndMessageLabels() {
        /*
         * 시나리오: 캡처 성공(CAPTURED) 시 UI 라벨이 갱신된다.
         *
         * 입력(Given):
         * - captor: CAPTURED(1131,468) 즉시 완료
         *
         * 예상 결과(Then):
         * - pointLabel에 (1131, 468)이 포함된다.
         * - messageLabel에 “좌표” 및 “저장” 관련 문구가 포함된다.
         */
        FakeMacroService macroService = new FakeMacroService();
        ScreenPoint captured = new ScreenPoint(1131, 468);
        MouseClickCaptor captor = new ImmediateCaptor(CaptureResult.captured(captured));

        MacroController controller = new MacroController(macroService, captor);

        Label messageLabel = new Label();
        Label pointLabel = new Label("좌표: (미설정)");

        runOnFxAndWait(() -> controller.capturePoint(messageLabel, pointLabel));

        // capturePoint는 Future 완료 후 Platform.runLater로 라벨을 갱신하므로 조건 충족까지 대기한다.
        waitUntilFx(() ->
                pointLabel.getText() != null
                        && pointLabel.getText().contains("1131")
                        && pointLabel.getText().contains("468")
        );

        assertNotNull(messageLabel.getText());
        assertTrue(messageLabel.getText().contains("좌표"));
        assertTrue(messageLabel.getText().contains("저장"));
    }

    @Test
    void start_afterCapture_thenMacroRequestMacroPointIsCaptured() {
        /*
         * 시나리오: 캡처 후 start 호출 시 MacroRequest.macroPoint가 캡처 좌표를 가진다.
         *
         * 입력(Given):
         * - captor: CAPTURED(777,888) 즉시 완료
         * - start 호출
         *
         * 예상 결과(Then):
         * - MacroService.start로 전달된 MacroRequest.macroPoint().base()가 (777,888)이다.
         */
        FakeMacroService macroService = new FakeMacroService();
        ScreenPoint captured = new ScreenPoint(777, 888);
        MouseClickCaptor captor = new ImmediateCaptor(CaptureResult.captured(captured));

        MacroController controller = new MacroController(macroService, captor);

        Label messageLabel = new Label();
        Label pointLabel = new Label("좌표: (미설정)");
        Label statusLabel = new Label();
        Button pauseResumeButton = new Button("Pause");

        runOnFxAndWait(() -> controller.capturePoint(messageLabel, pointLabel));
        waitUntilFx(() ->
                pointLabel.getText() != null
                        && pointLabel.getText().contains("777")
                        && pointLabel.getText().contains("888")
        );

        TextField repeatCountField = new TextField("0");
        runOnFxAndWait(() -> controller.start(statusLabel, messageLabel, pauseResumeButton, pointLabel, repeatCountField));

        assertNotNull(macroService.lastRequest, "start 요청이 서비스에 전달되지 않았다.");

        ScreenPoint p = macroService.lastRequest.macroPoint().base();
        assertEquals(777, p.x());
        assertEquals(888, p.y());
    }

    private static void runOnFxAndWait(Runnable r) {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                r.run();
            } finally {
                latch.countDown();
            }
        });

        try {
            assertTrue(latch.await(2, TimeUnit.SECONDS), "FX Thread 작업이 시간 내 완료되지 않았다.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("FX Thread 작업 대기 중 인터럽트가 발생했다.");
        }
    }

    private static void waitUntilFx(Check check) {
        /*
         * 역할: Thread.sleep 기반 대기는 실행 환경에 따라 테스트가 불안정해질 수 있다.
         * - JavaFX 스레드에서 조건을 점검하고, 만족 시 즉시 완료한다.
         * - 점검 스케줄은 별도 스레드에서 수행하되, UI 접근은 Platform.runLater로 제한한다.
         */
        CompletableFuture<Void> done = new CompletableFuture<>();

        try (CloseableScheduler scheduler = CloseableScheduler.create()) {
            scheduler.get().scheduleAtFixedRate(() -> {
                if (done.isDone()) {
                    return;
                }

                CountDownLatch latch = new CountDownLatch(1);
                final boolean[] ok = {false};

                Platform.runLater(() -> {
                    try {
                        ok[0] = check.ok();
                    } finally {
                        latch.countDown();
                    }
                });

                try {
                    if (!latch.await(2, TimeUnit.SECONDS)) {
                        done.completeExceptionally(new AssertionError("FX Thread 조건 점검이 시간 내 완료되지 않았다."));
                        return;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    done.completeExceptionally(e);
                    return;
                }

                if (ok[0]) {
                    done.complete(null);
                }
            }, 0, 20, TimeUnit.MILLISECONDS);

            // 역할: 환경 편차를 고려해 최대 대기 시간을 고정한다(회귀 시 즉시 실패).
            assertDoesNotThrow(() -> done.get(1500, TimeUnit.MILLISECONDS));
        }
    }

    @FunctionalInterface
    private interface Check {
        boolean ok();
    }

    /**
     * Executor 정리 누락/결과 무시 경고를 제거하기 위한 테스트 전용 래퍼.
     *
     * <p>
     * 역할:
     * - 종료/대기/실패 처리를 단일 책임으로 캡슐화한다.
     * - try-with-resources로 스케줄러 수명주기를 명시한다.
     * </p>
     */
    private static final class CloseableScheduler implements AutoCloseable {

        private final ScheduledExecutorService delegate;

        private CloseableScheduler(ScheduledExecutorService delegate) {
            this.delegate = delegate;
        }

        private static CloseableScheduler create() {
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "test-waitUntilFx");
                t.setDaemon(true);
                return t;
            });
            return new CloseableScheduler(scheduler);
        }

        private ScheduledExecutorService get() {
            return delegate;
        }

        @Override
        public void close() {
            delegate.shutdownNow();
            try {
                boolean terminated = delegate.awaitTermination(200, TimeUnit.MILLISECONDS);
                if (!terminated) {
                    // 역할: 종료 실패(스레드 누수 가능성)를 테스트 실패로 명시한다.
                    fail("scheduler가 시간 내 종료되지 않았다.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("scheduler 종료 대기 중 인터럽트가 발생했다.");
            }
        }
    }

    /**
     * 테스트용 MouseClickCaptor.
     *
     * <p>
     * 역할: 본 테스트는 captor 자체의 동작을 검증하지 않고,
     * 컨트롤러가 “CAPTURED 결과를 UI/요청에 반영”하는 계약만 검증한다.
     * </p>
     */
    private static final class ImmediateCaptor implements MouseClickCaptor {

        private final CaptureResult<ScreenPoint> result;

        private ImmediateCaptor(CaptureResult<ScreenPoint> result) {
            this.result = result;
        }

        @Override
        public CompletableFuture<CaptureResult<ScreenPoint>> captureNextClick(Duration timeout) {
            return CompletableFuture.completedFuture(result);
        }

        @Override
        public void cancel() {
            // no-op
        }
    }

    /**
     * 테스트용 MacroService.
     *
     * <p>
     * 역할: start로 전달된 MacroRequest를 기록하여,
     * 캡처 좌표가 요청에 반영되는지 검증한다.
     * </p>
     */
    private static final class FakeMacroService implements MacroService {

        private volatile MacroRequest lastRequest;

        @Override
        public void start(MacroRequest request) {
            this.lastRequest = request;
        }

        @Override
        public void stop() {
            // no-op
        }

        @Override
        public void pause() {
            // no-op
        }

        @Override
        public void resume() {
            // no-op
        }

        @Override
        public com.preview.mousemacroapp.domain.status.MacroStatus status() {
            return com.preview.mousemacroapp.domain.status.MacroStatus.STOPPED;
        }
    }
}
