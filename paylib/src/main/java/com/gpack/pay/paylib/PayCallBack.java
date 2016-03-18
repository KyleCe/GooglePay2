package com.gpack.pay.paylib;

import java.util.HashMap;

/**
 * Created by KyleCe on 2016/1/19.
 *
 * @author KyleCe
 */
public interface PayCallBack {

    void onSuccess(HashMap<String, String> map);

    void onFailure();

    void onError(Exception exception);

}
