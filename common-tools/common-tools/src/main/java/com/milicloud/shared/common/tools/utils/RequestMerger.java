package com.milicloud.shared.common.tools.utils;

import org.omg.CORBA.PRIVATE_MEMBER;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

/**
 * 请求合并类
 *
 * @author wsq
 * @date 2021/1/23  15:34
 */
public class RequestMerger<T, R> {

    public static Logger log = LoggerFactory.getLogger(RequestMerger.class);

    private LinkedBlockingQueue<Request<T, R>> queue;

    private int mergerCount;

    private long mergerMillisInteval;

    private int queueCapacity = 1;

    private MergerHandler mergerHandler;

    private static final int PARKING_TIME = 3000;

    public RequestMerger(int mergerCount, long mergerMillisInteval,
                         int queueCapacity, MergerHandler<T, R> mergerHandler){
        this.mergerCount = mergerCount;
        this.mergerMillisInteval = mergerMillisInteval;
        this.queueCapacity = queueCapacity;
        this.mergerHandler = mergerHandler;
        init();
    }

    void init() {
        queue = new LinkedBlockingQueue<>(queueCapacity);
        Thread thread = new Thread(() -> {
            while(true){
                try {
                    Request request = queue.peek();
                    int queueSize = queue.size();
                    boolean isTimeSatisfied = request != null && (System.currentTimeMillis()) >= mergerMillisInteval;
                    boolean isCountSatisfied = queueSize >= mergerCount;
                    int minCount = Math.min(queueSize,mergerCount);

                    if(isTimeSatisfied || isCountSatisfied){
                        log.info("开始合并,当前队列数量：{},当前合并数量：{}",queueSize,minCount);
                        List<Request> mergerRequests = new ArrayList<>();
                        for(int i = 0; i < minCount; i++){
                            mergerRequests.add(queue.take());
                        }
                        mergerHandler.handle(mergerRequests);
                    } else {
                        LockSupport.parkUntil(System.currentTimeMillis() + PARKING_TIME);
                    }
                }catch (Throwable e){
                    log.error("requestMerger thread error,errorMsg:{}",e.getMessage(),e);
                }
            }
        });
        thread.setName("requestMergerThread");
        thread.start();
    }

    public void putRequest(Request<T, R> request) throws InterruptedException{
        request.setEnqueueTime(System.currentTimeMillis());
        queue.put(request);
    }

    public boolean offerRequest(Request<T, R> request, int timeout, TimeUnit timeUnit) throws InterruptedException{
        request.setEnqueueTime(System.currentTimeMillis());
        return queue.offer(request,timeout,timeUnit);
    }

    public int queueSize(){return queue.size();}

    public static class Request<T, R> {

        private long enqueueTime;

        /*
        请求参数，最好标识唯一的请求
         */
        private T param;

        /*
        请求结果
         */
        private R result;

        private FutureTask<R> futureTask;

        public Request() {
            init();
        }

        private void init() {
            futureTask = new FutureTask<>(() -> result);
        }

        public long getEnqueueTime() {
            return enqueueTime;
        }

        public void setEnqueueTime(long enqueueTime) {
            this.enqueueTime = enqueueTime;
        }

        /*
        阻塞等待请求结果返回
         */
        public R getResult() throws ExecutionException, InterruptedException {
            return futureTask.get();
        }

        public R getResult(long timeout, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
            return futureTask.get(timeout, timeUnit);
        }

        /*
        设置请求结果，触发结果读取
         */
        public void setResult(R result) {
            this.result = result;
            futureTask.run();
        }

        public T getParam() {
            return param;
        }

        public void setParam(T param) {
            this.param = param;
        }

    }

    public interface MergerHandler<T, R>{
        void handle(List<Request<T, R>> requests) throws Exception;
    }
}
