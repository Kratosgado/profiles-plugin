package com.kratosgado.profiles.manager

import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import com.kratosgado.profiles.settings.ProfilesSettings
import kotlinx.collections.immutable.toImmutableList

object ProfileManager {

  fun getInstalledPlugins(): List<IdeaPluginDescriptor> {
    return PluginManagerCore.plugins.toList()
  }

  fun applyProfile(profileName: String): Boolean {
    val settings = ProfilesSettings.getInstance()
    val profilePlugins = settings.state.profiles[profileName]?.toImmutableList() ?: return false
    val globalPlugins = settings.state.globalPlugins.map { PluginId.getId(it) }.toSet()

    val profilePluginIds = profilePlugins.map { PluginId.getId(it) }.toSet()
    val allPlugins = getInstalledPlugins()

    var changed = false

    for (plugin in allPlugins) {
      val pluginId = plugin.pluginId
      val isEnabled = PluginManagerCore.isDisabled(pluginId).not()
      val shouldBeEnabled = profilePluginIds.contains(pluginId) || globalPlugins.contains(pluginId)

      if (isEnabled && !shouldBeEnabled) {
        PluginManagerCore.disablePlugin(pluginId)
        changed = true
      } else if (!isEnabled && shouldBeEnabled) {
        PluginManagerCore.enablePlugin(pluginId)
        changed = true
      }
    }

    if (changed) {
      settings.state.activeProfile = profileName
    }

    return changed
  }

  fun isRestartRequired(): Boolean {
    // As per requirements: "using PluginManagerConfigurable.isRestartRequired()"
    // Since it may not be directly accessible in this API version, we use reflection
    // to safely invoke PluginManagerConfigurable.isRestartRequired() if it exists.
    return try {
      val clazz = Class.forName("com.intellij.ide.plugins.PluginManagerConfigurable")
      val method = clazz.getMethod("isRestartRequired")
      method.invoke(null) as Boolean
    } catch (e: Exception) {
      // Fallback or use an alternative if missing in 2026.1
      true
    }
  }

  fun saveCurrentAsProfile(profileName: String) {
    val activePluginIds = getInstalledPlugins()
      .filter { !PluginManagerCore.isDisabled(it.pluginId) }
      .map { it.pluginId.idString }

    val settings = ProfilesSettings.getInstance()
    settings.state.profiles[profileName] = activePluginIds.toMutableList()
    settings.state.activeProfile = profileName
  }
}