package com.preview.mousemacroapp.domain.action;

/**
 * 마우스 버튼 종류를 정의한다.
 *
 * <p>
 * Infra 계층(AWT Robot 등)에서 OS 입력 마스크로 변환되어 사용된다.
 * </p>
 *
 * @since 0.4
 */
public enum MouseButton {

    /**
     * 좌클릭 버튼.
     */
    LEFT,

    /**
     * 가운데(휠) 버튼.
     */
    MIDDLE,

    /**
     * 우클릭 버튼.
     */
    RIGHT
}
