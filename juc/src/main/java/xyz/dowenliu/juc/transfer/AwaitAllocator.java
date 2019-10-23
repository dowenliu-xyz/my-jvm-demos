package xyz.dowenliu.juc.transfer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * 使用等待-通知机制的分配者。单例
 * <p>create at 2019/10/23</p>
 *
 * @author liufl
 */
public final class AwaitAllocator {
    // 已分配对象
    private List<Object> allocated = new ArrayList<>();

    private AwaitAllocator() {
    }

    public static final AwaitAllocator instance = new AwaitAllocator();

    // 一次性申请所有资源
    synchronized void apply(Object... resources) {
        List<Object> resourceList = Arrays.asList(resources);
        // 经典写法 while(条件不满足) { wait(); }
        while (resourceList.parallelStream().anyMatch(it -> allocated.contains(it))) {
            try {
                wait();
            } catch (InterruptedException ignored) {
            }
        }
        allocated.addAll(resourceList);
    }

    // 归还资源
    synchronized void release(Object... resources) {
        allocated.removeAll(Arrays.asList(resources));
        notifyAll(); // 尽量使用 notifyAll() 而不是 notify()
        // 要使用notify()需要满足以下三个条件:
        // 1. 所有等待线程拥有相同的等待条件；
        // 2. 所有等待线程被唤醒后，执行相同的操作；
        // 3. 只需要唤醒一个线程。
    }
}
