import java.io.FileInputStream
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "2.4.21"
}

val ksFile = rootProject.file("signing.properties")
val props = Properties()
if (ksFile.canRead()) {
    props.load(FileInputStream(ksFile))
    android.signingConfigs.create("sign").apply {
        storeFile = file(props["KEYSTORE_FILE"] as String)
        storePassword = props["KEYSTORE_PASSWORD"] as String
        keyAlias = props["KEYSTORE_ALIAS"] as String
        keyPassword = props["KEYSTORE_ALIAS_PASSWORD"] as String
    }
} else {
    android.signingConfigs.create("sign").apply {
        storeFile = android.signingConfigs.getByName("debug").storeFile
        storePassword = android.signingConfigs.getByName("debug").storePassword
        keyAlias = android.signingConfigs.getByName("debug").keyAlias
        keyPassword = android.signingConfigs.getByName("debug").keyPassword
    }
}

android {
    namespace = "com.itosfish.colorfeatureenhance"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.itosfish.colorfeatureenhance"
        minSdk = 34
        targetSdk = 37
        versionCode = 20261108
        versionName = "1.0.0"

        ndk {
            // 设置支持的SO库架构
            abiFilters.addAll(listOf("arm64-v8a"))
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("sign")
        }
        debug {
            signingConfig = signingConfigs.getByName("sign")
        }
    }
    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
        dex {
            useLegacyPackaging = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

// 模块模板只在 exampleConfig/mod 维护，打包时自动生成 assets/mod.zip
val moduleTemplateDir = rootProject.layout.projectDirectory.dir("exampleConfig/mod")
val preparedModuleDir = layout.buildDirectory.dir("moduleTemplate")
val generatedModuleAssetsDir = layout.buildDirectory.dir("generated/moduleAssets")

val prepareModuleTemplate by tasks.registering {
    inputs.dir(moduleTemplateDir)
    outputs.dir(preparedModuleDir)

    doLast {
        val source = moduleTemplateDir.asFile
        val output = preparedModuleDir.get().asFile
        if (output.exists()) output.deleteRecursively()

        copy {
            from(source)
            into(output)
        }

        output.walkTopDown().filter { it.isFile }.forEach { file ->
            val text = file.readText(Charsets.UTF_8)
            if (!text.contains('\u0000')) {
                file.writeText(text.replace("\r\n", "\n"), Charsets.UTF_8)
            }
        }
    }
}

val generateModuleZip by tasks.registering(org.gradle.api.tasks.bundling.Zip::class) {
    dependsOn(prepareModuleTemplate)
    from(preparedModuleDir)
    archiveFileName.set("mod.zip")
    destinationDirectory.set(generatedModuleAssetsDir)
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true

    eachFile {
        val executable = name.endsWith(".sh") || name == "update-binary"
        permissions { unix(if (executable) "rwxr-xr-x" else "rw-r--r--") }
    }
}

android.sourceSets.getByName("main").assets.srcDir(generatedModuleAssetsDir.get().asFile)

tasks.matching {
    it.name == "preBuild" || (it.name.startsWith("merge") && it.name.endsWith("Assets"))
}.configureEach {
    dependsOn(generateModuleZip)
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
    implementation(libs.androidx.material.icons.core)
    implementation(libs.material)
    implementation(libs.kotlinx.serialization.json)

    // 网络请求依赖，用于云端配置下载
    implementation(libs.okhttp)

    // implementation(libs.androidx.material.icons.extended)
}
