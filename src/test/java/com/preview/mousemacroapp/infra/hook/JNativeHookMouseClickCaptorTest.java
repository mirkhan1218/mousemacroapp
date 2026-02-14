package com.preview.mousemacroapp.infra.hook;

import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;
import com.preview.mousemacroapp.domain.point.ScreenPoint;
import com.preview.mousemacroapp.service.CaptureResult;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JNativeHookMouseClickCaptor 계약 테스트.
 *
 * <p>역할:</p>
 * - OS/라이브러리 의존을 테스트로 끌어오지 않기 위해 Fake Facade로 동작을 검증한다.
 * - 캡처 성공/취소/타임아웃의 상태를 명시적으로 보장한다.</p>
 */
class JNativeHookMouseClickCaptorTest {

    @Test
    void captureNextClick_whenClicked_thenCapturedAndListenerRemoved() throws Exception {
        FakeFacade facade = new FakeFacade();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        try {
            JNativeHookMouseClickCaptor captor = new JNativeHookMouseClickCaptor(facade, scheduler);

            CompletableFuture<CaptureResult<ScreenPoint>> future =
                    captor.captureNextClick(Duration.ofSeconds(2));

            // 역할: 리스너가 등록되었는지 확인한 뒤 클릭을 시뮬레이션한다.
            NativeMouseListener listener = awaitNonNull(() -> facade.mouseListener, 500);

            listener.nativeMouseClicked(createNativeMouseEvent(123, 456));

            CaptureResult<ScreenPoint> result = future.get(1, TimeUnit.SECONDS);
            assertEquals(CaptureResult.Status.CAPTURED, result.status());
            assertEquals(new ScreenPoint(123, 456), result.value().orElseThrow());

            assertEquals(1, facade.registerCallCount);
            assertEquals(1, facade.addMouseListenerCallCount);
            assertEquals(1, facade.removeMouseListenerCallCount);
        } finally {
            scheduler.shutdownNow();
        }
    }

    @Test
    void captureNextClick_whenCancelled_thenCancelledAndListenerRemoved() throws Exception {
        FakeFacade facade = new FakeFacade();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        try {
            JNativeHookMouseClickCaptor captor = new JNativeHookMouseClickCaptor(facade, scheduler);

            CompletableFuture<CaptureResult<ScreenPoint>> future =
                    captor.captureNextClick(Duration.ofSeconds(2));

            awaitNonNull(() -> facade.mouseListener, 500);

            captor.cancel();

            CaptureResult<ScreenPoint> result = future.get(1, TimeUnit.SECONDS);
            assertEquals(CaptureResult.Status.CANCELLED, result.status());

            assertEquals(1, facade.registerCallCount);
            assertEquals(1, facade.addMouseListenerCallCount);
            assertEquals(1, facade.removeMouseListenerCallCount);
        } finally {
            scheduler.shutdownNow();
        }
    }

    @Test
    void captureNextClick_whenTimeout_thenTimeoutAndListenerRemoved() throws Exception {
        FakeFacade facade = new FakeFacade();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        try {
            JNativeHookMouseClickCaptor captor = new JNativeHookMouseClickCaptor(facade, scheduler);

            CompletableFuture<CaptureResult<ScreenPoint>> future =
                    captor.captureNextClick(Duration.ofMillis(100));

            CaptureResult<ScreenPoint> result = future.get(1, TimeUnit.SECONDS);
            assertEquals(CaptureResult.Status.TIMEOUT, result.status());

            assertEquals(1, facade.registerCallCount);
            assertEquals(1, facade.addMouseListenerCallCount);
            assertEquals(1, facade.removeMouseListenerCallCount);
        } finally {
            scheduler.shutdownNow();
        }
    }

    private static NativeMouseEvent createNativeMouseEvent(int x, int y) {
        // 역할: 라이브러리 버전별 생성자 시그니처 차이를 흡수하기 위해
        //       "생성 → getX/getY 검증" 방식으로 안전하게 이벤트를 구성한다.
        try {
            for (Constructor<?> c : NativeMouseEvent.class.getConstructors()) {
                Class<?>[] p = c.getParameterTypes();

                // int 파라미터 인덱스 목록 수집
                int[] intIdx = new int[p.length];
                int intCnt = 0;
                for (int i = 0; i < p.length; i++) {
                    if (p[i] == int.class || p[i] == Integer.class) {
                        intIdx[intCnt++] = i;
                    }
                }

                // x,y 후보쌍을 모두 시도
                for (int a = 0; a < intCnt; a++) {
                    for (int b = 0; b < intCnt; b++) {
                        if (a == b) continue;

                        Object[] args = new Object[p.length];
                        for (int i = 0; i < p.length; i++) {
                            if (p[i] == int.class || p[i] == Integer.class) {
                                args[i] = 0;
                            } else if (p[i] == long.class || p[i] == Long.class) {
                                args[i] = 0L;
                            } else if (p[i] == short.class || p[i] == Short.class) {
                                args[i] = (short) 0;
                            } else if (p[i] == boolean.class || p[i] == Boolean.class) {
                                args[i] = false;
                            } else {
                                args[i] = null;
                            }
                        }

                        args[intIdx[a]] = x;
                        args[intIdx[b]] = y;

                        NativeMouseEvent e = (NativeMouseEvent) c.newInstance(args);
                        if (e.getX() == x && e.getY() == y) {
                            return e;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            // 아래에서 실패로 처리한다.
        }

        throw new IllegalStateException(
                "NativeMouseEvent 생성에 실패했다. (생성자 시그니처/좌표 매핑 확인 필요)"
        );
    }

    private static <T> T awaitNonNull(Callable<T> supplier, long maxWaitMillis) throws Exception {
        long deadline = System.currentTimeMillis() + maxWaitMillis;
        while (System.currentTimeMillis() < deadline) {
            T v = supplier.call();
            if (v != null) {
                return v;
            }
            Thread.sleep(10);
        }
        return null;
    }

    /**
     * 테스트 전용 JNativeHookFacade.
     *
     * <p>역할:</p>
     * - 전역 등록/리스너 등록/해제를 호출 횟수로 검증한다.
     * - 등록된 마우스 리스너를 테스트가 직접 호출할 수 있도록 노출한다.</p>
     */
    private static final class FakeFacade implements JNativeHookFacade {

        private volatile NativeMouseListener mouseListener;

        private int registerCallCount;
        private int addMouseListenerCallCount;
        private int removeMouseListenerCallCount;

        @Override
        public void register() throws NativeHookException {
            registerCallCount++;
        }

        @Override
        public void unregister() throws NativeHookException {
            // 이번 캡처 계약에서는 unregister를 강제하지 않는다.
        }

        @Override
        public void addKeyListener(com.github.kwhat.jnativehook.keyboard.NativeKeyListener listener) {
            // 캡처 테스트에서는 키 리스너를 다루지 않는다.
        }

        @Override
        public void removeKeyListener(com.github.kwhat.jnativehook.keyboard.NativeKeyListener listener) {
            // 캡처 테스트에서는 키 리스너를 다루지 않는다.
        }

        @Override
        public void addMouseListener(NativeMouseListener listener) {
            Objects.requireNonNull(listener, "listener");
            addMouseListenerCallCount++;
            mouseListener = listener;
        }

        @Override
        public void removeMouseListener(NativeMouseListener listener) {
            Objects.requireNonNull(listener, "listener");
            removeMouseListenerCallCount++;
            if (mouseListener == listener) {
                mouseListener = null;
            }
        }

        @Override
        public boolean isRegistered() {
            return true;
        }
    }
}
