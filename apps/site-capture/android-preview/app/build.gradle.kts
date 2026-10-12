plugins { id("com.android.application") }

android {
    namespace = "com.parkarsite.g1.preview"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.parkarsite.g1.readonlypreview"
        minSdk = 36
        targetSdk = 36
        versionCode = 1
        versionName = "0.0.1-read-only-not-for-device"
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
            "../../src/main/java",
            "../../../../source/g1-shared-workflow/src"
        )
        // The preview uses only the durable local store. Explicitly EXCLUDE
        // the coordinator's package-private synthetic fixture-permit path.
        getByName("main").java.exclude(
            "**/SharedTransferCoordinator.java",
            "**/SharedObservationController.java",
            "**/SharedTransferContracts.java"
        )
    }
}
