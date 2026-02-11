package com.preview.mousemacroapp.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MacroStatus 도메인 정책 테스트.
 *
 * <p>
 * 상태 판단 로직이 변경될 경우 실행 흐름 제어에 직접적인 영향을 주므로
 * 명시적으로 고정한다.
 * </p>
 *
 * @since 0.2
 */
class MacroStatusTest {

    @Test
    @DisplayName("STOPPED 상태 검증")
    void stoppedStateTest() {
        MacroStatus status = MacroStatus.STOPPED;

        assertTrue(status.isStopped());
        assertFalse(status.isRunning());
        assertFalse(status.isPaused());
        assertFalse(status.isActive());
    }

    @Test
    @DisplayName("RUNNING 상태 검증")
    void runningStateTest() {
        MacroStatus status = MacroStatus.RUNNING;

        assertFalse(status.isStopped());
        assertTrue(status.isRunning());
        assertFalse(status.isPaused());
        assertTrue(status.isActive());
    }

    @Test
    @DisplayName("PAUSED 상태 검증")
    void pausedStateTest() {
        MacroStatus status = MacroStatus.PAUSED;

        assertFalse(status.isStopped());
        assertFalse(status.isRunning());
        assertTrue(status.isPaused());
        assertTrue(status.isActive());
    }
}
