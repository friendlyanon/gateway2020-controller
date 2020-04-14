@file:Suppress("ConstantConditionIf")

import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
import java.util.jar.Attributes.Name
import java.util.jar.Attributes.Name.*

plugins {
    val ktlintVersion = "9.2.1"

    application
    kotlin("jvm") version "1.3.71"
    id("org.jlleitschuh.gradle.ktlint") version ktlintVersion
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("org.jlleitschuh.gradle.ktlint-idea") version ktlintVersion
}

group = "redacted"
version = "0.1"
val title = "Controller"
val vendor = "[DATA EXPUNGED]"
val shouldRelocate = !System.getenv("SHOULD_RELOCATE").isNullOrBlank()

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    ktlintRuleset(files("$projectDir/custom-rules/no-sun-imports.jar"))
    listOf(
        kotlin("stdlib-jdk8"),
        "org.mapdb:mapdb:3.0.8",
        "org.zeromq:jeromq:0.5.1",
        "org.slf4j:slf4j-api:1.7.30",
        "org.slf4j:slf4j-simple:1.7.30",
        "org.nanohttpd:nanohttpd:2.3.1",
        "mysql:mysql-connector-java:8.0.19",
        "com.fasterxml.jackson.module:jackson-module-kotlin:2.10.3"
    ).forEach { implementation(it) }
}

ktlint {
    verbose.set(true)
    outputToConsole.set(true)
    reporters {
        reporter(ReporterType.PLAIN)
        reporter(ReporterType.CHECKSTYLE)
    }
}

application {
    mainClassName = "gateway.controller.MainKt"
}

fun Jar.addVersionInfo() = manifest {
    infix fun Name.to(value: Any?) = toString() to value
    attributes(
        SPECIFICATION_TITLE to title,
        SPECIFICATION_VENDOR to vendor,
        SPECIFICATION_VERSION to project.version
    )
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    jar {
        addVersionInfo()
    }

    val shadowJarTask = named<ShadowJar>("shadowJar")
    val relocate = register<ConfigureShadowRelocation>("relocateShadowJar") {
        target = shadowJarTask.get()
    }

    shadowJar {
        destinationDirectory.set(File(projectDir, "./build/"))
        mergeServiceFiles()
        addVersionInfo()
        if (shouldRelocate) {
            dependsOn(relocate)
        }
    }
}
