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
import com.preview.mousemacroapp.service.MacroRequest;
import com.preview.mousemacroapp.service.MacroService;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.util.Objects;
import java.util.Random;

/**
 * UI 이벤트를 Service 호출로 변환하는 컨트롤러.
 *
 * <p>
 * UI에서 발생한 이벤트를 받아 {@link MacroService}의 단일 API로 위임한다.
 * UI 계층이 도메인/서비스 내부 제약(상태 전이 예외 등)을 직접 다루지 않도록,
 * 예외 메시지 처리와 상태 갱신을 이 계층에서 정리한다.
 * </p>
 */
public final class MacroController {

    private final MacroService macroService;

    public MacroController(MacroService macroService) {
        this.macroService = Objects.requireNonNull(macroService, "macroService");
    }

    public void start(Label statusLabel, Label messageLabel, Button pauseResumeButton) {
        try {
            // 역할: 최소 실행 요청을 생성한다. (UI 입력폼 도입 전 임시 기본값)
            MacroRequest request = defaultRequest();

            macroService.start(request);
            refresh(statusLabel, messageLabel, pauseResumeButton);

            publishMessage(messageLabel, "시작 요청이 처리되었다.");
        } catch (RuntimeException ex) {
            // 역할: UI에서는 예외를 터뜨리지 않고, 사용자에게 메시지로 전달한다.
            publishMessage(messageLabel, "시작 실패: " + ex.getMessage());
            refresh(statusLabel, messageLabel, pauseResumeButton);
        }
    }

    public void stop(Label statusLabel, Label messageLabel, Button pauseResumeButton) {
        try {
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

    public void refresh(Label statusLabel, Label messageLabel, Button pauseResumeButton) {
        MacroStatus status = macroService.status();

        statusLabel.setText(String.valueOf(status));

        // 역할: 메시지 null 금지 규칙을 공통 정책으로만 보정한다.
        ensureMessageLabelInitialized(messageLabel);

        // 역할: RUNNING/PAUSED 상태에서만 토글 버튼을 활성화한다.
        boolean toggleEnabled = (status == MacroStatus.RUNNING || status == MacroStatus.PAUSED);
        pauseResumeButton.setDisable(!toggleEnabled);

        // 역할: 토글 버튼 문구는 현재 상태에 따라 달라진다.
        pauseResumeButton.setText(status == MacroStatus.PAUSED ? "Resume" : "Pause");
    }

    /**
     * UI 메시지를 화면과 로그에 동시에 반영한다.
     *
     * <p><b>역할:</b></p>
     * <ul>
     *   <li>메시지 반영 정책을 한 곳으로 모아 중복을 제거한다.</li>
     *   <li>디버그 모드일 때만 콘솔 로그로도 관찰 가능하게 한다.</li>
     * </ul>
     */
    private void publishMessage(Label messageLabel, String message) {
        String normalized = normalizeMessage(message);
        messageLabel.setText(normalized);

        // 역할: 사용자에게 보여준 메시지는 디버그 모드에서 동일하게 콘솔에도 남긴다.
        DebugLog.log("UI_MSG", () -> normalized);
    }

    /**
     * UI 메시지의 null을 금지하기 위한 정규화 로직.
     *
     * <p><b>역할:</b></></p>
     * <ul>
     *     <li>UI Label에 null이 설정되지 않도록 방지한다.</li>
     *     <li>메시지 정책을 한 곳으로 모아, refresh/publishMessage 모두 동일 규칙을 따르게 한다.</li>
     * </ul>
     */
    private String normalizeMessage(String message) {
        return message == null ? "" : message;
    }

    /**
     * refresh에서 사용되는 메시지 Label 기본값 보정.
     *
     * <p><b>역할:</b></p>
     * <ul>
     *     <li>refresh는 "상태 반영"만 책임지고, 메시지 정책은 공통 규칙으로만 보정한다.</li>
     *     <li>불필요한 디버그 로그를 만들지 않기 위해 publishMessage를 사용하지 않는다.</li>
     * </ul>
     */
    private void ensureMessageLabelInitialized(Label messageLabel) {
        if (messageLabel.getText() == null) {
            messageLabel.setText("");
        }
    }

    private MacroRequest defaultRequest() {
        MacroPoint point = new MacroPoint("default", new ScreenPoint(300, 300), new ExactPositionPolicy());

        // 역할: UI 입력폼 도입 전까지는 최소 동작(단발 좌클릭)을 기본값으로 둔다.
        ClickAction action = ClickAction.singleLeft();

        // 역할: 기본 딜레이는 고정 300ms로 둔다. (UI 입력폼 도입 전 임시 기본값)
        DelayPolicy delayPolicy = new DelayPolicy(300, 0, 0);

        ClickPositionPolicy positionPolicy = point.positionPolicy();

        // 역할: 스케줄은 UI 입력이 생기기 전까지 "항상 허용"으로 둔다.
        ExecutionSchedule schedule = new ExecutionSchedule.Always();

        return new MacroRequest(
                point,
                action,
                positionPolicy,
                delayPolicy,
                schedule,
                new Random()
        );
    }
}
