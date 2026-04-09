plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    `maven-publish`
}

group = "io.github.artavazd-khachatryan"
version = "1.0.0-alpha02"

val isMac = System.getProperty("os.name").lowercase().contains("mac")

kotlin {
    android {
        namespace = "io.github.artavazdkhachatryan.markdowneditor"
        compileSdk = 36
        minSdk = 28
    }

    if (isMac) {
        val xcf = org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFrameworkConfig(project, "MarkdownEditor")

        listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { target ->
            target.binaries.framework {
                baseName = "MarkdownEditor"
                isStatic = false
                xcf.add(this)
            }
        }

        targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
            binaries.withType<org.jetbrains.kotlin.gradle.plugin.mpp.Framework>().configureEach {
                val framework = this
                val copyTaskName = "copyOfflineAssetsInto${framework.linkTaskName.replaceFirstChar { it.uppercaseChar() }}"
                val frameworkOutputDir = framework.outputDirectory
                tasks.register<Copy>(copyTaskName) {
                    from("src/iosMain/resources/offline")
                    into(frameworkOutputDir.resolve("MarkdownEditor.framework/offline"))
                    dependsOn(framework.linkTaskName)
                }
                tasks.named(framework.linkTaskName) {
                    finalizedBy(copyTaskName)
                }
                tasks.matching { it.name.contains("ForMarkdownEditorXCFramework") }.configureEach {
                    dependsOn(copyTaskName)
                }
            }
        }

        // After the XCFramework is assembled, copy offline assets into simulator slices
        // (fat framework assembly doesn't carry over custom resource folders)
        tasks.matching { it.name.startsWith("assembleMarkdownEditor") && it.name.endsWith("XCFramework") }.configureEach {
            doLast {
                val xcfDir = layout.buildDirectory.dir("XCFrameworks").get().asFile
                xcfDir.walkTopDown()
                    .filter { it.name == "MarkdownEditor.framework" && it.parentFile.name.contains("simulator") }
                    .forEach { frameworkDir ->
                        copy {
                            from("src/iosMain/resources/offline")
                            into(frameworkDir.resolve("offline"))
                        }
                    }
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.material3)
        }
        androidMain.dependencies {
            implementation(libs.androidx.webkit)
        }
    }
}
