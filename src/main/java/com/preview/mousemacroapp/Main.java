package com.preview.mousemacroapp;

import com.preview.mousemacroapp.debug.DebugLog;
import com.preview.mousemacroapp.debug.DebugMode;
import com.preview.mousemacroapp.infra.hook.*;
import com.preview.mousemacroapp.service.ClickExecutor;
import com.preview.mousemacroapp.service.DefaultMacroService;
import com.preview.mousemacroapp.service.MacroService;
import com.preview.mousemacroapp.ui.MainWindow;
import javafx.application.Application;
import javafx.stage.Stage;

import java.time.Clock;
import java.util.Arrays;

/**
 * 애플리케이션 진입점.
 *
 * <p>
 * 책임:
 * - 인프라 구현체를 조립하고 Service에 주입한다.
 * - UI 초기화를 수행한다.
 * </p>
 */
public class Main extends Application {

    /**
     * 역할: 개발 초기에는 실제 마우스 제어를 막기 위해 Dry-Run을 기본값으로 둔다.
     * 필요 시 false로 바꾸면 AWT Robot 기반 실행으로 전환된다.
     */
    private static final boolean DRY_RUN = true;

    private GlobalKeyHook globalKeyHook;

    @Override
    public void start(Stage primaryStage) {
        ClickExecutor clickExecutor = buildClickExecutor();
        MacroService macroService = new DefaultMacroService(clickExecutor);

        // 디버그 모드에서만 전역 키 입력 훅을 시작한다.
        globalKeyHook = new GlobalKeyHook(new JNativeHookFacade.Default());
        globalKeyHook.startIfDebugEnabled();

        MainWindow mainWindow = new MainWindow(macroService);

        primaryStage.setTitle("Mouse Macro App");
        primaryStage.setScene(mainWindow.scene());
        primaryStage.show();
    }

    @Override
    public void stop() {
        // 역할: 전역 훅은 등록/해제를 수명주기에 맞춰 반드시 정리한다.
        if (globalKeyHook != null) {
            globalKeyHook.stopIfStarted();
        }
    }

    private ClickExecutor buildClickExecutor() {
        if (DRY_RUN) {
            return new DryRunClickExecutor(Clock.systemDefaultZone());
        }

        AwtRobotMouse robotMouse = new AwtRobotMouse();
        return new RobotClickExecutor(robotMouse);
    }

    public static void main(String[] args) {
        DebugMode.initialize(args);
        DebugLog.log(() -> "mode=" + (DebugMode.isEnabled() ? "ON" : "OFF") + " args=" + Arrays.toString(args));
        launch(args);
    }
}
