import de.mannodermaus.gradle.plugins.junit5.junitPlatform
import org.jetbrains.kotlin.config.KotlinCompilerVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
    id("androidx.navigation.safeargs.kotlin")
    id("de.mannodermaus.android-junit5")
    id("deploygate")
}

repositories {
    flatDir {
        dirs("libs")
    }
}

android {
    compileSdkVersion(29)
    buildToolsVersion("29.0.2")
    defaultConfig {
        applicationId = "jp.shiita.astra"
        minSdkVersion(21)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        resValue("string", "sky_way_api_key", ApiKeys.SKY_WAY_API_KEY)
        resValue("string", "sky_way_domain", ApiKeys.SKY_WAY_DOMAIN)
    }
    signingConfigs {
        getByName("debug") {
            storeFile = rootProject.file("keystore/debug.keystore")
            storePassword = Keystore.STORE_PASSWORD
            keyAlias = Keystore.KEY_ALIAS
            keyPassword = Keystore.KEY_PASSWORD
        }
    }
    buildTypes {
        val appName = "Astra"
        getByName("debug") {
            resValue("string", "app_name", "$appName-debug")

            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
        }
        getByName("release") {
            resValue("string", "app_name", appName)

            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    androidExtensions {
        isExperimental = true
    }
    dataBinding {
        isEnabled = true
    }
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    lintOptions {
        disable("GoogleAppIndexingWarning")
    }
    testOptions {
        junitPlatform {
            filters {
                includeEngines("spek2")
            }
        }
    }
}

dependencies {
    implementation(fileTree("dir" to "libs", "include" to arrayOf("*.jar")))
    implementation(group = "", name = "skyway", ext = "aar")

    implementation(kotlin("stdlib-jdk8", KotlinCompilerVersion.VERSION))
    implementation(kotlin("reflect", KotlinCompilerVersion.VERSION))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.3.0")

    // AndroidX
    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("androidx.recyclerview:recyclerview:1.0.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation("androidx.core:core:1.1.0")
    implementation("androidx.core:core-ktx:1.1.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0-rc02")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.2.0-rc01")
    implementation("androidx.fragment:fragment-ktx:1.2.0-rc02")
    implementation("com.google.android.material:material:1.0.0")
    implementation("androidx.lifecycle:lifecycle-runtime:2.1.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.1.0")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.1.0")

    // Navigation
    implementation("androidx.navigation:navigation-runtime:2.1.0")
    implementation("androidx.navigation:navigation-runtime-ktx:2.1.0")
    implementation("androidx.navigation:navigation-fragment:2.1.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.1.0")
    implementation("androidx.navigation:navigation-ui:2.1.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.1.0")

    // GMS
    implementation("com.google.android.gms:play-services-location:17.0.0")

    // Dagger
    api("com.google.dagger:dagger:2.25.2")
    api("com.google.dagger:dagger-android:2.25.2")
    api("com.google.dagger:dagger-android-support:2.25.2")
    kapt("com.google.dagger:dagger-compiler:2.25.2")
    kapt("com.google.dagger:dagger-android-processor:2.25.2")
    compileOnly("com.squareup.inject:assisted-inject-annotations-dagger2:0.5.0")
    kapt("com.squareup.inject:assisted-inject-processor-dagger2:0.5.0")

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.0.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.0.1")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.10.0")
    kapt("com.github.bumptech.glide:compiler:4.10.0")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.6.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.6.0")

    // Moshi
    implementation("com.squareup.moshi:moshi:1.8.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.8.0")

    implementation("com.afollestad.material-dialogs:core:3.1.1")
    implementation("com.afollestad.material-dialogs:lifecycle:3.1.1")
    implementation("com.jakewharton.timber:timber:4.7.1")
    implementation("com.jakewharton.threetenabp:threetenabp:1.2.1")
    implementation("com.airbnb.android:lottie:3.1.0")
    implementation("org.permissionsdispatcher:permissionsdispatcher:4.3.1")
    kapt("org.permissionsdispatcher:permissionsdispatcher-processor:4.3.1")

    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.0-alpha-2")

    testImplementation("org.jetbrains.kotlin:kotlin-reflect:${KotlinCompilerVersion.VERSION}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.3.0")
    testImplementation("androidx.arch.core:core-testing:2.1.0")

    // JUnit
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.5.1")
    testImplementation("org.assertj:assertj-core:3.12.2")

    // Mockito
    testImplementation("org.mockito:mockito-core:3.0.0")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")

    // Spek
    testImplementation("org.spekframework.spek2:spek-dsl-jvm:2.0.6")
    testImplementation("org.spekframework.spek2:spek-runner-junit5:2.0.6")

    testImplementation("org.threeten:threetenbp:1.4.0") {
        exclude("com.jakewharton.threetenabp:threetenabp:1.2.1")
    }

    androidTestImplementation("org.jetbrains.kotlin:kotlin-reflect:${KotlinCompilerVersion.VERSION}")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.3.0")
    androidTestImplementation("androidx.arch.core:core-testing:2.1.0")

    // JUnit
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("org.assertj:assertj-core:3.12.2")

    androidTestImplementation("org.threeten:threetenbp:1.4.0") {
        exclude("com.jakewharton.threetenabp:threetenabp:1.2.1")
    }

    androidTestImplementation("androidx.test:runner:1.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
}