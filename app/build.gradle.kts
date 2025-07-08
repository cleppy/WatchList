plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt") // Required for Room's annotation processing
}

android {
    namespace = "com.example.watchlist"
    compileSdk = 36 // Recommended to use the latest stable SDK

    defaultConfig {
        applicationId = "com.example.watchlist"
        minSdk = 26 // Increased min SDK for better Compose compatibility
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // AndroidX Core & Lifecycle (Using libs.versions.toml aliases where available)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose BOM for consistent versions
    // Ensure your libs.versions.toml defines androidx.compose.bom correctly
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Material Icons Extended (For Icons.Filled.Bookmark etc.)
    // It's generally included with material3-window-size-class, but explicit addition can ensure it
    implementation("androidx.compose.material:material-icons-extended:1.6.8") // Explicit version for safety, or you can manage it via libs.versions.toml

    // Navigation for Jetpack Compose
    // Note: 2.8.0-beta02 is a beta. Consider using the latest stable if possible.
    implementation("androidx.navigation:navigation-compose:2.8.0-beta02")

    // Retrofit for networking
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0") // For JSON parsing

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Room for local database
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1") // Annotation processor
    implementation("androidx.room:room-ktx:2.6.1") // Kotlin extensions

    // For ViewModel in Compose (Using libs.versions.toml alias)
    // If you have lifecycle-viewmodel-compose defined in libs, use that instead of the direct string
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")

    // For testing (Using libs.versions.toml aliases)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}