/**
 * 실행 상태(Status) 도메인 모델을 제공한다.
 *
 * <p>
 * 매크로의 현재 실행 상태(STOPPED, RUNNING, PAUSED)를 정의하며,
 * 상태 판단 로직(isActive, isRunning 등)을 포함한다.
 * </p>
 *
 * <p>
 * 상태 변경은 Service 계층에서만 수행되어야 한다.
 * </p>
 */
package com.preview.mousemacroapp.domain.status;
