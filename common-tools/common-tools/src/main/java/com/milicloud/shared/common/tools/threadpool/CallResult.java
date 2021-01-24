package com.milicloud.shared.common.tools.threadpool;

/**
 * Callable调用结果封装
 *
 * @author wsq
 * @date 2021/1/21  20:29
 */
public class CallResult<R> {
    private  Throwable throwable;

    private  R resultData;

    public Throwable getThrowable(){return throwable;}

    public void setThrowable(Throwable throwable){this.throwable = throwable;}

    public R getResultData(){return resultData;}

    public void setResultData(R resultData){this.resultData = resultData;}

    public static CallResult newFailResult(Throwable throwable){
        CallResult callResult = new CallResult();
        callResult.setThrowable(throwable);
        return callResult;
    }

    public static <R> CallResult newSuccessResult(R resultData){
        CallResult callResult = new CallResult();
        callResult.setResultData(resultData);
        return callResult;
    }
}
