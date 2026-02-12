package com.preview.mousemacroapp.domain.timing;

import java.time.LocalTime;
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

    /**
     * 로컬 시간(LocalTime) 기준의 실행 범위를 정의한다.
     *
     * <p>
     * 자정(00:00)을 넘어가는 범위를 지원한다.
     * 예) 23:00 ~ 02:00
     * </p>
     *
     * <p>
     * 포함 규칙:
     * <ul>
     *   <li>시작(start) 시각은 포함(inclusive)</li>
     *   <li>종료(end) 시각은 제외(exclusive)</li>
     * </ul>
     * </p>
     *
     * @param startInclusive 시작 시각(포함)
     * @param endExclusive   종료 시각(제외)
     * @since 0.6
     */
    public static record LocalTimeRange(
            LocalTime startInclusive,
            LocalTime endExclusive
    ) {

        /**
         * 시간 범위 생성 시 입력 값을 검증한다.
         *
         * @throws NullPointerException     인자가 null인 경우
         * @throws IllegalArgumentException start와 end가 동일한 경우(길이 0 범위)
         */
        public LocalTimeRange {
            Objects.requireNonNull(startInclusive, "startInclusive");
            Objects.requireNonNull(endExclusive, "endExclusive");

            // 역할: 0 길이 범위는 실행 구간으로 의미가 없으므로 금지
            if (startInclusive.equals(endExclusive)) {
                throw new IllegalArgumentException("start와 end는 같을 수 없다. start=%s, end=%s"
                        .formatted(startInclusive, endExclusive));
            }
        }

        /**
         * 주어진 시각이 실행 범위에 포함되는지 판단한다.
         *
         * @param now 현재 시각
         * @return 포함되면 true
         * @throws NullPointerException now가 null인 경우
         */
        public boolean contains(LocalTime now) {
            Objects.requireNonNull(now, "now");

            // 역할: 일반 범위(자정 미통과) vs 자정 통과 범위를 분리 처리
            if (isOverMidnight()) {
                // 예) 23:00~02:00 -> [23:00..24:00) U [00:00..02:00)
                return !now.isBefore(startInclusive) || now.isBefore(endExclusive);
            }

            // 예) 09:00~18:00 -> [09:00..18:00)
            return !now.isBefore(startInclusive) && now.isBefore(endExclusive);
        }

        /**
         * 범위가 자정을 넘는지 여부를 반환한다.
         *
         * @return 자정 넘김이면 true
         */
        public boolean isOverMidnight() {
            return endExclusive.isBefore(startInclusive);
        }
    }
}
