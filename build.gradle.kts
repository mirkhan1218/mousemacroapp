plugins {
    id("java")
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "com.preview"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    // JavaFX Application 진입점
    mainClass.set("com.preview.mousemacroapp.Main")
}

javafx {
    version = "21"
    modules = listOf(
        "javafx.controls"
    )
}

dependencies {
    // 디버그 모드에서 전역 키 입력을 관찰하기 위한 글로벌 훅 라이브러리
    // Maven Central 기준 2.2.2 버전 사용. :contentReference[oaicite:0]{index=0}
    implementation("com.github.kwhat:jnativehook:2.2.2")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.withType<JavaExec>().configureEach {
    jvmArgs("-Dfile.encoding=UTF-8", "-Dsun.stdout.encoding=UTF-8", "-Dsun.stderr.encoding=UTF-8")
}

tasks.withType<Javadoc>().configureEach {
    // 개발 규칙: DocLint strict + -Werror (경고도 실패 처리)
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).apply {
        addBooleanOption("Xdoclint:all", true)
        addBooleanOption("Werror", true)
        // JavaFX 쪽 경고를 피하려고 하지 말고, 우리 코드의 Javadoc을 엄격히 유지한다.
    }
}
