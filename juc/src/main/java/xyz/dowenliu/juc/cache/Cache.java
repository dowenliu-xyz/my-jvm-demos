package xyz.dowenliu.juc.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * <p>create at 2019/10/23</p>
 *
 * @author liufl
 * @param <K> 缓存key类型
 * @param <V> 缓存value类型
 */
public class Cache<K, V> {
    /**
     * 缓存值包装类
     * @param <V> 缓存值类型
     */
    private static class Cached<V> {
        private final V value;

        private Cached(V value) {
            this.value = value;
        }

        /**
         * 缓存的值
         * @return 值或 {@code null}
         */
        public V getValue() {
            return value;
        }
    }

    private final Map<K, Cached<V>> m = new HashMap<>();
    private final ReadWriteLock rwl = new ReentrantReadWriteLock();
    // 读锁
    private final Lock r = rwl.readLock();
    // 写锁
    private final Lock w = rwl.writeLock();

    /**
     * 读缓存
     * @param key 缓存key
     * @return 如果 key 没有缓存，返回 null ；否则返回缓存值包装对象 {@link Cached<V>}
     */
    Cached<V> get(K key) {
        if (key == null) {
            return null;
        }
        r.lock();
        try {
            return m.get(key);
        } finally {
            r.unlock();
        }
    }

    /**
     * 更新缓存
     * @param key 缓存 key
     * @param value 缓存 value，可以为 {@code null}
     * @return 旧缓存值包装对象。如果没有旧值，返回 {@code null}
     */
    Cached<V> put(K key, V value) {
        if (key == null) {
            return null;
        }
        w.lock();
        try {
            return m.put(key, new Cached<>(value));
        } finally {
            w.unlock();
        }
    }
}
