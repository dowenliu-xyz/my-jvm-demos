package xyz.dowenliu.juc.semaphore;

import java.time.LocalTime;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * <p>create at 2019/10/23</p>
 *
 * @author liufl
 */
public class Pool<T> {
    private final List<T> pool;
    private final Semaphore semaphore;

    public Pool(int size, T t) {
        pool = new Vector<>();
        for (int i = 0; i < size; i++) {
            pool.add(t);
        }
        semaphore = new Semaphore(size);
    }

    // 利用对象池对象，调用 func
    <R> R exec(Function<T, R> func) {
        T t = null;
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        try {
            t = pool.remove(0);
            return func.apply(t);
        } finally {
            pool.add(t);
            semaphore.release();
        }
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // 创建对象池
        Pool<Long> pool = new Pool<>(10, 2L);
        // 通过对象池获取t，之后执行
        ExecutorService executor = new ThreadPoolExecutor(10,
                20,
                30,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100)
        );
        CompletionService<String> completionService = new ExecutorCompletionService<>(executor);
        for (int i = 0; i < 15; i++) {
            completionService.submit(() -> pool.exec(t -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {
                }
                return t.toString();
            }));
        }
        for (int i = 0; i < 15; i++) {
            Future<String> future = completionService.take();
            System.out.println(i + " - " + LocalTime.now() + " ---- " + future.get());
        }

        executor.shutdown();
    }
}
