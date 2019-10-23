package xyz.dowenliu.juc.transfer;

import java.util.Random;

/**
 * <p>create at 2019/10/23</p>
 *
 * @author liufl
 */
public class DualLockAccount {
    private int balance; // 余额

    public DualLockAccount(int balance) {
        this.balance = balance;
    }

    public void transfer(DualLockAccount target, int amount) {
        if (target == null) {
            return;
        }
        // 锁定转出账户
        synchronized (this) {
            // 锁定转入账户
            synchronized (target) { // 此处有可能发生死锁。 IntelliJ IDEA 也会发出警告：对方法参数target同步锁
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
}
