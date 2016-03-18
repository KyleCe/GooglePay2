package com.gpack.pay.paylib.util;

import android.text.TextUtils;

/**
 * Created by KyleCe on 2016/1/14.
 *
 * @author KyleCe
 */
public class Print {

    /**
     * print inventory info
     *
     * @param tag tag to tag
     * @param inv inventory to print
     */
    public static void inventoryInfo(String tag, Inventory inv) {
        if (inv == null) return;

        DU.sd(TextUtils.isEmpty(tag) ? "inventory info" : "inventory info " + tag,
                "own sku size:" +
                        (inv.getAllSkusKeySet() == null ? null : inv.getAllSkusKeySet().size()),
                "purchase size:" +
                        (inv.getAllPurchases() == null ? null : inv.getAllPurchases().size()));
    }

    /**
     * print inventory info
     *
     * @param inv inventory to print
     */
    public static void inventoryInfo(Inventory inv) {
        inventoryInfo(null, inv);
    }
}
