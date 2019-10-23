这个仓库主要放一些 Demo 示例

# 目录
## 1. Kotlin 实现 IGetInt java 接口的方法示例: [i-get-int](./i-get-int)
```java
public class IGetInt {
  String get(int i);
  String get(Integer i);
}
```
## 2. Java 并发示例 [juc](./juc)
此部分Demo基本用于实现[极客时间](https://time.geekbang.org/)
《[Java并发编程实战](https://time.geekbang.org/column/intro/159)》专栏中的示例
### 2.1. 线程安全的计数器[counter](./juc/src/main/java/xyz/dowenliu/juc/counter)：
展示 `synchronized` 用法和`Lock`(`ReentrantLock`)的简单用法
### 2.2. 账户转账[transfer](./juc/src/main/java/xyz/dowenliu/juc/transfer)：
展示锁与受保护资源的关联关系。

修改`Account`代码使方法`transfer`线程安全
```java
class Account {
  private int balance; // 余额
  public int getBalance() {
    return balance;
  }
  public void setBalance(int balance) {
    this.balance = balance;
  }
  
  /**
   * 转账操作
   * <br>
   * 线程不安全，需要修改为线程安全的实现
   * @param target 转账目标账户
   * @param amount 转账金额
   */
  public void transfer(Account target, int amount) {
    if (amount > 0 && this.balance >= amount) {
      this.balance -= amount;
      try {
        Thread.sleep(new Random().nextInt(200)); // 模拟转账延迟
      } catch (InterruptedException ignored) {}
      target.balance += amount;
    }
  }
}
```
仅使用 `synchronized(this)` 来保护`transfer`方法是无法保证线程安全的，
因为锁只保证了`this`的`balance`不会被并发修改，而`target`的`balance`是无保护的。

* 修改方案1：[GlobalLockAccount](./juc/src/main/java/xyz/dowenliu/juc/transfer/GlobalLockAccount.java)

  使用 `synchronized(Account.class)`来保护`transfer`方法。
  可以保证线程安全，但缺点很明显：所有的`transfer`操作都被串行化了。
  A转B操作和C转D操作并无关联却不能同时执行。锁的粒度太大，不具实际生产可行性。
* 修改方案2：[DualLockAccount](./juc/src/main/java/xyz/dowenliu/juc/transfer/DualLockAccount.java)

  使用各自的锁来保护`this`和`target`。优点是解决了方案1的串行化问题，缺点是会出现死锁
  
  > 死锁出现需**同时**满足以下四个条件，破坏任意一条都可以避免死锁。
  > 1. 互斥，共享资源 X 和 Y 只能被一个线程占用；
  > 2. 占有且等待，线程 T1 已经取得共享资源 X，在等待共享资源 Y 的时候，不释放共享资源 X；
  > 3. 不可抢占，其他线程不能强行抢占线程 T1 占有的资源；
  > 4. 循环等待，线程 T1 等待线程 T2 占有的资源，线程 T2 等待线程 T1 占有的资源，就是循环等待。
  >
  > 其中第一条无法破坏，因为需要保护资源，没有锁互斥就无法保护资源了。
  
  为了解决死锁问题又分别可以做以下方案
  
  * 方案2.1: [AllocatedAccount](./juc/src/main/java/xyz/dowenliu/juc/transfer/AllocatedAccount.java)。
    增加[Allocator](./juc/src/main/java/xyz/dowenliu/juc/transfer/Allocator.java)
    用于向线程同时分配转账账户和转账目标账户，破坏"占有且等待"条件。缺点是在向 allocator 申请资源时耗费过多的CPU。
    进一步优化此方案(使用等待-通知机制)：[AwaitAllocatedAccount](./juc/src/main/java/xyz/dowenliu/juc/transfer/AwaitAllocatedAccount.java)
  * 方案2.2：[TryLockAccount](./juc/src/main/java/xyz/dowenliu/juc/transfer/TryLockAccount.java)
    如果无法获取所有资源的锁则放弃已获得的锁，但有可能导致活锁。
  * 方案2.3(推荐方案)：[OrderedAccount](./juc/src/main/java/xyz/dowenliu/juc/transfer/OrderedAccount.java)
    对账户排序，按序加锁。破坏"循环等待"条件

... TBD