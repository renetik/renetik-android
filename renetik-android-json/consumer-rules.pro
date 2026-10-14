# JSON objects and arrays, including stores and presets built on them, are created from their
# KClass through kotlin-reflect, which calls the no-argument constructor.
-keepclassmembers class renetik.android.json.obj.CSJsonObject,
        renetik.android.json.array.CSJsonArray {
    public <init>();
}
-keepclassmembers class * extends renetik.android.json.obj.CSJsonObject {
    public <init>();
}
-keepclassmembers class * extends renetik.android.json.array.CSJsonArray {
    public <init>();
}
