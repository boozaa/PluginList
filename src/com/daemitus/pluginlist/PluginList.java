package com.daemitus.pluginlist;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
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
    private ChatColor colorFaked = ChatColor.YELLOW;
    private ChatColor colorHidden = ChatColor.GOLD;
    private ChatColor colorEnabled = ChatColor.GREEN;
    private ChatColor colorDisabled = ChatColor.RED;
    private ChatColor colorDefault = ChatColor.WHITE;
    private final String user = "pluginlist.user";
    private final String admin = "pluginlist.real";
    private final String version = "pluginlist.version";
//    private final Permission user = Bukkit.getServer().getPluginManager().getPermission("pluginlist.user");
//    private final Permission admin = Bukkit.getServer().getPluginManager().getPermission("pluginlist.real");
//    private final Permission version = Bukkit.getServer().getPluginManager().getPermission("pluginlist.version");
    private final String repo = "https://raw.github.com/daemitus/PluginList/master/src/files/";

    public void onDisable() {
    }

    public void onEnable() {
        this.getServer().getPluginManager().registerEvent(Type.PLAYER_COMMAND_PREPROCESS, new PlayerListener(this), Priority.Normal, this);
        load();
    }

    private void load() {
        File configFile = new File(this.getDataFolder() + File.separator + "config.yml");
        if (!configFile.exists())
            downloadFile("config.yml");
        Configuration config = new Configuration(configFile);
        config.load();

        Object obj = config.getList("hidden");
        if (obj != null)
            hiddenList.addAll((ArrayList<String>) obj);

        obj = config.getProperty("faked");
        if (obj != null)
            fakedList.addAll((ArrayList<String>) obj);

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

    }

    private void downloadFile(String filename) {
        //Southpaw018 - Cenotaph
        if (!this.getDataFolder().exists())
            this.getDataFolder().mkdirs();
        String datafile = this.getDataFolder().getPath() + File.separator + filename;
        String repofile = repo + filename;
        try {
            File download = new File(datafile);
            download.createNewFile();
            URL link = new URL(repofile);
            ReadableByteChannel rbc = Channels.newChannel(link.openStream());
            FileOutputStream fos = new FileOutputStream(download);
            fos.getChannel().transferFrom(rbc, 0, 1 << 24);
            PluginList.logger.log(Level.INFO, "PluginList: Downloaded file ".concat(datafile));
        } catch (MalformedURLException ex) {
            PluginList.logger.log(Level.WARNING, "PluginList: Malformed URL ".concat(repofile));
        } catch (FileNotFoundException ex) {
            PluginList.logger.log(Level.WARNING, "PluginList: File not found ".concat(datafile));
        } catch (IOException ex) {
            PluginList.logger.log(Level.WARNING, "PluginList: IOError downloading ".concat(repofile));
        }
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
                    String message = colorDefault + "Plugins: ";
                    boolean viewReal = player.hasPermission(admin);
                    List<String> output = new ArrayList<String>();
                    for (Plugin pl : plugin.getServer().getPluginManager().getPlugins()) {
                        String name = pl.getDescription().getName();
                        if (hiddenList.contains(name)) {
                            if (viewReal) {
                                output.add((pl.isEnabled() ? colorEnabled : colorDisabled) + name + " " + colorHidden + "(H)");
                            }
                        } else {
                            output.add((pl.isEnabled() ? colorEnabled : colorDisabled) + name);
                        }
                    }
                    for (String name : fakedList)
                        output.add((viewReal ? colorFaked : colorEnabled) + name);
                    Collections.sort(output, comparator);

                    for (int i = 0; i < output.size(); i++)
                        message += output.get(i) + colorDefault + ", ";

                    player.sendMessage(message.substring(0, message.length() - 2));
                }
            } else if (command.equalsIgnoreCase("ver") || command.equalsIgnoreCase("version")) {
                if (!player.hasPermission(version))
                    event.setCancelled(true);
            }
        }
    }
}
