plugins {
    kotlin("jvm") version "1.9.23"
}

group = "uk.ac.aber.cc39440"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(project(":serial-emulation"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}