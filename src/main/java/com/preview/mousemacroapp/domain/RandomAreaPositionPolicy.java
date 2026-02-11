package com.preview.mousemacroapp.domain;

import java.util.Objects;
import java.util.Random;

/**
 * 탐지 방지 모드: 기준 좌표 중심으로 지정된 영역 내에서 랜덤 좌표를 선택한다.
 *
 * <p>
 * halfWidth/halfHeight는 0 이상이어야 한다.
 * (0이면 정확 좌표와 동일한 결과 범위를 가진다)
 * </p>
 *
 * @param halfWidth  X 축 반폭(px)
 * @param halfHeight Y 축 반폭(px)
 * @since 0.3
 */
public record RandomAreaPositionPolicy(int halfWidth, int halfHeight) implements ClickPositionPolicy {

    /**
     * 탐지 방지 영역 정책을 생성한다.
     *
     * @throws IllegalArgumentException halfWidth 또는 halfHeight가 0 미만인 경우
     */
    public RandomAreaPositionPolicy {
        // 역할: 명세의 공통 제약(영역 값은 0 이상)을 도메인 생성 시점에 강제
        if (halfWidth < 0 || halfHeight < 0) {
            throw new IllegalArgumentException("영역 값은 0 이상이어야 한다. halfWidth=%d, halfHeight=%d"
                    .formatted(halfWidth, halfHeight));
        }
    }

    @Override
    public ScreenPoint resolve(ScreenPoint base, Random random) {
        Objects.requireNonNull(base, "base");
        Objects.requireNonNull(random, "random");

        // 역할: base를 중심으로 [-halfWidth..halfWidth], [-halfHeight..halfHeight] 범위에서 랜덤 선택
        int dx = nextInclusive(random, -halfWidth, halfWidth);
        int dy = nextInclusive(random, -halfHeight, halfHeight);

        return new ScreenPoint(base.x() + dx, base.y() + dy);
    }

    private static int nextInclusive(Random random, int minInclusive, int maxInclusive) {
        // min==max 케이스 포함
        int bound = (maxInclusive - minInclusive) + 1;
        return minInclusive + random.nextInt(bound);
    }
}
