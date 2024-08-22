plugins {
    // Apply the Android Application plugin using version catalog alias
   alias(libs.plugins.android.application)

    // Apply Kotlin Android plugin using version catalog alias
    alias(libs.plugins.jetbrains.kotlin.android)
    //id("com.android.application")
    // Apply Kotlin Compose plugin with specific version
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"

    // Apply Google Services plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.playtogether"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.playtogether"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

}

dependencies {

    // Core libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose libraries
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)


    // Navigation for Jetpack Compose
    implementation(libs.androidx.navigation.compose)

    // ViewModel for Jetpack Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.firebase.database.ktx)

    // Testing libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v115)
    androidTestImplementation(platform(libs.compose.bom.v20230800))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    androidTestImplementation(libs.androidx.espresso.core.v350)
    debugImplementation(libs.ui.test.manifest)

    //firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    // Firebase Authentication
    implementation(libs.firebase.auth.ktx)


}
