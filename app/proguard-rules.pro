# Keep data classes used for (de)serialization with kotlinx.serialization.
-keepattributes *Annotation*, InnerClasses
-keep,includedescriptorclasses class com.neofit.**$$serializer { *; }
-keepclassmembers class com.neofit.** {
    *** Companion;
}
-keepclasseswithmembers class com.neofit.** {
    kotlinx.serialization.KSerializer serializer(...);
}
