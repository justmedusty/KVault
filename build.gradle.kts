import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

group = "com.dustyn"
version = "1.1"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()

}
tasks.test {
    useJUnitPlatform()
    testLogging{
        events("passed","skipped","failed")
    }


}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)
    implementation("org.pgpainless:pgpainless-core:1.6.4")
    implementation("org.mindrot:jbcrypt:0.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    implementation("org.slf4j:slf4j-api:2.0.12")

}


compose.desktop {

    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "KVault"
            packageVersion = "1.1.0"
            description = "A multiplatform encrypted vault application"
            licenseFile.set(project.file("gpl-3.0.txt"))
            modules("java.instrument", "java.naming", "java.sql", "jdk.unsupported")
            macOS {
                dockName = "KVault"
            }
            windows {
                iconFile.set(project.file("src/main/resources/icon.ico"))
                dirChooser = true
                menu = true

            }
            linux {
                iconFile.set(project.file("src/main/resources/icon.png"))
                debMaintainer = "dustyngibb@protonmail.com"
                appCategory = "Privacy"
            }
        }


    }




}

