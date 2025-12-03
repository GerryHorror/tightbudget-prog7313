import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
    alias(libs.plugins.google.gms.google.services)
}

// Load secret values from .env and hydrate google-services.json before the build runs.
val envFile = rootProject.file(".env")
val envProps = Properties().apply {
    if (envFile.exists()) {
        envFile.inputStream().use { load(it) }
    }
}

val firebaseApiKey = envProps.getProperty("FIREBASE_API_KEY") ?: System.getenv("FIREBASE_API_KEY")
val googleServicesTemplate = file("google-services.template.json")
val googleServicesJson = file("google-services.json")

if (googleServicesTemplate.exists()) {
    if (firebaseApiKey.isNullOrBlank()) {
        logger.warn("FIREBASE_API_KEY missing in .env; google-services.json will not be generated.")
    } else {
        val generatedContent = googleServicesTemplate
            .readText()
            .replace("__FIREBASE_API_KEY__", firebaseApiKey)
        googleServicesJson.parentFile?.mkdirs()
        if (!googleServicesJson.exists() || googleServicesJson.readText() != generatedContent) {
            googleServicesJson.writeText(generatedContent)
        }
    }
} else {
    logger.warn("google-services.template.json is missing; cannot generate google-services.json.")
}

android {
    namespace = "com.example.tightbudget"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.tightbudget"
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
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.appcompat)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Room components
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Confetti library
    implementation(libs.konfetti.xml)

    implementation(libs.glide)
}
