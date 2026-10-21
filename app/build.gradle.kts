plugins {
    alias(libs.plugins.android.application)
}

android {
	namespace = "es.upm.miw.bantumi"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "es.upm.miw.bantumi"
        minSdk = 28
        targetSdk = 37
        versionCode = 1
        versionName = "1.3.5"
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    api(libs.material)

    implementation(libs.appcompat)
	
    implementation(libs.constraintLayout)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel)

    // LiveData
    implementation(libs.androidx.lifecycle.livedata)

    implementation(libs.androidx.lifecycle.livedata.core)

    // Room
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    // Preference
    implementation(libs.preference)
}