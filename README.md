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
### 2.3 简单的阻塞队列[BlockedQueue](./juc/src/main/java/xyz/dowenliu/juc/condition/BlockedQueue.java) :
展示 条件变量 `Condition` 的用法
### 2.4 Semaphore demo
#### 2.4.1 使用信号量实现的线程安全计数器：[SemaphoreCounter](./juc/src/main/java/xyz/dowenliu/juc/semaphore/SemaphoreCounter.java)
#### 2.4.2 对象池: [Pool](./juc/src/main/java/xyz/dowenliu/juc/semaphore/Pool.java)
### 2.5 读写锁：实现RAM缓存
#### 2.5.1 简单的 [Cache](./juc/src/main/java/xyz/dowenliu/juc/cache/Cache.java)
### 2.6 对 BLOCKED 的线程进行 interrupt() [InterruptDemo](./juc/src/main/java/xyz/dowenliu/juc/interrupt/InterruptDemo.java)
使用代码测试 interrupt() 对 BLOCKED 状态的线程的作用。证明结果，进行了中断标记，没有抛出异常
### 2.7 实现锁
#### 2.7.1 使用Unsafe类实现不带超时、不可中断、不可重入的非公平独占锁
[SimpleLock](./juc/src/main/java/xyz/dowenliu/concurrent/SimpleLock.java)  
测试 [SimpleLockDemo](./juc/src/main/java/xyz/dowenliu/concurrent/SimpleLockDemo.java)
#### 2.7.2 使用AQS实现不带超时、不可中断、不可重入的非公平独占锁
[SimpleAQSLock](./juc/src/main/java/xyz/dowenliu/juc/lock/SimpleAQSLock.java)  
测试 [SimpleAQSLockDemo](./juc/src/main/java/xyz/dowenliu/juc/lock/SimpleAQSLockDemo.java)

# 3. Java 核心技术示例
这部分示例大部分来自《Java核心技术》的示例代码。
## 3.1 JavaDoc 资源示例 [JavaDocResources]()
## 3.2 除0结果 [DivedByZero](./java-core/src/main/java/xyz/dowenliu/core/primitive/DivedByZero.java)
## 3.3 显示提供this [ThisArgument](./java-core/src/main/java/xyz/dowenliu/core/method/ThisArgument.java)
## 3.4 内部类示例1 [InnerClassTest1](./java-core/src/main/java/xyz/dowenliu/core/innerClass/InnerClassTest1.java)
展示 `outerObject.new InnerClass(construction_parameters)`的语法。
## 3.5 Proxy示例 [ProxyTest](./java-core/src/main/java/xyz/dowenliu/core/proxy/ProxyTest.java)
## 3.6 泛型示例
* 通配符示例 [Test](./java-core/src/main/java/xyz/dowenliu/core/generic/Test.java)
* 泛型 构造器表达式示例 [GenericAlgorithms](./java-core/src/main/java/xyz/dowenliu/core/generic/GenericAlgorithms.java)
* 泛型反射示例 [GenericReflectionTest](./java-core/src/main/java/xyz/dowenliu/core/generic/GenericReflectionTest.java)
## 3.7 集合
* BitSet 示例 [Sieve](./java-core/src/main/java/xyz/dowenliu/core/collection/Sieve.java)

# 4 性能编码
## 4.1 使用自定义异常时避免生成栈追踪信息
不构建 StackTrace 信息，不消耗系统性能。如果异常只是供业务使用，可以选择不构建异常以提高性能。

性能对比测试：[RuntimeExceptionWithoutStackTraceBenchmark](./performance-tuning/src/test/java/xyz/dowenliu/performance/tuning/exception/RuntimeExceptionWithoutStackTraceBenchmark.java)
## 4.2 正则表达式 捕获分组 vs 非捕获分组
减少非必要的捕获分组可以提高正则表达式的性能

性能对比测试: [RegexNonCapturingGroupBenchmark](./performance-tuning/src/test/java/xyz/dowenliu/performance/tuning/exception/RegexNonCapturingGroupBenchmark.java)

... TBD