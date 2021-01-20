package com.milicloud.shared.common.tools.threadpool;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author wsq
 * @date 2021/1/19  22:54
 */
public class ThreadPoolFactory {
    private final static Logger logger = LoggerFactory.getLogger(ThreadPoolFactory.class);

    private static Map<String, ThreadPoolExecutor> threadPoolMap = new HashMap<String,ThreadPoolExecutor>();

    private static void updateThreadNumDynamically(String threadPoolName, ThreadPoolConfig threadPoolConfig,ThreadPoolExecutor threadPoolExecutor){
        int configedThreadNum = threadPoolConfig.getThreadNum(threadPoolName);
        int currThreadPoolThreadNum = threadPoolExecutor.getMaximumPoolSize();

        if (configedThreadNum != currThreadPoolThreadNum) {

            int corePoolSize = threadPoolExecutor.getCorePoolSize();
            int maxPoolSize = threadPoolExecutor.getMaximumPoolSize();
            int poolSize = threadPoolExecutor.getPoolSize();
            int activeCount = threadPoolExecutor.getActiveCount();

            threadPoolExecutor.setCorePoolSize(configedThreadNum);
            threadPoolExecutor.setMaximumPoolSize(configedThreadNum);

            int newCorePoolSize = threadPoolExecutor.getCorePoolSize();
            int newMaxPoolSize = threadPoolExecutor.getMaximumPoolSize();
            int newPoolSize = threadPoolExecutor.getPoolSize();
            int newActiveCount = threadPoolExecutor.getActiveCount();

            logger.info(
                    "threadNumUpdated threadPoolName:{},corePoolSize:{} to {} ,maxPoolSize:{} to {},poolSize:{} to {},"
                            + "activeCount:{} to {},currentThread:{}",
                    threadPoolName, corePoolSize, newCorePoolSize, maxPoolSize, newMaxPoolSize, poolSize, newPoolSize,
                    activeCount, newActiveCount, Thread.currentThread());
        }
    }

    public static ThreadPoolExecutor createThreadPool(String threadPoolName,ThreadPoolConfig threadPoolConfig){
        return createThreadPool(threadPoolName,threadPoolConfig,null);
    }

    private static ThreadPoolExecutor createThreadPool(String threadPoolName, ThreadPoolConfig threadPoolConfig, RejectedExecutionHandler handler) {
        if (StringUtils.isBlank(threadPoolName)) {
            throw new IllegalArgumentException("threadPoolName is null");
        }
        if (threadPoolConfig == null) {
            throw new IllegalArgumentException("threadPoolConfig is null");
        }
        ThreadPoolExecutor threadPoolExecutor = threadPoolMap.get(threadPoolName);
        if (threadPoolExecutor != null) {
            updateThreadNumDynamically(threadPoolName, threadPoolConfig, threadPoolExecutor);
            logger.info("return cached threadPool:{},currentThread:{}", threadPoolName, Thread.currentThread());
            return threadPoolExecutor;
        }
        int initThreadNum = threadPoolConfig.getThreadNum(threadPoolName);

        threadPoolExecutor = new ThreadPoolExecutor(initThreadNum, initThreadNum, 0L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(true), new NamedThreadFactory(threadPoolName),
                handler == null ? new WaitingEnqueuePolicy() : handler);
        threadPoolMap.put(threadPoolName, threadPoolExecutor);

        return threadPoolExecutor;
    }
}
