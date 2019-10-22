package xyz.dowenliu.juc.transfer;

import java.util.Random;

/**
 * 使用 synchronized 锁定类来实现安全转账的方案。
 * 虽然确保了线程安全但因为锁的粒度很大，只能串行执行转账操作，性能很低，完全不能作生产实现。
 * <p>create at 2019/10/22</p>
 *
 * @author liufl
 */
public class LowPerformanceAccount {
    private int balance;

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    void transfer(LowPerformanceAccount target, int amount) {
        synchronized (LowPerformanceAccount.class) { // 不能使用 this 加锁，因为无法保护 target
            if (this.balance > amount) {
                this.balance -= amount;
                try {
                    Thread.sleep(new Random().nextInt(100));
                } catch (InterruptedException ignored) {
                }
                target.balance += amount;
            }
        }
    }
}
