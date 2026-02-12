package com.preview.mousemacroapp.domain;

import java.util.Objects;
import java.util.Random;

/**
 * 클릭 타이밍 정책(When)을 정의한다.
 *
 * <p>
 * 실제 클릭 간격은 다음과 같이 계산한다.
 * {@code baseIntervalMillis + randomDelayMillis}
 * </p>
 *
 * <p>
 * randomDelayMillis는 {@code [minRandomMillis..maxRandomMillis]} 범위에서 선택한다.
 * </p>
 *
 * @param baseIntervalMillis 기본 클릭 간격(ms). 0 이상
 * @param minRandomMillis    랜덤 딜레이 최소(ms). 0 이상
 * @param maxRandomMillis    랜덤 딜레이 최대(ms). 0 이상이며 min 이상
 * @since 0.5
 */
public record DelayPolicy(
        long baseIntervalMillis,
        long minRandomMillis,
        long maxRandomMillis
) {

    /**
     * 타이밍 정책 생성 시 입력 값을 검증한다.
     *
     * @throws IllegalArgumentException 값이 음수이거나 min > max 인 경우
     */
    public DelayPolicy {
        // 역할: 시간 값은 모두 0 이상이어야 한다.
        if (baseIntervalMillis < 0) {
            throw new IllegalArgumentException("baseIntervalMillis는 0 이상이어야 한다. baseIntervalMillis=%d"
                    .formatted(baseIntervalMillis));
        }
        if (minRandomMillis < 0) {
            throw new IllegalArgumentException("minRandomMillis는 0 이상이어야 한다. minRandomMillis=%d"
                    .formatted(minRandomMillis));
        }
        if (maxRandomMillis < 0) {
            throw new IllegalArgumentException("maxRandomMillis는 0 이상이어야 한다. maxRandomMillis=%d"
                    .formatted(maxRandomMillis));
        }
        if (minRandomMillis > maxRandomMillis) {
            throw new IllegalArgumentException("minRandomMillis는 maxRandomMillis 이하이어야 한다. min=%d, max=%d"
                    .formatted(minRandomMillis, maxRandomMillis));
        }
    }

    /**
     * 랜덤 딜레이를 포함한 실제 클릭 간격(ms)을 계산한다.
     *
     * @param random 랜덤 소스(테스트 재현성을 위해 외부 주입)
     * @return 실제 클릭 간격(ms)
     * @throws NullPointerException random이 null인 경우
     */
    public long resolveDelayMillis(Random random) {
        Objects.requireNonNull(random, "random");

        long randomDelay = nextLongInclusive(random, minRandomMillis, maxRandomMillis);
        return baseIntervalMillis + randomDelay;
    }

    private static long nextLongInclusive(Random random, long minInclusive, long maxInclusive) {
        if (minInclusive == maxInclusive) {
            return minInclusive;
        }

        // 역할: [min..max] 범위 long 랜덤 선택 (overflow 회피)
        long bound = (maxInclusive - minInclusive) + 1;
        long r = random.nextLong(bound);
        return minInclusive + r;
    }
}
