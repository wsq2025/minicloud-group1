package com.minicloud.shared.distribute.limit;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @author wsq
 * @date 2021/1/24  11:24
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface RateLimit {

    /**
     * 限流key
     */
    String limitKey();

    /**
     * 允许访问的次数，默认MAX_VALUE
     */
    long limitCount() default Integer.MAX_VALUE;

    /**
     * 时间段
     */
    long timeRange();

    /**
     * 阻塞时间段
     */
    long blockDuration();

    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
