package com.preview.mousemacroapp.domain;

import java.time.LocalTime;
import java.util.Objects;

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
public record LocalTimeRange(
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
