package com.minicloud.shared.distribute.limit;

import com.minicloud.shared.distribute.common.ExpressionEvaluator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;

/**
 * @author wsq
 * @date 2021/1/24  11:33
 */
@Aspect
@Order(1000)
public class RateLimitAspect {

    private ExpressionEvaluator evaluator = new ExpressionEvaluator();

    @Pointcut("@annotation(com.minicloud.shared.distribute.limit.RateLimit)")
    public void rateLimitPointcut(){}

    @Around("rateLimitPointcut()&&@annotation(rateLimit)")
    public Object limitBlock(ProceedingJoinPoint joinPoint,RateLimit rateLimit){
        Object[] args = joinPoint.getArgs();
        Method method = ((MethodSignature)joinPoint.getSignature()).getMethod();
        Class<?> clz = joinPoint.getTarget().getClass();
        String key =evaluator.getValue(joinPoint.getTarget(),args,clz,method,rateLimit.limitKey());

        String limitKey = clz.getSimpleName() + "#" + method.getName() + "_" +key;

        try {
            return joinPoint.proceed();
        }catch (Throwable throwable){
            return null;
        }
    }
}
