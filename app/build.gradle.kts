plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
    id("com.google.dagger.hilt.android")
    id("io.gitlab.arturbosch.detekt")

}

android {
    namespace = "ru.itis.semwork"
    compileSdk = 35

    defaultConfig {
        applicationId = "ru.itis.semwork"
        minSdk = 24
        targetSdk = 35
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
detekt {
    toolVersion = "1.23.0"
    config.setFrom(files("${rootProject.projectDir}/config/detekt.yml"))
    buildUponDefaultConfig = true
}

dependencies {
    implementation(project(":core:core-analytics"))
    implementation(project(":core:core-data"))
    implementation(project(":core:core-models"))
    implementation(project(":core:core-ui"))
    implementation(project(":feature:feature-random:feature-random-impl"))
    implementation(project(":feature:feature-search:feature-search-impl"))
    implementation(project(":feature:feature-favorites:feature-favorites-impl"))
    implementation(project(":feature:feature-creator:feature-creator-impl"))
    implementation(project(":feature:feature-auth:feature-auth-impl"))
    implementation(project(":feature:feature-auth:feature-auth-api"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material.icons.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.retrofit.converter.gson)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.perf)
}