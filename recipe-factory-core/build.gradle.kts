plugins {
    id("calculator-combined.compose-library")
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    api("org.pf4j:pf4j:3.9.0")
    kapt("org.pf4j:pf4j:3.9.0")

    api("com.electronwill.night-config:core:3.6.0")

    api(project(":planner-base"))

    api("com.github.java-json-tools:json-patch:1.13") {
        exclude("com.fasterxml.jackson.core", "jackson-databind")
    }

    api("com.fasterxml.jackson.core:jackson-databind:2.15.2")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("recipeFactoryCore") {
            from(components["kotlin"])
        }
    }
}