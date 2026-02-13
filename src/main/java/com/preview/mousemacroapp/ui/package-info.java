/**
 * UI(표현) 계층.
 *
 * <p>
 * 화면 구성과 사용자 입력 이벤트를 담당한다.
 * </p>
 *
 * <p><b>제약</b></p>
 * <ul>
 *   <li>비즈니스 로직을 포함하지 않는다.</li>
 *   <li>상태 전이/스레드 관리는 {@code service} 계층에 위임한다.</li>
 *   <li>UI는 {@code MacroService}만 호출하도록 한다.</li>
 * </ul>
 */
package com.preview.mousemacroapp.ui;
