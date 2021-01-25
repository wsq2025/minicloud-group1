package com.milicloud.test;

import com.milicloud.shared.common.tools.utils.RequestMerger;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * 高并发场景合并请求，减少数据库访问
 *
 * @author wsq
 * @date 2021/1/23  21:01
 */
@Slf4j
class RequestMergerTest {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        RequestMerger requestMerger = new RequestMerger(5, 10, 20, new HandleTest());

        //Thread.sleep(3000);

        Random random = new Random(10);
        for(int i = 0; i < 50; i++) {
            final String param = "p" + i;
            Thread thread = new Thread(() -> {
                log.info("开始请求");
                RequestMerger.Request request = new RequestMerger.Request();
                request.setParam(param);
                try {
                    requestMerger.putRequest(request);
                    //阻塞等待
                    log.info("返回请求结果：code-{},value-{}", request.getParam(), request.getResult());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            });
            Thread.sleep(random.nextInt(500));
            thread.start();
        }
    }
}

/*
批量处理，返回对应结果
 */
@Slf4j
class HandleTest<T, R> implements RequestMerger.MergerHandler<T, R>{
    @Override
    public void handle(List<RequestMerger.Request<T, R>> list) throws Exception {

        HashMap<String,Object> responses = new HashMap<String, Object>();

        List<String> params = new ArrayList<String>();
        for (RequestMerger.Request r : list){
            params.add(String.valueOf(r.getParam()));
        }

        responses = getInfosByParams(params);
        for (RequestMerger.Request r : list){
            r.setResult(responses.get(r.getParam()));
        }
    }

    private HashMap<String, Object> getInfosByParams(List<String> params) {
        Random random = new Random(5);
        HashMap<String, Object> responses = new HashMap<String, Object>();
        log.info("请求合并处理****,处理数量：{}",params.size());
        for (String code : params) {
            int value = random.nextInt(100);
            log.info("code:{},vale:{}", code, value);
            responses.put(code, value);
        }
        return responses;
    }
}
