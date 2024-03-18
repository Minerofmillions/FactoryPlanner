plugins {
    id("calculator-combined.compose-library")
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":general-utils"))
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("plannerBase") {
            from(components["kotlin"])
        }
    }
}