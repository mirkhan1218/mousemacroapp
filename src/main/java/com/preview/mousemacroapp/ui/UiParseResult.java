package com.preview.mousemacroapp.ui;

import java.util.Optional;

/**
 * UI 입력 파싱 결과를 표현한다.
 *
 * <p>
 * 역할: UI 계층에서 발생한 입력 오류를 예외로 던지지 않고,
 * 사용자 메시지로 변환 가능한 형태로 반환한다.
 * </p>
 *
 * @param valueOrNull 값(성공 시 존재)
 * @param errorOrNull 오류 메시지(실패 시 존재)
 * @param <T>         결과 타입
 * @since 0.6
 */
public record UiParseResult<T>(
        T valueOrNull,
        String errorOrNull
) {

    public static <T> UiParseResult<T> ok(T value) {
        return new UiParseResult<>(value, null);
    }

    public static <T> UiParseResult<T> error(String message) {
        return new UiParseResult<>(null, message);
    }

    public boolean isOk() {
        return errorOrNull == null;
    }

    public Optional<T> value() {
        return Optional.ofNullable(valueOrNull);
    }

    public Optional<String> errorMessage() {
        return Optional.ofNullable(errorOrNull);
    }
}
