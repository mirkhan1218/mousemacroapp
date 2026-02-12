/**
 * 실행 가능 시간 범위(Schedule, When-window)를 정의한다.
 *
 * <p>
 * 특정 시간대(LocalTimeRange)에 매크로 실행이 허용되는지 판단하는
 * 규칙(ExecutionSchedule)을 포함한다.
 * </p>
 *
 * <p>
 * 해당 패키지는 시간 조건 검증 로직만을 포함하며,
 * 실제 실행 제어는 Service 계층에서 수행한다.
 * </p>
 */
package com.preview.mousemacroapp.domain.schedule;
