package com.gpack.pay.paylib;

import android.content.Context;

import com.gpack.pay.paylib.util.DU;
import com.gpack.pay.paylib.util.TextU;

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

    private static QueryResultCallback queryResultCallback;

    private static OrderCheck orderCheck;

    private static OrderCheckResult orderCheckResult;

    private static int reportType;

    private static String phoneImei;

    public static final int TYPE_PIXCY = 100;

    public static final int CONSUME = 167;
    public static final int KEEP = 248;

    private static int consumeOrKeep = CONSUME;

    private static Context context;
    private static PurchasePresenterImp purchasePresenterImp;

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
     * init purchase presenter
     *
     * @param context
     * @param productId
     */
    public static void initPurchasePresenter(Context context, String productId) {
        purchasePresenterImp = new PurchasePresenterImp();
        purchasePresenterImp.init(context, productId);
    }

    public static void destroyPresenter() {
        if (purchasePresenterImp != null)
            purchasePresenterImp.destroy();
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
     * set report type and imei
     *
     * @param type
     * @param imei
     */
    public static void setReportTypeAndImei(int type, String imei) {
        if (type != TYPE_PIXCY) return;

        reportType = type;
        phoneImei = imei;
    }

    public static void setPhoneImei(String imei) {
        phoneImei = imei;
    }

    public static String getPhoneImei() {
        return phoneImei;
    }

    public static int getReportType() {
        return reportType;
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

    public static PurchasePresenterImp getPurchasePresenterImp() {
        return purchasePresenterImp;
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

    public static QueryResultCallback getQueryResultCallback() {
        return queryResultCallback;
    }

    public static void setQueryResultCallback(QueryResultCallback queryResultCallback) {
        PayDelegate.queryResultCallback = queryResultCallback;
    }

    public static int getConsumeOrKeep() {
        return consumeOrKeep;
    }

    public static void setConsumeOrKeep(int consume) {
        consumeOrKeep = consume;
    }

    /**
     * assign in-app item query result call back
     * <p>
     * <p>
     * <p>
     * INAPP_PURCHASE_ITEM_LIST---[]
     * RESPONSE_CODE---0
     * INAPP_PURCHASE_DATA_LIST---[]
     * INAPP_DATA_SIGNATURE_LIST---[]
     * <p>
     * INAPP_PURCHASE_ITEM_LIST---[movedata2sdcard]
     * RESPONSE_CODE---0
     * INAPP_PURCHASE_DATA_LIST---[{"orderId":"GPA.1377-0993-1239-63775","packageName":"me.pixcy.smartcleaner.pro","productId":"movedata2sdcard","purchaseTime":1458900535741,"purchaseState":0,"developerPayload":"ASDFGVASDFGVASDFGVASDFGVASDFGVASDFGVJo4pZXCVB","purchaseToken":"legeejmcbndiapicacbmcdcf.AO-J1OwJT1rxZpLES4pUvordTBubvECCb9bnIYbkWuWr5FhTsDQq4yQhuBdu-HDC8FY-CxxsDz9lLxTQHRZXms2SO7Q5cKLlRvW7nrGZIJJrSRGOiMvOBYP_x1Db49xD23Ry4KMx7Tk9"}]
     * INAPP_DATA_SIGNATURE_LIST---[eflMX2EmYxL0MLC5AX9CXDpnsPe4vFm2uD+0OzUHFIaRSXUnB2Q+ryblxa+HF0dmrptB+oQVJqc1ZGiFKSmvtVEejgMimyZ9fiA9lzU7DGlJUiAW8tY9o4X02uiX3N4MVS+3TITlSevD8j66LJgg96eHpeGLUFErQM8q/ysGCOnPNOWNqLnCaE6o/NptGZKVrI1jiC5gvkRA8BkgFGHEbY3oV7upfNizpfZxLKjEvo1S1j7ssf+XFPwpvqcaERwNU4e2z1fMyiN1siVTisTVTDiC8YaCWoYMDuMPvGXlLLvgYnviqg1WByf2B/Y8CqYQ9lIUnaLK/cV8r+OsYe9EVA==]
     *
     * @param result result callback
     */
    public static void assignInappItemQueryCallback(QueryResultCallback result) {
        queryResultCallback = result;
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
