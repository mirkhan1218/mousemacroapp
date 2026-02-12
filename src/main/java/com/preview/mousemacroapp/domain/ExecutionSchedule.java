package com.preview.mousemacroapp.domain;

import java.time.LocalTime;
import java.util.Objects;

/**
 * 실행 스케줄 정책을 정의한다.
 *
 * <p>
 * 지원 정책:
 * <ul>
 *   <li>즉시 실행(ALWAYS): 항상 실행 가능</li>
 *   <li>시간 범위(RANGE): 지정된 시간 범위에만 실행 가능</li>
 * </ul>
 * </p>
 *
 * @since 0.6
 */
public sealed interface ExecutionSchedule permits ExecutionSchedule.Always, ExecutionSchedule.Range {

    /**
     * 주어진 시각에 실행이 허용되는지 판단한다.
     *
     * @param now 현재 시각
     * @return 실행 가능하면 true
     */
    boolean isAllowed(LocalTime now);

    /**
     * 즉시 실행 스케줄: 항상 실행 가능.
     *
     * @since 0.6
     */
    final class Always implements ExecutionSchedule {

        @Override
        public boolean isAllowed(LocalTime now) {
            Objects.requireNonNull(now, "now");
            return true;
        }
    }

    /**
     * 시간 범위 실행 스케줄.
     *
     * @param timeRange 실행 허용 시간 범위
     * @since 0.6
     */
    record Range(LocalTimeRange timeRange) implements ExecutionSchedule {

        public Range {
            Objects.requireNonNull(timeRange, "timeRange");
        }

        @Override
        public boolean isAllowed(LocalTime now) {
            Objects.requireNonNull(now, "now");
            return timeRange.contains(now);
        }
    }
}
