/**
 * 클릭 위치 결정 정책(Where)을 제공한다.
 *
 * <p>
 * 기준 좌표를 실제 클릭 좌표로 변환하는 전략을 정의한다.
 * 예:
 * <ul>
 *     <li>ExactPositionPolicy : 정확 좌표</li>
 *     <li>RandomAreaPositionPolicy : 랜덤 영역</li>
 * </ul>
 * </p>
 *
 * <p>
 * 정책은 상태를 가지지 않으며, 순수 계산 로직만 포함한다.
 * </p>
 */
package com.preview.mousemacroapp.domain.action.policy;
