import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}



fun loadLocalProperties(file: File): Map<String, String> {
    val props = Properties()
    FileInputStream(file).use { props.load(it) }
    return props.entries.associate { it.key.toString() to it.value.toString() }
}

android {
    namespace = "com.summerlandsoftware.iot.kindle"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    defaultConfig {
        applicationId = "com.summerlandsoftware.iot.kindle"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        val localProps = loadLocalProperties(rootProject.file("local.properties"))
        val iotConnString = localProps["IOTHUB_CONNECTION_STRING"] ?: ""
        buildConfigField("String", "IOTHUB_CONNECTION_STRING", "\"$iotConnString\"")
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/thirdpartynotice.txt"
        }
    }

}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.activity:activity-ktx:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("com.microsoft.azure.sdk.iot:iot-device-client:2.5.0")
}

