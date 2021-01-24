package com.milicloud.shared.common.tools.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wsq
 * @date 2021/1/22  0:10
 */
public class ContextListenerManager {
    private final static Logger log = LoggerFactory.getLogger(ContextListenerManager.class);

    private static List<ConcreateContextListener> listenerList = new ArrayList<>();

    public static void addListener(ConcreateContextListener listener){listenerList.add(listener);}

    public static void onContextRefreshed(){
        for(ConcreateContextListener listener : listenerList){
            try {
                log.info("{},onContextRefreshed begin",listener.getClass().getName());
                listener.onContextRefreshed();
                log.info("{},onContextRefreshed done",listener.getClass().getName());
            }catch (Exception e){
                log.error("{},onContextRefreshed error ,errorMsg:{}",listener.getClass().getName(),e.getMessage(),e);
            }
        }
    }

    public static void onContextClosed() {
        for (ConcreateContextListener listener : listenerList) {
            try {
                log.info("{},onContextClosed begin", listener.getClass().getName());
                listener.onContextClosed();
                log.info("{},onContextClosed done", listener.getClass().getName());
            } catch (Exception ex) {
                log.error("{},onContextClosed error ,errorMsg:{}", listener.getClass().getName(), ex.getMessage(), ex);
            }
        }
    }
}
