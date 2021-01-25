package com.milicloud.shared.common.tools.spring;

/**
 * @author wsq
 * @date 2021/1/21  23:28
 */
public interface ConcreateContextListener {

    void onContextRefreshed() throws Exception;

    void onContextClosed() throws Exception;
}
