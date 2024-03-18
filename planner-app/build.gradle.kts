
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import kotlin.io.path.Path

plugins {
    id("calculator-combined.compose-app")
}

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)

    api(project(":planner-base"))
//    implementation(project(":recipe-factory-core"))
    implementation("org.jetbrains.compose.material:material-icons-extended:1.4.3")

    implementation(project(":recipe-factory-core"))

    api("org.pf4j:pf4j:3.9.0")
//    kapt("org.pf4j:pf4j:3.9.0")
}

compose.desktop {
    application {
        mainClass = "minerofmillions.recipe_factory.app.AppMainKt"

        nativeDistributions {
            modules("java.compiler", "java.instrument", "java.management", "java.naming", "java.net.http", "java.sql", "jdk.unsupported")
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "calculator-app"
            packageVersion = version.toString()
        }
    }
}

task<Copy>("copyToDesktop") {
     dependsOn("createDistributable")
    from(Path(buildDir.path, "compose", "binaries", "main", "app"))
    into("E:\\Jason\\Desktop")
}
