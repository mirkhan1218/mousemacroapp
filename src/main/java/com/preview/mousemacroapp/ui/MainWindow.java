package com.preview.mousemacroapp.ui;

import com.preview.mousemacroapp.service.MacroService;
import com.preview.mousemacroapp.service.MouseClickCaptor;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * UI 최상위 화면 구성 요소.
 *
 * <p>Stage에 장착할 Scene과 최소 컨트롤(버튼/상태표시)을 제공한다.</p>
 *
 * <p><b>책임</b></p>
 * <ul>
 *   <li>화면 레이아웃 구성</li>
 *   <li>버튼/라벨 생성</li>
 *   <li>이벤트 처리는 {@link MacroController}에 위임</li>
 * </ul>
 */
public final class MainWindow {

    private final Scene scene;

    public MainWindow(MacroService macroService, MouseClickCaptor clickCaptor) {
        MacroController controller = new MacroController(macroService, clickCaptor);

        Label statusLabel = new Label();
        Label messageLabel = new Label();
        Label pointLabel = new Label("좌표: (미설정)");

        TextField repeatCountField = new TextField("0");
        repeatCountField.setPrefColumnCount(6);

        Button captureButton = new Button("좌표 캡처");
        Button startButton = new Button("Start");
        Button pauseResumeButton = new Button("Pause");
        Button stopButton = new Button("Stop");

        /*
         * 역할: UI 이벤트를 Controller에 위임한다.
         * - Pause/Resume는 하나의 토글 버튼으로 운용한다.
         * - 좌표 캡처는 전역 클릭 1회를 기다렸다가 좌표를 화면에 반영한다.
         * - 반복 횟수(0=무한)를 start 요청에 반영한다.
         */
        captureButton.setOnAction(e -> controller.capturePoint(messageLabel, pointLabel));
        startButton.setOnAction(e -> controller.start(statusLabel, messageLabel, pauseResumeButton, pointLabel, repeatCountField));
        pauseResumeButton.setOnAction(e -> controller.togglePauseResume(statusLabel, messageLabel, pauseResumeButton));
        stopButton.setOnAction(e -> controller.stop(statusLabel, messageLabel, pauseResumeButton));

        HBox buttons = new HBox(8, captureButton, startButton, pauseResumeButton, stopButton);

        HBox repeatRow = new HBox(8, new Label("반복(0=무한):"), repeatCountField);

        Parent root = buildRoot(statusLabel, messageLabel, pointLabel, repeatRow, buttons);

        Scene created = new Scene(root, 520, 280);

        // ✅ Scene 생성 직후: ESC로 캡처 취소를 처리한다(앱 포커스 내에서 확실히 동작).
        created.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                controller.onEscapePressed(messageLabel);
                e.consume();
            }
        });

        // 역할: 최초 상태를 화면에 반영한다.
        controller.refresh(statusLabel, messageLabel, pauseResumeButton);
        this.scene = created;
    }

    private Parent buildRoot(Label statusLabel,
                             Label messageLabel,
                             Label pointLabel,
                             HBox repeatRow,
                             HBox buttons) {
        VBox root = new VBox(
                10,
                new Label("Status:"),
                statusLabel,
                new Label("Point:"),
                pointLabel,
                repeatRow,
                buttons,
                new Label("Message:"),
                messageLabel
        );
        root.setPadding(new Insets(12));
        return root;
    }

    public Scene scene() {
        return scene;
    }
}
