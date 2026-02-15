package com.preview.mousemacroapp.ui;

import com.preview.mousemacroapp.debug.DebugLog;
import com.preview.mousemacroapp.domain.action.ClickAction;
import com.preview.mousemacroapp.domain.action.policy.ClickPositionPolicy;
import com.preview.mousemacroapp.domain.action.policy.ExactPositionPolicy;
import com.preview.mousemacroapp.domain.point.MacroPoint;
import com.preview.mousemacroapp.domain.point.ScreenPoint;
import com.preview.mousemacroapp.domain.schedule.ExecutionSchedule;
import com.preview.mousemacroapp.domain.status.MacroStatus;
import com.preview.mousemacroapp.domain.timing.DelayPolicy;
import com.preview.mousemacroapp.service.CaptureResult;
import com.preview.mousemacroapp.service.MacroRequest;
import com.preview.mousemacroapp.service.MacroService;
import com.preview.mousemacroapp.service.MouseClickCaptor;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.time.Duration;
import java.util.Random;

/**
 * UI 이벤트를 Service 호출로 변환하는 컨트롤러.
 *
 * <p>UI에서 발생한 이벤트를 받아 {@link MacroService}의 단일 API로 위임한다.</p>
 *
 * <p><b>책임</b></p>
 * <ul>
 *   <li>UI 계층에서 예외를 사용자 메시지로 정리한다.</li>
 *   <li>상태 표시/버튼 활성화 등 화면 갱신 규칙을 일원화한다.</li>
 *   <li>좌표 캡처 결과를 UI에 반영하고, Start 요청에 사용한다.</li>
 *   <li>반복 횟수 입력을 파싱하여 실행 요청에 반영한다(0=무한).</li>
 * </ul>
 */
public final class MacroController {

    private final MacroService macroService;
    private final MouseClickCaptor clickCaptor;

    /**
     * 마지막으로 확정된 좌표(없으면 null).
     */
    private volatile ScreenPoint selectedPoint;

    public MacroController(MacroService macroService, MouseClickCaptor clickCaptor) {
        this.macroService = macroService;
        this.clickCaptor = clickCaptor;
    }

    public void start(Label statusLabel,
                      Label messageLabel,
                      Button pauseResumeButton,
                      Label pointLabel,
                      TextField repeatCountField) {
        try {
            DebugLog.log("UI_BTN", () -> "click Start");

            int repeatCount = parseRepeatCount(repeatCountField, messageLabel);
            if (repeatCount < 0) {
                // 역할: 파싱/검증 실패 시 서비스 호출을 중단한다.
                refresh(statusLabel, messageLabel, pauseResumeButton);
                return;
            }

            // 역할: 최소 실행 요청을 생성한다. (UI 입력폼 도입 전 기본값 + 선택 좌표 + 반복 횟수 반영)
            MacroRequest request = defaultRequest(repeatCount);

            macroService.start(request);
            refresh(statusLabel, messageLabel, pauseResumeButton);

            publishMessage(messageLabel, "시작 요청이 처리되었다.");
        } catch (RuntimeException ex) {
            publishMessage(messageLabel, "시작 실패: " + ex.getMessage());
            refresh(statusLabel, messageLabel, pauseResumeButton);
        }
    }

    public void stop(Label statusLabel, Label messageLabel, Button pauseResumeButton) {
        try {
            DebugLog.log("UI_BTN", () -> "click Stop");

            macroService.stop();
            refresh(statusLabel, messageLabel, pauseResumeButton);

            publishMessage(messageLabel, "정지 요청이 처리되었다.");
        } catch (RuntimeException ex) {
            publishMessage(messageLabel, "정지 실패: " + ex.getMessage());
            refresh(statusLabel, messageLabel, pauseResumeButton);
        }
    }

    /**
     * 일시정지/재개를 하나의 버튼으로 토글한다.
     */
    public void togglePauseResume(Label statusLabel, Label messageLabel, Button pauseResumeButton) {
        try {
            DebugLog.log("UI_BTN", () -> "click Pause/Resume text=" + pauseResumeButton.getText());

            MacroStatus status = macroService.status();
            if (status == MacroStatus.RUNNING) {
                macroService.pause();
                publishMessage(messageLabel, "일시정지 요청이 처리되었다.");
            } else if (status == MacroStatus.PAUSED) {
                macroService.resume();
                publishMessage(messageLabel, "재개 요청이 처리되었다.");
            } else {
                publishMessage(messageLabel, "현재 상태에서는 일시정지/재개가 불가하다: " + status);
            }
            refresh(statusLabel, messageLabel, pauseResumeButton);
        } catch (RuntimeException ex) {
            publishMessage(messageLabel, "일시정지/재개 실패: " + ex.getMessage());
            refresh(statusLabel, messageLabel, pauseResumeButton);
        }
    }

    /**
     * 좌표 캡처를 시작한다.
     *
     * <p>역할:</p>
     * <ul>
     *   <li>사용자에게 “클릭 대기/ESC 취소” 안내 메시지를 보여준다.</li>
     *   <li>캡처 완료 시 좌표 라벨을 갱신하고 이후 Start 요청에 반영한다.</li>
     * </ul>
     */
    public void capturePoint(Label messageLabel, Label pointLabel) {
        DebugLog.log("UI_BTN", () -> "click Capture");
        publishMessage(messageLabel, "좌표 캡처 대기 중... 화면에서 클릭하세요. (ESC: 취소)");

        clickCaptor.captureNextClick(Duration.ofSeconds(15))
                .thenAccept(result -> Platform.runLater(() -> applyCaptureResult(result, messageLabel, pointLabel)));
    }

    /**
     * ESC 입력 처리(캡처 취소).
     */
    public void onEscapePressed(Label messageLabel) {
        DebugLog.log("UI_KEY", () -> "press ESC");
        clickCaptor.cancel();
        publishMessage(messageLabel, "좌표 캡처 취소 요청이 처리되었다.");
    }

    public void refresh(Label statusLabel, Label messageLabel, Button pauseResumeButton) {
        MacroStatus status = macroService.status();
        statusLabel.setText(String.valueOf(status));

        if (messageLabel.getText() == null) {
            messageLabel.setText("");
        }

        // 역할: RUNNING/PAUSED 상태에서만 토글 버튼을 활성화한다.
        boolean toggleEnabled = (status == MacroStatus.RUNNING || status == MacroStatus.PAUSED);
        pauseResumeButton.setDisable(!toggleEnabled);

        // 역할: 토글 버튼 문구는 현재 상태에 따라 달라진다.
        pauseResumeButton.setText(status == MacroStatus.PAUSED ? "Resume" : "Pause");
    }

    private void applyCaptureResult(CaptureResult<ScreenPoint> result, Label messageLabel, Label pointLabel) {
        switch (result.status()) {
            case CAPTURED -> {
                ScreenPoint point = result.value().orElseThrow();
                selectedPoint = point;
                pointLabel.setText("좌표: (" + point.x() + ", " + point.y() + ")");
                publishMessage(messageLabel, "좌표가 저장되었습니다: (" + point.x() + ", " + point.y() + ")");
            }
            case CANCELLED -> publishMessage(messageLabel, "좌표 캡처가 취소되었다.");
            case TIMEOUT -> publishMessage(messageLabel, "좌표 캡처 시간이 초과되었다.");
            case FAILED -> publishMessage(messageLabel, "좌표 캡처 실패: " + result.reason().orElse("원인 불명"));
        }
    }

    private void publishMessage(Label messageLabel, String message) {
        messageLabel.setText(message);
        DebugLog.log("UI_MSG", () -> message);
    }

    /**
     * 반복 횟수를 파싱한다.
     *
     * <p>정책:</p>
     * <ul>
     *   <li>빈 값은 0(무한)으로 간주한다.</li>
     *   <li>숫자가 아니면 오류 메시지를 출력하고 -1을 반환한다.</li>
     *   <li>음수면 오류 메시지를 출력하고 -1을 반환한다.</li>
     * </ul>
     */
    private int parseRepeatCount(TextField repeatCountField, Label messageLabel) {
        String raw = (repeatCountField != null) ? repeatCountField.getText() : null;
        String trimmed = (raw == null) ? "" : raw.trim();

        if (trimmed.isEmpty()) {
            return 0;
        }

        try {
            int value = Integer.parseInt(trimmed);
            if (value < 0) {
                publishMessage(messageLabel, "반복 횟수는 0 이상이어야 한다.");
                return -1;
            }
            return value;
        } catch (NumberFormatException ex) {
            publishMessage(messageLabel, "반복 횟수는 숫자여야 한다.");
            return -1;
        }
    }

    private MacroRequest defaultRequest(int repeatCount) {
        ScreenPoint point = (selectedPoint != null) ? selectedPoint : new ScreenPoint(300, 300);
        MacroPoint macroPoint = new MacroPoint("default", point, new ExactPositionPolicy());

        // 역할: UI 입력폼 도입 전까지는 최소 동작(단발 좌클릭)을 기본값으로 둔다.
        ClickAction action = ClickAction.singleLeft();

        // 역할: 기본 딜레이는 고정 300ms로 둔다.
        DelayPolicy delayPolicy = new DelayPolicy(300, 0, 0);

        ClickPositionPolicy positionPolicy = macroPoint.positionPolicy();

        // 역할: 스케줄은 UI 입력이 생기기 전까지 "항상 허용"으로 둔다.
        ExecutionSchedule schedule = new ExecutionSchedule.Always();

        return new MacroRequest(
                macroPoint,
                action,
                positionPolicy,
                delayPolicy,
                schedule,
                new Random(),
                repeatCount
        );
    }
}
