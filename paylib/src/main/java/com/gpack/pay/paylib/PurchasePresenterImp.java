package com.gpack.pay.paylib;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.gpack.pay.paylib.util.DU;
import com.gpack.pay.paylib.util.HttpUtils;
import com.gpack.pay.paylib.util.IabBroadcastReceiver;
import com.gpack.pay.paylib.util.IabHelper;
import com.gpack.pay.paylib.util.IabResult;
import com.gpack.pay.paylib.util.Inventory;
import com.gpack.pay.paylib.util.PayURL;
import com.gpack.pay.paylib.util.Purchase;
import com.gpack.pay.paylib.util.SkuDetails;
import com.gpack.pay.paylib.util.TextU;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by KyleCe on 2016/3/26.
 *
 * @author: KyleCe
 */
public class PurchasePresenterImp implements PurchasePresenter
        , IabBroadcastReceiver.IabBroadcastListener {

    private static final String TAG = PurchasePresenterImp.class.getSimpleName();

    // The helper object
    IabHelper mHelper;

    // Provides purchase notification while this app is running
    IabBroadcastReceiver mBroadcastReceiver = null;

    // inventory query from server
    private static Inventory mInventory = null;

    Context context;

    // SKUs for our products: the premium upgrade (non-consumable) and gas (consumable)
    static final String SKU_PREMIUM = "premium";
    static final String SKU_GAS = "gas";
    // SKU for our subscription (infinite gas)
    static final String SKU_INFINITE_GAS_MONTHLY = "infinite_gas_monthly";
    static final String SKU_INFINITE_GAS_YEARLY = "infinite_gas_yearly";

    // Does the user have the premium upgrade?
    boolean mIsPremium = false;

    // Does the user have an active subscription to the infinite gas plan?
    boolean mSubscribedToInfiniteGas = false;

    // Will the subscription auto-renew?
    boolean mAutoRenewEnabled = false;

    // Tracks the currently owned infinite gas SKU, and the options in the Manage dialog
    String mInfiniteGasSku = "";
    String mFirstChoiceSku = "";
    String mSecondChoiceSku = "";

    // Used to select between purchasing gas on a monthly or yearly basis
    String mSelectedSubscriptionPeriod = "";

    private String productId = "";
    private String payload = "";


    // How many units (1/4 tank is our unit) fill in the tank.
    static final int TANK_MAX = 4;

    // Current amount of gas in tank, in units
    int mTank;

    private static final String PAYLOAD_ON_GOOGLE_WEB = "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ";

    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 10001;

    @Override
    public void init(final Context context, final String productId) {
        if (context == null) throw new NullPointerException(TAG + "context nonnull");

        this.context = context;
//        String base64EncodedPublicKey = getString(R.string.base64_publish_kty);
        String base64EncodedPublicKey = PayDelegate.getPublishKey();

        // Some sanity checks to see if the developer (that's you!) really followed the
        // instructions to run this sample (don't put these checks on your app!)
        if (base64EncodedPublicKey.contains("CONSTRUCT_YOUR")) {
            throw new RuntimeException("Please put your app's public key in Activity.java. See README.");
        }
        if (context.getPackageName().startsWith("com.example")) {
            throw new RuntimeException("Please change the sample's package name! See README.");
        }

        // Create the helper, passing it our context and the public key to verify signatures with
        Log.d(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(context, base64EncodedPublicKey);

        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(true);

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    complain("Problem setting up in-app billing: " + result);
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;

                // Important: Dynamically register for broadcast messages about updated purchases.
                // We register the receiver here instead of as a <receiver> in the Manifest
                // because we always call getPurchases() at startup, so therefore we can ignore
                // any broadcasts sent while the app isn't running.
                // Note: registering this listener in an Activity is a bad idea, but is done here
                // because this is a SAMPLE. Regardless, the receiver must be registered after
                // IabHelper is setup, but before first call to getPurchases().
                mBroadcastReceiver = new IabBroadcastReceiver(PurchasePresenterImp.this);
                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                context.registerReceiver(mBroadcastReceiver, broadcastFilter);

                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d(TAG, "Setup successful. Querying inventory.");
//                mHelper.queryInventoryAsync(mGotInventoryListener);

                // add the product IDs
                ArrayList<String> skuList = new ArrayList<String>();

                if (TextU.hasContent(productId))
                    skuList.add(productId);

                // comment for pass buy intent test
                mHelper.queryInventoryAsync(skuList, mGotInventoryListener);

            }
        });

    }

    @Override
    public void destroy() {
        destroyTheProcess();
    }

    private final int DO_PURCHASE_FROM_INTENT = 0;


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

                case DO_PURCHASE_FROM_INTENT:

                    DU.sd("handle", "handle product, id=" + productId);

                    if (DU.notNull(productId))
                        purchaseGoods(productId);

                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void receivedBroadcast() {

    }


    void complain(String message) {
        Log.e(TAG, "**** TrivialDrive Error: " + message);


        destroyTheProcess();

        //alert("Error: " + message);
    }


    /**
     * destroy the process
     */
    private void destroyTheProcess() {
        // very important:

        // very important:
        Log.d(TAG, "Destroying helper.");
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }

        try {
            if (mBroadcastReceiver != null) {
                context.unregisterReceiver(mBroadcastReceiver);
                mBroadcastReceiver = null;
            }
        } catch (Exception e) {
            DU.sd("broad cast receiver unregister error", e);
        }

    }


    String SKU_ANDROID_TEST_PURCHASE_GOOD = "android.test.purchased";

    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                return;
            }

            // FIXME: 2016/3/25 consume android.test.purchased
            if (inventory.hasPurchase(SKU_ANDROID_TEST_PURCHASE_GOOD)) {
                mHelper.consumeAsync(inventory.getPurchase(SKU_ANDROID_TEST_PURCHASE_GOOD), null);

                DU.sd("test purchase consuming", "real happen");
            }

            Log.d(TAG, "Query inventory was successful.");

            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */

            // store the inventory
            if (inventory != null)
                mInventory = inventory;

            // Do we have the premium upgrade?
            Purchase premiumPurchase = inventory.getPurchase(SKU_PREMIUM);
            mIsPremium = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));
            Log.d(TAG, "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));

            // First find out which subscription is auto renewing
            Purchase gasMonthly = inventory.getPurchase(SKU_INFINITE_GAS_MONTHLY);
            Purchase gasYearly = inventory.getPurchase(SKU_INFINITE_GAS_YEARLY);
            if (gasMonthly != null && gasMonthly.isAutoRenewing()) {
                mInfiniteGasSku = SKU_INFINITE_GAS_MONTHLY;
                mAutoRenewEnabled = true;
            } else if (gasYearly != null && gasYearly.isAutoRenewing()) {
                mInfiniteGasSku = SKU_INFINITE_GAS_YEARLY;
                mAutoRenewEnabled = true;
            } else {
                mInfiniteGasSku = "";
                mAutoRenewEnabled = false;
            }

            // The user is subscribed if either subscription exists, even if neither is auto
            // renewing
            mSubscribedToInfiniteGas = (gasMonthly != null && verifyDeveloperPayload(gasMonthly))
                    || (gasYearly != null && verifyDeveloperPayload(gasYearly));
            Log.d(TAG, "User " + (mSubscribedToInfiniteGas ? "HAS" : "DOES NOT HAVE")
                    + " infinite gas subscription.");
            if (mSubscribedToInfiniteGas) mTank = TANK_MAX;

            // Check for gas delivery -- if we own gas, we should fill up the tank immediately
            String sku = TextU.isEmpty(productId) ? SKU_GAS : productId;
            Purchase gasPurchase = inventory.getPurchase(sku);
            if (gasPurchase != null && verifyDeveloperPayload(gasPurchase)) {
                Log.d(TAG, "We have gas. Consuming it.");

//                // FIXME: 2016/2/18 not consume, hold the goods forever
                if (!PayActivity.isReportTypePixcy() || PayDelegate.getConsumeOrKeep() == PayDelegate.CONSUME)
                    try {
                        mHelper.consumeAsync(inventory.getPurchase(sku), mConsumeFinishedListener);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }

                return;
            }

            Log.d(TAG, "Initial inventory query finished; enabling main UI.");
//            handler.sendEmptyMessage(DO_PURCHASE_FROM_INTENT);
        }
    };


    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                complain("Error purchasing: " + result);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.");
                return;
            }

            Log.d(TAG, "Purchase successful.");

            // fixme: 2016/2/17 server check
            if (!isReportTypePixcy())
                recordDataToServer2ndEdition(purchase);

            // post purchase info to server
            DU.sd("post purchase info after purchase", "purchase json string=" + purchase.getOriginalJson());
            postPurchaseDataToServerAfter(purchase);

            if (purchase.getSku().equals(SKU_GAS) || purchase.getSku().equals(productId)) {
                // bought 1/4 tank of gas. So consume it.
                Log.d(TAG, "Purchase is gas. Starting gas consumption.");

                // FIXME: 2016/2/18  not consume, hold the goods forever
                if (!PayActivity.isReportTypePixcy() || PayDelegate.getConsumeOrKeep() == PayDelegate.CONSUME)
                    try {
                        mHelper.consumeAsync(purchase, mConsumeFinishedListener);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


            } else if (purchase.getSku().equals(SKU_PREMIUM)) {
                // bought the premium upgrade!
                Log.d(TAG, "Purchase is premium upgrade. Congratulating user.");
                mIsPremium = true;
            } else if (purchase.getSku().equals(SKU_INFINITE_GAS_MONTHLY)
                    || purchase.getSku().equals(SKU_INFINITE_GAS_YEARLY)) {
                // bought the infinite gas subscription
                Log.d(TAG, "Infinite gas subscription purchased.");
                mSubscribedToInfiniteGas = true;
                mAutoRenewEnabled = purchase.isAutoRenewing();
                mInfiniteGasSku = purchase.getSku();
                mTank = TANK_MAX;
            }
        }
    };

    // Called when consumption is complete
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            // We know this is the "gas" sku because it's the only one we consume,
            // so we don't check which sku was consumed. If you have more than one
            // sku, you probably should check...
            if (result.isSuccess()) {
                // successfully consumed, so we apply the effects of the item in our
                // game world's logic, which in our case means filling the gas tank a bit
                Log.d(TAG, "Consumption successful. Provisioning.");
                mTank = mTank == TANK_MAX ? TANK_MAX : mTank + 1;
//                saveData();
//                alert("You filled 1/4 tank. Your tank is now " + String.valueOf(mTank) + "/4 full!");
            } else {
                complain("Error while consuming: " + result);
            }
            Log.d(TAG, "End consumption flow.");
        }
    };


    void saveData(Activity act) {

        /*
         * WARNING: on a real application, we recommend you save data in a secure way to
         * prevent tampering. For simplicity in this sample, we simply store the data using a
         * SharedPreferences.
         */

        SharedPreferences.Editor spe = act.getPreferences(act.MODE_PRIVATE).edit();
        spe.putInt("tank", mTank);
        spe.apply();
        Log.d(TAG, "Saved data: tank = " + String.valueOf(mTank));
    }

    /**
     * Verifies the developer payload of a purchase.
     */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

//        return true;
        return payload.equals(this.payload);
    }

    private Activity activityBase;

//    @Override
//    public void purchaseActivityBase(Activity act) {
//        this.activityBase = act;
//    }

    /**
     * the buy core buy work, buy gas or the exact thing what you pass in
     * @throws NullPointerException activity to invoke purchase flow nonnull
     */
    public void purchaseGoods(String productId) {

        Log.d(TAG, "Buy gas button clicked.");

        DU.sd("goods id", "id = " + productId);

        if (activityBase == null)
            throw new NullPointerException("activity to invoke purchase flow nonnull");

//        if (mSubscribedToInfiniteGas) {
//            complain("No need! You're subscribed to infinite gas. Isn't that awesome?");
//            return;
//        }
//
//        if (mTank >= TANK_MAX) {
//            complain("Your tank is full. Drive around a bit!");
//            return;
//        }

        // launch the gas purchase UI flow.
        // We will be notified of completion via mPurchaseFinishedListener
        Log.d(TAG, "Launching purchase flow for gas.");

        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
//        String payload = "";/*comment for test*/
        String payload = PAYLOAD_ON_GOOGLE_WEB;

        DU.sd("payload check", "from intent: " + this.payload, "locale :" + payload);
        DU.sd("product id check", "from intent :" + this.productId, "locale :" + productId);

        String purchaseGoodsId = TextU.isEmpty(productId) ? SKU_GAS : productId;
        payload = TextU.hasContent(this.payload) ? this.payload : payload;

        mHelper.launchPurchaseFlow(activityBase, purchaseGoodsId, RC_REQUEST,
                mPurchaseFinishedListener, payload);

        // post the purchase data to server
        if (PayDelegate.getReportType() != PayDelegate.TYPE_PIXCY)/*type pixcy, no need to report before*/
            postPurchaseDataToServerBefore(productId, payload);
    }


    /**
     * post purchase data to server before the purchase operation
     *
     * @param productId product id to process
     * @param payload   the payload to check
     */
    private void postPurchaseDataToServerBefore(String productId, String payload) {
        // http://r.cutieriot.com/?type=order&
        // uid=xxx&
        // order_data={"productId":"xxx",type:"inapp",price:xxx,currency:xxx,"orderTime":1453127207346,"developerPayload":"xxx"}
        String url = urlHeadBuilder("order");
        HashMap<String, String> map = new HashMap<>();
        map.put("productId", productId);
        map.put("type", IabHelper.ITEM_TYPE_INAPP);

        SkuDetails skuDetails = mInventory.getSkuDetails(productId);
        if (skuDetails != null) {
            String price = skuDetails.getPrice();
            String currency = skuDetails.getPriceCurrencyCode();
            map.put("price", price);
            map.put("currency", currency);
        }

        String time = String.valueOf(System.currentTimeMillis());
        map.put("orderTime", time);
        map.put("developerPayload", payload);
        JSONObject json = new JSONObject(map);
        url += "&" + PayURL.ORDER_DATA_KEY + json.toString();

        recordDataToServer(url);
    }

    /**
     * if report type pixcy??
     */
    public static boolean isReportTypePixcy() {
        return PayDelegate.getReportType() == PayDelegate.TYPE_PIXCY;
    }

    /**
     * post purchase data to server after the purchase operation
     */
    private void postPurchaseDataToServerAfter(Purchase purchase) {
        String purchaseDataInJsonString = purchase.getOriginalJson();
        String signature = purchase.getSignature();

        String url = "";
        HashMap<String, String> param4CallBack = new HashMap<>();

        if (isReportTypePixcy()) {
            url = PayURL.P_HOST + PayURL.P_PURCHASE + purchaseDataInJsonString
                    + PayURL.P_SIGNATURE + signature
                    + PayURL.P_IMEI + PayDelegate.getPhoneImei();
            param4CallBack.put("imei", PayDelegate.getPhoneImei());
        } else {
            // http://r.cutieriot.com/?type=purchase&uid=xxx&purchase_data={purchase_data}
            url = urlHeadBuilder("purchase");
            url += "&" + PayURL.PURCHASE_DATA_KEY + purchaseDataInJsonString;

            // announce the api user by setting the call back
            param4CallBack.put("uid", PayDelegate.getUserId());
        }
        recordDataToServer(url);

        param4CallBack.put("purchase_data", TextU.isEmpty(purchaseDataInJsonString) ?
                "purchase data is null, check your code and account" : purchaseDataInJsonString);
        announceSuccessCallback(param4CallBack);
    }

    /**
     * url head builder
     * eg: http://r.cutieriot.com/?type=purchase&uid=xxx
     * eg: http://r.cutieriot.com/?type=order&uid=xxx
     *
     * @return the url head
     */
    @NonNull
    private String urlHeadBuilder(String type) {
        return PayURL.HOST + PayURL.TYPE_KEY + type +
                "&" + PayURL.UID_KEY + PayDelegate.getUserId();
    }

    /**
     * record the data on server with certain url,
     * if success, the response status code will be 200
     *
     * @param url the url to get
     */
    private void recordDataToServer(final String url) {
        PayDelegate.execute(new Runnable() {
            @Override
            public void run() {
                String result = HttpUtils.doGet(url);
                DU.sd("post purchase info ", "url=" + url, "result=" + result);
            }
        });
    }

    /**
     * record the data on server with certain url,
     * if success, the response status code will be 0
     * <p>
     * add on 2016-2-17 15:01:14
     *
     * @param purchase the url to get
     */
    private void recordDataToServer2ndEdition(Purchase purchase) {

        if (PayDelegate.getOrderCheck() == null) {
            sop("unexpected order info,check the " + PayDelegate.class.getSimpleName() + " , and see if the " +
                    PayDelegate.getOrderCheck().getClass().getSimpleName() + " is set correctly");
            return;// null order check build
        }

        String transaction = purchase.getOriginalJson();
        String logTmp = transaction;
        transaction = TextU.encode64(transaction);
        DU.sd("transaction data, expected purchase data", "before encode=" + logTmp, "after encode=" + transaction);

        String signature = purchase.getSignature();
        signature = TextU.encode64(signature);// encode

        StringBuilder url = new StringBuilder();
        OrderCheck order = PayDelegate.getOrderCheck();

        long time = System.currentTimeMillis();

        String publicKey = order.getPublickey();
        publicKey = TextU.encode64(publicKey);// encode

        url.append(PayURL.PAY_HOST)
                .append(PayURL.APPID).append(order.getAppid())
                .append(PayURL.PRODUCT_ID).append(order.getProductid())
                .append(PayURL.TRANSACTION).append(transaction)
                .append(PayURL.RECEIPT).append(signature)
                .append(PayURL.TIME).append(String.valueOf(time))
                .append(PayURL.SIGN).append(order.getSign())
                .append(PayURL.ACCOUNT_ID).append(order.getAccountid())
                .append(PayURL.ZONE_ID).append(order.getZoneid())
                .append(PayURL.ROLE_ID).append(order.getRoleid())
                .append(PayURL.PRODUCT_NAME).append(order.getProductname())
                .append(PayURL.PLATFORM).append("google")
                .append(PayURL.PAY_DESCRIPTION).append(order.getPaydescription())
                .append(PayURL.PUBLIC_KEY).append(publicKey);

        final String url_f = url.toString();

        PayDelegate.execute(new Runnable() {
            @Override
            public void run() {
                String result = HttpUtils.doGet(url_f);
                DU.sd("order info", "url=" + url_f, "result=" + result);

                // report
                PayDelegate.getOrderCheckResult().onResult(result);
            }
        });
    }

    /**
     * system out print
     */
    private void sop(Object o) {
        if (o == null) return;
        System.out.println(o);
    }

    /**
     * announce the success call back
     */
    private void announceSuccessCallback(HashMap<String, String> map) {
        // if the response callback is set, response to the callback
        if (PayDelegate.getCallBack() != null) {
            PayDelegate.getCallBack().onSuccess(map);
        }

        destroyTheProcess();
    }
}
