package com.preview.mousemacroapp.service;

import com.preview.mousemacroapp.domain.point.ScreenPoint;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * 전역 마우스 클릭(1회)을 캡처하는 포트.
 *
 * <p><b>역할:</b></p>
 * <ul>
 *     <li>UI/Service는 OS 이벤트 구현에 의존하지 않는다.</li>
 *     <li>Infra는 이 포트를 구현하여 전역 클릭 좌표를 제공한다.</li>
 * </ul>
 */
public interface MouseClickCaptor {

    /**
     * 다음 마우스 클릭 1회를 캡처한다.
     *
     * @param timeout 제한 시간(초과 시 TIMEOUT)
     * @return 캡처 결과(성공 시 좌표 포함)
     */
    CompletableFuture<CaptureResult<ScreenPoint>> captureNextClick(Duration timeout);

    /**
     * 진행 중인 캡처를 취소한다.
     *
     * <p>역할:</p>
     * <ul>
     *     <li>UI에서 "취소" 버튼/ESC 입력 등을 통해 캡처를 중단할 수 있다.</li>
     * </ul>
     */
    void cancel();
}
