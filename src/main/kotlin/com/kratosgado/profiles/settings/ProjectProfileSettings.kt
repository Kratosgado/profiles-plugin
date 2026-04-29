package com.example.profiles.settings

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

@State(
    name = "ProjectProfileSettings",
    storages = [Storage(StoragePathMacros.WORKSPACE_FILE)]
)
@Service(Service.Level.PROJECT)
class ProjectProfileSettings : PersistentStateComponent<ProjectProfileSettings.State> {

    class State {
        // The profile bound to this specific project
        var boundProfile: String? = null
    }

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    companion object {
        fun getInstance(project: Project): ProjectProfileSettings = project.getService(ProjectProfileSettings::class.java)
    }
}
