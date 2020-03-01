package xyz.dowenliu.juc;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * <p>create at 2020/3/1</p>
 *
 * @author liufl
 * @since 1.0
 */
public class CompletableFutureDemo {
    // 创建一个"已完成"的CompletableFuture
    @Test
    public void completedFutureExample() {
        CompletableFuture<String> cf =
                CompletableFuture.completedFuture("message");
        assertThat(cf.isDone()).isTrue();
        // getNow(Object) 如果Future已经完成，返回 get 的值，否则，返回传入参数
        assertThat(cf.getNow(null)).isEqualTo("message");
    }

    // 运行一个简单的异步阶段
    @Test
    public void runAsyncExample() {
        CompletableFuture<Void> cf = CompletableFuture.runAsync(() -> {
            // 以Async结尾的方法默认在内置的ForkJoinPool上执行，线程为守护线程
            assertThat(Thread.currentThread().isDaemon()).isTrue();
            randomSleep();
        });
        assertThat(cf.isDone()).isFalse();
        sleepEnough();
        assertThat(cf.isDone()).isTrue();
    }

    // 在前一阶段上应用函数
    @Test
    public void thenApplyExample() {
        final Thread mainThread = Thread.currentThread();
        CompletableFuture<String> cf =
                CompletableFuture.completedFuture("message").thenApply(s -> {
                    // thenApply阶段在调用者线程上执行。
                    // thenApply应用的方法还会导致阻塞
                    assertThat(Thread.currentThread().isDaemon()).isFalse();
                    assertThat(Thread.currentThread()).isSameAs(mainThread);
                    return s.toUpperCase();
                });
        // 因为thenApply前一阶段已完成，执行下面这一行之前，
        // thenApply已经开始在当前线程执行并且阻塞，所以getNow会拿到大写之后的 MESSAGE
        assertThat(cf.getNow(null)).isEqualTo("MESSAGE");
    }

    // 在前一阶段上异步应用函数
    @Test
    public void thenApplyAsyncExample() {
        CompletableFuture<String> cf =
                CompletableFuture.completedFuture("message").thenApplyAsync(s -> {
                    // thenApplyAsync阶段在ForkJoinPool上执行。
                    assertThat(Thread.currentThread().isDaemon()).isTrue();
                    randomSleep();
                    return s.toUpperCase();
                });
        assertThat(cf.getNow(null)).isNull();
        // join 等待阶段完成并返回结果或异常
        assertThat(cf.join()).isEqualTo("MESSAGE");
    }

    // 使用定制的Executor在前一阶段上异步应用函数
    static Executor executor = Executors.newFixedThreadPool(3, new ThreadFactory() {
        int count = 1;

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "custom-executor-" + count);
        }
    });

    @Test
    public void thenApplyAsyncWithExecutorExample() {
        CompletableFuture<String> cf =
                CompletableFuture.completedFuture("message").thenApplyAsync(s -> {
                    assertThat(Thread.currentThread().getName()).startsWith("custom-executor-");
                    assertThat(Thread.currentThread().isDaemon()).isFalse();
                    randomSleep();
                    return s.toUpperCase();
                }, executor); // <<<<<< 别忘记在大块的 lambd 后面写 executor参数
        assertThat(cf.getNow(null)).isNull();
        assertThat(cf.join()).isEqualTo("MESSAGE");
    }

    // 消费前一阶段的结果
    @Test
    public void thenAcceptExample() {
        Thread mainThread = Thread.currentThread();
        StringBuilder result = new StringBuilder();
        CompletableFuture.completedFuture("thenAccept message")
                .thenAccept(s -> {
                    result.append(s);
                    assertThat(Thread.currentThread()).isSameAs(mainThread);
                });
        assertThat(result.length())
                .withFailMessage("Result is empty").isGreaterThan(0);
    }

    // 异步消费前一阶段的结果
    @Test
    public void thenAcceptAsyncExample() {
        StringBuilder result = new StringBuilder();
        CompletableFuture<Void> cf = CompletableFuture.completedFuture("thenAccept message")
                .thenAcceptAsync(result::append);
        cf.join();
        assertThat(result.length()).withFailMessage("Result is empty").isGreaterThan(0);
    }

    // 完成计算异常
    @Test
    public void completeExceptionallyExample() {
        Thread mainThread = Thread.currentThread();
        CompletableFuture<String> cf = CompletableFuture.completedFuture("message").
                thenApplyAsync(String::toUpperCase, CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS));
        CompletableFuture<String> exceptionHandler = cf.handle((s, throwable) -> {
            assertThat(Thread.currentThread()).isSameAs(mainThread);
            return throwable != null ? "message upon cancel" : "";
        });
        // 手动令CompletableFuture异常完成。thenApplyAsync可能还未执行也可能开始执行了。
        cf.completeExceptionally(new RuntimeException("completed exceptionally"));
        assertThat(cf.isCompletedExceptionally()).withFailMessage("Was not completed exceptionally").isTrue();
        try {
            cf.join();
            fail("Should have throw an exception");
        } catch (CompletionException ex) {
            assertThat(ex.getCause().getMessage()).isEqualTo("completed exceptionally");
        }

        assertThat(exceptionHandler.join()).isEqualTo("message upon cancel");
    }

    // 取消计算
    @Test
    public void cancelExample() {
        CompletableFuture<String> cf = CompletableFuture.completedFuture("message")
                .thenApplyAsync(String::toUpperCase, CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS));
        CompletableFuture<String> cf2 = cf.exceptionally(throwable -> "canceled message");
        assertThat(cf.cancel(true)).withFailMessage("Was not canceled").isTrue();
        assertThat(cf.isCompletedExceptionally()).withFailMessage("Was not completed exceptionally").isTrue();
        assertThat(cf.isCancelled()).isTrue();
        assertThat(cf2.join()).isEqualTo("canceled message");
    }

    // 在两个完成阶段之一上应用函数
    @Test
    public void applyToEitherExample() {
        String origin = "Message";
        CompletableFuture<String> cf1 = CompletableFuture.completedFuture(origin).thenApplyAsync(CompletableFutureDemo::delayedUpperCase);
        CompletableFuture<String> cf2 = cf1.applyToEither(
                CompletableFuture.completedFuture(origin).thenApplyAsync(CompletableFutureDemo::delayedLowerCase), s -> s + " from applyToEither"
        );
        String result = cf2.join();
        System.out.println(result);
        assertThat(result).endsWith(" from applyToEither");
    }

    // 在两个完成阶段之一上消费
    @Test
    public void acceptEitherExample() {
        String origin = "Message";
        StringBuilder result = new StringBuilder();
        CompletableFuture<Void> cf1 = CompletableFuture.completedFuture(origin)
                .thenApplyAsync(CompletableFutureDemo::delayedUpperCase)
                .acceptEither(
                        CompletableFuture.completedFuture(origin).
                                thenApplyAsync(CompletableFutureDemo::delayedLowerCase),
                        s -> result.append(s).append(" acceptEither")
                );
        cf1.join();
        System.out.println(result);
        assertThat(result.toString()).endsWith("acceptEither");
    }

    // 在两阶段都执行完后执行Runnable
    @Test
    public void runAfterBothExample() {
        String origin = "Message";
        StringBuffer result = new StringBuffer();
        CompletableFuture.completedFuture(origin).thenApply(String::toUpperCase)
                .runAfterBoth(CompletableFuture.completedFuture(origin).thenApply(String::toLowerCase),
                        () -> result.append("done"));
        assertThat(result).isNotBlank();
    }

    // 使用BiConsumer处理两个阶段的结果

    @Test
    public void thenAcceptBothExample() {
        String origin = "Message";
        StringBuffer result = new StringBuffer();
        CompletableFuture.completedFuture(origin).thenApply(String::toUpperCase)
                .thenAcceptBoth(CompletableFuture.completedFuture(origin).thenApply(String::toLowerCase),
                        (s, s2) -> result.append(s).append(s2));
        assertThat(result.toString()).isEqualTo("MESSAGEmessage");
    }

    // 使用BiFunction处理两个阶段的结果
    @Test
    public void thenCombineExample() {
        String origin = "Message";
        CompletableFuture<String> cf = CompletableFuture.completedFuture(origin).thenApply(CompletableFutureDemo::delayedUpperCase)
                .thenCombine(CompletableFuture.completedFuture(origin).thenApply(CompletableFutureDemo::delayedLowerCase),
                        (s, s2) -> s + s2);
        assertThat(cf.getNow(null)).isEqualTo("MESSAGEmessage");
    }

    // 异步使用BiFunction处理两个阶段的结果
    @Test
    public void thenCombineAsyncExample() {
        Thread mainThread = Thread.currentThread();
        String origin = "Message";
        // 只要 thenCombine 的第一个参数是异步完成阶段的，thenCombine 也会异步执行，需要join()
        CompletableFuture<String> cf = CompletableFuture.completedFuture(origin).thenApply(CompletableFutureDemo::delayedUpperCase)
                .thenCombine(CompletableFuture.completedFuture(origin).thenApplyAsync(CompletableFutureDemo::delayedLowerCase),
                        (s, s2) -> {
                            assertThat(Thread.currentThread()).isNotSameAs(mainThread);
                            return s + s2;
                        });
        assertThat(cf.join()).isEqualTo("MESSAGEmessage");
    }

    // 组合 CompletableFuture
    @Test
    public void thenComposeExample() {
        String origin = "Message";
        CompletableFuture<String> cf = CompletableFuture.completedFuture(origin).thenApply(CompletableFutureDemo::delayedUpperCase)
                .thenCompose(upper -> CompletableFuture.completedFuture(origin).thenApply(CompletableFutureDemo::delayedLowerCase)
                        .thenApply(s -> upper + s));
        assertThat(cf.join()).isEqualTo("MESSAGEmessage");
    }

    // 当几个阶段中的一个完成，创建一个完成的阶段
    @Test
    public void anyOfExample() {
        StringBuilder result = new StringBuilder();
        List<String> messages = Arrays.asList("a", "b", "c");
        @SuppressWarnings("rawtypes") CompletableFuture[] futures = messages.stream()
                .map(msg -> CompletableFuture.completedFuture(msg).thenApply(CompletableFutureDemo::delayedUpperCase))
                .toArray(CompletableFuture[]::new);
        CompletableFuture.anyOf(futures).whenComplete((res, throwable) -> {
            if (throwable == null) {
                assertThat(isUpperCase((String) res)).isTrue();
                result.append(res);
            }
        });
        System.out.println(result);
        assertThat(result.length()).withFailMessage("Result is empty").isGreaterThan(0);
    }

    // 当所有的阶段都完成后创建一个阶段
    @Test
    public void allOfExample() {
        StringBuilder result = new StringBuilder();
        List<String> messages = Arrays.asList("a", "b", "c");
        @SuppressWarnings("rawtypes") CompletableFuture[] futures = messages.stream()
                .map(msg -> CompletableFuture.completedFuture(msg).thenApply(CompletableFutureDemo::delayedUpperCase))
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(futures).whenComplete((v, throwable) -> {
            for (CompletableFuture future : futures) {
                assertThat(isUpperCase((String) future.getNow(null))).isTrue();
            }
            result.append("done");
        });
        assertThat(result.length()).withFailMessage("Result is empty").isGreaterThan(0);
    }

    // 当所有的阶段都完成后异步地创建一个阶段
    @Test
    public void allOfAsyncExample() {
        StringBuilder result = new StringBuilder();
        List<String> message = Arrays.asList("a", "b", "c");
        @SuppressWarnings("rawtypes") CompletableFuture[] futures = message.stream()
                .map(msg -> CompletableFuture.completedFuture(msg).thenApplyAsync(CompletableFutureDemo::delayedUpperCase))
                .toArray(CompletableFuture[]::new);
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures).whenComplete((v, th) -> {
            for (CompletableFuture future : futures) {
                assertThat(isUpperCase((String) future.getNow(null))).isTrue();
            }
            result.append("done");
        });
        allOf.join();
        assertThat(result.length()).withFailMessage("Result was empty").isGreaterThan(0);
    }

    private static boolean isUpperCase(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (Character.isLowerCase(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static String delayedUpperCase(String s) {
        randomSleep();
        return s.toUpperCase();
    }

    private static String delayedLowerCase(String s) {
        randomSleep();
        return s.toLowerCase();
    }

    static Random random = new Random();

    private static void randomSleep() {
        try {
            Thread.sleep(random.nextInt(1000));
        } catch (InterruptedException e) {
            // ...
        }
    }

    private static void sleepEnough() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // ...
        }
    }
}
