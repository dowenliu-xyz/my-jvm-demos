package xyz.dowenliu.juc.counter;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 使用 {@link Lock} 实现的线程安全 {@link Counter}
 * <br>
 * 不保证继承此类的子类线程安全。
 * <p>create at 2019/10/22</p>
 *
 * @author liufl
 */
public class LockCounter implements Counter {
    private long value = 0L;
    private final Lock lock = new ReentrantLock(); // 用于保护 value 的锁。

    @Override
    public long get() {
        // 阻塞式的获取锁
        lock.lock();
        try {
            return value;
        } finally { // 使用 try{}finally{} 范式确保锁能释放
            lock.unlock();
        }
    }

    @Override
    public void addOne() {
        // 阻塞式的获取锁
        lock.lock();
        try {
            value = get() + 1; // 因为 lock 是 ReentrantLock ，可重入，不会阻塞
            // 如果是使用 synchronized 方式加锁，则不可重入，会阻塞。
            // 此处为了展示 ReentrantLock 可重入的特性，才写成 value = get() + 1;
            // 实际多写作" value += 1; 或 value = value + 1;
        } finally { // 使用 try{}finally{} 范式确保锁能释放
            lock.unlock();
        }
    }
}
