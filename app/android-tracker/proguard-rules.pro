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

-dontwarn java.lang.invoke.StringConcatFactory

##-------- rules for removing Log methods in release
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}


-keep class it.torino.tracker.data_upload.dto.** {*;}

# database data classes
-keep class it.torino.tracker.database.model.** {*;}

-keep class it.torino.tracker.tracker.sensors.SensingData { *; }
-keep class it.torino.tracker.tracker.sensors.step_counting.StepsData { *; }
-keep class it.torino.tracker.tracker.sensors.location_recognition.LocationData { *; }
-keep class it.torino.tracker.tracker.sensors.heart_rate_monitor.HeartRateData { *; }
-keep class it.torino.tracker.tracker.sensors.activity_recognition.ActivityData { *; }
-keep class it.torino.tracker.retrieval.data.TripData { *; }
-keep class it.torino.tracker.retrieval.data.SummaryData { *; }

-keep class it.torino.tracker.data_upload.data_senders.ActivityDataSender { *; }
#-keep class it.torino.tracker.data_upload.data_senders.SymptomsDataSender { *; }
-keep class it.torino.tracker.data_upload.data_senders.HeartRateDataSender { *; }
-keep class it.torino.tracker.data_upload.data_senders.LocationDataSender { *; }
-keep class it.torino.tracker.data_upload.data_senders.StepsDataSender { *; }
-keep class it.torino.tracker.data_upload.data_senders.TripsDataSender{ *; }


# for GSonObjects
-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}


# Gson classes
-keep class * extends com.google.gson.** {*;}

-keep class it.torino.tracker.data_upload.dts_data.** {*;}


## Keep ViewModelFactory and other necessary classes
-keep class it.torino.tracker.view_model.MyViewModelFactory { *; }
-keep class it.torino.tracker.view_model.** { *; }


## Keep ViewModelFactory and other necessary classes
-keep class it.torino.tracker.TrackerManager { *; }
-keep class it.torino.tracker.TrackerManager$Companion { *; }

-keep class it.torino.tracker.utils.Preferences { *; }

-keep class it.torino.tracker.retrieval.MobilityResultComputation { *; }
-keep class it.torino.tracker.utils.Utils { *; }
-keep class it.torino.tracker.utils.Utils$Companion { *; }

-keep class it.torino.tracker.utils.Utils$Companion { *; }
-keep class  it.torino.tracker.retrieval.data.MobilityElementData { *; }
-keep class  it.torino.tracker.retrieval.data.SummaryData { *; }
-keep class  it.torino.tracker.retrieval.data.TripData { *; }

