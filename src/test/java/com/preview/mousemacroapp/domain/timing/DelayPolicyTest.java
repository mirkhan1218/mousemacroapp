package com.preview.mousemacroapp.domain.timing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 클릭 타이밍 정책(When) 검증 테스트.
 *
 * <p><b>테스트 대상</b></p>
 * - DelayPolicy
 *
 * <p><b>검증 목적</b></p>
 * - base + random(min..max) 합산 규칙을 고정한다.
 * - 음수 값 및 min > max 입력을 생성 시점에 차단한다.
 *
 * <p><b>검증 범위</b></p>
 * - 생성 제약(음수, min>max)
 * - null random 방어
 * - min==max 결정적 동작
 * - 결과 범위: [base+min .. base+max]
 *
 * <p><b>회귀 방지 이유</b></p>
 * - 잘못된 딜레이 계산은 매크로 실행 간격을 왜곡시킨다.
 *
 * @since 0.5
 */
class DelayPolicyTest {

    /*
     * 시나리오: 시간 값은 모두 0 이상이어야 한다
     *
     * 입력(Given):
     * - new DelayPolicy(-1, 0, 0)
     * - new DelayPolicy(0, -1, 0)
     * - new DelayPolicy(0, 0, -1)
     *
     * 예상 결과(Then):
     * - IllegalArgumentException 발생
     */
    @Test
    @DisplayName("생성 제약: 시간 값은 0 이상")
    void mustRejectNegativeValues() {
        assertThrows(IllegalArgumentException.class, () -> new DelayPolicy(-1, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new DelayPolicy(0, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> new DelayPolicy(0, 0, -1));
    }

    /*
     * 시나리오: minRandomMillis > maxRandomMillis 는 허용하지 않는다
     *
     * 입력(Given):
     * - new DelayPolicy(0, 10, 9)
     *
     * 예상 결과(Then):
     * - IllegalArgumentException 발생
     */
    @Test
    @DisplayName("생성 제약: minRandomMillis <= maxRandomMillis")
    void mustRejectMinGreaterThanMax() {
        assertThrows(IllegalArgumentException.class, () -> new DelayPolicy(0, 10, 9));
    }

    /*
     * 시나리오: Random 주입은 필수이며 null이면 실패해야 한다
     *
     * 입력(Given):
     * - policy = new DelayPolicy(100, 0, 0)
     * - random = null
     *
     * 예상 결과(Then):
     * - NullPointerException 발생
     */
    @Test
    @DisplayName("random이 null이면 예외")
    void resolveDelay_nullRandom_shouldThrow() {
        DelayPolicy policy = new DelayPolicy(100, 0, 0);
        assertThrows(NullPointerException.class, () -> policy.resolveDelayMillis(null));
    }

    /*
     * 시나리오: min==max면 랜덤이더라도 결과가 고정되어야 한다
     *
     * 입력(Given):
     * - policy = new DelayPolicy(200, 50, 50)
     * - random = new Random(1)
     *
     * 예상 결과(Then):
     * - delay == 250
     */
    @Test
    @DisplayName("min=max이면 항상 base+min을 반환")
    void resolveDelay_sameMinMax_shouldBeDeterministic() {
        DelayPolicy policy = new DelayPolicy(200, 50, 50);
        long delay = policy.resolveDelayMillis(new Random(1));

        assertEquals(250L, delay);
    }

    /*
     * 시나리오: 계산 결과는 base + [min..max] 범위를 벗어나면 안 된다
     *
     * 입력(Given):
     * - base=1000
     * - min=10
     * - max=30
     * - policy = new DelayPolicy(base, min, max)
     * - random = new Random(123)
     * - 반복 횟수 = 1000
     *
     * 예상 결과(Then):
     * - delay ∈ [1010..1030]
     */
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

    /*
     * 시나리오: 완전 최소값(base=0,min=0,max=0)에서는 항상 0을 반환해야 한다
     *
     * 입력(Given):
     * - base=0, min=0, max=0
     * - policy = new DelayPolicy(0, 0, 0)
     * - random = new Random(999)
     *
     * 예상 결과(Then):
     * - delay == 0
     */
    @Test
    @DisplayName("경계값: base=0,min=0,max=0이면 항상 0")
    void zeroBaseZeroRange_shouldReturnZero() {
        DelayPolicy policy = new DelayPolicy(0, 0, 0);
        long delay = policy.resolveDelayMillis(new Random(999));

        assertEquals(0L, delay);
    }
}
