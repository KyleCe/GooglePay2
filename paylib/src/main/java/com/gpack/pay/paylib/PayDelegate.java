package com.gpack.pay.paylib;

import com.gpack.pay.paylib.util.DU;
import com.gpack.pay.paylib.util.TextU;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by KyleCe on 2016/1/18.
 *
 * @author KyleCe
 */
public class PayDelegate {
    private static String publishKey;
    private static String userId;

    private static PayCallBack callBack;

    private static OrderCheck orderCheck;

    private static OrderCheckResult orderCheckResult;

    /**
     * init the api with publish key and user id
     *
     * @param key publish key
     * @param uid user id
     * @throws IllegalArgumentException "the publish key cannot be null or empty"
     * @throws IllegalArgumentException "the user id cannot be null or empty"
     */
    public static void initAPI(String key, String uid) {
        if (TextU.isEmpty(key))
            throw new IllegalArgumentException("the publish key cannot be null or empty");

        if (TextU.isEmpty(uid))
            throw new IllegalArgumentException("the user id cannot be null or empty");

        setPublishKey(key);
        setUserId(uid);
    }

    /**
     * function to turn on the print function or not
     *
     * @param switcher thw boolean flag to control the whole print method
     */
    public static void turnOnPrint(boolean switcher) {
        DU.setON(switcher);
    }

    /**
     * set the pay api call back for user
     *
     * @param callBack the call back that will be called while the process finish
     * @throws NullPointerException the param callback is null may invoke the exception
     */
    public static void setPayCallBack(PayCallBack callBack) {
        if (DU.isNull(callBack))
            throw new NullPointerException("the call back interface cannot be null");

        setCallBack(callBack);
    }

    public static PayCallBack getCallBack() {
        return callBack;
    }

    private static void setCallBack(PayCallBack callBack) {
        PayDelegate.callBack = callBack;
    }

    public static OrderCheckResult getOrderCheckResult() {
        return orderCheckResult;
    }

    public static void setOrderCheckResult(OrderCheckResult orderCheckResult) {
        PayDelegate.orderCheckResult = orderCheckResult;
    }

    public static String getPublishKey() {
        return publishKey;
    }

    private static void setPublishKey(String publishKey) {
        PayDelegate.publishKey = publishKey;
    }

    public static String getUserId() {
        return userId;
    }

    public static void setUserId(String userId) {
        PayDelegate.userId = userId;
    }

    public static void setOrderCheck(OrderCheck cake) {
        orderCheck = cake;
    }

    public static OrderCheck getOrderCheck() {
        return orderCheck;
    }

    private static final int MAX_THREAD_COUNT = 3;
    private static ExecutorService mThreadPoolExecutor;

    /**
     * get thread pool service, limited by the max thread count variable
     *
     * @return thread pool service executor
     * @see #MAX_THREAD_COUNT
     */
    public static ExecutorService getThreadPool() {
        if (mThreadPoolExecutor == null) {
            return mThreadPoolExecutor = Executors.newFixedThreadPool(MAX_THREAD_COUNT);
        }
        return mThreadPoolExecutor;
    }

    /**
     * execute runnable with thread pool
     *
     * @param runnable to execute
     * @see #getThreadPool()
     */
    public static void execute(Runnable runnable) {
        if (DU.isNull(runnable)) return;

        getThreadPool().execute(runnable);
    }
}
