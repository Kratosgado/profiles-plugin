import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
  id("java")
  alias(libs.plugins.kotlin)
  alias(libs.plugins.intellijPlatform)
}

group = "com.kratosgado"
version = "1.0.0"

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
    name = "IDE Profiles"
    description = """
        <p>
          <b>IDE Profiles</b> lets you define named sets of enabled plugins and switch between them
          instantly based on your current work context — frontend, backend, data science, and more.
        </p>
        <ul>
          <li><b>Create profiles</b> — capture the current set of enabled/disabled plugins as a named profile.</li>
          <li><b>Switch instantly</b> — switch profiles from the status bar widget with a single click.</li>
          <li><b>Per-project binding</b> — bind a project to a profile so the right plugins activate automatically when you open it.</li>
          <li><b>Global plugins</b> — mark plugins as "always on" so they remain enabled across all profiles.</li>
        </ul>
        <p>Stop toggling plugins manually. Let your IDE adapt to what you're working on.</p>
    """.trimIndent()

    changeNotes = """
        <ul>
          <li>Initial release.</li>
          <li>Create, save, and switch named plugin profiles.</li>
          <li>Status bar widget for quick profile switching.</li>
          <li>Per-project profile binding with automatic activation on project open.</li>
          <li>Global plugins list for always-on plugins.</li>
        </ul>
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