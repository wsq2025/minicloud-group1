package com.milicloud.shared.common.tools.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author wsq
 * @date 2021/1/21  23:34
 */
public abstract class AbstractStarter implements InitializingBean, ConcreateContextListener {

    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    protected AtomicBoolean isStarted = new AtomicBoolean(false);

    @Override
    public void afterPropertiesSet() throws Exception {
        ContextListenerManager.addListener(this);
    }

    @Override
    public void onContextRefreshed() throws Exception {
        if(isStarted.compareAndSet(false,true)){
            log.info("{} start begin",this.getClass().getName());
            start();
            log.info("{} start done",this.getClass().getName());
        }
    }

    public abstract void start() throws Exception;

    @Override
    public void onContextClosed() throws Exception {
        if(isStarted.compareAndSet(true,false)){
            log.info("{} shundown begin",this.getClass().getName());
            shundown();
            log.info("{} shundown done",this.getClass().getName());
        }
    }

    protected abstract void shundown() throws Exception;
}
