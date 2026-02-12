package com.preview.mousemacroapp.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DelayPolicy(When) 도메인 테스트.
 *
 * @since 0.5
 */
class DelayPolicyTest {

    @Test
    @DisplayName("생성 제약: 시간 값은 0 이상")
    void mustRejectNegativeValues() {
        assertThrows(IllegalArgumentException.class, () -> new DelayPolicy(-1, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new DelayPolicy(0, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> new DelayPolicy(0, 0, -1));
    }

    @Test
    @DisplayName("생성 제약: minRandomMillis <= maxRandomMillis")
    void mustRejectMinGreaterThanMax() {
        assertThrows(IllegalArgumentException.class, () -> new DelayPolicy(0, 10, 9));
    }

    @Test
    @DisplayName("random이 null이면 예외")
    void resolveDelay_nullRandom_shouldThrow() {
        DelayPolicy policy = new DelayPolicy(100, 0, 0);
        assertThrows(NullPointerException.class, () -> policy.resolveDelayMillis(null));
    }

    @Test
    @DisplayName("min=max이면 항상 base+min을 반환")
    void resolveDelay_sameMinMax_shouldBeDeterministic() {
        DelayPolicy policy = new DelayPolicy(200, 50, 50);
        long delay = policy.resolveDelayMillis(new Random(1));

        assertEquals(250L, delay);
    }

    @Test
    @DisplayName("계산 결과는 base + [min..max] 범위에 있어야 한다")
    void resolveDelay_shouldStayWithinRange() {
        long base = 1000;
        long min = 10;
        long max = 30;

        DelayPolicy policy = new DelayPolicy(base, min, max);
        Random random = new Random(123);

        for (int i = 0; i < 1000; i++) {
            long delay = policy.resolveDelayMillis(random);
            assertTrue(delay >= base + min);
            assertTrue(delay <= base + max);
        }
    }
}
