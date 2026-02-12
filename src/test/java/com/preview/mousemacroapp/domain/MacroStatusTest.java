package com.preview.mousemacroapp.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MacroStatus 상태 판단 정책 검증 테스트.
 *
 * <p><b>테스트 대상</b></p>
 * - MacroStatus enum 및 상태 판단 메서드
 *
 * <p><b>검증 목적</b></p>
 * - isActive/isRunning/isPaused/isStopped 반환 규칙을 고정한다.
 *
 * <p><b>검증 범위</b></p>
 * - STOPPED, RUNNING, PAUSED 각 상태의 판단 결과
 *
 * <p><b>회귀 방지 이유</b></p>
 * - 상태 판단 로직이 변경되면 실행 흐름 및 UI 상태 제어가 붕괴될 수 있다.
 *
 * @since 0.2
 */
class MacroStatusTest {

    /*
     * 시나리오: STOPPED 상태
     *
     * 입력(Given):
     * - status = MacroStatus.STOPPED
     *
     * 예상 결과(Then):
     * - isStopped()  == true
     * - isRunning()  == false
     * - isPaused()   == false
     * - isActive()   == false
     */
    @Test
    @DisplayName("STOPPED 상태 검증")
    void stoppedStateTest() {
        MacroStatus status = MacroStatus.STOPPED;

        assertTrue(status.isStopped());
        assertFalse(status.isRunning());
        assertFalse(status.isPaused());
        assertFalse(status.isActive());
    }

    /*
     * 시나리오: RUNNING 상태
     *
     * 입력(Given):
     * - status = MacroStatus.RUNNING
     *
     * 예상 결과(Then):
     * - isStopped() == false
     * - isRunning() == true
     * - isPaused()  == false
     * - isActive()  == true
     */
    @Test
    @DisplayName("RUNNING 상태 검증")
    void runningStateTest() {
        MacroStatus status = MacroStatus.RUNNING;

        assertFalse(status.isStopped());
        assertTrue(status.isRunning());
        assertFalse(status.isPaused());
        assertTrue(status.isActive());
    }

    /*
     * 시나리오: PAUSED 상태
     *
     * 입력(Given):
     * - status = MacroStatus.PAUSED
     *
     * 예상 결과(Then):
     * - isStopped() == false
     * - isRunning() == false
     * - isPaused()  == true
     * - isActive()  == true
     */
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
