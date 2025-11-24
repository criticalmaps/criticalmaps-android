import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.unmock)
}

// Place actual signing configuration in "keystore.properties"
// "keystore.properties" is in .gitignore and will not be checked into repo
val keystorePropertiesFile = rootProject.file("keystore.properties").let {
    if (it.exists()) it else rootProject.file("dummy_keystore.properties")
}
val keystoreProperties = Properties().apply {
    load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "de.stephanlindauer.criticalmaps"

    signingConfigs {
        create("releaseConfig") {
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
        }
    }

    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "de.stephanlindauer.criticalmaps"
        minSdk = 26
        targetSdk = 36
        versionCode = 50
        versionName = "2.9.2"
        vectorDrawables.useSupportLibrary = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["disableAnalytics"] = "true"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            isPseudoLocalesEnabled = true
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs["releaseConfig"]
        }
    }

    packaging {
        resources {
            excludes += "META-INF/services/javax.annotation.processing.Processor"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    lint {
        warning.addAll(
            listOf(
                "MissingTranslation",
                "StringFormatInvalid",
                "NewApi",
                "InvalidPackage"
            )
        )
    }
}

dependencies {
    implementation(libs.otto)
    implementation(libs.maplibre)
    implementation(libs.picasso)
    implementation(libs.timber)
    implementation(libs.okhttp)
    implementation(libs.typed.preferences)
    implementation(libs.material)
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.exifinterface)
    implementation(libs.androidx.constraintlayout)

    implementation(libs.dagger)
    annotationProcessor(libs.dagger.compiler)

    debugImplementation(libs.leakcanary)

    testImplementation(libs.junit)
    testImplementation(libs.com.google.truth)
    testImplementation(libs.org.mockito.core)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.espresso.core)
}
