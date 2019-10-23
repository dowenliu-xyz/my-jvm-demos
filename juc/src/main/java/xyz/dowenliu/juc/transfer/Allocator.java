package xyz.dowenliu.juc.transfer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 分配者。单例
 * <p>create at 2019/10/23</p>
 *
 * @author liufl
 */
public final class Allocator {
    // 已分配对象
    private List<Object> allocated = new ArrayList<>();

    private Allocator() {
    }

    public static final Allocator instance = new Allocator();

    // 一次性申请所有资源
    synchronized boolean apply(Object... resources) {
        for (Object resource : resources) {
            if (allocated.contains(resource))
                return false;
        }
        allocated.addAll(Arrays.asList(resources));
        return true;
    }

    // 归还资源
    synchronized void release(Object... resources) {
        allocated.removeAll(Arrays.asList(resources));
    }
}
