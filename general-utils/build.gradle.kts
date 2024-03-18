plugins {
    id("calculator-combined.compose-library")
    `maven-publish`
}

dependencies {
    api("org.ojalgo:ojalgo:53.0.0")
    api("com.electronwill.night-config:core:3.6.0")
}

publishing {
    publications {
        create<MavenPublication>("generalUtils") {
            from(components["kotlin"])
        }
    }
}