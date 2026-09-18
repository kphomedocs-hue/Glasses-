plugins {
    id("com.android.application")
}

android {
    namespace = "com.parkarsite.g1capture"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.parkarsite.g1capture"
        minSdk = 26
        targetSdk = 36
        versionCode = 4
        versionName = "0.4-preflight"

        testInstrumentationRunner = "android.app.InstrumentationTestRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    lint {
        abortOnError = true
        warningsAsErrors = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
