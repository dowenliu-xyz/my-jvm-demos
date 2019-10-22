package xyz.dowenliu.juc.counter;

/**
 * 使用 synchronized 实现的线程安全 {@link Counter}。
 * <br>
 * 不保证继承此类的子类线程安全。
 * <p>create at 2019/10/22</p>
 *
 * @author liufl
 */
public class SyncCounter implements Counter {
    private long value = 0L; // 由 synchronized 隐式锁保护

    @Override
    public synchronized long get() {
        return value;
    }

    @Override
    public synchronized void addOne() {
        value += 1; // 尽管这里只有一行，实际运行时需要3步：
        // 1. 从 RAM 读 value 到 CPU Cache
        // 2. 在 CPU Cache 中操作 value 值 +1
        // 3. 将 value 的值写回 RAM
        // 为了防止线程在步骤1操作后步骤3操作前线程切换导致其他线程操作 value ，所以需要加锁.
    }
}
