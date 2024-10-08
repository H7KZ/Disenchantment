package cz.kominekjan.disenchantment.plugins;

import cz.kominekjan.disenchantment.plugins.impl.AdvancedPlugin;
import cz.kominekjan.disenchantment.plugins.impl.EcoPlugin;
import cz.kominekjan.disenchantment.plugins.impl.ExcellentPlugin;
import cz.kominekjan.disenchantment.plugins.impl.UberPlugin;
import cz.kominekjan.disenchantment.plugins.impl.squared.SquaredPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginManager {
    private static final Map<String, IPlugin> supportedPlugins = Map.of(
            AdvancedPlugin.name, new AdvancedPlugin(),
            EcoPlugin.name, new EcoPlugin(),
            ExcellentPlugin.name, new ExcellentPlugin(),
            UberPlugin.name, new UberPlugin(),
            SquaredPlugin.name, new SquaredPlugin()
    );
    private static final HashMap<String, IPlugin> activatedPlugins = new HashMap<>();

    public static Map<String, IPlugin> getSupportedPlugins() {
        return supportedPlugins;
    }

    public static List<IPlugin> getPlugins() {
        return new ArrayList<>(supportedPlugins.values());
    }

    public static IPlugin getPlugin(String pluginName) {
        return supportedPlugins.get(pluginName);
    }

    public static List<String> getPluginNames() {
        return new ArrayList<>(supportedPlugins.keySet());
    }

    public static boolean isSupported(String pluginName) {
        return supportedPlugins.containsKey(pluginName);
    }

    public static HashMap<String, IPlugin> getActivatedPlugins() {
        return activatedPlugins;
    }

    public static void setActivatedPlugins(List<String> plugins) {
        for (String pluginName : plugins) {
            addActivatedPlugin(pluginName);
        }
    }

    public static void addActivatedPlugin(String pluginName) {
        if (isSupported(pluginName)) {
            IPlugin plugin = supportedPlugins.get(pluginName);

            plugin.activate();

            activatedPlugins.put(pluginName, plugin);
        }
    }
}
