package xyz.dowenliu.core.generic;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.function.IntFunction;

/**
 * <p>create at 2019/11/4</p>
 *
 * @author Cay Horstmann
 * @author liufl
 */
public class GenericAlgorithms {
    public static void main(String[] args) {
        Pair<String> p = Pair.makePair(String::new); // 构造器表达式，还可以使用lambda
        System.out.println(p);

        p = Pair.makePair(String.class); // 使用 Class 泛型，只适用于存在公有无参构造器的类
        System.out.println(p);

        String[] ss = ArrayAlg.minmax("Tom", "Dick", "Harry");
        System.out.println(Arrays.toString(ss));

        ss = ArrayAlg.minmax((IntFunction<String[]>) String[]::new, "Tom", "Dick", "Harry");
        System.out.println(Arrays.toString(ss));
    }
}

class ArrayAlg {
    @SafeVarargs
    public static <T extends Comparable> T[] minmax(IntFunction<T[]> constr, T... a) {
        T[] mm = constr.apply(2);
        T min = a[0];
        T max = a[0];
        for (int i = 0; i < a.length; i++) {
            if (min.compareTo(a[i]) > 0) {
                min = a[i];
            }
            if (max.compareTo(a[i]) < 0) {
                max = a[i];
            }
        }
        mm[0] = min;
        mm[1] = max;
        return mm;
    }

    @SafeVarargs
    public static <T extends Comparable> T[] minmax(T... a) {
        T[] mm = (T[]) Array.newInstance(a.getClass().getComponentType(), 2); // compile warning
        T min = a[0];
        T max = a[0];
        for (int i = 0; i < a.length; i++) {
            if (min.compareTo(a[i]) > 0) {
                min = a[i];
            }
            if (max.compareTo(a[i]) < 0) {
                max = a[i];
            }
        }
        mm[0] = min;
        mm[1] = max;
        return mm;
    }
}
