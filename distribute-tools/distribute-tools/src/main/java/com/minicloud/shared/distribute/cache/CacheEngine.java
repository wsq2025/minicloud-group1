package com.minicloud.shared.distribute.cache;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 *
 * 基础缓存引擎
 *
 * @author wsq
 * @date 2021/1/24  11:03
 */
public interface CacheEngine {

    public String get(String key);

    public <T> T get(String key, Class<T> clz);

    public <T extends Serializable> boolean put(String key, T value, int expiredTime, TimeUnit timeUnit);

    /**
     *
     * 基于key删除缓存数据
     *
     * @param key
     * @return
     */
    public boolean invalid(String key);

    /**
     *
     * 指定过期时间自增计数器，默认每次加1，非滑动窗口
     *
     * @param key
     * @param expiredTime
     * @param timeUnit
     * @return
     */
    public long incrCount(String key, int expiredTime, TimeUnit timeUnit);

    /**
     *
     * 指定过期时间自增计数器，过期时间内超过最大值rateThreshould返回true,反之返回false
     *
     * @param key
     * @param rateThreshold
     * @param expiredTime
     * @param timeUnit
     * @return
     */
    public boolean rateLimit(final String key, final int rateThreshold, int expiredTime, TimeUnit timeUnit);
}
