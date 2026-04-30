package com.kratosgado.profiles.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(
  name = "PluginProfilesSettings",
  storages = [Storage("PluginProfiles.xml")]
)
@Service(Service.Level.APP)
class ProfilesSettings : PersistentStateComponent<ProfilesSettings.State> {

  class State {
    // Map of Profile Name to List of Plugin IDs (as Strings)
    var profiles: MutableMap<String, MutableList<String>> = mutableMapOf()

    // Plugins that are always enabled, regardless of the active profile
    var globalPlugins: MutableList<String> = mutableListOf()

    // The globally active profile (if any)
    var activeProfile: String? = null
  }

  private var myState = State()

  override fun getState(): State = myState

  override fun loadState(state: State) {
    myState = state
  }

  companion object {
    fun getInstance(): ProfilesSettings = ApplicationManager.getApplication().getService(ProfilesSettings::class.java)
  }
}