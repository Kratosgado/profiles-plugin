package com.kratosgado.profiles.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ex.ApplicationEx
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.util.Consumer
import com.kratosgado.com.kratosgado.profiles.utils.PopupConstants
import com.kratosgado.profiles.manager.ProfileManager
import com.kratosgado.profiles.settings.ProfilesSettings
import java.awt.event.MouseEvent
import javax.swing.Icon

class ProfileStatusBarWidgetFactory : StatusBarWidgetFactory {
  override fun getId(): String = "PluginProfilesWidget"
  override fun getDisplayName(): String = "IDE Profiles"
  override fun isAvailable(project: Project): Boolean = true
  override fun createWidget(project: Project): StatusBarWidget = ProfileStatusBarWidget()

  override fun disposeWidget(widget: StatusBarWidget) {
    // Nothing to dispose
  }

  override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true
}

class ProfileStatusBarWidget : StatusBarWidget, StatusBarWidget.MultipleTextValuesPresentation {

  override fun ID(): String = "PluginProfilesWidget"

  override fun getPresentation(): StatusBarWidget.WidgetPresentation = this

  override fun getTooltipText(): String = "Switch Plugin Profile"

  override fun getClickConsumer(): Consumer<MouseEvent>? = null

  override fun getPopupStep(): ListPopup {
    val settings = ProfilesSettings.getInstance()
    val profiles = settings.state.profiles.keys.toList()
    val options = mutableListOf<String>()
    options.addAll(profiles)
    options.add(PopupConstants.SAVE_CURRENT_PROFILE_TEXT)
    options.add(PopupConstants.CONFIGURE_PROFILES_TEXT)

    val step = object : BaseListPopupStep<String>("Select Plugin Profile", options) {
      override fun onChosen(selectedValue: String?, finalChoice: Boolean): PopupStep<*>? {
        when (selectedValue) {
          null -> return null
          PopupConstants.CONFIGURE_PROFILES_TEXT -> {
            ApplicationManager.getApplication().invokeLater {
              ShowSettingsUtil.getInstance().showSettingsDialog(null, "IDE Profiles")
            }
          }

          PopupConstants.SAVE_CURRENT_PROFILE_TEXT -> {
            ApplicationManager.getApplication().invokeLater {
              val name = Messages.showInputDialog(
                "Enter new profile name:",
                "Save Profile",
                Messages.getQuestionIcon()
              )
              if (!name.isNullOrBlank()) {
                ProfileManager.saveCurrentAsProfile(name)
              }
            }
            return null
          }
        }

        checkForRestart(selectedValue)
        return null
      }
    }
    return JBPopupFactory.getInstance().createListPopup(step)
  }

  fun checkForRestart(selectedValue: String) {
    ApplicationManager.getApplication().invokeLater {
      val changed = ProfileManager.applyProfile(selectedValue)
      if (changed && ProfileManager.isRestartRequired()) {
        val restart = Messages.showYesNoDialog(
          "Plugins were changed for profile '$selectedValue'. Restart IDE to apply changes?",
          "Restart Required",
          "Restart",
          "Later",
          Messages.getQuestionIcon()
        )
        if (restart == Messages.YES) {
          val app = ApplicationManager.getApplication() as ApplicationEx
          app.restart(true)
        }
      } else if (changed) {
        Messages.showInfoMessage("Profile '$selectedValue' applied successfully.", "Profile Applied")
      }
    }
  }

  override fun getSelectedValue(): String {
    val settings = ProfilesSettings.getInstance()
    return "Profile: ${settings.state.activeProfile ?: "Default"}"
  }

  override fun dispose() {
    // Nothing to dispose
  }

  override fun install(statusBar: StatusBar) {
    // Nothing to install
  }

  override fun getIcon(): Icon? = null
}