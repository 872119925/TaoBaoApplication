package com.fjg.tb.util;

/**
 * Created by mcs on 2015/11/3.
 */
public class ThreadPoolFactory {

    private static ThreadPoolProxy commonThreadPool;

    public static final int Common_CORE_POOL_SIZE = 8;
    public static final int Common_MAX_POOL_SIZE = 8;
    public static final int Common_KEEP_LIVE_TIME = 20;

    public static ThreadPoolProxy getCommonThreadPool() {
        if (commonThreadPool == null) {
            synchronized (ThreadPoolFactory.class) {
                if (commonThreadPool == null) {
                    commonThreadPool = new ThreadPoolProxy(Common_CORE_POOL_SIZE, Common_MAX_POOL_SIZE, Common_KEEP_LIVE_TIME);
                }
            }
        }
        return commonThreadPool;
    }
}
