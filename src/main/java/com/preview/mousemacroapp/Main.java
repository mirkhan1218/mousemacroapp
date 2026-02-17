package com.preview.mousemacroapp;

import com.preview.mousemacroapp.debug.DebugLog;
import com.preview.mousemacroapp.debug.DebugMode;
import com.preview.mousemacroapp.infra.hook.AwtRobotMouse;
import com.preview.mousemacroapp.infra.hook.DryRunClickExecutor;
import com.preview.mousemacroapp.infra.hook.GlobalKeyHook;
import com.preview.mousemacroapp.infra.hook.JNativeHookFacade;
import com.preview.mousemacroapp.infra.hook.JNativeHookMouseClickCaptor;
import com.preview.mousemacroapp.infra.hook.RobotClickExecutor;
import com.preview.mousemacroapp.service.ClickExecutor;
import com.preview.mousemacroapp.service.DefaultMacroService;
import com.preview.mousemacroapp.service.MacroService;
import com.preview.mousemacroapp.service.MouseClickCaptor;
import com.preview.mousemacroapp.ui.MainWindow;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.time.Clock;

/**
 * 애플리케이션 진입점.
 *
 * <p><b>책임</b></p>
 * <ul>
 *   <li>인프라 구현체를 조립하고 Service에 주입한다.</li>
 *   <li>디버그 모드 초기화 및 디버그 보조 훅(전역 키 로깅)을 제어한다.</li>
 *   <li>UI 초기화를 수행한다.</li>
 * </ul>
 */
public class Main extends Application {

    /**
     * 역할: 개발 초기에는 실제 마우스 제어를 막기 위해 Dry-Run을 기본값으로 둔다.
     * 필요 시 false로 바꾸면 AWT Robot 기반 실행으로 전환된다.
     */
    private static final boolean DRY_RUN = true;

    @Override
    public void start(Stage primaryStage) {
        // 역할: 실행 인자 기반으로 디버그 모드를 초기화한다.
        DebugMode.initialize(getParameters().getRaw().toArray(new String[0]));
        DebugLog.log(() -> "mode=ON args=" + getParameters().getRaw());

        ClickExecutor clickExecutor = buildClickExecutor();
        MacroService macroService = new DefaultMacroService(clickExecutor);

        // 역할: 전역 훅 파사드는 하나를 공유하여 등록/해제 및 리스너 관리 책임을 집중한다.
        JNativeHookFacade hookFacade = new JNativeHookFacade.Default();

        // 역할: 디버그 모드에서만 전역 키 입력 로거를 활성화한다.
        GlobalKeyHook globalKeyHook = new GlobalKeyHook(hookFacade);
        globalKeyHook.startIfDebugEnabled();

        // 역할: “전역 클릭 1회 캡처” 어댑터를 UI에 주입한다.
        MouseClickCaptor clickCaptor = new JNativeHookMouseClickCaptor(hookFacade);

        MainWindow mainWindow = new MainWindow(macroService, clickCaptor);

        primaryStage.setTitle("Mouse Macro App");
        primaryStage.setScene(mainWindow.scene());
        primaryStage.show();

        // 역할: 앱 종료 시 디버그 훅이 살아있다면 정리한다.
        // 역할: 창 닫기 = 앱 종료로 간주하고, 실행/훅/JavaFX 종료 순서로 정리한다.
        // - JNativeHook 등 외부 라이브러리의 non-daemon 스레드가 남으면 Gradle run이 종료되지 않을 수 있다.
        // - 최후에는 System.exit(0)로 프로세스를 확실히 종료한다.
        primaryStage.setOnCloseRequest(e -> {
            try {
                macroService.stop();
            } catch (RuntimeException ignored) {
                // 역할: 종료 시점 stop 실패로 앱 종료가 막히지 않도록 방어한다.
            }

            try {
                globalKeyHook.stopIfStarted();
            } catch (RuntimeException ignored) {
                // 역할: 디버그 훅 정리 실패로 앱 종료가 막히지 않도록 방어한다.
            }

            Platform.exit();
            System.exit(0);
        });
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
