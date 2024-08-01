plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "net.tacolc.linker_wear"
    compileSdk = 34

    defaultConfig {
        applicationId = "net.tacolc.linker"
        minSdk = 30
        targetSdk = 34
        versionCode = 23
        versionName = "1.3"

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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.play.services.wearable)
    implementation(libs.material)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.activity)
}