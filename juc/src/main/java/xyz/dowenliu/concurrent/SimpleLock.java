package xyz.dowenliu.concurrent;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * 使用{@link Unsafe} {@link Unsafe#park park}/{@link Unsafe#unpark unpark}
 * 实现的<em>不带超时</em>、<em>不可中断</em>、<em>不可重入</em>的非公平独占锁。
 * <p>
 * 本锁实现使用一个FIFO队列来实现线程等待、唤醒。
 * <p>create at 2020/2/27</p>
 *
 * @author liufl
 * @since 1.0
 */
public class SimpleLock {
    /**
     * 用于表示锁状态的状态变量。0为无锁，大于0为已加锁，不会小于0
     * 应该总是使用CAS操作来修改该状态。
     */
    private volatile long state = 0;
    /**
     * 持有锁的线程，用于unlock时判断是否应由当前线程解锁。
     */
    private volatile Thread holder;
    private final WaitingQueue queue = new WaitingQueue();

    /**
     * 加锁操作。一旦调用，在当前线程未成功获得锁之前，将一直阻塞，无法中断。
     * 本锁实现为不可重入锁，不要在 {@link #unlock} 之前再次调用，否则将导致死锁！
     * 在退出临界区时必须保证通过 {@link #unlock} 释放锁，否则（几乎一定）会导致死锁！
     *
     * @see #unlock()
     */
    public void lock() {
        if (casState(0, 1)) {
            this.holder = Thread.currentThread();
            // 成功获得锁
            return;
        }
        // 锁已经被其他线程占用，入队等待
        this.queue.enqueue();
        // 使用while而不是do-while，在 park 前再尝试一下占用锁，可能锁已经释放，
        // 这样可能能避免一次 park/unpark
        while (!casState(0, 1)) {
            // 没能占用锁 park 等待持有锁有线程在 unlock 时 unpark 当前线程
            park();
            // 在从 WAITING 状态被唤醒后，这里可以检查下线程中断状态。进行中断处理。
            // 此版本从简，不处理中断。
        }
        // 当前线程已获取锁。从等待队列中移除。
        this.queue.pop();
        this.holder = Thread.currentThread();
    }

    public void unlock() throws IllegalMonitorStateException {
        // 只能由加锁线程解锁
        if (this.holder != Thread.currentThread())
            throw new IllegalMonitorStateException();
        // 解锁以供其他线程竞争
        casState(1, 0); // 因为是加锁的线程操作，必定成功
        // 唤醒等待队列后继竞争锁
        WaitingQueue.Node next = this.queue.head.next;
        if (next != null) {
            unpart(next.thread);
        }
    }

    // 这个方法和 LockSupport.park() 操作完全相同
    private static void park() {
        UnsafeProvider.getUnsafe().park(false, 10_000_000L);
    }

    // 这个方法和 LockSupport.unpark() 操作完全相同
    private static void unpart(Thread thread) {
        UnsafeProvider.getUnsafe().unpark(thread);
    }

    // CAS操作：start
    /**
     * state字段offset,用于CAS操作
     */
    private static final long STATE;

    static {
        try {
            STATE = UnsafeProvider.getUnsafe().objectFieldOffset(
                    SimpleLock.class.getDeclaredField("state"));
        } catch (Exception ex) {
            throw new Error(ex);
        }
    }

    private boolean casState(long expected, long newState) {
        return UnsafeProvider.getUnsafe().compareAndSwapLong(
                this, STATE, expected, newState);
    }

    // CAS操作：end

    /**
     * 获取{@link Unsafe}实现的工具类。
     * 因为{@link Unsafe#getUnsafe()}方法对调用者敏感，只允许系统类加载器加载的类调用它。
     * 这个工具类使用反射获取{@link Unsafe}类中已初始化好的静态{@link Unsafe}实例。
     */
    private static class UnsafeProvider {
        private static final Unsafe UNSAFE;

        static {
            try {
                Class<Unsafe> unsafeClass = Unsafe.class;
                Field theUnsafe = unsafeClass.getDeclaredField("theUnsafe");
                theUnsafe.setAccessible(true);
                UNSAFE = (Unsafe) theUnsafe.get(null);
            } catch (Exception ex) {
                throw new Error(ex);
            }
        }

        public static Unsafe getUnsafe() {
            return UNSAFE;
        }
    }

    /**
     * 等待队列。入队和出队需要保证互斥。这里使用自旋锁。
     */
    private static class WaitingQueue {
        /**
         * 用于保证互斥操作的变量，只能使用CAS操作修改。
         */
        private volatile long state = 0;

        /**
         * 队列节点
         */
        static class Node {
            /**
             * 等待队列节点对应的线程。
             */
            volatile Thread thread;
            /**
             * 等待队列节点后继。如果为 null 说明当前节点后没有线程等待了。
             */
            volatile Node next;

            Node(Thread thread) {
                this.thread = thread;
            }
        }

        /**
         * 无意义的head节点，只是为了方便操作队列。
         */
        private Node head = new Node(null);
        /**
         * tail节点，方便进行新节点入队。
         */
        private volatile Node tail = head;

        /**
         * 入队。
         */
        public void enqueue() {
            for (; ; ) { // 自旋
                // 如果成功修改状态变量，则表示获取锁，其他竞争线程只能自旋等待
                if (casState(0, 1)) {
                    // 独占操作
                    Node newTail = new Node(Thread.currentThread());
                    Node t = this.tail;
                    t.next = newTail;
                    this.tail = newTail;
                    // 操作后解锁
                    casState(1, 0);
                    // 解锁后退出自旋
                    return;
                }
            }
        }

        /**
         * 出队
         */
        public void pop() {
            /*
            for (; ; ) {
                if (casState(0, 1)) {
                    Node h = this.head;
                    if (h.next == null)
                        throw new IllegalMonitorStateException();
                    this.head = h.next;
                    this.head.thread = null;
                    h.next = null; // help GC
                    casState(1, 0);
                    return;
                }
            }
            /*/
            // 按调用来看，此操作只会在线程独占锁之后操作，应该不必在自旋中操作。
            Node h = this.head;
            if (h.next == null)
                throw new IllegalMonitorStateException();
            this.head = h.next;
            this.head.thread = null;
            h.next = null; // help GC
            //*/
        }

        private static final long STATE;

        static {
            try {
                STATE = UnsafeProvider.getUnsafe().objectFieldOffset
                        (WaitingQueue.class.getDeclaredField("state"));
            } catch (Exception ex) {
                throw new Error(ex);
            }
        }

        private boolean casState(long expected, long newState) {
            return UnsafeProvider.getUnsafe().compareAndSwapLong
                    (this, STATE, expected, newState);
        }
    }
}
