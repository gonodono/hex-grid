plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.kotlin)
    id("maven-publish")
}

android {
    namespace = "com.gonodono.hexgrid.view"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
    }
    buildFeatures {
        buildConfig = true
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
                artifactId = "view"
                version = findProperty("library.version").toString()
            }
        }
    }
}

dependencies {
    api(project(":data"))
    implementation(project(":core"))
    implementation(libs.core.ktx)
}