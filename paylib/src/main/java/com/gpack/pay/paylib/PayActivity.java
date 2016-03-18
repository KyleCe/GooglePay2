package com.gpack.pay.paylib;

import android.app.Activity;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.gpack.pay.paylib.util.DU;
import com.gpack.pay.paylib.util.HttpUtils;
import com.gpack.pay.paylib.util.IabBroadcastReceiver;
import com.gpack.pay.paylib.util.IabBroadcastReceiver.IabBroadcastListener;
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
import java.util.List;

public class PayActivity extends Activity implements IabBroadcastListener,
        DialogInterface.OnClickListener {
    // Debug tag, for logging
    static final String TAG = "PayActivity";

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

    // SKUs for our products: the premium upgrade (non-consumable) and gas (consumable)
    static final String SKU_PREMIUM = "premium";
    static final String SKU_GAS = "gas";

    // SKU for our subscription (infinite gas)
    static final String SKU_INFINITE_GAS_MONTHLY = "infinite_gas_monthly";
    static final String SKU_INFINITE_GAS_YEARLY = "infinite_gas_yearly";

    // (arbitrary) request code for the purchase flow
    static final int RC_REQUEST = 10001;


    // Graphics for the gas gauge
//    static int[] TANK_RES_IDS = {R.drawable.gas0, R.drawable.gas1, R.drawable.gas2,
//            R.drawable.gas3, R.drawable.gas4};

    // How many units (1/4 tank is our unit) fill in the tank.
    static final int TANK_MAX = 4;

    // Current amount of gas in tank, in units
    int mTank;

    // The helper object
    IabHelper mHelper;

    // Provides purchase notification while this app is running
    IabBroadcastReceiver mBroadcastReceiver = null;

    // for test
    private static final String PAYLOAD_ON_GOOGLE_WEB = "bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ";

    // inventory query from server
    private static Inventory mInventory = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pay_google_main);

        testDataIntentTransform();

        checkAPIInit();

        // load game data
        loadData();

        /* base64EncodedPublicKey should be YOUR APPLICATION'S PUBLIC KEY
         * (that you got from the Google Play developer console). This is not your
         * developer public key, it's the *app-specific* public key.
         *
         * Instead of just storing the entire literal string here embedded in the
         * program,  construct the key at runtime from pieces or
         * use bit manipulation (for example, XOR with some other string) to hide
         * the actual key.  The key itself is not secret information, but we don't
         * want to make it easy for an attacker to replace the public key with one
         * of their own and then fake messages from the server.
         */

//        String base64EncodedPublicKey = getString(R.string.base64_publish_kty);
        String base64EncodedPublicKey = PayDelegate.getPublishKey();

        // Some sanity checks to see if the developer (that's you!) really followed the
        // instructions to run this sample (don't put these checks on your app!)
        if (base64EncodedPublicKey.contains("CONSTRUCT_YOUR")) {
            throw new RuntimeException("Please put your app's public key in Activity.java. See README.");
        }
        if (getPackageName().startsWith("com.example")) {
            throw new RuntimeException("Please change the sample's package name! See README.");
        }

        // Create the helper, passing it our context and the public key to verify signatures with
        Log.d(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(PayActivity.this, base64EncodedPublicKey);

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
                mBroadcastReceiver = new IabBroadcastReceiver(PayActivity.this);
                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                registerReceiver(mBroadcastReceiver, broadcastFilter);

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

    /**
     * check api init finished?
     *
     * @throws IllegalArgumentException must init api first
     */
    private void checkAPIInit() {
        if (TextU.isEmpty(PayDelegate.getPublishKey()))
            throw new IllegalArgumentException("must init api with publish key before using, see read me doc");
    }

    /**
     * consume already bought goods
     */
    private void consumeAlreadyBoughtGoods() {
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

    private String productId = "";
    private String payload = "";

    /**
     * test data transformed by intent
     */
    private void testDataIntentTransform() {
        Bundle bundle = getIntent().getExtras();

        if (DU.isNull(bundle)) return;

        String productId = bundle.getString("productId");
        if (TextUtils.isEmpty(productId)) return;

        String payload = bundle.getString("developerPayload");
        if (TextU.hasContent(payload))
            this.payload = payload;

        this.productId = productId;
    }

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
                
                // FIXME: 2016/2/18
                try {
                    mHelper.consumeAsync(inventory.getPurchase(sku), mConsumeFinishedListener);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }

                return;
            }

            updateUi();
            setWaitScreen(false);
            Log.d(TAG, "Initial inventory query finished; enabling main UI.");
            handler.sendEmptyMessage(DO_PURCHASE_FROM_INTENT);
        }
    };

    @Override
    public void receivedBroadcast() {
        // Received a broadcast notification that the inventory of items has changed
        Log.d(TAG, "Received broadcast notification. Querying inventory.");
        mHelper.queryInventoryAsync(mGotInventoryListener);
    }

    // User clicked the "Buy Gas" button
    public void onBuyGasButtonClicked(View arg0) {
        purchaseGoods("gas");
    }

    /**
     * the buy core buy gas work
     */
    private void purchaseGoods() {
        purchaseGoods(null);
    }

    /**
     * the buy core buy work, buy gas or the exact thing what you pass in
     *
     * @param productId the product id to purchase
     */
    private void purchaseGoods(String productId) {

        Log.d(TAG, "Buy gas button clicked.");

        DU.sd("goods id", "id = " + productId);

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
        setWaitScreen(true);
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

        mHelper.launchPurchaseFlow(this, purchaseGoodsId, RC_REQUEST,
                mPurchaseFinishedListener, payload);

        // post the purchase data to server
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
     * post purchase data to server after the purchase operation
     */
    private void postPurchaseDataToServerAfter(String purchaseDataInJsonString) {
        // http://r.cutieriot.com/?type=purchase&uid=xxx&purchase_data={purchase_data}
        String url = urlHeadBuilder("purchase");
        url += "&" + PayURL.PURCHASE_DATA_KEY + purchaseDataInJsonString;

        recordDataToServer(url);

        // announce the api user by setting the call back
        HashMap<String, String> param4CallBack = new HashMap<>();
        param4CallBack.put("uid", PayDelegate.getUserId());
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

    // User clicked the "Upgrade to Premium" button.
    public void onUpgradeAppButtonClicked(View arg0) {
        Log.d(TAG, "Upgrade button clicked; launching purchase flow for upgrade.");
        setWaitScreen(true);

        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
        String payload = "";

        payload = PAYLOAD_ON_GOOGLE_WEB;/* for test*/

        mHelper.launchPurchaseFlow(this, SKU_PREMIUM, RC_REQUEST,
                mPurchaseFinishedListener, payload);
    }

    // "Subscribe to infinite gas" button clicked. Explain to user, then start purchase
    // flow for subscription.
    public void onInfiniteGasButtonClicked(View arg0) {
        if (!mHelper.subscriptionsSupported()) {
            complain("Subscriptions not supported on your device yet. Sorry!");
            return;
        }

        CharSequence[] options;
        if (!mSubscribedToInfiniteGas || !mAutoRenewEnabled) {
            // Both subscription options should be available
            options = new CharSequence[2];
            options[0] = getString(R.string.subscription_period_monthly);
            options[1] = getString(R.string.subscription_period_yearly);
            mFirstChoiceSku = SKU_INFINITE_GAS_MONTHLY;
            mSecondChoiceSku = SKU_INFINITE_GAS_YEARLY;
        } else {
            // This is the subscription upgrade/downgrade path, so only one option is valid
            options = new CharSequence[1];
            if (mInfiniteGasSku.equals(SKU_INFINITE_GAS_MONTHLY)) {
                // Give the option to upgrade to yearly
                options[0] = getString(R.string.subscription_period_yearly);
                mFirstChoiceSku = SKU_INFINITE_GAS_YEARLY;
            } else {
                // Give the option to downgrade to monthly
                options[0] = getString(R.string.subscription_period_monthly);
                mFirstChoiceSku = SKU_INFINITE_GAS_MONTHLY;
            }
            mSecondChoiceSku = "";
        }

        int titleResId;
        if (!mSubscribedToInfiniteGas) {
            titleResId = R.string.subscription_period_prompt;
        } else if (!mAutoRenewEnabled) {
            titleResId = R.string.subscription_resignup_prompt;
        } else {
            titleResId = R.string.subscription_update_prompt;
        }

        Builder builder = new Builder(this);
        builder.setTitle(titleResId)
                .setSingleChoiceItems(options, 0 /* checkedItem */, this)
                .setPositiveButton(R.string.subscription_prompt_continue, this)
                .setNegativeButton(R.string.subscription_prompt_cancel, this);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int id) {
        if (id == 0 /* First choice item */) {
            mSelectedSubscriptionPeriod = mFirstChoiceSku;
        } else if (id == 1 /* Second choice item */) {
            mSelectedSubscriptionPeriod = mSecondChoiceSku;
        } else if (id == DialogInterface.BUTTON_POSITIVE /* continue button */) {
            /* TODO: for security, generate your payload here for verification. See the comments on
             *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
             *        an empty string, but on a production app you should carefully generate
             *        this. */
            String payload = "";

            // for test
            payload = PAYLOAD_ON_GOOGLE_WEB;

            if (TextUtils.isEmpty(mSelectedSubscriptionPeriod)) {
                // The user has not changed from the default selection
                mSelectedSubscriptionPeriod = mFirstChoiceSku;
            }

            List<String> oldSkus = null;
            if (!TextUtils.isEmpty(mInfiniteGasSku)
                    && !mInfiniteGasSku.equals(mSelectedSubscriptionPeriod)) {
                // The user currently has a valid subscription, any purchase action is going to
                // replace that subscription
                oldSkus = new ArrayList<String>();
                oldSkus.add(mInfiniteGasSku);
            }

            setWaitScreen(true);
            Log.d(TAG, "Launching purchase flow for gas subscription.");
            mHelper.launchPurchaseFlow(this, mSelectedSubscriptionPeriod, IabHelper.ITEM_TYPE_SUBS,
                    oldSkus, RC_REQUEST, mPurchaseFinishedListener, payload);
            // Reset the dialog options
            mSelectedSubscriptionPeriod = "";
            mFirstChoiceSku = "";
            mSecondChoiceSku = "";
        } else if (id != DialogInterface.BUTTON_NEGATIVE) {
            // There are only four buttons, this should not happen
            Log.e(TAG, "Unknown button clicked in subscription dialog: " + id);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }

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

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                complain("Error purchasing: " + result);
                setWaitScreen(false);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.");
                setWaitScreen(false);
                return;
            }

            Log.d(TAG, "Purchase successful.");

            // fixme: 2016/2/17 server check
            recordDataToServer2ndEdition(purchase);

            // post purchase info to server
            DU.sd("post purchase info after purchase", "purchase json string=" + purchase.getOriginalJson());
            postPurchaseDataToServerAfter(purchase.getOriginalJson());

            if (purchase.getSku().equals(SKU_GAS) || purchase.getSku().equals(productId)) {
                // bought 1/4 tank of gas. So consume it.
                Log.d(TAG, "Purchase is gas. Starting gas consumption.");

                try {
                    mHelper.consumeAsync(purchase, mConsumeFinishedListener);
                } catch (Exception e) {
                    e.printStackTrace();
                }


            } else if (purchase.getSku().equals(SKU_PREMIUM)) {
                // bought the premium upgrade!
                Log.d(TAG, "Purchase is premium upgrade. Congratulating user.");
                alert("Thank you for upgrading to premium!");
                mIsPremium = true;
                updateUi();
                setWaitScreen(false);
            } else if (purchase.getSku().equals(SKU_INFINITE_GAS_MONTHLY)
                    || purchase.getSku().equals(SKU_INFINITE_GAS_YEARLY)) {
                // bought the infinite gas subscription
                Log.d(TAG, "Infinite gas subscription purchased.");
                alert("Thank you for subscribing to infinite gas!");
                mSubscribedToInfiniteGas = true;
                mAutoRenewEnabled = purchase.isAutoRenewing();
                mInfiniteGasSku = purchase.getSku();
                mTank = TANK_MAX;
                updateUi();
                setWaitScreen(false);
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
                saveData();
                alert("You filled 1/4 tank. Your tank is now " + String.valueOf(mTank) + "/4 full!");
            } else {
                complain("Error while consuming: " + result);
            }
            updateUi();
            setWaitScreen(false);
            Log.d(TAG, "End consumption flow.");
        }
    };

    // Drive button clicked. Burn gas!
    public void onDriveButtonClicked(View arg0) {
        Log.d(TAG, "Drive button clicked.");
        if (!mSubscribedToInfiniteGas && mTank <= 0)
            alert("Oh, no! You are out of gas! Try buying some!");
        else {
            if (!mSubscribedToInfiniteGas) --mTank;
            saveData();
            alert("Vroooom, you drove a few miles.");
            updateUi();
            Log.d(TAG, "Vrooom. Tank is now " + mTank);
        }
    }

    // We're being destroyed. It's important to dispose of the helper here!
    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyTheProcess();
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
                unregisterReceiver(mBroadcastReceiver);
                mBroadcastReceiver = null;
            }
        } catch (Exception e) {
            DU.sd("broad cast receiver unregister error", e);
        }

    }

    // updates UI to reflect model
    public void updateUi() {
        // update the car color to reflect premium status or lack thereof
//        ((ImageView)findViewById(R.id.free_or_premium)).setImageResource(mIsPremium ? R.drawable.premium : R.drawable.free);
//
//        // "Upgrade" button is only visible if the user is not premium
//        findViewById(R.id.upgrade_button).setVisibility(mIsPremium ? View.GONE : View.VISIBLE);
//
//        ImageView infiniteGasButton = (ImageView) findViewById(R.id.infinite_gas_button);
//        if (mSubscribedToInfiniteGas) {
//            // If subscription is active, show "Manage Infinite Gas"
//            infiniteGasButton.setImageResource(R.drawable.manage_infinite_gas);
//        } else {
//            // The user does not have infinite gas, show "Get Infinite Gas"
//            infiniteGasButton.setImageResource(R.drawable.get_infinite_gas);
//        }
//
//        // update gas gauge to reflect tank status
//        if (mSubscribedToInfiniteGas) {
//            ((ImageView)findViewById(R.id.gas_gauge)).setImageResource(R.drawable.gas_inf);
//        }
//        else {
//            int index = mTank >= TANK_RES_IDS.length ? TANK_RES_IDS.length - 1 : mTank;
//            ((ImageView)findViewById(R.id.gas_gauge)).setImageResource(TANK_RES_IDS[index]);
//        }
    }

    // Enables or disables the "please wait" screen.
    void setWaitScreen(boolean set) {
//        findViewById(R.id.screen_main).setVisibility(set ? View.GONE : View.VISIBLE);
//        findViewById(R.id.screen_wait).setVisibility(set ? View.VISIBLE : View.GONE);
    }

    void complain(String message) {
        Log.e(TAG, "**** TrivialDrive Error: " + message);


        destroyTheProcess();
        finish();

        //alert("Error: " + message);
    }

    void alert(String message) {
        if(!getPackageName().startsWith("com.example"))
            return;// not example, fast fail

        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        Log.d(TAG, "Showing alert dialog: " + message);
        bld.create().show();
    }


    void saveData() {

        /*
         * WARNING: on a real application, we recommend you save data in a secure way to
         * prevent tampering. For simplicity in this sample, we simply store the data using a
         * SharedPreferences.
         */

        SharedPreferences.Editor spe = getPreferences(MODE_PRIVATE).edit();
        spe.putInt("tank", mTank);
        spe.apply();
        Log.d(TAG, "Saved data: tank = " + String.valueOf(mTank));
    }

    void loadData() {
        SharedPreferences sp = getPreferences(MODE_PRIVATE);
        mTank = sp.getInt("tank", 2);
        Log.d(TAG, "Loaded data: tank = " + String.valueOf(mTank));
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
        finish();
    }


    /**
     * announce the Failure call back
     */
    private void announceFailureCallback() {
        // if the response callback is set, response to the callback
        if (PayDelegate.getCallBack() != null) {
            PayDelegate.getCallBack().onFailure();
        }

        destroyTheProcess();
        finish();
    }

    /**
     * announce the Error call back
     */
    private void announceErrorCallback(Exception exception) {
        // if the response callback is set, response to the callback
        if (PayDelegate.getCallBack() != null) {
            PayDelegate.getCallBack().onError(exception);
        }

        destroyTheProcess();
        finish();
    }
}