package com.preview.mousemacroapp.infra.hook;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JNativeHook(GlobalScreen) 접근을 감싼 파사드.
 *
 * <p>역할:</p>
 * - 전역 훅 등록/해제 및 리스너 연결을 단일 책임으로 통제한다.
 * - 외부 라이브러리 의존을 경계로 고정하여, 상위 계층(서비스/UI)에 누수되지 않게 한다.
 */
public interface JNativeHookFacade {

    void register() throws NativeHookException;

    void unregister() throws NativeHookException;

    void addKeyListener(NativeKeyListener listener);

    void removeKeyListener(NativeKeyListener listener);

    boolean isRegistered();

    /**
     * 기본 구현체.
     */
    final class Default implements JNativeHookFacade {

        private static volatile boolean registered;

        public Default() {
            // JNativeHook 내부 로거가 콘솔을 오염시키는 것을 막는다.
            Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
            logger.setLevel(Level.OFF);
            logger.setUseParentHandlers(false);
        }

        @Override
        public void register() throws NativeHookException {
            if (registered) {
                return;
            }
            GlobalScreen.registerNativeHook();
            registered = true;
        }

        @Override
        public void unregister() throws NativeHookException {
            if (!registered) {
                return;
            }
            GlobalScreen.unregisterNativeHook();
            registered = false;
        }

        @Override
        public void addKeyListener(NativeKeyListener listener) {
            Objects.requireNonNull(listener, "listener");
            GlobalScreen.addNativeKeyListener(listener);
        }

        @Override
        public void removeKeyListener(NativeKeyListener listener) {
            Objects.requireNonNull(listener, "listener");
            GlobalScreen.removeNativeKeyListener(listener);
        }

        @Override
        public boolean isRegistered() {
            return registered;
        }
    }
}
