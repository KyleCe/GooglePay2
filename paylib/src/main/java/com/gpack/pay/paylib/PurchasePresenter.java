package com.gpack.pay.paylib;

import android.content.Context;

/**
 * Created by KyleCe on 2016/3/26.
 *
 * @author: KyleCe
 */
public interface PurchasePresenter {
    void init(Context context, String productId);

//    void purchaseActivityBase(Activity act);

    void destroy();
}
