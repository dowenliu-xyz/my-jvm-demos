package xyz.dowenliu.core.collection;

import java.util.BitSet;

/**
 * This program runs the Sieve of Erathostenes benchmark. It computes all primes up to 2,000,000.
 * <p>create at 2019/11/5</p>
 *
 * @author Cay Horstmann
 * @author liufl
 */
public class Sieve {
    public static void main(String[] args) {
        int n = 2000000;
        long start = System.currentTimeMillis();
        BitSet b = new BitSet(n + 1);
        int count = 0;
        int i;
        for (i = 2; i <= n; i++) {
            b.set(i); // 0-1 为 false 2-2000001为true
        }
        i = 2;
        while (i * i <= n) { // 最后一轮循环结束后，i 和 n 之前已经没有合数位为true
            if (b.get(i)) {
                count++;
                int k = 2 * i; // k 总是 i 的倍数
                while (k <= n) {
                    b.clear(k);
                    k += i;
                }
            }
            i++;
        }
        while (i <= n) {
            if (b.get(i)) count++;
            i++;
        }
        long end = System.currentTimeMillis();
        System.out.println(count + " primes");
        System.out.println((end - start) + " milliseconds");
    }
}
