# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\stephan\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
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

-dontwarn org.apache.**
-dontwarn org.osmdroid.**
-dontwarn android.support.**
-dontwarn android.support.v4.**
-dontwarn javax.management.**
-dontwarn javax.xml.**
-dontwarn javax.lang.**
-dontwarn org.apache.**
-dontwarn org.slf4j.**
-dontwarn com.google.code.**
-dontwarn oauth.signpost.**
-dontwarn twitter4j.**

# okhttp3
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
 # A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# picasso
-dontwarn com.squareup.okhttp.**

# otto
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.squareup.otto.Subscribe public *;
    @com.squareup.otto.Produce public *;
}