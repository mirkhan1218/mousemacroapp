package com.preview.mousemacroapp.service;

import com.preview.mousemacroapp.domain.action.ClickAction;
import com.preview.mousemacroapp.domain.action.policy.ClickPositionPolicy;
import com.preview.mousemacroapp.domain.point.MacroPoint;
import com.preview.mousemacroapp.domain.schedule.ExecutionSchedule;
import com.preview.mousemacroapp.domain.timing.DelayPolicy;

import java.util.Objects;
import java.util.Random;

/**
 * 매크로 실행 요청(설정 묶음).
 *
 * <p>
 * UI 입력값을 Service가 소비 가능한 형태로 변환한 요청 모델이다.
 * 본 모델은 실행 정책(Where/How/When/TimeRange)을 한 번에 전달하는 목적을 가진다.
 * </p>
 *
 * <p>
 * 스케줄 미지정은 {@link ExecutionSchedule.Always}로 치환한다(Null Object).
 * Optional을 매개변수로 전달하지 않고, “항상 존재하는 정책” 형태로 단순화한다.
 * </p>
 *
 * @param macroPoint     기준 좌표
 * @param clickAction    클릭 동작 정책(How)
 * @param positionPolicy 실제 클릭 좌표 결정 정책(Where)
 * @param delayPolicy    클릭 간격 정책(When)
 * @param schedule       실행 가능 시간 정책(미지정 시 Always)
 * @param random         랜덤 소스(재현 가능한 테스트/디버그를 위해 외부 주입)
 * @param repeatCount    반복 횟수(0=무한, 1 이상=해당 횟수만 실행)
 * @since 0.6
 */
public record MacroRequest(
        MacroPoint macroPoint,
        ClickAction clickAction,
        ClickPositionPolicy positionPolicy,
        DelayPolicy delayPolicy,
        ExecutionSchedule schedule,
        Random random,
        int repeatCount
) {

    /**
     * 요청 불변식(Null 금지 및 반복 횟수 정책)을 강제한다.
     *
     * @throws NullPointerException     필드 중 하나라도 null인 경우
     * @throws IllegalArgumentException repeatCount가 음수인 경우
     * @since 0.6
     */
    public MacroRequest {
        Objects.requireNonNull(macroPoint, "macroPoint");
        Objects.requireNonNull(clickAction, "clickAction");
        Objects.requireNonNull(positionPolicy, "positionPolicy");
        Objects.requireNonNull(delayPolicy, "delayPolicy");
        Objects.requireNonNull(schedule, "schedule");
        Objects.requireNonNull(random, "random");

        // 역할: 0=무한, 1 이상=제한 반복. 음수는 UI/외부 입력 오류로 간주하여 거부한다.
        if (repeatCount < 0) {
            throw new IllegalArgumentException("repeatCount는 0 이상이어야 한다. repeatCount=" + repeatCount);
        }
    }

    /**
     * 스케줄이 null이면 {@link ExecutionSchedule.Always}로 치환하여 생성한다.
     *
     * @param macroPoint     기준 좌표
     * @param clickAction    클릭 동작 정책(How)
     * @param positionPolicy 실제 클릭 좌표 결정 정책(Where)
     * @param delayPolicy    클릭 간격 정책(When)
     * @param scheduleOrNull 스케줄(없으면 null)
     * @param random         랜덤 소스
     * @param repeatCount    반복 횟수(0=무한)
     * @return 실행 요청
     * @throws NullPointerException     macroPoint/clickAction/positionPolicy/delayPolicy/random이 null인 경우
     * @throws IllegalArgumentException repeatCount가 음수인 경우
     * @since 0.6
     */
    public static MacroRequest of(
            MacroPoint macroPoint,
            ClickAction clickAction,
            ClickPositionPolicy positionPolicy,
            DelayPolicy delayPolicy,
            ExecutionSchedule scheduleOrNull,
            Random random,
            int repeatCount
    ) {
        Objects.requireNonNull(macroPoint, "macroPoint");
        Objects.requireNonNull(clickAction, "clickAction");
        Objects.requireNonNull(positionPolicy, "positionPolicy");
        Objects.requireNonNull(delayPolicy, "delayPolicy");
        Objects.requireNonNull(random, "random");

        ExecutionSchedule schedule = (scheduleOrNull != null) ? scheduleOrNull : new ExecutionSchedule.Always();

        return new MacroRequest(
                macroPoint,
                clickAction,
                positionPolicy,
                delayPolicy,
                schedule,
                random,
                repeatCount
        );
    }
}
