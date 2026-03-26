# Add project specific ProGuard rules here.
-keep class com.budgetr.app.data.** { *; }
-keep class com.squareup.moshi.** { *; }
-keepclassmembers class ** {
    @com.squareup.moshi.FromJson *;
    @com.squareup.moshi.ToJson *;
}
