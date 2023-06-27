import net.minecraftforge.gradle.userdev.UserDevExtension
import net.minecraftforge.gradle.userdev.DependencyManagementExtension
import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    kotlin("jvm") version "1.8.22"
}

buildscript {
    repositories {
        maven { url = uri("https://repo.siro256.dev/repository/maven-public/") }
    }

    dependencies {
        classpath("net.minecraftforge.gradle:ForgeGradle:5.1.69") {
            isChanging = true
        }
    }
}

group = "dev.siro256.rtmpack.undergroundbracketkt"
version = "1.0.0-SNAPSHOT"
description = "The bracket model for the RealTrainMod"
val projectURL = "https://github.com/Kotatsu-RTM/UndergroundBracketKt"
val authors =
    listOf(
        "Siro_256 Twitter: @ffffff_256"
    )

apply(plugin = "net.minecraftforge.gradle")

repositories {
    maven { url = uri("https://repo.siro256.dev/repository/maven-public/") }
    maven { url = uri("https://cursemaven.com") }
}

dependencies {
    val forgeDependencyManager =
        project.extensions[DependencyManagementExtension.EXTENSION_NAME] as DependencyManagementExtension

    implementation("com.github.kotatsu-rtm.kotatsulib:KotatsuLib-mc1_12_2:0.0.1-SNAPSHOT")

    add("minecraft", "net.minecraftforge:forge:1.12.2-14.23.5.2860")

    implementation(forgeDependencyManager.deobf("curse.maven:ngtlib-288989:3873392"))
    implementation(forgeDependencyManager.deobf("curse.maven:realtrainmod-288988:3873403"))
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
}

configure<UserDevExtension> {
    mappings("snapshot", "20171003-1.12")
}

kotlin {
    jvmToolchain(8)
}

val tmpSrc = File(buildDir, "tmpSrc/main/kotlin")

val tokens = mapOf(
    "modId" to project.name.toLowerCase(),
    "modName" to project.name,
    "modVersion" to project.version.toString(),
    "description" to project.description,
    "modPage" to projectURL,
    "authors" to authors.joinToString { "\"$it\"" }
)

tasks {
    withType<Jar> {
        exclude("META-INF/versions/9/module-info.class")
    }

    create("cloneSource", Copy::class) {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        from(File(projectDir, "src/main/kotlin/"))
        into(tmpSrc)
        filter<ReplaceTokens>("tokens" to tokens)
    }

    compileKotlin {
        doFirst {
            sourceSets.main.get().kotlin.setSrcDirs(tmpSrc.toPath())
        }

        kotlinOptions.allWarningsAsErrors = true

        dependsOn("cloneSource")
    }

    processResources {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        exclude(listOf("**/*.blend", "**/*.blend1", "**/*.psd"))

        filesMatching("assets/undergroundbracketkt/model_jsons/**") {
            name = name.replace(".json", "")
        }

        filesMatching(
            listOf(
                "mcmod.info",
                "**/*.json"
            )
        ) {
            filter<ReplaceTokens>("tokens" to tokens)
        }
    }
}
