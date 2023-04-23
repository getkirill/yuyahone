plugins {
    kotlin("jvm") version "1.8.0"
    `maven-publish`
}

group = "com.kraskaska.economics"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

// Local repo
publishing {
    publications {
        create<MavenPublication>("yuyahone") {
            from(components["java"])
        }
    }
}