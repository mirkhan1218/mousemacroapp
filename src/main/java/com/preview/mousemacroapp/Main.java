package com.preview.mousemacroapp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * MouseMacroApp의 애플리케이션 진입점이다.
 * <p>
 * Stage 0에서는 JavaFX 런타임/Gradle 실행 정합성만 확보한다.
 * 실제 UI/상태 흐름/기능 구현은 이후 Stage에서 진행한다.
 * </p>
 *
 * @author preview
 * @since 0.1
 */
public final class Main extends Application {

    /**
     * JavaFX Application 런칭 진입점이다.
     *
     * @param args 실행 인자
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * JavaFX Stage 초기 화면을 구성한다.
     * <p>
     * [추가]
     * Stage 0: "실행 가능" 상태를 검증하기 위한 최소 UI.
     * </p>
     *
     * @param primaryStage 기본 Stage
     */
    @Override
    public void start(Stage primaryStage) {
        // 역할: 실행 확인을 위한 최소 Scene 구성 (기능 UI는 Stage 1부터)
        Label label = new Label("MouseMacroApp - Stage 0 (JavaFX 실행 확인)");
        StackPane root = new StackPane(label);

        primaryStage.setTitle("MouseMacroApp");
        primaryStage.setScene(new Scene(root, 520, 180));
        primaryStage.show();
    }
}
