plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.avatar"
    compileSdk = 36   // ✅ Subido a 36

    defaultConfig {
        applicationId = "com.example.avatar"
        minSdk = 24
        targetSdkVersion(36)   // ✅ usa targetSdkVersion(), no targetSdk =
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Si usas RecyclerView y Glide para tu carrusel
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation ("nl.dionsegijn:konfetti-xml:2.0.5")
    implementation ("com.airbnb.android:lottie:6.0.0")

    implementation(libs.legacy.support.v4)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
