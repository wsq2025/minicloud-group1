package com.minicloud.shared.distribute.common;

import com.minicloud.shared.distribute.cache.CacheEngine;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.apache.commons.lang.StringUtils;
/**
 * @author wsq
 * @date 2021/1/24  12:52
 */
public class RateLimitTemplate {

    private CacheEngine cacheEngine;

    public CacheEngine getCacheEngine(){return cacheEngine;}

    public void setCacheEngine(CacheEngine cacheEngine){this.cacheEngine = cacheEngine;}

    public <T> T execute(String limitKey, Supplier<T> resultSupplier,long rateThreshold, long limitTime,TimeUnit timeUnit,String errorCode){
        return execute(limitKey,resultSupplier,rateThreshold,limitTime,-1,timeUnit,errorCode);
    }

    /**
     * @param limitKey  限流KEY
     * @param resultSupplier 回调方法
     * @param rateThreshold 限流阈值
     * @param limitTime 限流时间段
     * @param blockDuration 阻塞时间段
     * @param timeUnit 单位
     * @param errorCode 指定限流错误码
     * @param <T>
     * @return
     */
    private <T> T execute(String limitKey, Supplier<T> resultSupplier, long rateThreshold, long limitTime, int blockDuration, TimeUnit timeUnit, String errorCode) {
        boolean blocked = tryAcquire(limitKey, rateThreshold, limitTime, blockDuration, timeUnit);
        if (StringUtils.isNotBlank(errorCode)) {
            if (!blocked)
                return null;
        } else if (blocked) {
            return null;
        }

        return resultSupplier.get();
    }

    public boolean tryAcquire(String limitKey, long rateThreshold, long limitTime, TimeUnit timeUnit){
        return tryAcquire(limitKey,rateThreshold,limitTime,-1,timeUnit);
    }

    private boolean tryAcquire(String limitKey, long rateThreshold, long limitTime, int blockDuration, TimeUnit timeUnit) {
        limitKey = genLimitKey(limitKey,timeUnit);
        String blockKey = genBlockKey(limitKey,timeUnit);
        String blockValue = cacheEngine.get(blockKey);
        if(StringUtils.isNotBlank(blockValue)){
            return false;
        }

        boolean blocked = cacheEngine.rateLimit(limitKey, (int) rateThreshold, (int) limitTime, timeUnit);
        if(blocked){
            if(blockDuration > 0){
                cacheEngine.put(blockKey,blockKey,(int)blockDuration,timeUnit);
            }

            return false;
        }

        return true;
    }

    protected String genLimitKey(String key, TimeUnit timeUnit) {
        return String.format("rate_%s_%s",key,timeUnit);
    }

    protected String genBlockKey(String key, TimeUnit timeUnit) {
        return String.format("block_%s_%s",key,timeUnit);
    }
}
