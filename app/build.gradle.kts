plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

}

android {
    namespace = "com.vms.wizwarehouse"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.vms.wizwarehouse"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.3.145"

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

        debug {
            isDebuggable = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures{
        buildConfig = true
        viewBinding = true
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // The following line is optional, as the core library is included indirectly by camera-camera2
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    // If you want to additionally use the CameraX Lifecycle library
    implementation(libs.androidx.camera.lifecycle)
    // If you want to additionally use the CameraX VideoCapture library
    implementation(libs.androidx.camera.video)
    // If you want to additionally use the CameraX View class
    implementation(libs.androidx.camera.view)
    // If you want to additionally add CameraX ML Kit Vision Integration
    implementation(libs.androidx.camera.mlkit.vision)
    // If you want to additionally use the CameraX Extensions library
    implementation(libs.androidx.camera.extensions)
    implementation("com.google.guava:guava:33.3.1-android")


    // location dependency
    implementation(libs.gms.play.services.location)
    implementation(libs.play.services.location.v2101)


    // lottie dependency
    implementation(libs.lottie)


    // circle imageview dependency
    implementation("de.hdodenhof:circleimageview:3.1.0")

    //signature pad
    implementation("com.github.gcacace:signature-pad:1.3.1")

    //retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")
    implementation("com.squareup.okio:okio:3.0.0")


    // Rating bar
    implementation("me.zhanghai.android.materialratingbar:library:1.4.0")

    // pin-view
    implementation("io.github.chaosleung:pinview:1.4.4")

    //Preference Encryption
    implementation (libs.androidx.security.crypto)
    implementation(libs.androidx.security.crypto.v110alpha06)

    //For test implementation for Java/Kotlin
    testImplementation(libs.org.jacoco.org.jacoco.agent)
    testImplementation(libs.junit.jupiter)


    // face detection
//    implementation("com.google.mlkit:face-detection:16.1.5")
    // new face detection
    implementation("com.google.android.gms:play-services-mlkit-face-detection:17.1.0")

    // socket.io dependency
    implementation("io.socket:socket.io-client:2.1.0")

    //room database
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    // Room with RxJava (optional)
    implementation("androidx.room:room-rxjava3:2.6.1")
    // Room with Coroutine support (optional)
    implementation("androidx.room:room-ktx:2.6.1")

    // Work Manager
    implementation("androidx.work:work-runtime:2.9.0")

    //firebase and crash-analytics
    // Add the Firebase SDK for Crashlytics.
//    implementation(libs.firebase.crashlytics)
//    //Add the Firebase SDK for Google Analytics.
//    implementation(libs.firebase.analytics)

    implementation("com.google.firebase:firebase-crashlytics:19.4.3")

    implementation("androidx.recyclerview:recyclerview:1.3.1")

    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    implementation(libs.material)
}