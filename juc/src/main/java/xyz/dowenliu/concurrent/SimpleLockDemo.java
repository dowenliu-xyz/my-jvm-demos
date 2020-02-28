package xyz.dowenliu.concurrent;

import java.time.Duration;
import java.time.Instant;

/**
 * <p>create at 2020/2/28</p>
 *
 * @author liufl
 * @since 1.0
 */
public class SimpleLockDemo {
    private long count;
    private SimpleLock lock = new SimpleLock();

    private void countUp() {
        try {
            lock.lock();
            count++;
            // 增加并发竞争的机会
            try {
                Thread.sleep(0, 1);
            } catch (InterruptedException ignored) {
            }
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        final SimpleLockDemo demo = new SimpleLockDemo();
        final long loop1 = 1000;
        final long loop2 = 1000;
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < loop1; i++) {
                demo.countUp();
            }
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < loop2; i++) {
                demo.countUp();
            }
        });
        Instant start = Instant.now();
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        Duration cast = Duration.between(start, Instant.now());
        System.out.println(String.format("Expect count: %d, actual: %d", loop1 + loop2, demo.count));
        System.out.println(String.format("Total cast: %s", cast.toString()));
    }
}
