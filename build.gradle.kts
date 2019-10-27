import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.50"
    id("com.github.johnrengelman.shadow") version "5.0.0"
    application
}
group = "com.github.youta1119"
version = "0.0.1"
val mainClass = "com.github.youta1119.kotlin.dotnet.cli.K2DotNetCompiler"
repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-compiler:1.3.50")
    implementation(kotlin("stdlib-jdk8"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClassName = mainClass
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("kotlin-compiler")
        version = ""
        classifier = null
        manifest {
            attributes(mapOf("Main-Class" to mainClass))
        }
    }
}