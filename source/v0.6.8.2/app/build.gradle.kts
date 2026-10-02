plugins { id("com.android.application") }

val buildCommit = System.getenv("GITHUB_SHA") ?: "LOCAL"
val buildRun = System.getenv("GITHUB_RUN_ID") ?: "LOCAL"
val buildAttempt = System.getenv("GITHUB_RUN_ATTEMPT") ?: "LOCAL"

android {
    namespace = "com.parkarsite.g6acreddiag682"
    compileSdk = 36
    buildFeatures { buildConfig = true }
    defaultConfig {
        applicationId = "com.parkarsite.g6acreddiag682"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.6.8.2"
        buildConfigField("String", "BUILD_COMMIT", "\"$buildCommit\"")
        buildConfigField("String", "BUILD_RUN", "\"$buildRun\"")
        buildConfigField("String", "BUILD_ATTEMPT", "\"$buildAttempt\"")
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
