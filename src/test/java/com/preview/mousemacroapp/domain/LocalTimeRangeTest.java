package com.preview.mousemacroapp.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LocalTimeRange(시간 범위) 정책 검증 테스트.
 *
 * <p><b>테스트 대상</b></p>
 * - LocalTimeRange
 *
 * <p><b>검증 목적</b></p>
 * - 시작 포함(inclusive), 종료 제외(exclusive) 규칙을 고정한다.
 * - 자정 넘김 범위 계산을 정확히 보장한다.
 *
 * <p><b>검증 범위</b></p>
 * - start == end 예외
 * - 자정 미통과 범위 포함/제외
 * - 자정 통과 범위 포함/제외
 * - 경계값: 00:00~23:59 포함/제외
 *
 * <p><b>회귀 방지 이유</b></p>
 * - 시간 조건 판단 오류는 실행 스케줄 제어를 붕괴시킬 수 있다.
 *
 * @since 0.6
 */
class LocalTimeRangeTest {

    /*
     * 시나리오: start==end 인 0 길이 범위는 금지한다
     *
     * 입력(Given):
     * - start=09:00
     * - end=09:00
     *
     * 예상 결과(Then):
     * - IllegalArgumentException 발생
     */
    @Test
    @DisplayName("생성 제약: start와 end가 같으면 예외")
    void startEqualsEnd_shouldThrow() {
        LocalTime t = LocalTime.of(9, 0);
        assertThrows(IllegalArgumentException.class, () -> new LocalTimeRange(t, t));
    }

    /*
     * 시나리오: 자정 미통과 범위는 시작 포함/종료 제외 규칙을 따른다
     *
     * 입력(Given):
     * - range = new LocalTimeRange(09:00, 18:00)
     *
     * 예상 결과(Then):
     * - contains(09:00) == true
     * - contains(17:59) == true
     * - contains(18:00) == false
     * - contains(08:59) == false
     */
    @Test
    @DisplayName("자정 미통과 범위: 시작 포함, 종료 제외")
    void normalRange_inclusiveExclusive() {
        LocalTimeRange range = new LocalTimeRange(LocalTime.of(9, 0), LocalTime.of(18, 0));

        assertTrue(range.contains(LocalTime.of(9, 0)));   // start inclusive
        assertTrue(range.contains(LocalTime.of(17, 59)));
        assertFalse(range.contains(LocalTime.of(18, 0))); // end exclusive
        assertFalse(range.contains(LocalTime.of(8, 59)));
    }

    /*
     * 시나리오: 자정 통과 범위는 두 구간의 합집합으로 판단한다
     *
     * 입력(Given):
     * - range = new LocalTimeRange(23:00, 02:00)
     *
     * 예상 결과(Then):
     * - isOverMidnight() == true
     * - contains(23:00) == true
     * - contains(23:59) == true
     * - contains(00:00) == true
     * - contains(01:59) == true
     * - contains(02:00) == false
     * - contains(22:59) == false
     * - contains(02:01) == false
     */
    @Test
    @DisplayName("자정 통과 범위: 23:00~02:00 포함 규칙 검증")
    void overMidnightRange_containsLogic() {
        LocalTimeRange range = new LocalTimeRange(LocalTime.of(23, 0), LocalTime.of(2, 0));

        assertTrue(range.isOverMidnight());

        // [23:00..24:00)
        assertTrue(range.contains(LocalTime.of(23, 0)));
        assertTrue(range.contains(LocalTime.of(23, 59)));

        // [00:00..02:00)
        assertTrue(range.contains(LocalTime.of(0, 0)));
        assertTrue(range.contains(LocalTime.of(1, 59)));

        // end exclusive
        assertFalse(range.contains(LocalTime.of(2, 0)));

        // outside
        assertFalse(range.contains(LocalTime.of(22, 59)));
        assertFalse(range.contains(LocalTime.of(2, 1)));
    }

    /*
     * 시나리오: 00:00~23:59 범위에서 포함/제외 경계값을 고정한다
     *
     * 입력(Given):
     * - range = new LocalTimeRange(00:00, 23:59)
     *
     * 예상 결과(Then):
     * - contains(00:00) == true
     * - contains(23:58) == true
     * - contains(23:59) == false (종료 제외)
     */
    @Test
    @DisplayName("경계값: 00:00~23:59는 23:59를 제외한다")
    void almostFullDayRange_inclusiveExclusive() {
        LocalTimeRange range = new LocalTimeRange(LocalTime.of(0, 0), LocalTime.of(23, 59));

        assertTrue(range.contains(LocalTime.of(0, 0)));
        assertTrue(range.contains(LocalTime.of(23, 58)));
        assertFalse(range.contains(LocalTime.of(23, 59)));
    }
}
