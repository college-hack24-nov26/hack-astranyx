# Room Database
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>();
}
-keep class * extends androidx.room.RoomDatabase
-keep class com.sktech.wastetrack.data.local.db.entity.** { *; }
-keep class com.sktech.wastetrack.data.local.db.dao.** { *; }

# Domain Models (prevent field name obfuscation breaking Gson and Firestore)
-keep class com.sktech.wastetrack.domain.model.** { *; }
-keep class com.sktech.wastetrack.data.ml.** { *; }
-keep class com.sktech.wastetrack.data.remote.api.** { *; }

# Gson serialization
-keepattributes Signature
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ZXing
-keep class com.google.zxing.** { *; }

# Razorpay Payment Gateway
-dontwarn com.razorpay.**
-keep class com.razorpay.** { *; }
-optimizations !class/merging/vertical*,!class/merging/horizontal*
-keepclasseswithmembers class * {
    public void onPayment*(...);
}

