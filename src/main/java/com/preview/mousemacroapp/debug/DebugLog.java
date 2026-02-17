package com.preview.mousemacroapp.debug;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 디버그 로그 출력 유틸리티.
 *
 * <p>역할:</p>
 * - 디버그 모드가 활성화된 경우에만 로그를 출력한다.
 * - 출력 포맷을 일원화하여 UI 기능 개발 시 동작 흐름을 쉽게 추적할 수 있게 한다.
 */
public final class DebugLog {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private static final Clock CLOCK = Clock.system(KST);

    private DebugLog() {
        // 유틸리티 클래스
    }

    private static String now() {
        return LocalDateTime.now(CLOCK).format(FORMAT);
    }

    public static void log(Supplier<String> messageSupplier) {
        if (!DebugMode.isEnabled()) {
            return;
        }

        System.out.println("[DEBUG] " + now() + " " + messageSupplier.get());
    }

    public static void log(String category, Supplier<String> messageSupplier) {
        if (!DebugMode.isEnabled()) {
            return;
        }

        String message = messageSupplier.get();
        System.out.printf("[DEBUG][%s] " + now() + " %s%n", category, message);
    }
}
