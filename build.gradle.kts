buildscript {
    repositories {
        google()
        mavenCentral()
        jcenter()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.0.4")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.20")
    }
}

plugins {
    id("com.github.ben-manes.versions") version "0.39.0"
}

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven("https://developer.huawei.com/repo/")
    }
}

tasks {
    val clean by registering(Delete::class) {
        delete(buildDir)
    }

    //检查依赖库更新
    //gradlew dependencyUpdates
    dependencyUpdates {
        rejectVersionIf {
            isNonStable(candidate.version)
        }
        checkForGradleUpdate = true
        outputFormatter = "html"
        outputDir = "build/dependencyUpdates"
        reportfileName = "report"
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}