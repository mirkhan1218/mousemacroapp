package com.preview.mousemacroapp.debug;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DebugMode 계약 테스트.
 *
 * <p>역할:</p>
 * - 실행 인자(-debug) 파싱 결과가 기대대로 동작하는지 회귀를 막는다.
 */
class DebugModeTest {

    @AfterEach
    void tearDown() {
        // 역할: 전역 상태 유틸은 테스트 간 누수 방지를 위해 항상 리셋한다.
        DebugMode.setEnabledForTest(false);
    }

    @Test
    void initialize_whenArgsContainDebug_thenEnabledTrue() {
        DebugMode.initialize(new String[]{"-debug"});
        assertTrue(DebugMode.isEnabled());
    }

    @Test
    void initialize_whenArgsDoNotContainDebug_thenEnabledFalse() {
        DebugMode.initialize(new String[]{"-somethingElse"});
        assertFalse(DebugMode.isEnabled());
    }

    @Test
    void initialize_whenArgsEmpty_thenEnabledFalse() {
        DebugMode.initialize(new String[]{});
        assertFalse(DebugMode.isEnabled());
    }

    @Test
    void initialize_whenArgsNull_thenThrow() {
        assertThrows(NullPointerException.class, () -> DebugMode.initialize(null));
    }
}
