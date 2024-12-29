import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml

plugins {
    `java-library`
    `maven-publish`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.8"
    id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.2.0" // Generates plugin.yml based on the Gradle config
}

group = "me.DJ1TJOO"
version = "1.0-SNAPSHOT"
description = "CustomGUI"

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 21 on systems that only have JDK 11 installed for example.
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

dependencies {
    // compileOnly(libs.io.papermc.paper.paper.api)
    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")

    compileOnly("com.github.retrooper:packetevents-spigot:2.7.0")
}

tasks {
    compileJava {
        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release = 21
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    maven {
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }

    maven { url = uri("https://repo.codemc.io/repository/maven-releases/") }

    maven { url = uri("https://repo.codemc.io/repository/maven-snapshots/") }
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}

bukkitPluginYaml {
    main = "me.dj1tjoo.customgui.CustomGUIPlugin"
    load = BukkitPluginYaml.PluginLoadOrder.STARTUP
    authors.add("DJ1TJOO")
    commands.create("open-gui")
    depend = listOf("packetevents")
    apiVersion = "1.21"
}