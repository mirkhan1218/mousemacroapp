package com.preview.mousemacroapp.domain;

/**
 * 프로그램의 실행 상태를 정의한다.
 *
 * <p>
 * 상태는 Controller 계층에서만 변경되어야 하며,
 * View는 해당 상태를 관찰(bind)만 수행한다.
 * </p>
 *
 * <p>
 * 상태 정의:
 * <ul>
 *     <li>STOPPED : 실행 중이 아님</li>
 *     <li>RUNNING : 매크로 실행 중</li>
 *     <li>PAUSED  : 실행은 유지하되 일시 정지</li>
 * </ul>
 * </p>
 *
 * @since 0.2
 */
public enum MacroStatus {

    /**
     * 매크로가 실행되지 않은 상태.
     */
    STOPPED,

    /**
     * 매크로가 현재 실행 중인 상태.
     */
    RUNNING,

    /**
     * 매크로 실행이 일시 정지된 상태.
     */
    PAUSED;

    /**
     * 실행 중(RUNNING 또는 PAUSED) 여부를 반환한다.
     *
     * <p>
     * UI 잠금 조건, 실행 흐름 제어에서 사용된다.
     * </p>
     *
     * @return 실행 중이면 true
     */
    public boolean isActive() {
        // 역할: 실행 중 상태를 단일 판단 지점으로 통합
        return this == RUNNING || this == PAUSED;
    }

    /**
     * 실행 중 상태(RUNNING) 여부를 반환한다.
     *
     * @return RUNNING이면 true
     */
    public boolean isRunning() {
        return this == RUNNING;
    }

    /**
     * 일시 정지 상태(PAUSED) 여부를 반환한다.
     *
     * @return PAUSED이면 true
     */
    public boolean isPaused() {
        return this == PAUSED;
    }

    /**
     * 정지 상태(STOPPED) 여부를 반환한다.
     *
     * @return STOPPED이면 true
     */
    public boolean isStopped() {
        return this == STOPPED;
    }
}
