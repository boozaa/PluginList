package com.daemitus.pluginlist;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {

    private final PluginList plugin;
    public static boolean hideEverything = false;
    public static final List<String> hiddenList = new ArrayList<String>();
    public static final List<String> fakedList = new ArrayList<String>();
    public static final List<String> fakedDisabledList = new ArrayList<String>();
    public static ChatColor colorFaked = ChatColor.YELLOW;
    public static ChatColor colorHidden = ChatColor.GOLD;
    public static ChatColor colorEnabled = ChatColor.GREEN;
    public static ChatColor colorDisabled = ChatColor.RED;
    public static ChatColor colorDefault = ChatColor.WHITE;

    public Config(final PluginList plugin) {
        this.plugin = plugin;
    }

    public void load() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        checkFile(configFile);

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        hideEverything = config.getBoolean("hide_everything", hideEverything);
        hiddenList.addAll(config.getStringList("hidden"));
        fakedList.addAll(config.getStringList("faked"));
        fakedDisabledList.addAll(config.getStringList("fakedDisabled"));

        try {
            colorFaked = ChatColor.valueOf(config.getString("ColorFaked", colorFaked.name()));
        } catch (Exception ex) {
            PluginList.logger.log(Level.WARNING, "[PluginList] ColorFaked has an invalid value");
        }
        try {
            colorHidden = ChatColor.valueOf(config.getString("ColorHidden", colorHidden.name()));
        } catch (Exception ex) {
            PluginList.logger.log(Level.WARNING, "[PluginList] ColorHidden has an invalid value");
        }
        try {
            colorEnabled = ChatColor.valueOf(config.getString("ColorEnabled", colorEnabled.name()));
        } catch (Exception ex) {
            PluginList.logger.log(Level.WARNING, "[PluginList] ColorEnabled has an invalid value");
        }
        try {
            colorDisabled = ChatColor.valueOf(config.getString("ColorDisabled", colorDisabled.name()));
        } catch (Exception ex) {
            PluginList.logger.log(Level.WARNING, "[PluginList] ColorDisabled has an invalid value");
        }
        try {
            colorDefault = ChatColor.valueOf(config.getString("ColorDefault", colorDefault.name()));
        } catch (Exception ex) {
            PluginList.logger.log(Level.WARNING, "[PluginList] ColorDefault has an invalid value");
        }
        return;
    }

    private boolean checkFile(File file) {
        try {
            if (file.exists())
                return true;

            File dir = file.getParentFile();
            if (!dir.exists())
                dir.mkdir();

            file.createNewFile();

            InputStream in = null;
            OutputStream out = null;

            try {
                in = plugin.getResource("files/" + file.getName());
                out = new FileOutputStream(file);

                int len;
                byte[] buf = new byte[1024];
                while ((len = in.read(buf)) > 0)
                    out.write(buf, 0, len);

            } finally {
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
            }

            PluginList.logger.log(Level.INFO, "[PluginList] Retrieved file " + file.getName());
            return true;
        } catch (IOException ex) {
            PluginList.logger.log(Level.SEVERE, null, ex);
            return false;
        }
    }
}
