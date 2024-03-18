// Define Java conventions for this organization.
// Projects need to use the Java, Checkstyle and Spotbugs plugins.

plugins {
    kotlin("jvm")
    kotlin("kapt")
//    checkstyle

    // NOTE: external plugin version is specified in implementation dependency artifact of the project's build file
//    id("com.github.spotbugs")
}

group = "minerofmillions"

// Projects should use Maven Central for external dependencies
// This could be the organization's private repository
repositories {
    mavenCentral()
}

dependencies {
    api("io.github.microutils:kotlin-logging-jvm:2.0.11")
    implementation("ch.qos.logback:logback-classic:1.2.6")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:1.7.3")
}

version = "1.0.0"

// Use the Checkstyle rules provided by the convention plugin
// Do not allow any warnings
/*
checkstyle {
    config = resources.text.fromString(com.example.CheckstyleUtil.getCheckstyleConfig("/checkstyle.xml"))
    maxWarnings = 0
}
*/

// Enable deprecation messages when compiling Java code
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:deprecation")
}
