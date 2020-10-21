package com.fjg.tb.util;

import android.os.Handler;

/**
 * Created by mcs on 2015/11/3.
 */
public class ThreadPoolUtils {
    /**
     * @param task
     */
    public static void runTaskInThread(Runnable task) {
        ThreadPoolFactory.getCommonThreadPool().execute(task);
    }

    private static Handler handler = new Handler();

    /**
     * @param task
     */
    public static void runTaskInUIThread(Runnable task, long waitTime) {
        handler.postDelayed(task, waitTime);
    }

}
