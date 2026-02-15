package com.preview.mousemacroapp.service;

import com.preview.mousemacroapp.domain.status.MacroStatus;

import java.time.Clock;
import java.util.Objects;

/**
 * {@link MacroService} 기본 구현체.
 *
 * <p>
 * UI가 호출하는 상태 전이(start/stop/pause/resume)의 단일 진입점이다.
 * 동시 호출(버튼 연타 등)에서도 상태가 깨지지 않도록 동기화 경계를 제공한다.
 * </p>
 *
 * @since 0.6
 */
public final class DefaultMacroService implements MacroService {

    private final Object lock = new Object();

    private final MacroRunner runner;

    private volatile MacroStatus status = MacroStatus.STOPPED;

    /**
     * 기본 시스템 시간대(Clock.systemDefaultZone())를 사용하는 서비스 생성자.
     *
     * @param clickExecutor 클릭 실행 포트(Infra 구현체 주입)
     * @throws NullPointerException clickExecutor가 null인 경우
     * @since 0.6
     */
    public DefaultMacroService(ClickExecutor clickExecutor) {
        this(clickExecutor, Clock.systemDefaultZone());
    }

    /**
     * 실행 엔진과 시간 소스를 주입하는 서비스 생성자.
     *
     * <p>
     * Clock 주입은 스케줄/타이밍 관련 디버그 및 테스트 재현성을 위해 필요하다.
     * </p>
     *
     * @param clickExecutor 클릭 실행 포트(Infra 구현체 주입)
     * @param clock         시간 소스
     * @throws NullPointerException clickExecutor 또는 clock이 null인 경우
     * @since 0.6
     */
    public DefaultMacroService(ClickExecutor clickExecutor, Clock clock) {
        Objects.requireNonNull(clickExecutor, "clickExecutor");
        Objects.requireNonNull(clock, "clock");
        this.runner = new MacroRunner(clickExecutor, clock);
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException request가 null인 경우
     * @since 0.6
     */
    @Override
    public void start(MacroRequest request) {
        Objects.requireNonNull(request, "request");

        synchronized (lock) {
            // 역할: 상태 전이의 단일 판단 지점(동시 start 요청 방지)
            if (status.isActive()) {
                throw new IllegalStateException("이미 실행 중이므로 start 할 수 없다. status=" + status);
            }

            runner.start(
                    request.macroPoint(),
                    request.clickAction(),
                    request.positionPolicy(),
                    request.delayPolicy(),
                    request.schedule(),
                    request.random(),
                    request.repeatCount(),
                    this::onRunnerCompleted
            );

            status = MacroStatus.RUNNING;
        }
    }

    private void onRunnerCompleted() {
        synchronized (lock) {
            // 역할: 제한 반복 종료/스레드 종료 시 STOPPED로 수렴(멱등)
            status = MacroStatus.STOPPED;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since 0.6
     */
    @Override
    public void stop() {
        synchronized (lock) {
            // 역할: stop은 현재 상태와 무관하게 STOPPED로 수렴(멱등)
            runner.requestStop();
            status = MacroStatus.STOPPED;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since 0.6
     */
    @Override
    public void pause() {
        synchronized (lock) {
            // 역할: RUNNING에서만 PAUSED로 전이 가능(명세 기반 상태 전이 제한)
            if (!status.isRunning()) {
                throw new IllegalStateException("RUNNING 상태에서만 pause 할 수 있다. status=" + status);
            }
            runner.pause();
            status = MacroStatus.PAUSED;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since 0.6
     */
    @Override
    public void resume() {
        synchronized (lock) {
            // 역할: PAUSED에서만 RUNNING으로 전이 가능(명세 기반 상태 전이 제한)
            if (!status.isPaused()) {
                throw new IllegalStateException("PAUSED 상태에서만 resume 할 수 있다. status=" + status);
            }
            runner.resume();
            status = MacroStatus.RUNNING;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @since 0.6
     */
    @Override
    public MacroStatus status() {
        return status;
    }
}
