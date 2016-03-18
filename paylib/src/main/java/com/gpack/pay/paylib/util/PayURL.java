package com.gpack.pay.paylib.util;

/**
 * Created by KyleCe on 2016/1/18.
 *
 * @author KyleCe
 */
public class PayURL {

    /*http://www.clashofcuties.com/verify.php?purchase_data=INAPP_PURCHASE_DATA&data_signature=INAPP_DATA_SIGNATURE&public_key=PUBLIC_KEY*/
    public static final String HOST = "http://r.cutieriot.com/?";
    public static final String VERIFY = "verify.php?";
    public static final String TYPE_KEY = "type=";
    public static final String UID_KEY = "uid=";
    public static final String ORDER_DATA_KEY = "order_data=";
    public static final String PURCHASE_DATA_KEY = "purchase_data=";
    public static final String PUBLISH_KEY = "public_key=";

    /**
     * server check
     * http://pay.cutieriot.com/insert_android.php
     * ?appid=52a67a8fe1382353b6f330c0
     * &productid=s_p_60
     * &transaction=sss
     * &receipt=xxx
     * &time=1231
     * &sign=see
     * &accountid=fs_001
     * &zoneid=1001
     * &roleid=1001000001
     * &productname=60js
     * &platform=apple
     * &paydescription=s_s_s_s
     * &publickey=xxxxx
     */
    public static final String PAY_HOST = "http://pay.cutieriot.com/insert_android.php?";
    public static final String APPID = "appid=";
    public static final String PRODUCT_ID = "&productid=";
    public static final String TRANSACTION = "&transaction=";
    public static final String RECEIPT = "&receipt=";
    public static final String TIME = "&time=";
    public static final String SIGN = "&sign=";
    public static final String ACCOUNT_ID = "&accountid=";
    public static final String ZONE_ID = "&zoneid=";
    public static final String ROLE_ID = "&roleid=";
    public static final String PRODUCT_NAME = "&productname=";
    public static final String PLATFORM = "&platform=";
    public static final String PAY_DESCRIPTION = "&paydescription=";
    public static final String PUBLIC_KEY = "&publickey=";
}
