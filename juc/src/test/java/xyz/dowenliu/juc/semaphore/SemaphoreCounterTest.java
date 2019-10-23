package xyz.dowenliu.juc.semaphore;

import org.junit.Test;
import xyz.dowenliu.juc.counter.CounterTest;

/**
 * <p>create at 2019/10/23</p>
 *
 * @author liufl
 */
public class SemaphoreCounterTest {
    @Test
    public void test() throws InterruptedException {
        CounterTest.testCounter(new SemaphoreCounter());
    }
}
