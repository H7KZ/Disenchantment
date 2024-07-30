package cz.kominekjan.disenchantment.plugins;

import cz.kominekjan.disenchantment.plugins.impl.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisenchantmentPluginManager {
    private static final Map<String, IDisenchantmentPlugin> supportedPlugins = Map.of(
            AdvancedPlugin.name, new AdvancedPlugin(),
            EcoPlugin.name, new EcoPlugin(),
            ExcellentPlugin.name, new ExcellentPlugin(),
            UberPlugin.name, new UberPlugin(),
            SquaredPlugin.name, new SquaredPlugin()
    );
    private static final HashMap<String, IDisenchantmentPlugin> activatedPlugins = new HashMap<>();

    public static Map<String, IDisenchantmentPlugin> getSupportedPlugins() {
        return supportedPlugins;
    }

    public static List<IDisenchantmentPlugin> getPlugins() {
        return new ArrayList<>(supportedPlugins.values());
    }

    public static IDisenchantmentPlugin getPlugin(String pluginName) {
        return supportedPlugins.get(pluginName);
    }

    public static List<String> getPluginNames() {
        return new ArrayList<>(supportedPlugins.keySet());
    }

    public static boolean isSupported(String pluginName) {
        return supportedPlugins.containsKey(pluginName);
    }

    public static HashMap<String, IDisenchantmentPlugin> getActivatedPlugins() {
        return activatedPlugins;
    }

    public static void setActivatedPlugins(List<String> plugins) {
        for (String pluginName : plugins) {
            addActivatedPlugin(pluginName);
        }
    }

    public static void addActivatedPlugin(String pluginName) {
        if (isSupported(pluginName)) activatedPlugins.put(pluginName, supportedPlugins.get(pluginName));
    }
}
