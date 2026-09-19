plugins { id("com.android.application") }
android {
 namespace = "com.parkarsite.g1catalogshapeprobe"
 compileSdk = 36
 defaultConfig { applicationId = "com.parkarsite.g1catalogshapeprobe"; minSdk = 26; targetSdk = 36; versionCode = 4; versionName = "0.4.3" }
 buildTypes { release { isMinifyEnabled = false; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro") } }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
