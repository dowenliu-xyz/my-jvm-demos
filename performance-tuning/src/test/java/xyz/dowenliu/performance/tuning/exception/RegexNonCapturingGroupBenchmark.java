package xyz.dowenliu.performance.tuning.exception;

import org.junit.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <p>create at 2020/3/16</p>
 *
 * @author liufl
 * @since 1.0
 */
public class RegexNonCapturingGroupBenchmark {
    private static final String text = "<input high=\"20\" weight=\"70\">test</input>";
    private static final int LOOPS = 100_000;

    @Test
    public void test() {
        final Instant instant1 = Instant.now();
        for (int i = 0; i < LOOPS; i++) {
        final Pattern capturing = Pattern.compile("(<input.*?)(.*?)(</input>)");
            final Matcher matcher = capturing.matcher(text);
            assertThat(matcher.groupCount()).isEqualTo(3);
        }
        final Duration capturingCost = Duration.between(instant1, Instant.now());
        System.out.println("捕获嵌套 " + LOOPS + " 次耗时 " + capturingCost);
        final Instant instant2 = Instant.now();
        for (int i = 0; i < LOOPS; i++) {
            final Pattern capturing = Pattern.compile("(?:<input.*?)(.*?)(?:</input>)");
            final Matcher matcher = capturing.matcher(text);
            assertThat(matcher.groupCount()).isEqualTo(1);
        }
        final Duration nonCapturingCost = Duration.between(instant2, Instant.now());
        System.out.println("非捕获嵌套 " + LOOPS + " 次耗时 " + nonCapturingCost);
        assertThat(nonCapturingCost.compareTo(capturingCost)).isLessThan(0);
    }
}
