package com.preview.mousemacroapp.domain.action.policy;

import com.preview.mousemacroapp.domain.point.ScreenPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 클릭 위치 결정 정책(Where) 검증 테스트.
 *
 * <p><b>테스트 대상</b></p>
 * - ClickPositionPolicy 및 구현체(ExactPositionPolicy, RandomAreaPositionPolicy)
 *
 * <p><b>검증 목적</b></p>
 * - 탐지 방지(RandomArea) 정책이 지정 범위를 벗어나지 않음을 보장한다.
 * - 음수 영역 입력을 생성 시점에 차단한다.
 * - 정확 좌표(Exact) 정책이 항상 기준 좌표를 반환함을 고정한다.
 *
 * <p><b>검증 범위</b></p>
 * - 생성 제약(halfWidth/halfHeight 음수)
 * - zero area(0,0) == 기준 좌표 반환
 * - random area 결과 범위 유지
 * - null base/random 방어
 *
 * <p><b>회귀 방지 이유</b></p>
 * - 좌표 정책 오류는 실제 클릭 좌표가 흔들려 오동작/탐지 위험을 증가시킨다.
 *
 * @since 0.3
 */
class ClickPositionPolicyTest {

    /*
     * 시나리오: 음수 영역 값은 생성 시점에 차단해야 한다
     *
     * 입력(Given):
     * - new RandomAreaPositionPolicy(-1, 0)
     * - new RandomAreaPositionPolicy(0, -5)
     *
     * 예상 결과(Then):
     * - IllegalArgumentException 발생
     */
    @Test
    @DisplayName("영역 값이 음수이면 예외 발생")
    void negativeArea_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> new RandomAreaPositionPolicy(-1, 0));

        assertThrows(IllegalArgumentException.class,
                () -> new RandomAreaPositionPolicy(0, -5));
    }

    /*
     * 시나리오: 영역 값이 0이면 기준 좌표 그대로 반환해야 한다
     *
     * 입력(Given):
     * - base = (100, 200)
     * - policy = RandomAreaPositionPolicy(0, 0)
     * - random = new Random(1)
     *
     * 예상 결과(Then):
     * - resolve(base, random) == base
     */
    @Test
    @DisplayName("영역이 0이면 항상 기준 좌표 반환")
    void zeroArea_shouldReturnBase() {
        ScreenPoint base = new ScreenPoint(100, 200);
        ClickPositionPolicy policy = new RandomAreaPositionPolicy(0, 0);

        Random random = new Random(1);

        ScreenPoint resolved = policy.resolve(base, random);

        assertEquals(base, resolved);
    }

    /*
     * 시나리오: 랜덤 영역 좌표는 지정 범위를 벗어나지 않아야 한다
     *
     * 입력(Given):
     * - base = (500, 500)
     * - halfWidth = 10  → X 허용 범위: [490..510]
     * - halfHeight = 20 → Y 허용 범위: [480..520]
     * - policy = RandomAreaPositionPolicy(10, 20)
     * - random = new Random(123)
     * - 반복 횟수 = 1000
     *
     * 예상 결과(Then):
     * - 모든 반복에서 resolved.x ∈ [490..510]
     * - 모든 반복에서 resolved.y ∈ [480..520]
     */
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

    /*
     * 시나리오: 극소 랜덤 영역(±1)에서도 범위를 벗어나지 않아야 한다
     *
     * 입력(Given):
     * - base = (0, 0)
     * - halfWidth = 1  → X 허용 범위: [-1..1]
     * - halfHeight = 1 → Y 허용 범위: [-1..1]
     * - policy = RandomAreaPositionPolicy(1, 1)
     * - random = new Random(7)
     * - 반복 횟수 = 1000
     *
     * 예상 결과(Then):
     * - 모든 반복에서 resolved.x ∈ [-1..1]
     * - 모든 반복에서 resolved.y ∈ [-1..1]
     */
    @Test
    @DisplayName("경계값: halfWidth=1, halfHeight=1에서도 범위를 벗어나지 않는다")
    void minimalArea_shouldStayWithinRange() {
        ScreenPoint base = new ScreenPoint(0, 0);
        int halfWidth = 1;
        int halfHeight = 1;

        ClickPositionPolicy policy = new RandomAreaPositionPolicy(halfWidth, halfHeight);
        Random random = new Random(7);

        for (int i = 0; i < 1000; i++) {
            ScreenPoint resolved = policy.resolve(base, random);

            assertTrue(resolved.x() >= -1);
            assertTrue(resolved.x() <= 1);
            assertTrue(resolved.y() >= -1);
            assertTrue(resolved.y() <= 1);
        }
    }

    /*
     * 시나리오: 정확 좌표 정책은 항상 기준 좌표를 반환해야 한다
     *
     * 입력(Given):
     * - base = (7, 9)
     * - policy = new ExactPositionPolicy()
     * - random = new Random(999)
     *
     * 예상 결과(Then):
     * - resolve(base, random) == base
     */
    @Test
    @DisplayName("정확 좌표 정책은 항상 기준 좌표를 반환")
    void exactPolicy_shouldReturnBase() {
        ScreenPoint base = new ScreenPoint(7, 9);
        ClickPositionPolicy policy = new ExactPositionPolicy();

        ScreenPoint resolved = policy.resolve(base, new Random(999));

        assertEquals(base, resolved);
    }
}
