import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

group = "com.dustyn"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}
tasks.test {
    // Discover and execute JUnit4-based tests
    useJUnit()

    // Discover and execute TestNG-based tests
    useTestNG()

    // Discover and execute JUnit Platform-based (JUnit 5, JUnit Jupiter) tests
    // Note that JUnit 5 has the ability to execute JUnit 4 tests as well
    useJUnitPlatform()
}
dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation("org.pgpainless:pgpainless-core:1.6.6")
    implementation("org.mindrot:jbcrypt:0.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")

}


compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "KVault"
            packageVersion = "1.0.0"
        }
    }
}

