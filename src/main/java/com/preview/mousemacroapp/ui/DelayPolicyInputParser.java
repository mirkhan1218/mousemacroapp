package com.preview.mousemacroapp.ui;

import com.preview.mousemacroapp.domain.timing.DelayPolicy;

/**
 * DelayPolicy용 UI 입력 파서.
 *
 * <p>
 * 역할:
 * <ul>
 *   <li>빈 값은 기본값으로 치환한다.</li>
 *   <li>숫자 형식/범위 오류를 사용자 메시지로 반환한다.</li>
 *   <li>min/max 관계(min <= max)를 강제한다.</li>
 * </ul>
 * </p>
 *
 * @since 0.6
 */
public final class DelayPolicyInputParser {

    /**
     * UI 입력이 비어있을 때 적용할 기본 클릭 간격(ms).
     */
    public static final long DEFAULT_BASE_INTERVAL_MILLIS = 300;

    private DelayPolicyInputParser() {
    }

    /**
     * UI 문자열 입력을 DelayPolicy로 변환한다.
     *
     * <p>정책:</p>
     * <ul>
     *   <li>base가 비어있으면 300ms</li>
     *   <li>min/max가 모두 비어있으면 0/0</li>
     *   <li>min만 있고 max가 비어있으면 max=min</li>
     *   <li>음수, 숫자 아님, min>max 는 오류</li>
     * </ul>
     *
     * @param baseRaw baseInterval 입력(공백 허용)
     * @param minRaw  minRandom 입력(공백 허용)
     * @param maxRaw  maxRandom 입력(공백 허용)
     * @return 파싱 결과
     */
    public static UiParseResult<DelayPolicy> parse(String baseRaw, String minRaw, String maxRaw) {
        String base = normalize(baseRaw);
        String min = normalize(minRaw);
        String max = normalize(maxRaw);

        long baseMillis = base.isEmpty()
                ? DEFAULT_BASE_INTERVAL_MILLIS
                : parseNonNegativeLong("기본 간격", base);

        if (baseMillis < 0) {
            return UiParseResult.error("기본 간격은 0 이상 숫자여야 한다.");
        }

        Long minMillisObj = min.isEmpty() ? null : parseNonNegativeLongOrNull("랜덤 최소", min);
        if (minMillisObj != null && minMillisObj < 0) {
            return UiParseResult.error("랜덤 최소는 0 이상 숫자여야 한다.");
        }

        Long maxMillisObj = max.isEmpty() ? null : parseNonNegativeLongOrNull("랜덤 최대", max);
        if (maxMillisObj != null && maxMillisObj < 0) {
            return UiParseResult.error("랜덤 최대는 0 이상 숫자여야 한다.");
        }

        long minMillis = (minMillisObj != null) ? minMillisObj : 0;
        long maxMillis = (maxMillisObj != null) ? maxMillisObj : 0;

        // 역할: min만 입력된 경우 사용성 편의로 max=min으로 보정한다.
        if (minMillisObj != null && maxMillisObj == null) {
            maxMillis = minMillis;
        }

        if (minMillis > maxMillis) {
            return UiParseResult.error("랜덤 최소는 랜덤 최대 이하이어야 한다.");
        }

        try {
            return UiParseResult.ok(new DelayPolicy(baseMillis, minMillis, maxMillis));
        } catch (IllegalArgumentException ex) {
            // 역할: 도메인 검증 메시지를 UI 메시지로 전달한다.
            return UiParseResult.error(ex.getMessage());
        }
    }

    private static String normalize(String raw) {
        return (raw == null) ? "" : raw.trim();
    }

    private static long parseNonNegativeLong(String label, String raw) {
        Long v = parseNonNegativeLongOrNull(label, raw);
        return (v != null) ? v : -1;
    }

    private static Long parseNonNegativeLongOrNull(String label, String raw) {
        try {
            long value = Long.parseLong(raw);
            if (value < 0) {
                return -1L;
            }
            return value;
        } catch (NumberFormatException ex) {
            return -1L;
        }
    }
}
