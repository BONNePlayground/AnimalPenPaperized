import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml

plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.18"
    id("xyz.jpenilla.run-paper") version "3.0.0-beta.1"
    id("xyz.jpenilla.resource-factory-bukkit-convention") version "1.3.0"
}

group = project.findProperty("maven_group") as String
version = project.findProperty("plugin_version") as String
description = "Capture and store animals in single block"

val minecraftVersion = project.findProperty("minecraft_version") as String
val javaVersion = (project.findProperty("java_version") as String).toInt()

base {
    archivesName.set(project.findProperty("archives_name") as String + "-$minecraftVersion")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)
}

dependencies {
    paperweight.paperDevBundle("$minecraftVersion-R0.1-SNAPSHOT")
}

tasks {
    compileJava {
        options.release = javaVersion
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }
}

// Configure plugin.yml generation
// - name, version, and description are inherited from the Gradle project.
bukkitPluginYaml {
    main = "lv.id.bonne.animalpenpaper.AnimalPenPlugin"
    load = BukkitPluginYaml.PluginLoadOrder.STARTUP
    authors.add("BONNe")
    apiVersion = minecraftVersion
    website = "https://github.com/BONNePlayground/AnimalPenPaperized"
}
