package com.preview.mousemacroapp.service;

import com.preview.mousemacroapp.domain.status.MacroStatus;

/**
 * 매크로 실행 흐름 제어 서비스.
 *
 * <p>
 * UI는 이 서비스만 호출하여 실행/정지/일시정지/재개를 제어한다.
 * 상태 전이의 단일 진입점이며, 실행 스레드 관리는 내부 Runner가 책임진다.
 * </p>
 *
 * @since 0.6
 */
public interface MacroService {

    /**
     * 매크로를 시작한다.
     *
     * @param request 실행 요청
     * @throws IllegalStateException 이미 실행 중(RUNNING/PAUSED)인 경우
     */
    void start(MacroRequest request);

    /**
     * 매크로를 정지한다.
     * (STOPPED 상태로 전이)
     */
    void stop();

    /**
     * 매크로를 일시 정지한다.
     *
     * @throws IllegalStateException RUNNING이 아닌 경우
     */
    void pause();

    /**
     * 일시 정지된 매크로를 재개한다.
     *
     * @throws IllegalStateException PAUSED가 아닌 경우
     */
    void resume();

    /**
     * 현재 상태를 반환한다.
     *
     * @return 실행 상태
     */
    MacroStatus status();
}
