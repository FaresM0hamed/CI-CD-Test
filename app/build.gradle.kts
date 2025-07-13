import java.io.FileInputStream
import java.io.FileWriter
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

// Load version properties
val versionPropsFile = rootProject.file("version.properties")
val versionProps = Properties()

if (versionPropsFile.exists()) {
    versionProps.load(FileInputStream(versionPropsFile))
}

val versionMajor = versionProps["versionMajor"].toString().toInt()
val versionMinor = versionProps["versionMinor"].toString().toInt()
val versionPatch = versionProps["versionPatch"].toString().toInt()

fun generateVersionCode(): Int {
    return 26 * 10000000 + versionMajor * 10000 + versionMinor * 100 + versionPatch
}

fun generateVersionName(): String {
    return "$versionMajor.$versionMinor.$versionPatch"
}

android {
    namespace = "com.devfares.cicdtests"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.devfares.cicdtests"
        minSdk = 28
        targetSdk = 35
        versionCode = generateVersionCode()
        versionName = generateVersionName()

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
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(platform(libs.firebase.bom))
}

// Task to auto-increment patch version after release build
tasks.register("incrementPatchVersion") {
    doLast {
        val propsFile = rootProject.file("version.properties")
        val props = Properties()
        props.load(FileInputStream(propsFile))

        val currentPatch = props["versionPatch"].toString().toInt()
        props["versionPatch"] = (currentPatch + 1).toString()

        props.store(FileWriter(propsFile), null)
        println("✅ Bumped patch version to ${props["versionMajor"]}.${props["versionMinor"]}.${props["versionPatch"]}")
    }
}

// Attach increment to assembleRelease
gradle.taskGraph.whenReady {
    if (allTasks.any { it.name == "assembleRelease" }) {
        tasks.named("assembleRelease").configure {
            finalizedBy("incrementPatchVersion")
        }
    }
}