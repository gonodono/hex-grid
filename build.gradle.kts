import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import java.time.Year

plugins {
    id("com.android.library") version "8.2.2" apply false
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("org.jetbrains.dokka") version "1.9.10"
}

buildscript {
    dependencies {
        classpath("org.jetbrains.dokka:dokka-base:1.9.10")
    }
}

tasks.dokkaHtmlMultiModule {
    outputDirectory.set(file("docs"))
    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
        customAssets = listOf(file("images/logo-icon.svg"))
        footerMessage = footer()
    }
}

subprojects {
    if (name in listOf("data", "view", "compose")) {
        apply(plugin = "org.jetbrains.dokka")
        tasks.withType<DokkaTaskPartial>().configureEach {
            outputDirectory.set(file("$buildDir/docs"))
            pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
                if (this@subprojects.name == "view") {
                    suppressInheritedMembers = true
                } else {
                    suppressObviousFunctions = true
                }
                footerMessage = footer()
            }
        }
    }
}

fun footer() = "© ${Year.now().value} Mike M."