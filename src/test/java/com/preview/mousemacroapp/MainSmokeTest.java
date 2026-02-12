package com.preview.mousemacroapp;

import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Main 엔트리포인트(스모크) 테스트.
 *
 * <p><b>테스트 대상</b></p>
 * - Main 클래스의 실행 진입점(main, start)
 *
 * <p><b>검증 목적</b></p>
 * - Gradle run 및 JavaFX 라이프사이클 진입점 시그니처를 고정한다.
 *
 * <p><b>검증 범위</b></p>
 * - main(String[]) 존재 및 static 여부
 * - start(Stage) 존재 및 접근 제한자/반환 타입
 *
 * <p><b>회귀 방지 이유</b></p>
 * - 진입점 시그니처 변경 시 애플리케이션이 실행 불가능해지는 문제를 사전에 차단한다.
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
        // 역할: Gradle run에서 사용하는 main(String[]) 존재 여부 검증
        Method main = Main.class.getDeclaredMethod("main", String[].class);
        assertTrue(Modifier.isStatic(main.getModifiers()), "main 메서드는 static이어야 한다.");
        assertEquals(void.class, main.getReturnType(), "main 메서드는 void를 반환해야 한다.");
    }

    /*
     * 시나리오:
     * - JavaFX start(Stage) 시그니처 유지 확인
     *
     * 입력(Given):
     * - Main.class에서 "start(Stage)" 조회
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
        // 역할: JavaFX Application#start(Stage) 오버라이드 여부를 시그니처로 검증
        Method start = Main.class.getDeclaredMethod("start", Stage.class);

        assertEquals(void.class, start.getReturnType(), "start(Stage)는 void를 반환해야 한다.");
        assertTrue(Modifier.isPublic(start.getModifiers()), "start(Stage)는 public이어야 한다.");
        assertFalse(Modifier.isStatic(start.getModifiers()), "start(Stage)는 static이면 안 된다.");
    }
}
