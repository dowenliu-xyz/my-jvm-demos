package xyz.dowenliu.juc.interrupt;

public class InterruptDemo {
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(InterruptDemo::run);
        Thread t2 = new Thread(InterruptDemo::run);
        t1.start();
        t2.start();
        Thread.sleep(1000);
        t1.interrupt();
        t2.interrupt();
        t1.join();
        t2.join();
        // 输出:
        // interrupted while sleeping
        // interrupted
        // 证明线程在 BLOCKED 状态时被 interrupt() 会标记中断，但不会抛出异常
    }

    public static synchronized void run() {
        if (Thread.interrupted()) {
            System.out.println("interrupted");
            return;
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            System.out.println("interrupted while sleeping");
            return;
        }
        System.out.println("exiting.");
    }
}
