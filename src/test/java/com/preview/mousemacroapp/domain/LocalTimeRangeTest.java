package com.preview.mousemacroapp.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LocalTimeRange 도메인 테스트.
 *
 * @since 0.6
 */
class LocalTimeRangeTest {

    @Test
    @DisplayName("생성 제약: start와 end가 같으면 예외")
    void startEqualsEnd_shouldThrow() {
        LocalTime t = LocalTime.of(9, 0);
        assertThrows(IllegalArgumentException.class, () -> new LocalTimeRange(t, t));
    }

    @Test
    @DisplayName("자정 미통과 범위: 시작 포함, 종료 제외")
    void normalRange_inclusiveExclusive() {
        LocalTimeRange range = new LocalTimeRange(LocalTime.of(9, 0), LocalTime.of(18, 0));

        assertTrue(range.contains(LocalTime.of(9, 0)));   // start inclusive
        assertTrue(range.contains(LocalTime.of(17, 59)));
        assertFalse(range.contains(LocalTime.of(18, 0))); // end exclusive
        assertFalse(range.contains(LocalTime.of(8, 59)));
    }

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
}
