package com.jankominek.disenchantment.plugins;

import java.util.ArrayList;
import java.util.List;

import static com.jankominek.disenchantment.Disenchantment.nms;

/**
 * Manages the discovery, activation, and deactivation of third-party enchantment plugin adapters.
 *
 * <p>During server startup, this manager cross-references plugins loaded on the server
 * against the set of supported adapters provided by the current {@link com.jankominek.disenchantment.nms.NMS}
 * implementation and activates any that are present.</p>
 */
public class SupportedPluginManager {
    private static final List<ISupportedPlugin> activatedPlugins = new ArrayList<>();

    /**
     * Returns all third-party plugin adapters supported by the current NMS version,
     * regardless of whether they are installed on the server.
     *
     * @return a list of all supported plugin adapters
     */
    public static List<ISupportedPlugin> getAllSupportedPlugins() {
        return nms.getSupportedPlugins();
    }

    /**
     * Looks up a supported plugin adapter by its plugin name.
     *
     * @param pluginName the name of the third-party plugin
     * @return the matching adapter, or {@code null} if no supported adapter has that name
     */
    public static ISupportedPlugin getSupportedPluginByName(String pluginName) {
        for (ISupportedPlugin supportedPlugin : getAllSupportedPlugins()) {
            if (supportedPlugin.getName().equals(pluginName)) return supportedPlugin;
        }

        return null;
    }

    /**
     * Returns the names of all supported third-party plugin adapters.
     *
     * @return a list of plugin names
     */
    public static List<String> getAllSupportedPluginsNames() {
        return getAllSupportedPlugins().stream().map(ISupportedPlugin::getName).toList();
    }

    /**
     * Activates adapters for any supported plugins whose names appear in the given list
     * (typically the list of plugins currently loaded on the server).
     *
     * @param plugins the names of plugins currently loaded on the server
     */
    public static void activatePlugins(List<String> plugins) {
        for (String plugin : plugins) {
            ISupportedPlugin supportedPlugin = getSupportedPluginByName(plugin);

            if (supportedPlugin != null) {
                activatedPlugins.add(supportedPlugin);
                supportedPlugin.activate();
            }
        }
    }

    /**
     * Returns all plugin adapters that have been activated (i.e. their corresponding
     * third-party plugin is installed on the server).
     *
     * @return a list of activated plugin adapters
     */
    public static List<ISupportedPlugin> getAllActivatedPlugins() {
        return activatedPlugins;
    }

    /**
     * Returns the names of all currently activated plugin adapters.
     *
     * @return a list of activated plugin names
     */
    public static List<String> getAllActivatedPluginsNames() {
        return activatedPlugins.stream().map(ISupportedPlugin::getName).toList();
    }

    /**
     * Deactivates all plugin adapters, clearing the activated list.
     * Called during plugin shutdown.
     */
    public static void deactivateAllPlugins() {
        activatedPlugins.clear();
    }
}
