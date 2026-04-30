package com.kratosgado.profiles.settings

import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.setToolTipText
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.text.HtmlChunk
import com.intellij.ui.CollectionListModel
import com.intellij.ui.JBSplitter
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.kratosgado.profiles.manager.ProfileManager
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.GridLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

class ProfilesConfigurable : Configurable {

  private val GLOBAL_PLUGINS_KEY = "<Global Plugins>"

  private var mainPanel: JPanel? = null
  private val profilesListModel = CollectionListModel<String>()
  private val profilesList = JBList(profilesListModel)

  private val pluginsPanel = JPanel(GridLayout(0, 1))

  // In-memory state of edits before "Apply" is clicked
  // profileName -> Set of Plugin IDs
  private var editingProfiles = mutableMapOf<String, MutableSet<String>>()
  private var editingGlobalPlugins = mutableSetOf<String>()

  private var currentlySelectedProfile: String? = null
  private val pluginCheckboxes = mutableMapOf<String, JBCheckBox>()

  private fun getAllManageablePlugins(): List<IdeaPluginDescriptor> {
    return ProfileManager.getInstalledPlugins()
  }

  override fun createComponent(): JComponent? {
    val settings = ProfilesSettings.getInstance().state

    // Initialize in-memory state
    editingProfiles.clear()
    for ((name, plugins) in settings.profiles) {
      editingProfiles[name] = plugins.toMutableSet()
    }
    editingGlobalPlugins = settings.globalPlugins.toMutableSet()

    // Populate left list
    profilesListModel.removeAll()
    profilesListModel.add(GLOBAL_PLUGINS_KEY)
    settings.profiles.keys.forEach { profilesListModel.add(it) }

    val decorator = ToolbarDecorator.createDecorator(profilesList)
      .setAddAction {
        val name = Messages.showInputDialog("Enter new profile name:", "New Profile", Messages.getQuestionIcon())
        if (!name.isNullOrBlank() && name != GLOBAL_PLUGINS_KEY && !profilesListModel.items.contains(name)) {
          profilesListModel.add(name)
          editingProfiles[name] = mutableSetOf()
          profilesList.setSelectedValue(name, true)
        }
      }
      .setRemoveAction {
        val selected = profilesList.selectedValue
        if (selected != null && selected != GLOBAL_PLUGINS_KEY) {
          profilesListModel.remove(selected)
          editingProfiles.remove(selected)
        }
      }
      .disableUpDownActions()

    val leftPanel = decorator.createPanel()

    // Populate right list (checkboxes)
    val manageablePlugins = getAllManageablePlugins().sortedBy { it.name }
    val pluginMap = manageablePlugins.associateBy { it.pluginId.idString }
    pluginCheckboxes.clear()

    fun getDependencies(idStr: String): Set<String> {
      val deps = mutableSetOf<String>()
      val queue = ArrayDeque<String>()
      queue.add(idStr)
      while (queue.isNotEmpty()) {
        val current = queue.removeFirst()
        val p = pluginMap[current] ?: continue
        for (dep in p.dependencies) {
          if (!dep.isOptional) {
            val depId = dep.pluginId.idString
            if (pluginMap.containsKey(depId) && deps.add(depId)) {
              queue.add(depId)
            }
          }
        }
      }
      return deps
    }

    fun getDependents(idStr: String): Set<String> {
      val dependents = mutableSetOf<String>()
      val queue = ArrayDeque<String>()
      queue.add(idStr)
      while (queue.isNotEmpty()) {
        val current = queue.removeFirst()
        for (p in manageablePlugins) {
          val pId = p.pluginId.idString
          if (!dependents.contains(pId)) {
            val hasDep = p.dependencies.any { !it.isOptional && it.pluginId.idString == current }
            if (hasDep) {
              dependents.add(pId)
              queue.add(pId)
            }
          }
        }
      }
      return dependents
    }

    for (plugin in manageablePlugins) {
      val cb = JBCheckBox(plugin.name)
      cb.setToolTipText(HtmlChunk.text(plugin.description ?: plugin.pluginId.idString))
      cb.addActionListener {
        val selectedProfile = currentlySelectedProfile ?: return@addActionListener
        val idStr = plugin.pluginId.idString
        when (cb.isSelected) {
          true -> {
            val idsToAdd = getDependencies(idStr) + idStr
            idsToAdd.stream().forEach {
              when (selectedProfile) {
                GLOBAL_PLUGINS_KEY -> editingGlobalPlugins.add(it)
                else -> editingProfiles[selectedProfile]?.add(it)
              }
              pluginCheckboxes[it]?.isSelected = true
            }
          }

          else -> {
            val idsToRemove = getDependents(idStr) + idStr
            for (id in idsToRemove) {
              if (selectedProfile == GLOBAL_PLUGINS_KEY) {
                editingGlobalPlugins.remove(id)
              } else {
                editingProfiles[selectedProfile]?.remove(id)
              }
              pluginCheckboxes[id]?.isSelected = false
            }
          }
        }
      }
      pluginCheckboxes[plugin.pluginId.idString] = cb
      pluginsPanel.add(cb)
    }

    val rightPanel = JPanel(BorderLayout())

    val buttonsPanel = JPanel(FlowLayout(FlowLayout.LEFT))
    val selectAllButton = JButton("Select All")
    val deselectAllButton = JButton("Deselect All")

    selectAllButton.addActionListener {
      val selectedProfile = currentlySelectedProfile ?: return@addActionListener
      pluginCheckboxes.values.forEach { cb ->
        if (!cb.isSelected) {
          cb.isSelected = true
        }
      }
      val allIds = pluginCheckboxes.keys
      if (selectedProfile == GLOBAL_PLUGINS_KEY) {
        editingGlobalPlugins.addAll(allIds)
      } else {
        editingProfiles[selectedProfile]?.addAll(allIds)
      }
    }

    deselectAllButton.addActionListener {
      val selectedProfile = currentlySelectedProfile ?: return@addActionListener
      pluginCheckboxes.values.forEach { cb ->
        cb.isSelected = false
      }
      if (selectedProfile == GLOBAL_PLUGINS_KEY) {
        editingGlobalPlugins.clear()
      } else {
        editingProfiles[selectedProfile]?.clear()
      }
    }

    buttonsPanel.add(selectAllButton)
    buttonsPanel.add(deselectAllButton)
    rightPanel.add(buttonsPanel, BorderLayout.NORTH)

    val scrollPane = JBScrollPane(pluginsPanel)
    scrollPane.border = JBUI.Borders.empty()
    rightPanel.add(scrollPane, BorderLayout.CENTER)

    val splitter = JBSplitter(false, 0.3f)
    splitter.firstComponent = leftPanel
    splitter.secondComponent = rightPanel

    profilesList.addListSelectionListener {
      val selected = profilesList.selectedValue
      if (selected != currentlySelectedProfile) {
        currentlySelectedProfile = selected
        updateCheckboxes(selected)
      }
    }

    profilesList.selectedIndex = 0

    mainPanel = JPanel(BorderLayout())
    mainPanel?.add(splitter, BorderLayout.CENTER)

    return mainPanel
  }

  private fun updateCheckboxes(selectedProfile: String?) {
    if (selectedProfile == null) {
      pluginCheckboxes.values.forEach { it.isEnabled = false; it.isSelected = false }
      return
    }

    pluginCheckboxes.values.forEach { it.isEnabled = true }

    val activeIds = if (selectedProfile == GLOBAL_PLUGINS_KEY) {
      editingGlobalPlugins
    } else {
      editingProfiles[selectedProfile] ?: emptySet()
    }

    for ((id, cb) in pluginCheckboxes) {
      // If we are looking at a specific profile, we might want to gray out or visually indicate
      // plugins that are ALREADY enabled globally. But for simplicity, let's just show raw state.
      cb.isSelected = activeIds.contains(id)
    }
  }

  override fun isModified(): Boolean {
    val settings = ProfilesSettings.getInstance().state

    if (settings.globalPlugins.toSet() != editingGlobalPlugins) return true
    if (settings.profiles.keys != editingProfiles.keys) return true

    for ((name, plugins) in editingProfiles) {
      val existing = settings.profiles[name]?.toSet() ?: emptySet()
      if (existing != plugins) return true
    }

    return false
  }

  override fun apply() {
    val settings = ProfilesSettings.getInstance().state

    settings.globalPlugins.clear()
    settings.globalPlugins.addAll(editingGlobalPlugins)

    settings.profiles.clear()
    for ((name, plugins) in editingProfiles) {
      settings.profiles[name] = plugins.toMutableList()
    }
  }

  override fun getDisplayName(): String = "IDE Profiles"

  override fun reset() {
    // Reset just recreates the component state by clearing and re-reading
    val selected = profilesList.selectedValue

    val settings = ProfilesSettings.getInstance().state
    editingProfiles.clear()
    for ((name, plugins) in settings.profiles) {
      editingProfiles[name] = plugins.toMutableSet()
    }
    editingGlobalPlugins = settings.globalPlugins.toMutableSet()

    profilesListModel.removeAll()
    profilesListModel.add(GLOBAL_PLUGINS_KEY)
    settings.profiles.keys.forEach { profilesListModel.add(it) }

    if (selected != null && profilesListModel.items.contains(selected)) {
      profilesList.setSelectedValue(selected, true)
    } else {
      profilesList.selectedIndex = 0
    }
  }
}