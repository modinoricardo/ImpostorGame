import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    id("kotlin-parcelize")
}

val localProperties = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    namespace = "com.ricardomodino.impostorgame"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.ricardomodino.impostorgame"
        minSdk = 24
        targetSdk = 36
        versionCode = 18
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "GEMINI_API_KEY",  "\"${localProperties["GEMINI_API_KEY"]  ?: ""}\"")
        buildConfigField("String", "GMAIL_SENDER",    "\"${localProperties["GMAIL_SENDER"]    ?: ""}\"")
        buildConfigField("String", "GMAIL_PASSWORD",  "\"${localProperties["GMAIL_PASSWORD"]  ?: ""}\"")
        buildConfigField("String", "GMAIL_RECEIVER",  "\"${localProperties["GMAIL_RECEIVER"]  ?: ""}\"")
        buildConfigField("String", "SUPABASE_URL",      "\"${localProperties["SUPABASE_URL"]      ?: ""}\"")
        buildConfigField("String", "SUPABASE_KEY",      "\"${localProperties["SUPABASE_KEY"]      ?: ""}\"")
        buildConfigField("String", "ADMOB_APP_ID",      "\"${localProperties["ADMOB_APP_ID"]      ?: ""}\"")
        buildConfigField("String", "ADMOB_BANNER_MAIN", "\"${localProperties["ADMOB_BANNER_MAIN"] ?: ""}\"")
        buildConfigField("String", "ADMOB_BANNER_VICTORY","\"${localProperties["ADMOB_BANNER_VICTORY"] ?: ""}\"")
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/NOTICE.md",
                "META-INF/LICENSE.md",
                "META-INF/NOTICE",
                "META-INF/LICENSE"
            )
        }
    }
}

dependencies {
    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // ViewModel con viewModelScope
    implementation(libs.lifecycle.viewmodel.ktx)

    implementation("com.google.android.gms:play-services-ads:23.6.0")
    implementation("com.google.guava:guava:33.3.1-android")
    implementation("com.airbnb.android:lottie:6.6.0")

    implementation("androidx.camera:camera-camera2:1.4.1")
    implementation("androidx.camera:camera-lifecycle:1.4.1")
    implementation("androidx.camera:camera-view:1.4.1")
    implementation("androidx.exifinterface:exifinterface:1.3.7")

    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation("com.sun.mail:android-mail:1.6.7")
    implementation("com.sun.mail:android-activation:1.6.7")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("androidx.fragment:fragment-ktx:1.8.+")
    implementation(libs.androidx.material3)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
