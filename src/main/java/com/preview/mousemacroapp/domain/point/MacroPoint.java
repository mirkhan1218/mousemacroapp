package com.preview.mousemacroapp.domain.point;

import com.preview.mousemacroapp.domain.action.policy.ClickPositionPolicy;

import java.util.Objects;

/**
 * 좌표 리스트의 단일 항목(매크로 포인트)이다.
 * <p>
 * 각 항목은 기준 좌표와 클릭 위치 결정 정책(Where)을 가진다.
 * </p>
 *
 * @param name 사용자 표시명(빈 문자열 허용하지 않음)
 * @param base 기준 좌표
 * @param positionPolicy 클릭 위치 결정 정책
 * @since 0.3
 */
public record MacroPoint(
        String name,
        ScreenPoint base,
        ClickPositionPolicy positionPolicy
) {

    /**
     * 매크로 포인트 생성 시 입력 값을 검증한다.
     *
     * @throws NullPointerException 인자가 null인 경우
     * @throws IllegalArgumentException name이 비어있는 경우
     */
    public MacroPoint {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(base, "base");
        Objects.requireNonNull(positionPolicy, "positionPolicy");

        // 역할: 리스트 UX/표시에 필요한 최소 식별자 보장
        if (name.isBlank()) {
            throw new IllegalArgumentException("name은 비어 있을 수 없다.");
        }
    }
}
