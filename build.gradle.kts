import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "2.1.10"
    id("org.jetbrains.compose") version "1.6.10"
    id("org.jetbrains.kotlin.plugin.compose")
    id("jacoco")
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://raw.github.com/gephi/gephi/mvn-thirdparty-repo/")
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.apache.commons:commons-lang3:3.12.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.3")
    implementation("org.xerial:sqlite-jdbc:3.49.1.0")
    implementation("org.gephi:gephi-toolkit:0.10.1:all")
    implementation("org.neo4j.driver:neo4j-java-driver:5.14.0")
    testImplementation("io.mockk:mockk:1.13.8")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.10.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    testImplementation(compose.desktop.uiTestJUnit4)
    testImplementation("org.neo4j:neo4j-common:4.4.5")
    testImplementation("org.neo4j:neo4j-kernel:4.4.5")
    testImplementation("org.neo4j:neo4j-dbms:4.4.5")
    testImplementation("org.neo4j:neo4j:4.4.43")
    implementation("org.neo4j.test:neo4j-harness:5.13.0")
    testImplementation("org.neo4j.test:neo4j-harness:5.13.0")
}

kotlin {
    jvmToolchain(18)
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(false)
        html.required.set(true)
        csv.required.set(true)
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "graphs-graphs-team-9"
            packageVersion = "1.0.0"
        }
    }
}
