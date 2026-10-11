plugins { id("com.android.application") }
android {
 namespace = "com.parkarsite.g6ap2pdiag685"
 compileSdk = 36
 buildFeatures { buildConfig = true }
 defaultConfig {
   applicationId = "com.parkarsite.g6ap2pdiag685"
   minSdk = 26
   targetSdk = 36
   versionCode = 1
   versionName = "0.6.8.5"
   // Exact GitHub Actions run identity is embedded in the APK at compile time.
   // Local builds are explicitly marked UNVERIFIED and cannot be physically promoted.
   buildConfigField("String", "BUILD_COMMIT", "\"${System.getenv("GITHUB_SHA") ?: "LOCAL_UNVERIFIED"}\"")
   buildConfigField("String", "BUILD_RUN", "\"${System.getenv("GITHUB_RUN_ID") ?: "LOCAL_UNVERIFIED"}\"")
   buildConfigField("String", "BUILD_ATTEMPT", "\"${System.getenv("GITHUB_RUN_ATTEMPT") ?: "LOCAL_UNVERIFIED"}\"")
 }
 buildTypes { release { isMinifyEnabled = false; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro") } }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
