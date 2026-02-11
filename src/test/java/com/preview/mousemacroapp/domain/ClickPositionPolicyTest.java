package com.preview.mousemacroapp.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 클릭 위치 결정 정책(Where) 테스트.
 *
 * @since 0.3
 */
class ClickPositionPolicyTest {

    @Test
    @DisplayName("영역 값이 음수이면 예외 발생")
    void negativeArea_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> new RandomAreaPositionPolicy(-1, 0));

        assertThrows(IllegalArgumentException.class,
                () -> new RandomAreaPositionPolicy(0, -5));
    }

    @Test
    @DisplayName("영역이 0이면 항상 기준 좌표 반환")
    void zeroArea_shouldReturnBase() {
        ScreenPoint base = new ScreenPoint(100, 200);
        ClickPositionPolicy policy = new RandomAreaPositionPolicy(0, 0);

        Random random = new Random(1);

        ScreenPoint resolved = policy.resolve(base, random);

        assertEquals(base, resolved);
    }

    @Test
    @DisplayName("랜덤 영역이 지정 범위를 벗어나지 않아야 한다")
    void randomArea_shouldStayWithinRange() {
        ScreenPoint base = new ScreenPoint(500, 500);
        int halfWidth = 10;
        int halfHeight = 20;

        ClickPositionPolicy policy = new RandomAreaPositionPolicy(halfWidth, halfHeight);

        Random random = new Random(123);

        for (int i = 0; i < 1000; i++) {
            ScreenPoint resolved = policy.resolve(base, random);

            assertTrue(resolved.x() >= base.x() - halfWidth);
            assertTrue(resolved.x() <= base.x() + halfWidth);
            assertTrue(resolved.y() >= base.y() - halfHeight);
            assertTrue(resolved.y() <= base.y() + halfHeight);
        }
    }

    @Test
    @DisplayName("정확 좌표 정책은 항상 기준 좌표를 반환")
    void exactPolicy_shouldReturnBase() {
        ScreenPoint base = new ScreenPoint(7, 9);
        ClickPositionPolicy policy = new ExactPositionPolicy();

        ScreenPoint resolved = policy.resolve(base, new Random(999));

        assertEquals(base, resolved);
    }
}
