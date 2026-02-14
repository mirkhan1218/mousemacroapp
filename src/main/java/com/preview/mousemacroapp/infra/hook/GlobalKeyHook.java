package com.preview.mousemacroapp.infra.hook;

import com.preview.mousemacroapp.debug.DebugLog;
import com.preview.mousemacroapp.debug.DebugMode;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import java.util.Objects;

/**
 * 디버그 모드 전용 전역 키 입력 로거.
 *
 * <p>역할:</p>
 * - UI가 포커스를 잃어도 전역 키 입력을 관찰할 수 있게 한다.
 * - 디버그 모드에서만 활성화되어, 일반 실행 시 OS 훅을 사용하지 않는다.
 */
public final class GlobalKeyHook {

    private final JNativeHookFacade facade;
    private final NativeKeyListener listener;

    private volatile boolean started;

    public GlobalKeyHook(JNativeHookFacade facade) {
        this.facade = Objects.requireNonNull(facade, "facade");
        this.listener = new LoggingKeyListener();
    }

    /**
     * 디버그 모드가 아닌 경우 시작하지 않는다(멱등).
     */
    public void startIfDebugEnabled() {
        if (!DebugMode.isEnabled()) {
            return;
        }
        if (started) {
            return;
        }

        try {
            facade.register();
            facade.addKeyListener(listener);
            started = true;

            DebugLog.log("GLOBAL_KEY", () -> "hook started");
        } catch (NativeHookException e) {
            // 역할: 전역 훅 실패는 앱 기능을 막지 않는다(디버그 보조 수단).
            DebugLog.log("GLOBAL_KEY", () -> "hook start failed: " + e.getClass().getSimpleName() + " " + e.getMessage());
        }
    }

    /**
     * 시작된 경우에만 종료한다(멱등).
     */
    public void stopIfStarted() {
        if (!started) {
            return;
        }
        try {
            facade.removeKeyListener(listener);
            facade.unregister();
            started = false;

            DebugLog.log("GLOBAL_KEY", () -> "hook stopped");
        } catch (NativeHookException e) {
            DebugLog.log("GLOBAL_KEY", () -> "hook stop failed: " + e.getClass().getSimpleName() + " " + e.getMessage());
        }
    }

    private static final class LoggingKeyListener implements NativeKeyListener {

        @Override
        public void nativeKeyPressed(NativeKeyEvent e) {
            DebugLog.log("GLOBAL_KEY", () ->
                    "pressed text=" + NativeKeyEvent.getKeyText(e.getKeyCode())
                            + " code=" + e.getKeyCode()
                            + " modifiers=" + e.getModifiers()
            );
        }

        @Override
        public void nativeKeyReleased(NativeKeyEvent e) {
            DebugLog.log("GLOBAL_KEY", () ->
                    "released text=" + NativeKeyEvent.getKeyText(e.getKeyCode())
                            + " code=" + e.getKeyCode()
                            + " modifiers=" + e.getModifiers()
            );
        }
    }
}
