package com.preview.mousemacroapp;

import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stage 0: JavaFX 엔트리포인트 정합성(스모크) 테스트.
 * <p>
 * UI 자동화 대신, 실행 가능한 진입점 시그니처를 고정한다.
 * </p>
 *
 * @since 0.1
 */
final class MainSmokeTest {

    @Test
    void mainMethod_shouldExistAndBeStatic() throws Exception {
        // 역할: Gradle run에서 사용하는 main(String[]) 존재 여부 검증
        Method main = Main.class.getDeclaredMethod("main", String[].class);
        assertTrue(Modifier.isStatic(main.getModifiers()), "main 메서드는 static이어야 한다.");
        assertEquals(void.class, main.getReturnType(), "main 메서드는 void를 반환해야 한다.");
    }

    @Test
    void startMethod_shouldOverrideApplicationStart() throws Exception {
        // 역할: JavaFX Application#start(Stage) 오버라이드 여부를 시그니처로 검증
        Method start = Main.class.getDeclaredMethod("start", Stage.class);

        assertEquals(void.class, start.getReturnType(), "start(Stage)는 void를 반환해야 한다.");
        assertTrue(Modifier.isPublic(start.getModifiers()), "start(Stage)는 public이어야 한다.");
        assertFalse(Modifier.isStatic(start.getModifiers()), "start(Stage)는 static이면 안 된다.");
    }
}
