plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "it.torino.mobin"
    compileSdk = 34

    defaultConfig {
        applicationId = "it.torino.mobin"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

        signingConfigs {
            create("release") {
                storeFile = file(project.properties["keystore"] as String)
                storePassword = project.properties["keystore_password"] as String
                keyAlias = project.properties["key_alias"] as String
                keyPassword = project.properties["key_password"] as String
            }
            getByName("debug") {
                // Debug configuration can be similar to release or different based on your requirement
                storeFile = file(project.properties["keystore"] as String)
                storePassword = project.properties["keystore_password"] as String
                keyAlias = project.properties["key_alias"] as String
                keyPassword = project.properties["key_password"] as String
            }
        }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
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