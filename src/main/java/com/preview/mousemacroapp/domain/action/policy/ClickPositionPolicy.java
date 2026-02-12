package com.preview.mousemacroapp.domain.action.policy;

import com.preview.mousemacroapp.domain.point.ScreenPoint;

import java.util.Random;

/**
 * 클릭 위치 결정 정책(Where)을 정의한다.
 * <p>
 * 클릭 실행 시점마다 실제 클릭할 좌표를 결정한다.
 * </p>
 *
 * @since 0.3
 */
public interface ClickPositionPolicy {

    /**
     * 기준 좌표를 바탕으로 실제 클릭 좌표를 계산한다.
     *
     * @param base 기준 좌표
     * @param random 랜덤 소스(재현 가능한 테스트를 위해 외부 주입)
     * @return 실제 클릭 좌표
     * @throws NullPointerException 인자가 null인 경우
     */
    ScreenPoint resolve(ScreenPoint base, Random random);
}
