package com.preview.mousemacroapp.service;

import java.util.Objects;
import java.util.Optional;

/**
 * 캡처 결과 모델.
 *
 * <p><b>역할:</b></p>
 * <ul>
 *     <li>성공/취소/타임아웃/실패를 명시적으로 구분한다.</li>
 *     <li>성공일 때만 값이 존재한다.</li>
 * </ul>
 */
public final class CaptureResult<T> {

    public enum Status {
        CAPTURED,
        CANCELLED,
        TIMEOUT,
        FAILED
    }

    private final Status status;
    private final T value;
    private final String reason;

    private CaptureResult(Status status, T value, String reason) {
        this.status = Objects.requireNonNull(status, "status");
        this.value = value;
        this.reason = reason;
    }

    public static <T> CaptureResult<T> captured(T value) {
        return new CaptureResult<>(Status.CAPTURED, Objects.requireNonNull(value, "value"), null);
    }

    public static <T> CaptureResult<T> cancelled() {
        return new CaptureResult<>(Status.CANCELLED, null, null);
    }

    public static <T> CaptureResult<T> timeout() {
        return new CaptureResult<>(Status.TIMEOUT, null, null);
    }

    public static <T> CaptureResult<T> failed(String reason) {
        return new CaptureResult<>(Status.FAILED, null, reason);
    }

    public Status status() {
        return status;
    }

    public Optional<T> value() {
        return Optional.ofNullable(value);
    }

    public Optional<String> reason() {
        return Optional.ofNullable(reason);
    }
}
