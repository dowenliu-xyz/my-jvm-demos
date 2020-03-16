package xyz.dowenliu.performance.tuning.exception;

import org.junit.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <p>create at 2020/3/16</p>
 *
 * @author liufl
 * @since 1.0
 */
public class RuntimeExceptionWithoutStackTraceBenchmark {
    private static final int LOOP_COUNT = 100_000;

    @Test
    public void test() {
        final Instant withStackTraceStart = Instant.now();
        for (int i = 0; i < LOOP_COUNT; i++) {
            final RuntimeException e = new RuntimeException(String.valueOf(i));
            assertThat(e.getStackTrace()).isNotEmpty();
        }
        final Duration withStackTraceCaost = Duration.between(withStackTraceStart, Instant.now());
        System.out.println("创建" + LOOP_COUNT  + "个运行时异常耗时：" + withStackTraceCaost);
        final Instant withoutStackTraceStart = Instant.now();
        for (int i = 0; i < LOOP_COUNT; i++) {
            final NoTraceRuntimeException e = new NoTraceRuntimeException(String.valueOf(i));
            assertThat(e.getStackTrace()).isEmpty();
        }
        final Duration withoutStackTraceCaost = Duration.between(withoutStackTraceStart, Instant.now());
        System.out.println("创建" + LOOP_COUNT + "个无异常栈运行时异常耗时：" + withoutStackTraceCaost);
        assertThat(withStackTraceCaost.compareTo(withoutStackTraceCaost)).isGreaterThan(0);
    }
}
