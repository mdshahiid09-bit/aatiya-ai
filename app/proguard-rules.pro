# Proguard rules for AATIYA AI
-keepattributes *Annotation*
-dontwarn javax.annotation.**
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
