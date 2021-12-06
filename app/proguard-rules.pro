# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

-keep public class * extends android.view.View {
public <init>(android.content.Context);
public <init>(android.content.Context, android.util.AttributeSet);
public <init>(android.content.Context, android.util.AttributeSet, int);
public void set*(...);
}

-keepclasseswithmembers class * {
 public <init>(android.content.Context, android.util.AttributeSet);
 }

 -keepclasseswithmembers class * {
 public <init>(android.content.Context, android.util.AttributeSet, int);
 }

-keepclassmembers class * extends android.content.Context {
 public void *(android.view.View);
 public void *(android.view.MenuItem);
 }

-keepclassmembers class * implements android.os.Parcelable {
 static ** CREATOR;
 }

-keepclassmembers class **.R$* {
public static <fields>;
 }


-keepattributes Exceptions,InnerClasses,Signature

-keep class com.google.api.client.**
-keepclassmembers class com.google.api.client.** {
  *;
   }


 -keep class com.google.android.gms.**
 -keepclassmembers class com.google.android.gms.** {
 *;
  }
 -keep class com.google.gson.**
 -keepclassmembers class com.google.gson.** {
 *;
 }

-keep class com.google.api.client.** { *; }
-dontwarn com.google.api.client.*
-keep class org.apache.http.** { *; }
-dontwarn org.apache.http.*

-dontnote org.apache.http.**
-dontwarn java.awt.**
-dontwarn org.postgresql.**

-dontwarn javax.activation.**
-dontnote javax.activation.**

-dontwarn myjava.awt.datatransfer.**
-dontnote myjava.awt.datatransfer.**


-dontwarn com.google.android.gms.**
-keep class com.google.android.gms.**
# The official support library.
-keep class android.support.v4.** { *; }
-keepclassmembers class android.support.v4.** {
 *;
 }
 -keep interface android.support.v4.** { *; }
 -keep class android.support.v7.** { *; }


-keepclassmembers class android.support.v7.** {
*;
}
-keep interface android.support.v7.** { *; }

-keep class org.apache.http.** { *; }
-keepclassmembers class org.apache.http.** {*;}
-dontwarn org.apache.**


-keep class org.codehaus.mojo.animal_sniffer.** { *; }
-keep class java.nio.file.** { *; }
-keep class java.lang.invoke.** { *; }

-keepclassmembers class com.fasterxml.jackson.core.**

-keepclassmembers class org.codehaus.mojo.animal_sniffer.**
-keepclassmembers class java.nio.file.**
-keepclassmembers class java.lang.invoke.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
@retrofit2.http.* <methods>;
}

-keep class android.net.http.** { *; }
-keepclassmembers class android.net.http.** {*;}
-dontwarn android.net.**

-keep public class com.epiphany.callshow.model.**