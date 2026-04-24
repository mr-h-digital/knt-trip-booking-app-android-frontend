plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    // id("com.google.gms.google-services")  -- enable after adding google-services.json
}

android {
    namespace = "com.kntransport.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.kntransport.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // API base URL — override in local.properties or CI env var
        buildConfigField("String", "API_BASE_URL", "\"https://api.ktransport.co.za/\"")
        buildConfigField("String", "API_BASE_URL_DEV", "\"http://10.0.2.2:8080/\"")
        buildConfigField("String", "API_BASE_URL_RAILWAY", "\"https://knt-trip-booking-app-spring-boot-backend-production.up.railway.app/\"")
    }

    buildTypes {
        debug {
            buildConfigField("String", "ACTIVE_API_URL", "\"https://knt-trip-booking-app-spring-boot-backend-production.up.railway.app/\"")
            buildConfigField("Boolean", "USE_MOCK_DATA", "false")
        }
        create("local") {
            initWith(getByName("debug"))
            buildConfigField("String", "ACTIVE_API_URL", "\"http://10.0.2.2:8080/\"")
            buildConfigField("Boolean", "USE_MOCK_DATA", "false")
        }
        create("staging") {
            initWith(getByName("debug"))
            buildConfigField("String", "ACTIVE_API_URL", "\"https://knt-trip-booking-app-spring-boot-backend-production.up.railway.app/\"")
            buildConfigField("Boolean", "USE_MOCK_DATA", "false")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField("String", "ACTIVE_API_URL", "\"https://api.ktransport.co.za/\"")
            buildConfigField("Boolean", "USE_MOCK_DATA", "false")
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

    buildFeatures {
        compose    = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Navigation + Lifecycle
    implementation("androidx.navigation:navigation-compose:2.9.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.compose.material:material-icons-extended")

    // Image loading
    implementation("io.coil-kt.coil3:coil-compose:3.1.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.1.0")

    // Networking — Retrofit + OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Security — encrypted token storage
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Splash Screen API
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Paging 3
    implementation("androidx.paging:paging-runtime:3.3.6")
    implementation("androidx.paging:paging-compose:3.3.6")

    // Connectivity
    implementation("androidx.core:core-ktx:1.17.0")

    // DataStore (preferences)
    implementation("androidx.datastore:datastore-preferences:1.1.4")

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
