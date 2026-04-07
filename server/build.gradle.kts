plugins {
    application
}

dependencies {
    implementation("org.xerial:sqlite-jdbc:3.46.1.3")
}

application {
    mainClass.set("MockServer")
}

sourceSets {
    named("main") {
        java.setSrcDirs(listOf(rootProject.projectDir))
        java.include("MockServer.java")
    }
}

tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}