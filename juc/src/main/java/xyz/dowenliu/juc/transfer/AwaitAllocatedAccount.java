package xyz.dowenliu.juc.transfer;

import java.util.Random;

/**
 * <p>create at 2019/10/23</p>
 *
 * @author liufl
 */
public class AwaitAllocatedAccount {
    private final AwaitAllocator allocator = AwaitAllocator.instance; // 分配器
    private int balance; // 余额

    public AwaitAllocatedAccount(int balance) {
        this.balance = balance;
    }

    // 转账
    public void transfer(AwaitAllocatedAccount target, int amount) {
        if (target == null) {
            return;
        }
        // 一次性申请转出账户和转入账户
        allocator.apply(this, target);
        try {
            // 锁定转出账户
            synchronized (this) {
                // 锁定转入账户
                //noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (target) {
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
        } finally {
            allocator.release(this, target); // 确保释放资源
        }
    }
}
