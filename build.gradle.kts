plugins {
    kotlin("jvm") version "2.1.0"
    application
}

group = "cl.caparazonpizza"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("cl.caparazonpizza.MainKt")
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=UTF-8")
}

tasks.test {
    useJUnitPlatform()
}
