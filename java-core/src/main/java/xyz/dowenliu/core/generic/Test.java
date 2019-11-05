package xyz.dowenliu.core.generic;

/**
 * <p>create at 2019/11/4</p>
 *
 * @author Cay Horstmann
 * @author liufl
 */
public class Test {
    public static void main(String[] args) {
        Manager ceo = new Manager("CEO", 800000, 2003, 12, 15);
        Manager cfo = new Manager("CFO", 600000, 2003, 12, 15);
        Pair<Manager> buddies = new Pair<>(ceo, cfo);
        printBuddies(buddies); // Pair<? extends Employee> 可以接收 Pair<Manager>

        ceo.setBonus(1000000);
        cfo.setBonus(500000);
        Manager[] managers = {ceo, cfo};

        Pair<Employee> result = new Pair<>();
        minmaxBonus(managers, result); // Pair<? super Manager> 可以接收 Pair<Employee>
        System.out.println("first: " + result.getFirst().getName()
                + ", second: " + result.getSecond().getName());
        maxminBonus(managers, result);
        System.out.println("first: " + result.getFirst().getName()
                + ", second: " + result.getSecond().getName());
    }

    private static void printBuddies(Pair<? extends Employee> p) { // 子类型限定。可以接收 Pair<Employee> （上限）、Pair<Manager>（子类型）
        Employee first = p.getFirst(); // 类型参数用于方法返回值时，返回值只能赋值给上限类型或其超类型
        Employee second = p.getSecond();
        System.out.println(first.getName() + " and " + second.getName() + " are buddies.");
        // 类型参数用于方法参数时，只能接收 null
        // p.setFirst(null); // OK
        // p.setFirst(new Employee("someone", 10000, 2019, 1,1)); // compile error
        // p.setFirst(new Manager("someone", 10000, 2019, 1,1)); // compile error
        // 下面的方式也不行
        // p.setFirst(p.getFirst()); // compile error
    }

    private static void minmaxBonus(Manager[] a, Pair<? super Manager> result) { // 超类型限定。可以接收 Pair<Manager> （下限）、Pair<Employee>、Pair<Object> （超类型）
        if (a.length == 0) return;
        Manager min = a[0];
        Manager max = a[0];
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < a.length; i++) {
            if (min.getBonus() > a[i].getBonus()) {
                min = a[i];
            }
            if (max.getBonus() < a[i].getBonus()) {
                max = a[i];
            }
        }
        result.setFirst(min); // 与子类型限定不同，类型参数用于方法参数时只能接收类型限定下限类型（此处为 Manager 类型）或 null
        result.setSecond(max);
        // 与子类型限定不同，类型参数用于方法返回值时，只能赋值给 Object 类型的变量
        // Object first = result.getFirst(); // OK
        // Employee first = result.getFirst(); // compile error
    }

    private static void maxminBonus(Manager[] a, Pair<? super Manager> result) {
        minmaxBonus(a, result);
        PairAlg.swapHelper(result); // 通配符类型被 swapHelper 的类型变量 T 捕获
    }
}

class PairAlg {
    public static boolean hasNulls(Pair<?> pair) {
        return pair.getFirst() == null || pair.getSecond() == null;
    }

    public static void swap(Pair<?> p) {
        swapHelper(p); // 类型捕获
        // 不能使用 在这个方法进行 swapHelper 中的操作，原因：
        // 1. 不能创建通配符?类型的变量
        // 2. 如果通配符有限定，set和get方法将类型冲突
    }

    public static <T> void swapHelper(Pair<T> p) {
        T t = p.getFirst();
        p.setFirst(p.getSecond());
        p.setSecond(t);
    }
}
