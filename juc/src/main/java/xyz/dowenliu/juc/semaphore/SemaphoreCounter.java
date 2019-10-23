package xyz.dowenliu.juc.semaphore;

import xyz.dowenliu.juc.counter.Counter;

import java.util.concurrent.Semaphore;

/**
 * 使用 {@link Semaphore} 实现的线程安全计数器
 * <p>create at 2019/10/23</p>
 *
 * @author liufl
 */
public class SemaphoreCounter implements Counter {
    private long value = 0L;
    private final Semaphore semaphore = new Semaphore(1);

    @Override
    public long get() {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            return value;
        } finally {
            semaphore.release();
        }
    }

    @Override
    public void addOne() {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            value += 1;
        } finally {
            semaphore.release();
        }
    }
}
