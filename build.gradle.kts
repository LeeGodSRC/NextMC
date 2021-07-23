plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.20"
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "5.2.0"
    id("org.jetbrains.kotlin.kapt") version "1.5.20"
}

group = "org.fairy"
version = "0.0.1b1"

repositories {
    mavenCentral()
    jcenter()
    maven("https://libraries.minecraft.net")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.5.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.0")
    implementation("io.netty:netty-all:4.1.65.Final")
    implementation("com.google.guava:guava:30.1.1-jre")
    implementation("com.github.ben-manes.caffeine:caffeine:3.0.3")
    implementation("com.mojang:authlib:1.5.25")
    implementation("net.jafama:jafama:2.3.2")
    implementation("it.unimi.dsi:fastutil:8.5.4")

    api(platform("net.kyori:adventure-bom:4.8.1"))
    api("net.kyori:adventure-api")
    api("net.kyori:adventure-text-serializer-gson")
    api("net.kyori:adventure-text-serializer-legacy")
    api("net.kyori:adventure-text-serializer-plain")

    api("org.apache.logging.log4j:log4j-api:" + findProperty("log4j.version"))
    api("org.apache.logging.log4j:log4j-core:" + findProperty("log4j.version"))
    api("org.apache.logging.log4j:log4j-slf4j-impl:" + findProperty("log4j.version"))
    api("org.apache.logging.log4j:log4j-iostreams:" + findProperty("log4j.version"))

    compileOnly("org.jetbrains:annotations:21.0.1")

    implementation("net.java.dev.jna:jna:" + findProperty("jna.version"))
    implementation("com.lmax:disruptor:" + findProperty("disruptor.version"))
    implementation("net.minecrell:terminalconsoleappender:" + findProperty("minecrell.version"))

    implementation("net.sf.jopt-simple:jopt-simple:5.0.4")
    testImplementation("junit:junit:4.12")

    implementation("org.openjdk.jmh:jmh-core:1.21")
    kaptTest("org.openjdk.jmh:jmh-generator-annprocess:1.21")
}