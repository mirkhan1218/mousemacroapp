/**
 * 네이티브 훅(JNativeHook) 연동 계층이다.
 * <p>
 * 훅 등록/해제 실패 가능성을 전제로 설계하며,
 * 실패 시 프로그램을 강제 종료하지 않고 사용자에게 안내한다.
 * </p>
 *
 * @since 0.1
 */
package com.preview.mousemacroapp.infra.hook;
