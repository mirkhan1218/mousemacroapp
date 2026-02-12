package com.preview.mousemacroapp.domain.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MacroStatus 상태 판단 정책 검증 테스트.
 *
 * <p><b>테스트 대상</b></p>
 * - MacroStatus의 상태 판단 메서드(isActive/isRunning/isPaused/isStopped)
 *
 * <p><b>검증 목적</b></p>
 * - 실행 엔진/컨트롤러가 의존하는 상태 판단 결과를 고정한다.
 *
 * <p><b>검증 범위</b></p>
 * - STOPPED/RUNNING/PAUSED 각각에 대한 판단 메서드 반환값
 *
 * <p><b>회귀 방지 이유</b></p>
 * - 상태 판단 로직 변경 시 실행 흐름 분기(잠금/재시작/일시정지)가 오동작하는 것을 방지한다.
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
     * - isStopped()  == false
     * - isRunning()  == true
     * - isPaused()   == false
     * - isActive()   == true
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
     * - isStopped()  == false
     * - isRunning()  == false
     * - isPaused()   == true
     * - isActive()   == true
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
