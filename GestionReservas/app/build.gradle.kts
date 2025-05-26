plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.gestionreservas"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.gestionreservas"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "GMAIL_CLIENT_ID", "\"${project.property("GMAIL_CLIENT_ID")}\"")
        buildConfigField("String", "GMAIL_CLIENT_SECRET", "\"${project.property("GMAIL_CLIENT_SECRET")}\"")

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
    viewBinding{
        enable=true
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    //corrutinas
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    //Auth correo
    implementation ("com.google.android.gms:play-services-auth:20.7.0")

    implementation ("androidx.work:work-runtime-ktx:2.9.0")

    implementation("org.jsoup:jsoup:1.15.4")


    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.retrofit)
    implementation(libs.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.drawerlayout)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.androidx.swiperefreshlayout)

    testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation ("io.mockk:mockk:1.13.5")
    androidTestImplementation ("androidx.test.espresso:espresso-intents:3.6.1")
    testImplementation ("com.squareup.okhttp3:mockwebserver:4.9.0")
    androidTestImplementation("com.squareup.okhttp3:mockwebserver:4.9.0")
    androidTestImplementation ("androidx.test.espresso:espresso-contrib:3.5.1")


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

}

