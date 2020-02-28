package xyz.dowenliu.juc.lock;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * 使用{@link java.util.concurrent.locks.AbstractQueuedSynchronizer}实现的
 * <em>不带超时</em>、<em>不可中断</em>、<em>不可重入</em>的非公平独占锁。
 * <p>create at 2020/2/28</p>
 *
 * @author liufl
 * @since 1.0
 */
public class SimpleAQSLock {
    /**
     * 实现 AQS 模板方法。实现独占模式对应的{@link #tryAcquire(int)}、
     * {@link #tryRelease(int)}方法即可。
     *
     * 实现为不可重入方案。state 为0 表示锁未被占用，state 为1 表示已被占用。
     */
    private static class Synchronizer extends AbstractQueuedSynchronizer {

        private static final long serialVersionUID = -515094610901287967L;

        @Override
        protected boolean isHeldExclusively() {
            return getExclusiveOwnerThread() == Thread.currentThread();
        }

        @Override
        protected boolean tryAcquire(int arg) {
            // 不管当前等待队列中有没有等待线程，尝试加锁。非公平
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        @Override
        protected boolean tryRelease(int arg) {
            // 保证只有加锁线程进行解锁
            if (Thread.currentThread() != getExclusiveOwnerThread())
                throw new IllegalMonitorStateException();
            // 解锁
            return compareAndSetState(1, 0);
        }
    }

    private final Synchronizer sync = new Synchronizer();

    public void lock() {
        this.sync.acquire(1);
    }

    public void unlock() {
        this.sync.release(1);
    }
}
