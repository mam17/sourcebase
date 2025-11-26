import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.kapt")
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        //noinspection OldTargetApi
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val formattedDate = SimpleDateFormat("MM.dd.yyyy").format(Date())
        base.archivesBaseName = "ControlCenterTheme_v${versionName}(${versionCode})_${formattedDate}"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Thêm dòng này !
            manifestPlaceholders["ad_app_id"] = "ca-app-pub-3940256099942544~3347511713"
        }

        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            manifestPlaceholders["ad_app_id"] = "ca-app-pub-3940256099942544~3347511713"

            buildConfigField("String", "inter_splash", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "inter_main", "\"ca-app-pub-3940256099942544/1033173712\"")
            buildConfigField("String", "appopen_resume", "\"ca-app-pub-3940256099942544/9257395921\"")
            buildConfigField("String", "native_home", "\"ca-app-pub-3940256099942544/2247696110\"")
            buildConfigField("String", "banner_splash", "\"ca-app-pub-3940256099942544/2014213617\"")
            buildConfigField("String", "reward_create", "\"ca-app-pub-3940256099942544/5224354917\"")

        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    bundle {
        language {
            enableSplit = false
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.multidex)
    implementation(libs.androidx.preference.ktx)

    // hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // rxkotlin
    implementation(libs.rxandroid)
    implementation(libs.rxkotlin)

    //Recycler view
    implementation(libs.androidx.recyclerview)

    //Gson
    implementation(libs.gson)

    // Room Database
    implementation(libs.androidx.room.runtime)
    //noinspection KaptUsageInsteadOfKsp
    kapt(libs.androidx.room.compiler)

    //shimmer
    implementation(libs.shimmer)

    // ViewModel and LiveData
    implementation(libs.androidx.lifecycle.extensions)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.common.java8)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    //glide
    implementation(libs.glide)

    //rating
    implementation(libs.andratingbar)

    //Dot
    implementation(libs.dotsindicator)
    
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    //Lottie load gif
    implementation("com.airbnb.android:lottie:6.6.7")

    //AdMob
    implementation("com.google.android.gms:play-services-ads:24.7.0")
    implementation("com.google.android.gms:play-services-appset:16.1.0")

    //UMP
    implementation("com.google.android.ump:user-messaging-platform:4.0.0")

    //Facebook SDK
    implementation("com.facebook.android:facebook-android-sdk:18.1.3")

    //Firebase
    implementation("com.google.firebase:firebase-config-ktx:22.1.2")
    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
//    implementation("com.google.firebase:firebase-analytics:23.0.0")
    implementation("com.google.firebase:firebase-core:21.1.1")

    //mediation admob
    implementation("com.google.ads.mediation:facebook:6.20.0.0")
    implementation("com.google.ads.mediation:applovin:13.3.1.0")
    implementation("com.google.ads.mediation:vungle:7.5.0.0")
    implementation("com.google.ads.mediation:pangle:7.2.0.6.0")
    implementation("com.google.ads.mediation:mintegral:16.9.71.0")

    //adjust
    implementation("com.adjust.sdk:adjust-android:5.4.6")

    //Appsflyer
    implementation("com.appsflyer:af-android-sdk:6.17.0")
    implementation("com.android.installreferrer:installreferrer:2.2")

}