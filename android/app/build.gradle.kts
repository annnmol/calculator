plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val signingEnvironment = mapOf(
    "KEYSTORE_FILE" to providers.environmentVariable("KEYSTORE_FILE").orNull,
    "KEYSTORE_PASSWORD" to providers.environmentVariable("KEYSTORE_PASSWORD").orNull,
    "KEY_ALIAS" to providers.environmentVariable("KEY_ALIAS").orNull,
    "KEY_PASSWORD" to providers.environmentVariable("KEY_PASSWORD").orNull,
)
val configuredSigningValues = signingEnvironment.values.count { !it.isNullOrBlank() }
if (configuredSigningValues != 0 && configuredSigningValues != signingEnvironment.size) {
    throw GradleException("All release signing environment variables must be provided together.")
}
val releaseSigningConfigured = configuredSigningValues == signingEnvironment.size

android {
    namespace = "com.anmoltanwar.calculator"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.anmoltanwar.calculator"
        minSdk = 36
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (releaseSigningConfigured) {
            create("release") {
                storeFile = file(signingEnvironment.getValue("KEYSTORE_FILE")!!)
                storePassword = signingEnvironment.getValue("KEYSTORE_PASSWORD")
                keyAlias = signingEnvironment.getValue("KEY_ALIAS")
                keyPassword = signingEnvironment.getValue("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
               isMinifyEnabled = true
               isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            if (releaseSigningConfigured) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.google.material)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}