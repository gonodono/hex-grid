plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.compose.compiler)
    id("maven-publish")
}

android {
    namespace = "com.gonodono.hexgrid.compose"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
    }
    buildFeatures {
        compose = true
    }
    buildTypes.all {
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    publishing {
        singleVariant("release")
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.gonodono.hexgrid"
                artifactId = "compose"
                version = findProperty("library.version").toString()
            }
        }
    }
}

dependencies {
    api(project(":data"))
    implementation(project(":core"))
    implementation(libs.core.ktx)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
}