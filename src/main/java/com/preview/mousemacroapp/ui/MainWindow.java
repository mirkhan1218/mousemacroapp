package com.preview.mousemacroapp.ui;

import com.preview.mousemacroapp.debug.DebugLog;
import com.preview.mousemacroapp.service.MacroService;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * UI 최상위 화면 구성 요소.
 *
 * <p>
 * Stage에 장착할 Scene과 최소 컨트롤(버튼/상태표시)을 제공한다.
 * </p>
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

    public MainWindow(MacroService macroService) {
        MacroController controller = new MacroController(macroService);

        Label statusLabel = new Label();
        Label messageLabel = new Label();

        Button startButton = new Button("Start");
        Button pauseResumeButton = new Button("Pause");
        Button stopButton = new Button("Stop");

        /*
         * 역할: UI 이벤트를 Controller에 위임한다.
         * - Pause/Resume는 하나의 토글 버튼으로 운용한다.
         */
        startButton.setOnAction(e -> {
            DebugLog.log("UI_BTN", () -> "click Start");
            controller.start(statusLabel, messageLabel, pauseResumeButton);
        });
        pauseResumeButton.setOnAction(e -> {
            DebugLog.log("UI_BTN", () -> "click Pause/Resume text=" + pauseResumeButton.getText());
            controller.togglePauseResume(statusLabel, messageLabel, pauseResumeButton);
        });
        stopButton.setOnAction(e -> {
            DebugLog.log("UI_BTN", () -> "click Stop");
            controller.stop(statusLabel, messageLabel, pauseResumeButton);
        });

        HBox buttons = new HBox(8, startButton, pauseResumeButton, stopButton);

        VBox root = new VBox(10,
                new Label("Status:"),
                statusLabel,
                buttons,
                new Label("Message:"),
                messageLabel
        );
        root.setPadding(new Insets(12));

        // 역할: 최초 상태를 화면에 반영한다.
        controller.refresh(statusLabel, messageLabel, pauseResumeButton);

        this.scene = new Scene(root, 520, 220);

        this.scene.addEventFilter(KeyEvent.KEY_PRESSED, e ->
                DebugLog.log("UI_KEY", () ->
                        "pressed code=" + e.getCode()
                                + " typed char=" + e.getCharacter()
                                + " text=" + e.getText()
                                + " ctrl=" + e.isControlDown()
                                + " alt=" + e.isAltDown()
                                + " shift=" + e.isShiftDown()
                )
        );
    }

    public Scene scene() {
        return scene;
    }
}
