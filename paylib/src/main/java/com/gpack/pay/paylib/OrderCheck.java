package com.gpack.pay.paylib;

/**
 * the order check needed info
 * Created by KyleCe on 2016/2/17.
 *
 * @author KyleCe
 *         <a href="https://github.com/KyleCe">KyleCe@github</a>
 */
public class OrderCheck {

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

    private static String appid;/* stands for google or facebook */
    private static String productid;
    private static String transaction;
    private static String receipt;
    private static String time;
    private static String sign;
    private static String accountid;
    private static String zoneid;
    private static String roleid;
    private static String productname;
    private static String platform;
    private static String paydescription;
    private static String publickey;


    // builder pattern to init
    public static class Builder {
        private String appid;
        private String productid;
        private String transaction;

        private String receipt;
        private String time;
        private String sign;
        private String accountid;
        private String zoneid;
        private String roleid;
        private String productname;
        private String platform;
        private String paydescription;
        private String publickey;

        public Builder(String appid, String productid) {
            this.appid = appid;
            this.productid = productid;
        }

        public Builder transaction(String transaction) {
            this.transaction = transaction;
            return this;
        }

        public Builder receipt(String receipt) {
            this.receipt = receipt;
            return this;
        }

        public Builder time(String time) {
            this.time = time;
            return this;
        }

        public Builder sign(String sign) {
            this.sign = sign;
            return this;
        }

        public Builder accountid(String accountid) {
            this.accountid = accountid;
            return this;
        }

        public Builder zoneid(String zoneid) {
            this.zoneid = zoneid;
            return this;
        }

        public Builder roleid(String roleid) {
            this.roleid = roleid;
            return this;
        }

        public Builder productname(String productname) {
            this.productname = productname;
            return this;
        }

        public Builder platform(String platform) {
            this.platform = platform;
            return this;
        }

        public Builder paydescription(String paydescription) {
            this.paydescription = paydescription;
            return this;
        }

        public Builder publickey(String publickey) {
            this.publickey = publickey;
            return this;
        }

        public OrderCheck build() {
            return new OrderCheck(this);
        }
    }

    private OrderCheck(Builder builder) {
        appid = builder.appid;
        productid = builder.productid;
        transaction = builder.transaction;
        receipt = builder.receipt;
        time = builder.time;
        sign = builder.sign;
        accountid = builder.accountid;
        zoneid = builder.zoneid;
        roleid = builder.roleid;
        productname = builder.productname;
        platform = builder.platform;
        paydescription = builder.paydescription;
        publickey = builder.publickey;

    }

    public static String getAppid() {
        return appid;
    }

    public static String getProductid() {
        return productid;
    }

    public static String getTransaction() {
        return transaction;
    }

    public static String getReceipt() {
        return receipt;
    }

    public static String getTime() {
        return time;
    }

    public static String getSign() {
        return sign;
    }

    public static String getAccountid() {
        return accountid;
    }

    public static String getZoneid() {
        return zoneid;
    }

    public static String getRoleid() {
        return roleid;
    }

    public static String getProductname() {
        return productname;
    }

    public static String getPlatform() {
        return platform;
    }

    public static String getPaydescription() {
        return paydescription;
    }

    public static String getPublickey() {
        return publickey;
    }

    @Override
    public String toString() {
        return "";
    }
}
