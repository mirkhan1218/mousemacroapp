/**
 * MouseMacroApp의 도메인 모델 루트 패키지이다.
 *
 * <p>
 * 도메인은 실행 상태(Status), 좌표(Point), 클릭 동작(Action),
 * 클릭 위치 정책(Where), 클릭 타이밍(When-interval),
 * 실행 가능 시간 범위(Schedule, When-window)로 개념 단위로 분리된다.
 * </p>
 *
 * <p>
 * 도메인 계층은 UI 및 Service 계층에 의존하지 않으며,
 * 순수한 정책과 제약만을 포함한다.
 * </p>
 */
package com.preview.mousemacroapp.domain;
