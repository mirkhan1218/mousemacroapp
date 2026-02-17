package com.preview.mousemacroapp.debug;

import java.util.Arrays;
import java.util.Objects;

/**
 * 디버그 모드 활성화 상태를 관리한다.
 *
 * <p>역할:</p>
 * - 실행 인자 {@code -debug}에 의해 디버그 모드를 활성화한다.
 * - 디버그 모드 판정 책임을 한 곳으로 집중하여 UI/Service/Infra에 파편화되지 않도록 한다.
 */
public final class DebugMode {

    private static volatile boolean enabled;

    private DebugMode() {
        // 유틸리티 클래스
    }

    /**
     * 실행 인자로부터 디버그 모드를 초기화한다.
     *
     * @param args 애플리케이션 실행 인자
     */
    public static void initialize(String[] args) {
        Objects.requireNonNull(args, "args");
        enabled = Arrays.stream(args).anyMatch(a -> "-debug".equals(a) || "--debug".equals(a));
    }

    /**
     * 디버그 모드 활성 여부를 반환한다.
     *
     * @return 활성화 여부
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * 테스트 전용 강제 설정.
     *
     * <p>역할: 전역 상태 유틸의 테스트 간 누수 방지.</p>
     */
    public static void setEnabledForTest(boolean value) {
        enabled = value;
    }
}
