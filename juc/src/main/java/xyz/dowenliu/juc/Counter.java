package xyz.dowenliu.juc;

/**
 * 计数器接口。实现类需要保证线程安全
 * <p>create at 2019/10/22</p>
 *
 * @author liufl
 */
public interface Counter {
    long get();

    void addOne();
}
