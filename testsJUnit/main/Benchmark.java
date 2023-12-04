package main;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public class Benchmark {

    public static Optional<Long> run(CallbackRunnable func, int nThreads, Object... args) {

        CountDownLatch latch = new CountDownLatch(nThreads);
        long[] executionTimes = new long[nThreads];

        for (int i = 0; i < nThreads; i++) {
            final int index = i;
            new Thread(() -> {
                long startTime = System.nanoTime();
                func.run(args);
                executionTimes[index] = System.nanoTime() - startTime;
                latch.countDown();
            }).start();
        }

        try {
            latch.await();

            long total = 0;
            for (long time : executionTimes) {
                total += time;
            }
            return Optional.of(total / nThreads);
        } catch (InterruptedException e) {
            return Optional.empty();
        }
    }

    @FunctionalInterface
    public interface CallbackRunnable {
        void run(Object... args);
    }
}