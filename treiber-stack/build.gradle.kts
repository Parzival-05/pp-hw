plugins {
    kotlin("jvm") version "1.9.22"
}
group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlinx:lincheck:2.28.1")
    testImplementation("junit:junit:4.13.2")
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:atomicfu:0.23.2")

}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

