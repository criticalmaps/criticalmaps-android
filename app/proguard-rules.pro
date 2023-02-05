# Add project specific ProGuard rules here.

# Keep names so we don't have to upload the mapping file to playstore with every release
-dontobfuscate

# otto
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.squareup.otto.Subscribe public *;
    @com.squareup.otto.Produce public *;
}
