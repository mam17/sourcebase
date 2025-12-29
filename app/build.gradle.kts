import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.dagger.hilt.android")
}
configurations.configureEach {
    exclude(
        group = "org.jetbrains.kotlin",
        module = "kotlin-android-extensions-runtime"
    )
}
android {
    namespace = "com.example.myapplication"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        //noinspection OldTargetApi
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val formattedDate = SimpleDateFormat("MM.dd.yyyy").format(Date())
        project.extra.set("archivesBaseName", "ControlCenterTheme_v${versionName}(${versionCode})_${formattedDate}")

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    bundle {
        @Suppress("UnstableApiUsage")
        language {
            enableSplit = false
        }
    }
}
kapt {
    correctErrorTypes = true
    arguments {
        arg("dagger.fastInit", "enabled")
        arg("dagger.hilt.android.internal.disableAndroidSuperclassValidation", "true")
        arg("dagger.hilt.android.internal.projectType", "APP")
        arg("dagger.hilt.internal.useAggregatingRootProcessor", "false")
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
}