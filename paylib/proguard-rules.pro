# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class com.android.vending.licensing.ILicensingService

-keep public class custom.components.**

#-keep publisroid.View {
#    public <init>(android.content.Context);
#    public <init>(android.content.Context, android.util.AttributeSet);
#    public <init>(android.content.Context, android.util.AttributeSet, int);
#    public void set*(...);
#    public void get*(...);
#}

# remove log
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}

-keepclassmembers class * {
 public void onClickButton1(android.view.View);
 public void onClickButton2(android.view.View);
 public void onClickButton3(android.view.View);
}

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}


-keep class com.android.vending.billing.**

-keepattributes InnerClasses

-keep class **.R
-keep class **.R$* {
    <fields>;
}

#google
-dontwarn com.google.**
-keep class com.google.** { *; }

#pay activity
-keep class com.gpack.pay.paylib.PayActivity{
*;
}

# pay delegate
-keep class com.gpack.pay.paylib.PayDelegate{
*;
}

# query result
-keep class com.gpack.pay.paylib.QueryResultCallback{
*;
}

-keep class com.gpack.pay.paylib.PayCallBack{
*;
}

-keep class com.gpack.pay.paylib.OrderCheck{
*;
}
-keep class com.gpack.pay.paylib.OrderCheck$Builder{
*;
}

-keep class com.gpack.pay.paylib.OrderCheckResult{
*;
}