package com.preview.mousemacroapp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Main 엔트리포인트(스모크) 테스트.
 *
 * <p><b>테스트 대상</b></p>
 * - Main 클래스의 실행 진입점(main, start)
 *
 * <p><b>검증 목적</b></p>
 * - Gradle run 및 JavaFX 라이프사이클 진입점 시그니처를 고정한다.
 * - 도메인 패키지 구조 리팩토링 이후에도 주요 타입 로딩이 가능함을 보장한다.
 *
 * <p><b>검증 범위</b></p>
 * - main(String[]) 존재 및 static 여부
 * - start(Stage) 존재 및 접근 제한자/반환 타입
 * - 주요 도메인 타입 Class.forName 로딩 가능 여부
 *
 * <p><b>회귀 방지 이유</b></p>
 * - 진입점 시그니처 변경 시 애플리케이션이 실행 불가능해지는 문제를 사전에 차단한다.
 * - 패키지 리팩토링 중 클래스 경로가 끊기는 문제를 조기에 탐지한다.
 *
 * @since 0.1
 */
final class MainSmokeTest {

    /*
     * 시나리오:
     * - Main.main(String[]) 존재 및 static 여부 확인
     *
     * 입력(Given):
     * - Main.class에서 "main(String[])" 리플렉션 조회
     * - 대상 메서드: Main.main(String[])
     *
     * 예상 결과(Then):
     * - 메서드 존재
     * - static == true
     * - 반환 타입 == void
     */
    @Test
    void mainMethod_shouldExistAndBeStatic() throws Exception {
        var main = Main.class.getDeclaredMethod("main", String[].class);

        assertTrue(java.lang.reflect.Modifier.isStatic(main.getModifiers()), "main 메서드는 static이어야 한다.");
        assertEquals(void.class, main.getReturnType(), "main 메서드는 void를 반환해야 한다.");
    }

    /*
     * 시나리오:
     * - JavaFX start(Stage) 시그니처 유지 확인
     *
     * 입력(Given):
     * - Main.class에서 "start(javafx.stage.Stage)" 조회
     * - 대상 메서드: Main.start(Stage)
     *
     * 예상 결과(Then):
     * - 메서드 존재
     * - public == true
     * - static == false
     * - 반환 타입 == void
     */
    @Test
    void startMethod_shouldOverrideApplicationStart() throws Exception {
        var start = Main.class.getDeclaredMethod("start", javafx.stage.Stage.class);

        assertEquals(void.class, start.getReturnType(), "start(Stage)는 void를 반환해야 한다.");
        assertTrue(java.lang.reflect.Modifier.isPublic(start.getModifiers()), "start(Stage)는 public이어야 한다.");
        assertFalse(java.lang.reflect.Modifier.isStatic(start.getModifiers()), "start(Stage)는 static이면 안 된다.");
    }

    /*
     * 시나리오: 도메인 패키지 구조 리팩토링 이후에도 주요 타입이 정상 로딩되어야 한다
     *
     * 입력(Given):
     * - 각 도메인 타입의 FQCN 문자열 목록
     *
     * 예상 결과(Then):
     * - Class.forName(...)이 예외 없이 성공
     */
    @Test
    @DisplayName("도메인 주요 타입은 새 패키지 구조에서 정상 로딩되어야 한다")
    void domainTypes_shouldBeLoadableAfterPackageRefactor() throws Exception {
        String[] fqcn = {
                "com.preview.mousemacroapp.domain.status.MacroStatus",
                "com.preview.mousemacroapp.domain.point.ScreenPoint",
                "com.preview.mousemacroapp.domain.point.MacroPoint",
                "com.preview.mousemacroapp.domain.action.MouseButton",
                "com.preview.mousemacroapp.domain.action.ClickAction",
                "com.preview.mousemacroapp.domain.action.policy.ClickPositionPolicy",
                "com.preview.mousemacroapp.domain.action.policy.ExactPositionPolicy",
                "com.preview.mousemacroapp.domain.action.policy.RandomAreaPositionPolicy",
                "com.preview.mousemacroapp.domain.timing.DelayPolicy",
                "com.preview.mousemacroapp.domain.schedule.LocalTimeRange",
                "com.preview.mousemacroapp.domain.schedule.ExecutionSchedule"
        };

        for (String name : fqcn) {
            assertNotNull(Class.forName(name), "클래스 로딩 실패: " + name);
        }
    }
}
