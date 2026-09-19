plugins {
    id("com.android.application")
}

android {
    namespace = "com.parkarsite.g1mediacountprobe"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.parkarsite.g1mediacountprobe"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.3"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
