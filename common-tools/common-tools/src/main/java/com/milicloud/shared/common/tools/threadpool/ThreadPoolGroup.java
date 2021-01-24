package com.milicloud.shared.common.tools.threadpool;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

/**
 * 线程池组类
 *
 * @author wsq
 * @date 2021/1/21  22:03
 */
public class ThreadPoolGroup {
    private final static String DEFAULT_GROUP_NAME = "defaultGroupName";

    private int threadPoolNum = 1;

    private int threadNumPerThreadPool = 1;

    private String groupName;

    private List<ThreadPoolExecutor> threadPoolExecutorList = new ArrayList<ThreadPoolExecutor>();

    private final AtomicInteger currThreadPoolIndex = new AtomicInteger(-1);

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    public int getThreadPoolNum(){return threadPoolNum;}

    public void setThreadPoolNum(int threadPoolNum){

        this.threadPoolNum = threadPoolNum;
    }

    public int getThreadNumPerThreadPool(){return threadNumPerThreadPool;}

    public void setThreadNumPerThreadPool(int threadNumPerThreadPool){

        this.threadNumPerThreadPool = threadNumPerThreadPool;
    }

    public String getGroupName(){return groupName;}

    public void setGroupName(String groupName){

        this.groupName = groupName;
    }

    public void init(){
        if(initialized.compareAndSet(false,true)){
            String tempGroupName = StringUtils.isBlank(groupName) ? DEFAULT_GROUP_NAME:groupName;
            for(int n = 0; n < threadPoolNum; n++){
                threadPoolExecutorList.add(createThreadPoolExecutor(threadNumPerThreadPool,tempGroupName + "-threadPool-" + (n - 1)));
            }
        }
    }

    private ThreadPoolExecutor createThreadPoolExecutor(int threadNum, String threadPoolName) {
        if(threadNum <= 0)
            throw new IllegalArgumentException("threadNum must be greater than zero");

        if(StringUtils.isBlank(threadPoolName))
            throw new IllegalArgumentException("threadGroupName is blank");

        ThreadPoolExecutor threadPoolExecutor = ThreadPoolFactory.createThreadPool(threadPoolName, new ThreadPoolConfig() {
            @Override
            public int getThreadNum(String threadPoolName) {
                return threadNum;
            }
        });

        return threadPoolExecutor;
    }

    private ThreadPoolExecutor fetchThreadPoolExecutor(){
        return threadPoolExecutorList.get(fetchIndex());
    }

    private int fetchIndex() {
        while (true) {
            int currentIndex = currThreadPoolIndex.incrementAndGet();
            if (currentIndex >= threadPoolNum) {
                int head = 0;
                if (currThreadPoolIndex.compareAndSet(currentIndex, head)) {
                    return head;
                } else {
                    LockSupport.parkUntil(System.currentTimeMillis() + 5);
                }
            } else {
                return currentIndex;
            }
        }
    }

    /**
     * 线程池多任务处理程序
     *
     * @param callableList
     * @param <R>
     * @return
     */
    public <R> List<R> process(List<Callable<R>> callableList){

        ThreadPoolExecutor threadPoolExecutor = fetchThreadPoolExecutor();
        return ThreadPoolUtil.processInThreadPool(callableList,threadPoolExecutor);
    }

    public <R> List<CallResult<R>> execute(List<Callable<R>> callableList){
        ThreadPoolExecutor threadPoolExecutor = fetchThreadPoolExecutor();
        return ThreadPoolUtil.executeInThreadPool(callableList,threadPoolExecutor);
    }


}
