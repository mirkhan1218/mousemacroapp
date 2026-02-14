package com.preview.mousemacroapp.debug;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DebugLog 계약 테스트.
 *
 * <p>역할:</p>
 * - 디버그 모드에서만 출력되는지
 * - 카테고리/시간 포맷이 유지되는지
 * 를 회귀로부터 보호한다.
 */
class DebugLogTest {

    private static final Pattern KST_TIMESTAMP =
            Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}");

    @AfterEach
    void tearDown() {
        DebugMode.setEnabledForTest(false);
    }

    @Test
    void log_whenDebugDisabled_thenNoOutput() {
        DebugMode.setEnabledForTest(false);

        String out = captureStdout(() -> DebugLog.log("UI_MSG", () -> "한글 메시지"));
        assertTrue(out.isBlank(), "디버그 비활성 상태에서는 출력이 없어야 한다.");
    }

    @Test
    void log_whenDebugEnabled_thenOutputContainsCategoryTimestampAndMessage() {
        DebugMode.setEnabledForTest(true);

        String out = captureStdout(() -> DebugLog.log("UI_MSG", () -> "시작 요청이 처리되었다."));
        assertFalse(out.isBlank());

        // 예: [DEBUG][UI_MSG] 2026-02-14 15:05:13.696 시작 요청이 처리되었다.
        assertTrue(out.startsWith("[DEBUG][UI_MSG] "), "카테고리 프리픽스가 유지되어야 한다: " + out);

        assertTrue(KST_TIMESTAMP.matcher(out).find(),
                "시간 포맷 yyyy-MM-dd HH:mm:ss.SSS 가 포함되어야 한다: " + out);

        assertTrue(out.contains("시작 요청이 처리되었다."),
                "메시지 본문이 포함되어야 한다: " + out);
    }

    @Test
    void log_withoutCategory_whenDebugEnabled_thenOutputContainsTimestampAndMessage() {
        DebugMode.setEnabledForTest(true);

        String out = captureStdout(() -> DebugLog.log(() -> "mode=ON"));
        assertFalse(out.isBlank());

        // 예: [DEBUG] 2026-02-14 15:05:11.384 mode=ON
        assertTrue(out.startsWith("[DEBUG] "), "기본 프리픽스가 유지되어야 한다: " + out);

        assertTrue(KST_TIMESTAMP.matcher(out).find(),
                "시간 포맷 yyyy-MM-dd HH:mm:ss.SSS 가 포함되어야 한다: " + out);

        assertTrue(out.contains("mode=ON"), "메시지 본문이 포함되어야 한다: " + out);
    }

    private static String captureStdout(Runnable action) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
            action.run();
        } finally {
            System.setOut(original);
        }
        return buffer.toString(StandardCharsets.UTF_8).trim();
    }
}
