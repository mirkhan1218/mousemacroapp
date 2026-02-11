package com.preview.mousemacroapp.domain;

/**
 * 화면 상의 좌표를 표현하는 값 객체이다.
 * <p>
 * 도메인 계층에서 AWT/JavaFX 타입 의존을 피하기 위해 별도 타입으로 정의한다.
 * </p>
 *
 * @param x 화면 X 좌표
 * @param y 화면 Y 좌표
 * @since 0.3
 */
public record ScreenPoint(int x, int y) {
}
