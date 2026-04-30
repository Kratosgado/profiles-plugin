package com.kratosgado.profiles.startup

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ex.ApplicationEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.ui.Messages
import com.kratosgado.profiles.manager.ProfileManager
import com.kratosgado.profiles.settings.ProjectProfileSettings

class ProfileProjectActivity : ProjectActivity {
  override suspend fun execute(project: Project) {
    val projectSettings = ProjectProfileSettings.getInstance(project)
    val boundProfile = projectSettings.state.boundProfile ?: return

    ApplicationManager.getApplication().invokeLater {
      val changed = ProfileManager.applyProfile(boundProfile)
      if (changed && ProfileManager.isRestartRequired()) {
        val restart = Messages.showYesNoDialog(
          "This project is bound to plugin profile '$boundProfile'. Restart IDE to apply changes?",
          "Restart Required",
          "Restart",
          "Later",
          Messages.getQuestionIcon()
        )
        if (restart == Messages.YES) {
          val app = ApplicationManager.getApplication() as ApplicationEx
          app.restart(true)
        }
      }
    }
  }
}