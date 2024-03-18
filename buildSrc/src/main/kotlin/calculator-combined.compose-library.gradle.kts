plugins {
    id("calculator-combined.kotlin-conventions")
    id("org.jetbrains.compose")
}

dependencies {
    implementation("com.arkivanov.decompose:decompose:2.0.1")
    implementation("com.arkivanov.decompose:extensions-compose-jetbrains:2.0.1")

    implementation(compose.desktop.common)
}