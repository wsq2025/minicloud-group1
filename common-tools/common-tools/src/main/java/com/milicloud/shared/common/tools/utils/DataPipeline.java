package com.milicloud.shared.common.tools.utils;

import com.milicloud.shared.common.tools.threadpool.ThreadPoolUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * 分步的多线程数据处理管道
 *
 * @author wsq
 * @date 2021/1/22  1:32
 */
public class DataPipeline<T> {
    private static final Logger log = LoggerFactory.getLogger(DataPipeline.class);

    private List<HandlerConfig<T>> handlerConfigs = new ArrayList<>();

    private DataPipeline(){}

    public static <T> DataPipeline<T> newPipeline(){return new DataPipeline<>();}

    public void pushData(List<T> dataList){
        handlerConfigs.forEach((dc) -> {
            handlerData(dataList,dc);
        });
    }

    private void handlerData(List<T> dataList, HandlerConfig<T> handlerConfig) {
        if(handlerConfig.handlerTransferType == HandlerTransferType.SYNC){
            List<Callable<Void>> callableList = dataList.stream().map((d) -> (Callable<Void>)() -> {
                handlerConfig.dataHandler.handler(d);
                return null;
            }).collect(Collectors.toList());
            ThreadPoolUtil.executeInThreadPool(callableList,handlerConfig.executorService);
        }else{
            dataList.forEach((d) -> {
                handlerConfig.executorService.execute(() -> {
                    handlerConfig.dataHandler.handler(d);
                });
            });
        }
    }

    public DataPipeline addHandler(DataHandler<T> dataHandler, ExecutorService executorService){
        return this.addHandler(dataHandler,executorService,HandlerTransferType.SYNC);
    }

    public DataPipeline addHandler(DataHandler<T> dataHandler, ExecutorService executorService, HandlerTransferType type) {
        Objects.requireNonNull(dataHandler,"dataHandler is null");
        Objects.requireNonNull(executorService, "executorService is null");
        Objects.requireNonNull(type, "handlerTransferType is null");

        HandlerConfig config = new HandlerConfig();
        config.dataHandler = new DataHandlerWrapper(dataHandler);
        config.executorService = executorService;
        config.handlerTransferType = type;
        handlerConfigs.add(config);
        return this;
    }

    @FunctionalInterface
    public interface DataHandler<T>{
        void handler(T t);
    }

    public enum HandlerTransferType{
        /**
         *同步方式，多个handler串行
         */
        SYNC,

        /**
         *异步，多handler并行
         */
        ASYNC;
    }

    private class HandlerConfig<T>{
        DataHandler<T> dataHandler;
        ExecutorService executorService;
        HandlerTransferType handlerTransferType;
    }

    private class DataHandlerWrapper<T> implements DataHandler<T>{

        DataHandler<T> dataHandler;

        public DataHandlerWrapper(DataHandler<T> dataHandler){this.dataHandler = dataHandler;}

        @Override
        public void handler(T t) {
            try {
                this.dataHandler.handler(t);
            }catch (Exception ex){
                log.error("dataHandler:{},exception caught,errorMsg:{}", dataHandler.getClass(), ex.getMessage(),
                        ex);
            }
        }
    }
}
