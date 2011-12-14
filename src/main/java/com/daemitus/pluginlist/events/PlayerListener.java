package com.daemitus.pluginlist.events;

import com.daemitus.pluginlist.Config;
import com.daemitus.pluginlist.Perm;
import com.daemitus.pluginlist.PluginList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class PlayerListener extends org.bukkit.event.player.PlayerListener {

    private final PluginList plugin;
    private static final Pattern plPattern = Pattern.compile("^/(?i)(plugins|pl)$");
    private final Pattern verPattern = Pattern.compile("^/(?i)(version|ver|icanhasbukkit)$");
    private final Comparator<String> comparator = new Comparator<String>() {

        @Override
        public int compare(String str1, String str2) {
            return str1.substring(2).toLowerCase().compareTo(str2.substring(2).toLowerCase());
        }
    };

    public PlayerListener(final PluginList plugin, final PluginManager pm) {
        this.plugin = plugin;
        pm.registerEvent(Type.PLAYER_COMMAND_PREPROCESS, this, Priority.Normal, plugin);
    }

    @Override
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (plPattern.matcher(event.getMessage()).matches()) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            boolean viewReal = hasPermission(player, Perm.admin);

            List<String> output = new ArrayList<String>();

            for (Plugin pl : plugin.getServer().getPluginManager().getPlugins()) {
                String name = pl.getDescription().getName();
                if ((Config.hideEverything || Config.hiddenList.contains(name)) && viewReal) {
                    output.add((pl.isEnabled() ? Config.colorEnabled : Config.colorDisabled) + name + " " + Config.colorHidden + "(H)");
                } else {
                    output.add((pl.isEnabled() ? Config.colorEnabled : Config.colorDisabled) + name);
                }
            }

            ChatColor ce = viewReal ? Config.colorFaked : Config.colorEnabled;
            for (String name : Config.fakedList)
                output.add(ce + name);

            ChatColor cd = viewReal ? Config.colorFaked : Config.colorDisabled;
            for (String name : Config.fakedDisabledList)
                output.add(cd + name);

            Collections.sort(output, comparator);

            StringBuilder sb = new StringBuilder();
            sb.append(Config.colorDefault).append("Plugins: ");
            for (String str : output)
                sb.append(str).append(Config.colorDefault).append(", ");
            sb.delete(sb.length() - 2, sb.length());

            player.sendMessage(sb.toString());

        } else if (verPattern.matcher(event.getMessage()).matches()) {
            Player player = event.getPlayer();
            if (!hasPermission(player, Perm.version))
                event.setCancelled(true);
        }
    }

    private boolean hasPermission(Player player, String permission) {
        return player.isOp() || player.hasPermission(permission);
    }
}