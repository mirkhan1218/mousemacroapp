package com.preview.mousemacroapp.domain;

import java.util.Objects;
import java.util.Random;

/**
 * 정확 좌표 클릭 정책이다.
 *
 * @since 0.3
 */
public final class ExactPositionPolicy implements ClickPositionPolicy {

    @Override
    public ScreenPoint resolve(ScreenPoint base, Random random) {
        Objects.requireNonNull(base, "base");
        Objects.requireNonNull(random, "random");
        // 역할: 기준 좌표를 그대로 반환
        return base;
    }
}
