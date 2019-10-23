package xyz.dowenliu.juc.transfer;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>create at 2019/10/23</p>
 *
 * @author liufl
 */
public class TryLockAccount {
    private int balance; // 余额
    private final Lock lock = new ReentrantLock();

    public TryLockAccount(int balance) {
        this.balance = balance;
    }

    // 转账
    public void transfer(TryLockAccount target, int amount) {
        while (true) {
            if (this.lock.tryLock()) {
                try {
                    if (target.lock.tryLock()) {
                        try {
                            if (amount > 0 && this.balance >= amount) {
                                this.balance -= amount;
                                try {
                                    Thread.sleep(new Random().nextInt(200)); // 模拟转账延迟
                                } catch (InterruptedException ignored) {
                                }
                                target.balance += amount;
                            }
                            break; // 退出循环
                        } finally {
                            target.lock.unlock();
                        }
                    } // if
                } finally {
                    this.lock.unlock();
                }
            } // if
            try {
                Thread.sleep(new Random().nextInt(100)); // sleep 以避免活锁
            } catch (InterruptedException ignored) {
            }
        }
    }
}
