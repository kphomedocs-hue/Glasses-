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
        testInstrumentationRunner = "com.parkarsite.g1.preview.OfflineArchiveInstrumentation"
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
    }
}

/** Strip every shared transfer coordinator/test-authorization class from preview Java compilation. */
tasks.withType<org.gradle.api.tasks.compile.JavaCompile>().configureEach {
    exclude("**/SharedTransferCoordinator.java",
            "**/SharedObservationController.java",
            "**/SharedTransferContracts.java")
}
