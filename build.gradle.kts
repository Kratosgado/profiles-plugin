import com.github.javaparser.printer.concretesyntaxmodel.CsmElement.token
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
  id("java")
  alias(libs.plugins.kotlin)
  alias(libs.plugins.intellijPlatform)
}

group = "com.kratosgado"
version = "1.0.0-SNAPSHOT"

// Set the JVM language level used to build the project.
kotlin {
  jvmToolchain(21)
}

repositories {
  mavenCentral()
  intellijPlatform {
    defaultRepositories()
  }
}

// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
  intellijPlatform {
    intellijIdea(providers.gradleProperty("platformVersion"))
    pluginVerifier()
    zipSigner()
    testFramework(TestFrameworkType.Platform)


    // Add plugin dependencies for compilation here, for example:
    // bundledPlugin("com.intellij.java")

  }
}

intellijPlatform {
  pluginConfiguration {
    ideaVersion {
      sinceBuild = providers.gradleProperty("pluginSinceBuild")
      untilBuild = provider { null }  // open-ended
    }
    name = "Plugin Profiles"
    description = """
            Define plugin sets as profiles and switch between them
            based on your current work context.
        """.trimIndent()

    changeNotes = """
            Initial version
        """.trimIndent()
  }

  pluginVerification {
    ides {
      recommended()
    }
  }

  publishing {
    token.set(System.getenv("PUBLISH_TOKEN"))
  }
}

tasks {
  wrapper {
    gradleVersion = providers.gradleProperty("gradleVersion").get()
  }
}