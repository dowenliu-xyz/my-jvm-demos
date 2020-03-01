package xyz.dowenliu.core.monitor;

import java.util.concurrent.TimeUnit;

/**
 * 父子类都有synchronized方法，问是否能并发执行子类对象的Synchronized方法。
 * <p>create at 2020/2/28</p>
 *
 * @author liufl
 * @since 1.0
 */
public class SynchronizedExtendDemo {
    static class SuperClass {
        public synchronized void doSuper() {
            System.out.println("Super before sleep");
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException ignored) {
            }
            System.out.println("Super after sleep");
        }
    }

    static class ChildClass extends SuperClass {
        public synchronized void doChild() {
            System.out.println("Child before sleep");
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException ignored) {
            }
            System.out.println("Child after sleep");
        }

//        public void doSuper() {
//            super.doSuper();
//        }
    }

    public static void main(String[] args) throws InterruptedException {
        final ChildClass childClass = new ChildClass();
        Thread doSuper = new Thread(childClass::doSuper);
        Thread doChild = new Thread(childClass::doChild);
        doSuper.start();
        doChild.start();
        doSuper.join();
        doChild.join();
    }
}
