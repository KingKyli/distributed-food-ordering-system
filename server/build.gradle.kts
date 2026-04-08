plugins {
    application
}

dependencies {
    implementation("org.xerial:sqlite-jdbc:3.46.1.3")
}

application {
    mainClass.set("MockServer")
}

tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}