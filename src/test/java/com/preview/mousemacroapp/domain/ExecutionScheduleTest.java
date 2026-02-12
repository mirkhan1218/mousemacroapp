package com.preview.mousemacroapp.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExecutionSchedule 도메인 테스트.
 *
 * @since 0.6
 */
class ExecutionScheduleTest {

    @Test
    @DisplayName("Always: 언제나 실행 가능")
    void always_shouldAllowAnyTime() {
        ExecutionSchedule schedule = new ExecutionSchedule.Always();

        assertTrue(schedule.isAllowed(LocalTime.of(0, 0)));
        assertTrue(schedule.isAllowed(LocalTime.of(12, 34)));
        assertTrue(schedule.isAllowed(LocalTime.of(23, 59)));
    }

    @Test
    @DisplayName("Range: timeRange.contains 결과를 따른다")
    void range_shouldDelegateToTimeRange() {
        LocalTimeRange range = new LocalTimeRange(LocalTime.of(9, 0), LocalTime.of(18, 0));
        ExecutionSchedule schedule = new ExecutionSchedule.Range(range);

        assertTrue(schedule.isAllowed(LocalTime.of(9, 0)));
        assertTrue(schedule.isAllowed(LocalTime.of(17, 0)));
        assertFalse(schedule.isAllowed(LocalTime.of(18, 0)));
        assertFalse(schedule.isAllowed(LocalTime.of(8, 59)));
    }
}
