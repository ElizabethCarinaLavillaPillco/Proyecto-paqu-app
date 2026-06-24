plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.paqu"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.paqu"
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    // Esta sección soluciona los 14 conflictos de versiones forzando las estables
    configurations.all {
        resolutionStrategy {
            force("androidx.activity:activity:1.9.3")
            force("androidx.activity:activity-ktx:1.9.3")
            force("androidx.core:core:1.13.1")
            force("androidx.core:core-ktx:1.13.1")
            // Evita que navigation-event pida SDK 36
            force("androidx.navigation:navigation-common:2.8.3")
            force("androidx.navigation:navigation-runtime:2.8.3")
        }
    }
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.activity.ktx)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    
    // Firebase (Usamos una versión de BoM estable para API 35)
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore)
    implementation("com.google.firebase:firebase-analytics")
    
    // Google & Auth
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // UI & Animaciones
    implementation("com.airbnb.android:lottie:6.4.0")
    implementation("com.google.android.flexbox:flexbox:3.0.0")
    implementation("com.tbuonomo:dotsindicator:5.0")
    
    // Ciclo de vida y Corrutinas
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Facebook (Versión estable)
    implementation("com.facebook.android:facebook-android-sdk:17.0.0")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    implementation("androidx.work:work-runtime:2.9.1")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.2")
}