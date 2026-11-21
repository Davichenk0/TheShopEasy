import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.kapt)
}

// Las llaves se leen de local.properties (archivo ignorado por git).
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

fun localConfig(name: String, default: String = ""): String =
    localProperties.getProperty(name, default).replace("\"", "\\\"")

fun localBoolean(name: String, default: Boolean): String =
    (localProperties.getProperty(name, default.toString()).ifBlank { default.toString() })
        .toBooleanStrictOrNull()?.toString() ?: default.toString()

android {
    namespace = "com.example.shopeasy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.shopeasy"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Firebase (Firestore + Auth anónima)
        buildConfigField("String", "FIREBASE_PROJECT_ID", "\"${localConfig("FIREBASE_PROJECT_ID")}\"")
        buildConfigField("String", "FIREBASE_APP_ID", "\"${localConfig("FIREBASE_APP_ID")}\"")
        buildConfigField("String", "FIREBASE_API_KEY", "\"${localConfig("FIREBASE_API_KEY")}\"")

        // ePayco (Standard Checkout)
        buildConfigField("String", "EPAYCO_P_CUST_ID_CLIENTE", "\"${localConfig("EPAYCO_P_CUST_ID_CLIENTE")}\"")
        buildConfigField("String", "EPAYCO_P_KEY", "\"${localConfig("EPAYCO_P_KEY")}\"")
        buildConfigField("String", "EPAYCO_PUBLIC_KEY", "\"${localConfig("EPAYCO_PUBLIC_KEY")}\"")
        buildConfigField("String", "EPAYCO_CONFIRMATION_URL", "\"${localConfig("EPAYCO_CONFIRMATION_URL")}\"")
        buildConfigField("boolean", "EPAYCO_TEST", localBoolean("EPAYCO_TEST", true))
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
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    implementation(libs.glide)
    kapt(libs.glide.compiler)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.postgres)
    implementation(libs.supabase.auth)
    implementation(libs.supabase.storage)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.googleid)
    implementation(libs.credential.manager)
    implementation(libs.credential.manager.play)
    implementation(libs.biometric)
    implementation(libs.androidx.browser)

    implementation(libs.coil)
    implementation(libs.supabase.storage)

    // Carrito y órdenes en Firestore
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.coroutines.play.services)
}
