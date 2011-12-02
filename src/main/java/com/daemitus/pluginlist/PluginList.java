package com.daemitus.pluginlist;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.Plugin;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;

public class PluginList extends JavaPlugin {

    public static final Logger logger = Bukkit.getServer().getLogger();
    private final List<String> hiddenList = new ArrayList<String>();
    private final List<String> fakedList = new ArrayList<String>();
    private final List<String> fakedDisabledList = new ArrayList<String>();
    private ChatColor colorFaked = ChatColor.YELLOW;
    private ChatColor colorHidden = ChatColor.GOLD;
    private ChatColor colorEnabled = ChatColor.GREEN;
    private ChatColor colorDisabled = ChatColor.RED;
    private ChatColor colorDefault = ChatColor.WHITE;
    private final String user = "pluginlist.user";
    private final String admin = "pluginlist.real";
    private final String version = "pluginlist.version";

    @Override
    public void onDisable() {
    }

    @Override
    public void onEnable() {
        hiddenList.clear();
        fakedList.clear();
        fakedDisabledList.clear();

        this.getServer().getPluginManager().registerEvent(Type.PLAYER_COMMAND_PREPROCESS, new PlayerListener(this), Priority.Normal, this);
        if (!load()) {
            this.getServer().getPluginManager().disablePlugin(this);
            logger.log(Level.SEVERE, String.format("PluginList %1$s DISABLED", this.getDescription().getVersion()));
        } else {
            logger.log(Level.INFO, String.format("PluginList %1$s Enabled", this.getDescription().getVersion()));
        }
    }

    private boolean load() {
        try {
            File configFile = new File(this.getDataFolder() + "/config.yml");
            if (!configFile.exists())
                if (!getFile("config.yml"))
                    return false;

            YamlConfiguration config = new YamlConfiguration();
            config.load(configFile);

            Object obj = config.getList("hidden");
            if (obj != null)
                hiddenList.addAll((ArrayList<String>) obj);

            obj = config.getList("faked");
            if (obj != null)
                fakedList.addAll((ArrayList<String>) obj);

            obj = config.getList("fakedDisabled");
            if (obj != null)
                fakedDisabledList.addAll((ArrayList<String>) obj);

            try {
                colorFaked = ChatColor.valueOf(config.getString("ColorFaked", colorFaked.name()));
            } catch (Exception ex) {
                PluginList.logger.log(Level.WARNING, "PluginList: ColorFaked has an invalid value");
            }
            try {
                colorHidden = ChatColor.valueOf(config.getString("ColorHidden", colorHidden.name()));
            } catch (Exception ex) {
                PluginList.logger.log(Level.WARNING, "PluginList: ColorHidden has an invalid value");
            }
            try {
                colorEnabled = ChatColor.valueOf(config.getString("ColorEnabled", colorEnabled.name()));
            } catch (Exception ex) {
                PluginList.logger.log(Level.WARNING, "PluginList: ColorEnabled has an invalid value");
            }
            try {
                colorDisabled = ChatColor.valueOf(config.getString("ColorDisabled", colorDisabled.name()));
            } catch (Exception ex) {
                PluginList.logger.log(Level.WARNING, "PluginList: ColorDisabled has an invalid value");
            }
            try {
                colorDefault = ChatColor.valueOf(config.getString("ColorDefault", colorDefault.name()));
            } catch (Exception ex) {
                PluginList.logger.log(Level.WARNING, "PluginList: ColorDefault has an invalid value");
            }
            return true;
        } catch (FileNotFoundException ex) {
            PluginList.logger.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            PluginList.logger.log(Level.SEVERE, null, ex);
        } catch (InvalidConfigurationException ex) {
            PluginList.logger.log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private boolean getFile(String filename) {
        try {
            if (!this.getDataFolder().exists())
                this.getDataFolder().mkdirs();
            File file = new File(this.getDataFolder().getAbsolutePath() + File.separator + filename);
            file.createNewFile();

            InputStream fis = this.getResource("files/" + filename);
            FileOutputStream fos = new FileOutputStream(file);

            try {
                byte[] buf = new byte[1024];
                int i = 0;
                while ((i = fis.read(buf)) != -1) {
                    fos.write(buf, 0, i);
                }
            } catch (Exception e) {
                e.printStackTrace(System.out);
            } finally {
                if (fis != null) {
                    fis.close();
                }
                if (fos != null) {
                    fos.close();
                }
            }
            PluginList.logger.log(Level.INFO, String.format("PluginList: Retrieved file %1$s", filename));
            return true;
        } catch (IOException ex) {
            PluginList.logger.log(Level.SEVERE, String.format("PluginList: Error retrieving %1$s", filename));
            return false;
        }
    }

    private class PlayerListener extends org.bukkit.event.player.PlayerListener {

        private final PluginList plugin;
        private final Comparator<String> comparator = new Comparator<String>() {

            @Override
            public int compare(String s1, String s2) {
                return s1.substring(2).toLowerCase().compareTo(s2.substring(2).toLowerCase());
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
                    String message = colorDefault + "Plugins: ";
                    boolean viewReal = player.hasPermission(admin);
                    List<String> output = new ArrayList<String>();
                    for (Plugin pl : plugin.getServer().getPluginManager().getPlugins()) {
                        String name = pl.getDescription().getName();
                        if (hiddenList.contains(name) || hiddenList.contains("*")) {
                            if (viewReal) {
                                output.add((pl.isEnabled() ? colorEnabled : colorDisabled) + name + " " + colorHidden + "(H)");
                            }
                        } else {
                            output.add((pl.isEnabled() ? colorEnabled : colorDisabled) + name);
                        }
                    }
                    for (String name : fakedList)
                        output.add((viewReal ? colorFaked : colorEnabled) + name);
                    for (String name : fakedDisabledList)
                        output.add((viewReal ? colorFaked : colorDisabled) + name);
                    Collections.sort(output, comparator);

                    for (int i = 0; i < output.size(); i++)
                        message += output.get(i) + colorDefault + ", ";

                    player.sendMessage(message.substring(0, message.length() - 2));
                }
            } else if (command.equalsIgnoreCase("ver") || command.equalsIgnoreCase("version") || command.equalsIgnoreCase("icanhasbukkit")) {
                if (!player.hasPermission(version))
                    event.setCancelled(true);
            }
        }
    }
}
