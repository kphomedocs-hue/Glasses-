plugins { id("com.android.application") }

android {
    namespace = "com.parkarsite.vglandroid"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.parkarsite.vglandroid.simulator"
        minSdk = 36
        targetSdk = 36
        versionCode = 1
        versionName = "0.1-source-only"
        testInstrumentationRunner = "com.parkarsite.vglandroid.HarnessInstrumentation"
    }
    buildTypes {
        getByName("debug") { isDebuggable = true }
        getByName("release") { isMinifyEnabled = false }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    sourceSets {
        getByName("main").java.srcDirs(
            "../../src",
            "../../../../modules/protocol/src/main/java",
            "../../../../source/g6a-identity-core/src",
            "../../../../source/g6a-recovery-core/src"
        )
    }
}
