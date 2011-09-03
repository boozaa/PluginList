package com.daemitus.pluginlist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    private final List<String> hiddenList = new ArrayList<String>();
    private final List<String> fakedList = new ArrayList<String>();
    private final ChatColor faked = ChatColor.YELLOW;
    private final ChatColor hidden = ChatColor.GOLD;
    private final ChatColor enabled = ChatColor.GREEN;
    private final ChatColor disabled = ChatColor.RED;
    private final String user = "pluginlist.user";
    private final String admin = "pluginlist.real";
    private final String version = "pluginlist.version";
//    private final Permission user = Bukkit.getServer().getPluginManager().getPermission("pluginlist.user");
//    private final Permission admin = Bukkit.getServer().getPluginManager().getPermission("pluginlist.real");
//    private final Permission version = Bukkit.getServer().getPluginManager().getPermission("pluginlist.version");

    public void onDisable() {
    }

    public void onEnable() {
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_COMMAND_PREPROCESS, new PlayerListener(this), Priority.Normal, this);
        load();
    }

    private void load() {
        Configuration config = this.getConfiguration();
        config.load();

        Object obj = config.getList("hidden");
        if (obj == null)
            config.setProperty("hidden", new ArrayList<String>());
        else
            hiddenList.addAll((ArrayList<String>) obj);

        obj = config.getProperty("faked");
        if (obj == null)
            config.setProperty("faked", new ArrayList<String>());
        else
            fakedList.addAll((ArrayList<String>) obj);

        config.save();
    }

    private class PlayerListener extends org.bukkit.event.player.PlayerListener {

        private final PluginList plugin;
        private final Comparator<String> comparator = new Comparator<String>() {

            public int compare(String s1, String s2) {
                return s1.substring(2).compareTo(s2.substring(2));
            }
        };

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
                    boolean viewReal = player.hasPermission(admin);
                    List<String> output = new ArrayList<String>();
                    for (Plugin pl : plugin.getServer().getPluginManager().getPlugins()) {
                        String name = pl.getDescription().getName();
                        if (hiddenList.contains(name)) {
                            if (viewReal) {
                                output.add((pl.isEnabled() ? enabled : disabled) + name + " " + hidden + "(H)");
                            }
                        } else {
                            output.add((pl.isEnabled() ? enabled : disabled) + name);
                        }
                    }
                    for (String name : fakedList)
                        output.add((viewReal ? faked : enabled) + name);
                    Collections.sort(output, comparator);

                    for (int i = 0; i < output.size(); i++)
                        message += output.get(i) + ChatColor.WHITE + ", ";

                    player.sendMessage(message.substring(0, message.length() - 2));
                }
            } else if (command.equalsIgnoreCase("ver") || command.equalsIgnoreCase("version")) {
                if (!player.hasPermission(version))
                    event.setCancelled(true);
            }
        }
    }
}
