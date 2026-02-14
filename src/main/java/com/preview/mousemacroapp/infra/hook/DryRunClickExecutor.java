package com.preview.mousemacroapp.infra.hook;

import com.preview.mousemacroapp.domain.action.ClickAction;
import com.preview.mousemacroapp.domain.point.ScreenPoint;
import com.preview.mousemacroapp.service.ClickExecutor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * 실제 마우스를 제어하지 않고, 클릭 요청을 로그로만 출력하는 실행기.
 *
 * <p>역할:</p>
 * <ul>
 *   <li>개발 초기 단계에서 사용자의 마우스 제어를 방해하지 않기 위해 "대체 실행"을 제공한다.</li>
 *   <li>Service/Domain 로직 검증(상태 전이, 루프 제어, 딜레이 등)은 유지한다.</li>
 *   <li>로그 타임스탬프는 한국 표준시(KST, Asia/Seoul) 기준으로 통일한다.</li>
 * </ul>
 */
public final class DryRunClickExecutor implements ClickExecutor {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");


    /*
     * 국제 표준 형태를 따르되, 한국 시간 표시를 위해 KST + 날짜 포함 형태로 고정한다.
     */

    private static final DateTimeFormatter KST_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final Clock clock;

    public DryRunClickExecutor(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public void execute(ClickAction action, ScreenPoint point) {
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(point, "point");

        // 역할: 시간 포맷은 사람이 비교하기 쉽도록 KST(Asia/Seoul)로 통일한다.
        Instant now = Instant.now(clock);
        String kstTime = ZonedDateTime.ofInstant(now, KST).format(KST_FORMAT);

        System.out.printf("[DRY-RUN] %s action=%s point=%s%n", kstTime, action, point);
    }
}
