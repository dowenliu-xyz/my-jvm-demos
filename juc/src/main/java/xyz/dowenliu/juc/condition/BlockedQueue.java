package xyz.dowenliu.juc.condition;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 一个简单的阻塞队列
 * <p>create at 2019/10/23</p>
 *
 * @author liufl
 */
public class BlockedQueue<E> {
    private final LinkedList<E> container = new LinkedList<>(); // 实际存放元素 E 的容器
    private final int capacity; // 阻塞队列的容量上限
    private final Lock lock = new ReentrantLock(); // 用于保护窗口 container 的锁
    // 条件变量：队列未满
    private final Condition notFull = lock.newCondition();
    // 条件变量：队列非空
    private final Condition notEmpty = lock.newCondition();

    public BlockedQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("阻塞队列容量必须大于0");
        }
        this.capacity = capacity;
    }

    public BlockedQueue() {
        this(Integer.MAX_VALUE);
    }

    /**
     * 入队操作。如果队列容量已满则线程阻塞，等待线程未满信号或中断信号
     * @param e 入队元素
     * @throws NullPointerException 如果 {@code e} 为 {@code null}
     */
    public void enqueue(E e) {
        if (e == null) {
            throw new NullPointerException();
        }
        lock.lock();
        try {
            while (container.size() >= capacity) {
                if (Thread.interrupted()) {
                    return; // 响应中断
                }
                // 等待队列不满信号
                try {
                    notFull.await();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt(); // 重置中断标识以供检测
                }
            }
            container.addLast(e);
            // 入队后，通知队列非空可出队
            notEmpty.signalAll(); // 同 notify()、notifyAll() 类似，尽可能使用 signalAll()
        } finally {
            lock.unlock(); // 确保锁被释放
        }
    }

    public E dequeue() {
        lock.lock();
        try {
            while (container.isEmpty()) {
                if (Thread.interrupted()) {
                    return null; // 响应中断
                }
                // 等待队列不空信号
                try {
                    notEmpty.await();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt(); // 重置中断标识以供检测
                }
            }
            E first = container.pollFirst(); // 只有当前线程能操作 container ，可断言 first != null
            // 出队后，通知队列不满可入队
            notFull.signalAll();
            return first;
        } finally {
            lock.unlock(); // 确保锁被释放
        }
    }
}
