plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal() // so that external plugins can be resolved in dependencies section
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
    implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:5.0.12")
    implementation("org.jetbrains.compose:compose-gradle-plugin:1.4.3")
    testImplementation("junit:junit:4.13.1")
}
