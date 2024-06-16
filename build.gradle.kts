import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import java.time.Year

plugins {
    alias(libs.plugins.application) apply false
    alias(libs.plugins.library) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.dokka)
}

buildscript {
    dependencies {
        classpath(libs.dokka.base)
    }
}

subprojects {
    if (name in listOf("data", "view", "compose")) {
        apply(plugin = "org.jetbrains.dokka")
        tasks.withType<DokkaTaskPartial>().configureEach {
            val build = project.layout.buildDirectory.get()
            outputDirectory.set(file("$build/docs"))
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

tasks.dokkaHtmlMultiModule {
    outputDirectory.set(file("docs"))
    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
        customAssets = listOf(file("images/logo-icon.svg"))
        footerMessage = footer()
    }
}

fun footer() = "© ${Year.now().value} Mike M."