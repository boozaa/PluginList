package com.daemitus.pluginlist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.util.config.Configuration;

public class PluginList extends JavaPlugin {

    public static final Logger logger = Logger.getLogger("Minecraft");
    private final List<String> hidden = new ArrayList<String>();
    private final List<String> faked = new ArrayList<String>();

    public void onDisable() {
    }

    public void onEnable() {
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_COMMAND_PREPROCESS, new PlayerListener(this), Priority.Normal, this);
        load();
    }

    private void load() {
        Configuration config = this.getConfiguration();
        config.load();

        Object obj = config.getProperty("hidden");
        if (obj == null)
            config.setProperty("hidden", new ArrayList<String>());
        else
            hidden.addAll((ArrayList<String>) obj);

        obj = config.getProperty("faked");
        if (obj == null)
            config.setProperty("faked", new ArrayList<String>());
        else
            faked.addAll((ArrayList<String>) obj);

        config.save();

    }

    private class PlayerListener extends org.bukkit.event.player.PlayerListener {

        private final PluginList plugin;
        private final String user = "pluginlist.user";
        private final String admin = "pluginlist.hidden";
        private final String version = "pluginlist.version";
        //Why dont you work :\
        //private final Permission user = Bukkit.getServer().getPluginManager().getPermission("pluginlist.user");
        //private final Permission admin = Bukkit.getServer().getPluginManager().getPermission("pluginlist.hidden");
        //private final Permission version = Bukkit.getServer().getPluginManager().getPermission("pluginlist.version");

        private PlayerListener(final PluginList plugin) {
            this.plugin = plugin;
        }

        @Override
        public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {

            Player player = event.getPlayer();
            String[] split = event.getMessage().split("\\s+");
            String command = split[0].substring(1);
            if (command.equalsIgnoreCase("plugins") || command.equalsIgnoreCase("pl")) {
                event.setCancelled(true);
                if (player.hasPermission(user)) {
                    String message = ChatColor.WHITE + "Plugins: ";
                    boolean viewHidden = player.hasPermission(admin);
                    List<String> list = new ArrayList<String>();
                    for (Plugin pl : plugin.getServer().getPluginManager().getPlugins())
                        list.add(pl.getDescription().getName());
                    for (String name : faked)
                        list.add(name);
                    if (!viewHidden)
                        for (String name : hidden)
                            list.remove(name);
                    Collections.sort(list);

                    for (String name : list) {
                        Plugin pl = plugin.getServer().getPluginManager().getPlugin(name);
                        if (pl == null) {
                            message += ChatColor.GREEN + name;
                        } else {
                            message += (pl.isEnabled() ? ChatColor.GREEN : ChatColor.RED) + name;
                        }
                        message += ChatColor.WHITE + ", ";
                    }
                    player.sendMessage(message.substring(0, message.length() - 2));
                }
            } else if (command.equalsIgnoreCase("ver") || command.equalsIgnoreCase("version")) {
                if (!player.hasPermission(version))
                    event.setCancelled(true);
            }
        }
    }
}
