package com.preview.mousemacroapp;

import com.preview.mousemacroapp.infra.hook.AwtRobotMouse;
import com.preview.mousemacroapp.infra.hook.DryRunClickExecutor;
import com.preview.mousemacroapp.infra.hook.RobotClickExecutor;
import com.preview.mousemacroapp.service.ClickExecutor;
import com.preview.mousemacroapp.service.DefaultMacroService;
import com.preview.mousemacroapp.service.MacroService;
import com.preview.mousemacroapp.ui.MainWindow;
import javafx.application.Application;
import javafx.stage.Stage;

import java.time.Clock;

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

    @Override
    public void start(Stage primaryStage) {
        ClickExecutor clickExecutor = buildClickExecutor();
        MacroService macroService = new DefaultMacroService(clickExecutor);

        MainWindow mainWindow = new MainWindow(macroService);

        primaryStage.setTitle("Mouse Macro App");
        primaryStage.setScene(mainWindow.scene());
        primaryStage.show();
    }

    private ClickExecutor buildClickExecutor() {
        if (DRY_RUN) {
            return new DryRunClickExecutor(Clock.systemDefaultZone());
        }

        AwtRobotMouse robotMouse = new AwtRobotMouse();
        return new RobotClickExecutor(robotMouse);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
