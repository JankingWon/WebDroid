package cn.janking.webDroid.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadUtil {
    private static ThreadPoolExecutor threadPoolExecutor;

    synchronized private static ThreadPoolExecutor getInstance() {
        if (threadPoolExecutor == null) {
            int poolSize = Runtime.getRuntime().availableProcessors() * 2;
            BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(512);
            RejectedExecutionHandler policy = new ThreadPoolExecutor.DiscardPolicy();
            threadPoolExecutor = new ThreadPoolExecutor(poolSize, poolSize,
                    0, TimeUnit.SECONDS,
                    queue,
                    policy);
        }
        return threadPoolExecutor;
    }

    public static void execute(Runnable runnable){
        getInstance().execute(runnable);
    }
}
