import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.compose.compiler)
    id("com.google.devtools.ksp") version "2.0.21-1.0.27"
}

val localProperties =  Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load( FileInputStream(localPropertiesFile))
    println("Loaded local.properties")
}


android {
    namespace = "it.torino.mobin"
    compileSdk = 35

    buildFeatures {
        // Enable the generation of BuildConfig fields
        buildConfig = true
    }

    defaultConfig {
        applicationId = "it.torino.mobin"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = localProperties["GOOGLE_MAPS_API_KEY"] as String? ?: ""

        // Make sure to call toString() since Kotlin doesn't automatically call it.
        buildConfigField("String", "GOOGLE_MAPS_API_KEY", "\"${localProperties.getProperty("GOOGLE_MAPS_API_KEY")}\"")
        buildConfigField("String", "keystore_password", "\"${localProperties.getProperty("keystore_password")}\"")
        buildConfigField("String", "key_alias", "\"${localProperties.getProperty("key_alias")}\"")
        buildConfigField("String", "key_password", "\"${localProperties.getProperty("key_password")}\"")
        buildConfigField("String", "keystore", "\"${localProperties.getProperty("keystore")}\"")

    }

    signingConfigs {
        create("release") {
            storeFile = file(localProperties["keystore"] as String)
            storePassword = localProperties["keystore_password"] as String
            keyAlias = localProperties["key_alias"] as String
            keyPassword = localProperties["key_password"] as String
        }
        getByName("debug") {
            // Your debug configuration here, if it's different
            storeFile = file(localProperties["keystore"] as String)
            storePassword = localProperties["keystore_password"] as String
            keyAlias = localProperties["key_alias"] as String
            keyPassword = localProperties["key_password"] as String
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":app:android-tracker"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.navigation.compose)
    implementation(libs.androidbrowserhelper)
    ksp(libs.dagger.compiler)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Kotlin BOM
    implementation(platform(libs.kotlin.bom))

    // AndroidX, Compose, and others
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.splashscreen)
    implementation(libs.navigation.compose)
//    implementation("com.google.android.gms")
    implementation(libs.accompanist.permissions)
//    implementation("com.google.android.gms")
    implementation(libs.androidx.compose.ui)
    implementation(libs.play.services.location)
    implementation(libs.androidx.constraintlayout)
    androidTestImplementation(libs.androidx.compose.ui)
    implementation(libs.androidx.appcompat)
    implementation(libs.android.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}