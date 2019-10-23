package xyz.dowenliu.juc.transfer;

import java.util.Random;

/**
 * 使用 synchronized 锁定类来实现安全转账的方案。
 * 虽然确保了线程安全但因为锁的粒度很大，只能串行执行转账操作，性能很低，完全不能作生产实现。
 * <p>create at 2019/10/22</p>
 *
 * @author liufl
 */
public class GlobalLockAccount {
    private int balance; // 余额

    public GlobalLockAccount(int balance) {
        this.balance = balance;
    }

    // 转账
    void transfer(GlobalLockAccount target, int amount) {
        synchronized (GlobalLockAccount.class) { // 不能使用 this 加锁，因为无法保护 target
            if (amount > 0 && this.balance >= amount) {
                this.balance -= amount;
                try {
                    Thread.sleep(new Random().nextInt(200)); // 模拟转账延迟
                } catch (InterruptedException ignored) {
                }
                target.balance += amount;
            }
        }
    }
}
