package xyz.dowenliu.core.method;

import java.util.Random;

/**
 * <p>create at 2019/11/12</p>
 *
 * @author liufl
 */
public class ThisArgument {
    private int id;

    /**
     * 可以显式的为方法提供 this，主要用于注解。<br/>
     * 实际很少用到
     */
    private void gitMeThis(ThisArgument this) {
        System.out.println(this);
    }

    @Override
    public String toString() {
        return "MethodThisTest{" +
                "id=" + id +
                '}';
    }

    class Inner {
        private final String id;

        /**
         * 显式的为内部类提供外置类对象的引用。
         * <br/>很少这么用
         */
        Inner(ThisArgument ThisArgument.this) {
            System.out.println(ThisArgument.this);
            id = String.valueOf(ThisArgument.this.id);
        }


        String getId() {
            return id;
        }
    }

    public static void main(String[] args) {
        ThisArgument test = new ThisArgument();
        test.id = new Random().nextInt(100);
        test.gitMeThis();
        System.out.println(test);
        Inner inner = test.new Inner();
        System.out.println(inner.getId());
    }
}
