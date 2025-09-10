plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.gps_test"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.gps_test"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    implementation(libs.material.v1110)
    implementation(libs.drawerlayout)
    implementation(libs.appcompat.v161)
    implementation(libs.play.services.location)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")


    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}