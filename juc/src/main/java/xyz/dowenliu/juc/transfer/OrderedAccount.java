package xyz.dowenliu.juc.transfer;

import java.util.Random;

/**
 * <p>create at 2019/10/23</p>
 *
 * @author liufl
 */
public class OrderedAccount {
    private final int id; // 账号，用于排序
    private int balance; // 余额

    public OrderedAccount(int id, int balance) {
        this.id = id;
        this.balance = balance;
    }

    // 转账
    public void transfer(OrderedAccount target, int amount) {
        // 按id排序
        OrderedAccount left = this;
        OrderedAccount right = target;
        if (this.id > target.id) {
            left = target;
            right = this;
        }
        // 锁定序号小的账户
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (left) {
            // 锁定序号大的账户
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (right) {
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
