package name.auh.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * 并发测试工具
 */
@Slf4j
public class ConcurrentTestUtil {

    @FunctionalInterface
    public interface JobDo {

        void process();
    }

    @Slf4j
    public static class Job implements Runnable {

        CyclicBarrier cyclicBarrier;

        JobDo jobDo;

        ExecutorService executorService;

        int threadCount;

        boolean loopAllTime = false;

        public Job(CyclicBarrier cyclicBarrier, JobDo jobDo, int threadCount, boolean loopAllTime) {
            this.loopAllTime = loopAllTime;
            this.cyclicBarrier = cyclicBarrier;
            this.jobDo = jobDo;
            this.threadCount = threadCount;

            executorService = new ThreadPoolExecutor(threadCount, threadCount,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(1000), (r, e) -> {
            });
        }

        public void start() {
            while (loopAllTime) {
                executorService.submit(this);
            }

            for (int i = 0; i < threadCount; i++) {
                executorService.submit(this);
            }
            executorService.shutdown();
        }

        @Override
        public void run() {
            try {
                cyclicBarrier.await();
                jobDo.process();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {

        new Job(
                new CyclicBarrier(600),
                () -> {
                    //do something
                }
                ,
                600,
                true)
                .start();

    }

}


