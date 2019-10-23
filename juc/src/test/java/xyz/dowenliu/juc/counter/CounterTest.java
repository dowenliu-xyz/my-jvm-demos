package xyz.dowenliu.juc.counter;

import org.junit.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <p>create at 2019/10/22</p>
 *
 * @author liufl
 */
public class CounterTest {
    private static void count(Counter counter, long times) {
        if (counter == null) {
            return;
        }
        if (times <= 0) {
            return;
        }
        for (int i = 0; i < times; i++) {
            counter.addOne();
        }
    }

    private static long randLong() {
        return (long) (new Random(System.currentTimeMillis()).nextDouble() * 10000L);
    }

    public static void testCounter(final Counter counter) throws InterruptedException {
        if (counter == null) {
            throw new NullPointerException("Counter is null!");
        }
        final long times1 = randLong();
        final long times2 = randLong();

        final Thread th1 = new Thread(() -> count(counter, times1));
        th1.start();
        final Thread th2 = new Thread(() -> count(counter, times2));
        th2.start();

        th1.join();
        th2.join();

        assertThat(counter.get()).isEqualTo(times1 + times2);
    }

    @Test
    public void testLockCounter() throws InterruptedException {
        testCounter(new LockCounter());
    }

    @Test
    public void testSyncCounter() throws InterruptedException {
        testCounter(new SyncCounter());
    }
}
