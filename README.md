# IntelliJ Profiles Plugin

The IntelliJ Profiles Plugin allows users to define and switch between custom sets of plugins ("profiles") depending on their current work context (e.g., frontend, backend, database). It provides a seamless way to enable or disable groups of plugins without manually toggling each one, thereby optimizing IDE performance and minimizing clutter.

## Features

- **Profile Management**: Create custom profiles containing a subset of your installed plugins.
- **Global Plugins**: Define plugins that should remain active across *all* profiles (e.g., themes, keymaps, core tools).
- **Project Binding**: Bind a specific profile to a project, so you will be prompted to switch automatically when you open that project.
- **Status Bar Widget**: Quickly switch profiles on the fly via a convenient widget in the IDE status bar.
- **Auto-Switching**: When you open a project with a bound profile that isn't currently active, the plugin will prompt you to switch to it and restart the IDE.

## Setup & Build

This plugin is built using the Gradle IntelliJ Plugin. To build the plugin:

1. Clone the repository.
2. Run `./gradlew buildPlugin` to build the plugin distribution.
3. The built plugin zip file will be located in `build/distributions/`.

To run the plugin in a sandbox IDE for testing:
```bash
./gradlew runIde
```

## How to Use

1. **Open Settings**: Go to `Settings/Preferences -> Tools -> Profiles`.
2. **Create Profiles**: Use the UI to add a new profile. Select which plugins belong to the profile.
3. **Configure Global Plugins**: On the right side, tick the plugins that you consider global. These plugins will always be enabled regardless of which profile is selected.
4. **Bind Profile**: Use the dropdown at the bottom of the Settings page to bind a profile to the current project.
5. **Switch Profiles**: Click on the `Profiles` widget in the status bar at the bottom right of the IDE to quickly switch between profiles. Note that switching profiles requires an IDE restart to take effect.

## Technical Details

- Uses `PersistentStateComponent` for both application-level (`ProfilesSettings`) and project-level (`ProjectProfileSettings`) storage.
- Interacts with `PluginManagerCore` to retrieve installed plugins and manage their enabled/disabled states.
- Implements `StatusBarWidgetFactory` for the quick-switch UI.
- Uses `ProjectActivity` to perform context-aware profile checks on startup.
