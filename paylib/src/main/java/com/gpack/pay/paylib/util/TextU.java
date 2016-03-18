package com.gpack.pay.paylib.util;


import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by KyleCe on 2015/10/15.
 *
 * @author KyleCe
 */
public class TextU {
    public static boolean hasContent(String s) {
        return s != null && !s.equals("");
    }

    public static boolean isEmpty(String s) {
        return !hasContent(s);
    }

    public static boolean hasContent(String[] strs) {
        if (strs == null || strs.length == 0) return false;
        return strs[0] != null && !strs[0].equals("");
    }

    /**
     * url encode : url encode the source string
     *
     * @param sourceString the source string to url encode
     */
    public static String URLEncode(String sourceString) {
        String encodedStr = "";
        try {
            encodedStr = URLEncoder.encode(sourceString, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return sourceString;
        }
        return encodedStr;
    }


    /**
     * <p>Capitalizes a String changing the first letter to title case as
     * per {@link Character#toTitleCase(char)}. No other letters are changed.</p>
     * <p/>
     * <p>For a word based algorithm.
     * A <code>null</code> input String returns <code>null</code>.</p>
     * <p/>
     * <pre>
     * TextU.capitalize(null)  = null
     * TextU.capitalize("")    = ""
     * TextU.capitalize("cat") = "Cat"
     * TextU.capitalize("cAt") = "CAt"
     * </pre>
     *
     * @param str the String to capitalize, may be null
     * @return the capitalized String, <code>null</code> if null String input
     * @since 2.0
     */
    public static String capitalize(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        return new StringBuilder(strLen)
                .append(Character.toTitleCase(str.charAt(0)))
                .append(str.substring(1))
                .toString();
    }


    /**
     * capitalize only the first char, the other chars in the string will be converted to lower case
     * <p/>
     * <pre>
     * TextU.capitalizeOnlyFirstChar(null)  = null
     * TextU.capitalizeOnlyFirstChar("")    = ""
     * TextU.capitalizeOnlyFirstChar("cat") = "Cat"
     * TextU.capitalizeOnlyFirstChar("cAtCh ME") = "Catch me"
     * </pre>
     * <p/>
     * <p/>
     * Created by KyleCe on 2015/12/23.
     *
     * @author KyleCe
     * <a href="https://github.com/KyleCe">KyleCe@github</a>
     */
    public static String capitalizeOnlyFirstChar(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) return str;

        return new StringBuilder(strLen)
                .append(Character.toTitleCase(str.charAt(0)))
                .append(str.substring(1).toLowerCase())
                .toString();
    }


    /**
     * <p>Uncapitalizes a String changing the first letter to title case as
     * per {@link Character#toLowerCase(char)}. No other letters are changed.</p>
     * <p/>
     * <p>For a word based algorithm
     * A <code>null</code> input String returns <code>null</code>.</p>
     * <p/>
     * <pre>
     * TextU.uncapitalize(null)  = null
     * TextU.uncapitalize("")    = ""
     * TextU.uncapitalize("Cat") = "cat"
     * TextU.uncapitalize("CAT") = "cAT"
     * </pre>
     *
     * @param str the String to uncapitalize, may be null
     * @return the uncapitalized String, <code>null</code> if null String input
     * @see #capitalize(String)
     * @since 2.0
     */
    public static String uncapitalize(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        return new StringBuilder(strLen)
                .append(Character.toLowerCase(str.charAt(0)))
                .append(str.substring(1))
                .toString();
    }


    /**
     * encode base 64 for user Name
     * pay attention to the '+' '-' replacement
     * return the source string if failed
     *
     * @param sourceString the source string to encode with base 64
     */
    public static String encode64(String sourceString) {

        try {
            Base64 base64 = new Base64(true);// set url safe to true
            sourceString = new String(base64.encodeBase64(sourceString.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return sourceString;
        }

        return sourceString;
    }

    /**
     * decode base 64 string
     * <p/>
     * return the encoded string if failed
     *
     * @param encodedString the encode text
     */
    public static String decode64(String encodedString) {
        String result = "";
        if (!hasContent(encodedString)) return result;
        try {
            Base64 base64 = new Base64(true);
            result = new String(base64.decodeBase64(encodedString.getBytes()), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return encodedString;
        }
        return result;
    }

}
