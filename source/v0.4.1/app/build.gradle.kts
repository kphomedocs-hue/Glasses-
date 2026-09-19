plugins { id("com.android.application") }
android {
 namespace = "com.parkarsite.g1p2passociationprobe"
 compileSdk = 36
 defaultConfig { applicationId = "com.parkarsite.g1p2passociationprobe"; minSdk = 26; targetSdk = 36; versionCode = 2; versionName = "0.4.1" }
 buildTypes { release { isMinifyEnabled = false; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro") } }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
