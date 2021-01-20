package com.milicloud.shared.common.tools.threadpool;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author wsq
 * @date 2021/1/20  0:02
 */
public class WaitingEnqueuePolicy implements RejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        try {
            if(!executor.isShutdown()){
                executor.getQueue().put(r);
            }
        }catch (InterruptedException e){
            throw new RuntimeException(e.getMessage(),e);
        }
    }
}
