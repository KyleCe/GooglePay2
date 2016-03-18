package com.gpack.pay.paylib;

//import com.alibaba.fastjson.JSON;

import com.gpack.pay.paylib.util.DU;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by KyleCe on 2015/11/9.
 *
 * @author KyleCe
 */
public class JsonUtils {

    public static int getInt(JSONObject json, String name, int defValue) {
        try {
            if (json != null && json.has(name)) {
                return json.getInt(name);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return defValue;
        }
        return defValue;
    }

//    public static String getString(JSONObject json, String name, String defValue) {
//        try {
//            if (json != null && json.has(name)) {
//                String sValue = json.getString(name);
//                return sValue != null ? sValue : defValue;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return defValue;
//        }
//        return defValue;
//    }
//
//    public static long getLong(JSONObject json, String name, long defValue) {
//        try {
//            if (json != null && json.has(name)) {
//                return json.getLong(name);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return defValue;
//        }
//        return defValue;
//    }
//
//    public static JSONObject getJSONObject(JSONObject json, String name) {
//        try {
//            if (json != null && json.has(name)) {
//                return json.getJSONObject(name);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//        return null;
//    }
//
//    /**
//     * get json object by index and key
//     * <p/>
//     * {"ad":{"id":1,"title":"广告","desc":"广告","sortNum":0,"iconUrl":"http:\/\/7xnw5u.com1.z0.glb.clouddn.com\/582164_204623018248_2.jpg"}}
//     * object string={"id":1,"title":"广告","desc":"广告","sortNum":0,"iconUrl":"http:\/\/7xnw5u.com1.z0.glb.clouddn.com\/582164_204623018248_2.jpg"}
//     * json object with org json{"id":1,"title":"广告","desc":"广告","sortNum":0,"iconUrl":"http:\/\/7xnw5u.com1.z0.glb.clouddn.com\/582164_204623018248_2.jpg"}
//     */
//    public static JSONObject getJSONObject(JSONArray objArray, String key, int index) {
//        try {
//            if (objArray != null) {
//                JSONObject object = objArray.getJSONObject(index);
//                String objStr = object.getString(key);
//
//                JSONObject jsonObj = new JSONObject(objStr);
//
//                DU.sd("json object", object, "object string=" + objStr,
//                        "json object with org json" + jsonObj);
//
//                return jsonObj;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//        return null;
//    }
//
//    public static JSONArray getJSONArray(JSONObject json, String name) {
//        try {
//            if (json != null && json.has(name)) {
//                return json.getJSONArray(name);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//        return null;
//    }
//
//    public static String getString(JSONArray json, int idx, String defValue) {
//        try {
//            if (json != null) {
//                String sValue = json.getString(idx);
//                return sValue != null ? sValue : defValue;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return defValue;
//        }
//        return defValue;
//    }
//
//    public static boolean getBoolean(JSONObject json, String name, boolean defValue) {
//        try {
//            if (json != null && json.has(name)) {
//                return json.getBoolean(name);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return defValue;
//        }
//        return defValue;
//    }
//
//
//    public static int getArraySize(JSONArray json) {
//        try {
//            if (json != null) {
//                return json.length();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return 0;
//        }
//        return 0;
//    }
//
//    public static JSONObject getAt(JSONArray json, int idx) {
//        try {
//            if (json != null && idx < json.length()) {
//                return (JSONObject) json.get(idx);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//        return null;
//    }
}

