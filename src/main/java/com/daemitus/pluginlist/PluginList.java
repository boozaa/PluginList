package com.daemitus.pluginlist;

import com.daemitus.pluginlist.events.PlayerListener;
import java.util.logging.Level;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

public class PluginList extends JavaPlugin {

    public static final Logger logger = Bukkit.getServer().getLogger();
    private final Config config = new Config(this);

    @Override
    public void onDisable() {
    }

    @Override
    public void onEnable() {
        PluginManager pm = this.getServer().getPluginManager();
        new PlayerListener(this, pm);
        config.load();

        PluginList.logger.log(Level.INFO, "[PluginList] " + this.getDescription().getVersion() + " Enabled");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (sender.hasPermission(Perm.reload)) {
                reload();
                sender.sendMessage("§c[§bPluginList§c]§f Reloaded");
            }
        } else {
            reload();
            sender.sendMessage("[PluginList] Reloaded");
        }
        return true;
    }

    private void reload() {
        Config.fakedList.clear();
        Config.hiddenList.clear();
        Config.fakedDisabledList.clear();
        config.load();
    }
}
