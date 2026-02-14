package com.preview.mousemacroapp.ui;

import com.preview.mousemacroapp.domain.point.ScreenPoint;
import com.preview.mousemacroapp.service.CaptureResult;
import com.preview.mousemacroapp.service.MacroRequest;
import com.preview.mousemacroapp.service.MacroService;
import com.preview.mousemacroapp.service.MouseClickCaptor;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
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
    static void initJavaFx() throws Exception {
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
        Platform.startup(latch::countDown);
        assertTrue(latch.await(2, TimeUnit.SECONDS), "JavaFX Platform 초기화에 실패했다.");
    }

    @Test
    void capturePoint_whenCaptured_thenUpdatesPointAndMessageLabels() throws Exception {
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
    void start_afterCapture_thenMacroRequestMacroPointIsCaptured() throws Exception {
        /*
         * 시나리오: 캡처 후 start 호출 시 MacroRequest.macroPoint가 캡처 좌표를 가진다.
         *
         * 입력(Given):
         * - captor: CAPTURED(777,888) 즉시 완료
         * - start 호출
         *
         * 예상 결과(Then):
         * - MacroService.start로 전달된 MacroRequest.macroPoint().screenPoint()가 (777,888)이다.
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

        runOnFxAndWait(() -> controller.start(statusLabel, messageLabel, pauseResumeButton, pointLabel));

        assertNotNull(macroService.lastRequest, "start 요청이 서비스에 전달되지 않았다.");

        // MacroRequest는 record이며 컴포넌트명이 macroPoint이다.:contentReference[oaicite:1]{index=1}
        ScreenPoint p = macroService.lastRequest.macroPoint().base();
        assertEquals(777, p.x());
        assertEquals(888, p.y());
    }

    private static void runOnFxAndWait(Runnable r) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                r.run();
            } finally {
                latch.countDown();
            }
        });
        assertTrue(latch.await(2, TimeUnit.SECONDS), "FX Thread 작업이 시간 내 완료되지 않았다.");
    }

    private static void waitUntilFx(Check check) throws Exception {
        long deadline = System.currentTimeMillis() + 1500;
        while (System.currentTimeMillis() < deadline) {
            final boolean[] ok = {false};
            CountDownLatch latch = new CountDownLatch(1);

            Platform.runLater(() -> {
                try {
                    ok[0] = check.ok();
                } finally {
                    latch.countDown();
                }
            });

            assertTrue(latch.await(2, TimeUnit.SECONDS), "FX Thread 조건 점검이 시간 내 완료되지 않았다.");
            if (ok[0]) {
                return;
            }
            Thread.sleep(20);
        }
        fail("기대 조건이 시간 내 충족되지 않았다.");
    }

    @FunctionalInterface
    private interface Check {
        boolean ok();
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
