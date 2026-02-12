package com.preview.mousemacroapp.domain;

import java.util.Objects;

/**
 * 클릭 동작 방식(How)을 표현하는 도메인 모델이다.
 *
 * <p>
 * 실행은 service 계층(Robot)에서 수행하며,
 * 도메인은 동작 종류/파라미터 유효성만 책임진다.
 * </p>
 *
 * @param button     클릭에 사용할 마우스 버튼
 * @param clickCount 클릭 횟수(단일/더블 등). 홀드의 경우 1로 고정한다.
 * @param holdMillis 홀드 시간(ms). 홀드가 아닌 경우 0으로 고정한다.
 * @since 0.4
 */
public record ClickAction(
        MouseButton button,
        int clickCount,
        long holdMillis
) {

    /**
     * 단일 좌클릭 동작을 생성한다.
     *
     * @return 단일 좌클릭 ClickAction
     */
    public static ClickAction singleLeft() {
        return new ClickAction(MouseButton.LEFT, 1, 0);
    }

    /**
     * 더블 좌클릭 동작을 생성한다.
     *
     * @return 더블 좌클릭 ClickAction
     */
    public static ClickAction doubleLeft() {
        return new ClickAction(MouseButton.LEFT, 2, 0);
    }

    /**
     * 우클릭 동작을 생성한다.
     *
     * @return 우클릭 ClickAction
     */
    public static ClickAction rightClick() {
        return new ClickAction(MouseButton.RIGHT, 1, 0);
    }

    /**
     * 홀드 클릭 동작을 생성한다.
     *
     * @param holdMillis 홀드 시간(ms). 0 이상이어야 한다.
     * @param button     홀드할 버튼(좌/우)
     * @return 홀드 ClickAction
     * @throws IllegalArgumentException holdMillis가 0 미만인 경우
     */
    public static ClickAction hold(MouseButton button, long holdMillis) {
        if (holdMillis < 0) {
            throw new IllegalArgumentException("holdMillis는 0 이상이어야 한다. holdMillis=%d"
                    .formatted(holdMillis));
        }
        return new ClickAction(button, 1, holdMillis);
    }

    /**
     * ClickAction 생성 시 입력 값을 검증한다.
     *
     * @throws NullPointerException     button이 null인 경우
     * @throws IllegalArgumentException clickCount가 1 미만인 경우
     * @throws IllegalArgumentException holdMillis가 0 미만인 경우
     */
    public ClickAction {
        Objects.requireNonNull(button, "button");

        // 역할: 클릭 횟수는 1 이상만 허용 (0회 클릭은 정책으로 의미 없음)
        if (clickCount < 1) {
            throw new IllegalArgumentException("clickCount는 1 이상이어야 한다. clickCount=%d"
                    .formatted(clickCount));
        }

        // 역할: 홀드 시간은 0 이상만 허용
        if (holdMillis < 0) {
            throw new IllegalArgumentException("holdMillis는 0 이상이어야 한다. holdMillis=%d"
                    .formatted(holdMillis));
        }

        // 역할: holdMillis가 0이 아닌 경우, clickCount는 1로 고정 (홀드는 '누르고-해제' 1회)
        if (holdMillis > 0 && clickCount != 1) {
            throw new IllegalArgumentException("홀드 동작은 clickCount=1 이어야 한다. clickCount=%d, holdMillis=%d"
                    .formatted(clickCount, holdMillis));
        }
    }
}
