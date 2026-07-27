# Keep MySQL JDBC driver
-keep class com.mysql.** { *; }
-keep class com.mysql.jdbc.Driver

# Keep Jazz Cinema models
-keep class com.jazzcinema.app.model.** { *; }

# Keep Gson model serialization
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Media3
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Suppress JNDI/RMI warnings from mysql-connector (not used on Android)
-dontwarn javax.naming.**
-dontwarn javax.management.**
-dontwarn com.mysql.jdbc.integration.**
