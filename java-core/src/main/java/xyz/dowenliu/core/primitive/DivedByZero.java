package xyz.dowenliu.core.primitive;

import java.util.Random;

/**
 * <p>create at 2019/11/11</p>
 *
 * @author liufl
 */
public class DivedByZero {
    public static void main(String[] args) {
        // 整数除以整数0，抛出 ArithmeticException
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < 10; i++) {
            int toBeDived = random.nextBoolean() ? random.nextInt(10) : -random.nextInt(10);
            try {
                //noinspection divzero
                System.out.println(toBeDived + " / 0 =  " + (toBeDived / 0));
            } catch (ArithmeticException ex) {
                System.out.println(toBeDived + " / 0 cause exception: " + ex.toString());
            }
        }
        System.out.println("---");
        // 数值类型除以浮点0.0，结果为 +-Infinity或NaN
        for (int i = 0; i < 10; i++) {
            int toBeDived = random.nextBoolean() ? random.nextInt(10) : -random.nextInt(10);
            //noinspection divzero
            System.out.println(toBeDived + " / 0.0 = " + (toBeDived / 0.0)); // toBeDived 自动转换为 double 型再参与运算
        }
    }
}
