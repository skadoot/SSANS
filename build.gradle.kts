plugins {
    kotlin("jvm") version "1.9.23"
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "uk.ac.aber.cc39440"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.openjfx:javafx-controls:21.0.3")
    implementation("org.openjfx:javafx-fxml:21.0.3")

    implementation(project(":arduino-emulation"))
    implementation(project(":serial-emulation"))
}

javafx {
    version = "21.0.3"
    modules("javafx.controls", "javafx.fxml")
}

tasks.test {
    useJUnitPlatform()
}
tasks.jar {
    manifest {
        attributes("Main-Class" to "MainKt")
    }
}
tasks.shadowJar {
    archiveBaseName = "ssans"
    archiveClassifier = ""
    archiveVersion = ""
}
kotlin {
    jvmToolchain(21)
}