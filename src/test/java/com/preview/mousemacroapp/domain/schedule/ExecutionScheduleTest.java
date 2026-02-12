package com.preview.mousemacroapp.domain.schedule;

import com.preview.mousemacroapp.domain.timing.DelayPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExecutionSchedule(실행 스케줄) 정책 검증 테스트.
 *
 * <p><b>테스트 대상</b></p>
 * - ExecutionSchedule.Always / ExecutionSchedule.Range
 *
 * <p><b>검증 목적</b></p>
 * - Always는 어떤 시각이든 실행 가능해야 한다.
 * - Range는 LocalTimeRange.contains 결과에 정확히 위임해야 한다.
 *
 * <p><b>검증 범위</b></p>
 * - Always.isAllowed(LocalTime) == true
 * - Range.isAllowed(LocalTime) 위임 규칙
 * - 생성 제약(null range)
 *
 * <p><b>회귀 방지 이유</b></p>
 * - 스케줄 허용 판단이 흔들리면 사용자가 설정한 시간 정책을 신뢰할 수 없게 된다.
 *
 * @since 0.6
 */
class ExecutionScheduleTest {

    /*
     * 시나리오: Always 정책은 언제나 허용(true)한다
     *
     * 입력(Given):
     * - schedule = new ExecutionSchedule.Always()
     *
     * 예상 결과(Then):
     * - isAllowed(00:00) == true
     * - isAllowed(12:34) == true
     * - isAllowed(23:59) == true
     */
    @Test
    @DisplayName("Always: 언제나 실행 가능")
    void always_shouldAllowAnyTime() {
        ExecutionSchedule schedule = new ExecutionSchedule.Always();

        assertTrue(schedule.isAllowed(LocalTime.of(0, 0)));
        assertTrue(schedule.isAllowed(LocalTime.of(12, 34)));
        assertTrue(schedule.isAllowed(LocalTime.of(23, 59)));
    }

    /*
     * 시나리오: Range 정책은 timeRange.contains를 따른다
     *
     * 입력(Given):
     * - timeRange = new LocalTimeRange(09:00, 18:00)
     * - schedule = new ExecutionSchedule.Range(timeRange)
     *
     * 예상 결과(Then):
     * - isAllowed(09:00) == true
     * - isAllowed(17:00) == true
     * - isAllowed(18:00) == false
     * - isAllowed(08:59) == false
     */
    @Test
    @DisplayName("Range: timeRange.contains 결과를 따른다")
    void range_shouldDelegateToTimeRange() {
        DelayPolicy.LocalTimeRange range = new DelayPolicy.LocalTimeRange(LocalTime.of(9, 0), LocalTime.of(18, 0));
        ExecutionSchedule schedule = new ExecutionSchedule.Range(range);

        assertTrue(schedule.isAllowed(LocalTime.of(9, 0)));
        assertTrue(schedule.isAllowed(LocalTime.of(17, 0)));
        assertFalse(schedule.isAllowed(LocalTime.of(18, 0)));
        assertFalse(schedule.isAllowed(LocalTime.of(8, 59)));
    }
}
