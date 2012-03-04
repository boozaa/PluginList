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
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginList extends JavaPlugin {

    public static final Logger logger = Bukkit.getServer().getLogger();
    private final List<String> hiddenList;
    private final List<String> fakedList;
    private final List<String> fakedDisabledList;
    private ChatColor colorFaked;
    private ChatColor colorHidden;
    private ChatColor colorEnabled;
    private ChatColor colorDisabled;
    private ChatColor colorDefault;
    private final String user = "pluginlist.user";
    private final String admin = "pluginlist.real";
    private final String version = "pluginlist.version";

    public PluginList() {
        this.hiddenList = new ArrayList();
        this.fakedList = new ArrayList();
        this.fakedDisabledList = new ArrayList();
        this.colorFaked = ChatColor.YELLOW;
        this.colorHidden = ChatColor.GOLD;
        this.colorEnabled = ChatColor.GREEN;
        this.colorDisabled = ChatColor.RED;
        this.colorDefault = ChatColor.WHITE;
    }

    @Override
    public void onDisable() {
        logger.log(Level.INFO, String.format("PluginList %1$s DISABLED", new Object[]{getDescription().getVersion()}));
    }

    @Override
    public void onEnable() {
        this.hiddenList.clear();
        this.fakedList.clear();
        this.fakedDisabledList.clear();


        getServer().getPluginManager().registerEvents(new PlayerCommandListener(), this);
        if (!load()) {
            getServer().getPluginManager().disablePlugin(this);
        } else {
            logger.log(Level.INFO, String.format("PluginList %1$s Enabled", new Object[]{getDescription().getVersion()}));
        }
    }

    private boolean load() {
        try {
            File configFile = new File(getDataFolder() + "/config.yml");
            if ((!configFile.exists())
                    && (!getFile("config.yml"))) {
                return false;
            }
            YamlConfiguration config = new YamlConfiguration();
            config.load(configFile);

            Object obj = config.getList("hidden");
            if (obj != null) {
                this.hiddenList.addAll((ArrayList) obj);
            }
            obj = config.getList("faked");
            if (obj != null) {
                this.fakedList.addAll((ArrayList) obj);
            }
            obj = config.getList("fakedDisabled");
            if (obj != null)
                this.fakedDisabledList.addAll((ArrayList) obj);
            try {
                this.colorFaked = ChatColor.valueOf(config.getString("ColorFaked", this.colorFaked.name()));
            } catch (Exception ex) {
                logger.log(Level.WARNING, "PluginList: ColorFaked has an invalid value");
            }
            try {
                this.colorHidden = ChatColor.valueOf(config.getString("ColorHidden", this.colorHidden.name()));
            } catch (Exception ex) {
                logger.log(Level.WARNING, "PluginList: ColorHidden has an invalid value");
            }
            try {
                this.colorEnabled = ChatColor.valueOf(config.getString("ColorEnabled", this.colorEnabled.name()));
            } catch (Exception ex) {
                logger.log(Level.WARNING, "PluginList: ColorEnabled has an invalid value");
            }
            try {
                this.colorDisabled = ChatColor.valueOf(config.getString("ColorDisabled", this.colorDisabled.name()));
            } catch (Exception ex) {
                logger.log(Level.WARNING, "PluginList: ColorDisabled has an invalid value");
            }
            try {
                this.colorDefault = ChatColor.valueOf(config.getString("ColorDefault", this.colorDefault.name()));
            } catch (Exception ex) {
                logger.log(Level.WARNING, "PluginList: ColorDefault has an invalid value");
            }
            return true;
        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (InvalidConfigurationException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private boolean getFile(String filename) {
        try {
            if (!getDataFolder().exists())
                getDataFolder().mkdirs();
            File file = new File(getDataFolder().getAbsolutePath() + File.separator + filename);
            file.createNewFile();

            InputStream fis = getResource("files/" + filename);
            FileOutputStream fos = new FileOutputStream(file);
            try {
                byte[] buf = new byte[1024];
                int i = 0;
                while ((i = fis.read(buf)) != -1)
                    fos.write(buf, 0, i);
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
            logger.log(Level.INFO, String.format("PluginList: Retrieved file %1$s", new Object[]{filename}));
            return true;
        } catch (IOException ex) {
            logger.log(Level.SEVERE, String.format("PluginList: Error retrieving %1$s", new Object[]{filename}));
        }
        return false;
    }

    private class PlayerCommandListener implements Listener {

        private final Comparator<String> comparator = new Comparator() {

            @Override
            public int compare(Object s1, Object s2) {
                return s1.toString().substring(2).toLowerCase().compareTo(s2.toString().substring(2).toLowerCase());
            }
        };

        @EventHandler(priority = EventPriority.NORMAL)
        public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
            Player player = event.getPlayer();
            String[] split = event.getMessage().split("\\s+");
            String command = split[0].substring(1);
            if ((command.equalsIgnoreCase("plugins")) || (command.equalsIgnoreCase("pl"))) {
                event.setCancelled(true);
                if (player.hasPermission("pluginlist.user")) {
                    String message = new StringBuilder().append(PluginList.this.colorDefault).append("Plugins: ").toString();
                    boolean viewReal = player.hasPermission("pluginlist.real");
                    List output = new ArrayList();
                    for (Plugin pl : Bukkit.getServer().getPluginManager().getPlugins()) {
                        String name = pl.getDescription().getName();
                        if ((PluginList.this.hiddenList.contains(name)) || (PluginList.this.hiddenList.contains("*"))) {
                            if (viewReal)
                                output.add(new StringBuilder().append(pl.isEnabled() ? PluginList.this.colorEnabled : PluginList.this.colorDisabled).append(name).append(" ").append(PluginList.this.colorHidden).append("(H)").toString());
                        } else {
                            output.add(new StringBuilder().append(pl.isEnabled() ? PluginList.this.colorEnabled : PluginList.this.colorDisabled).append(name).toString());
                        }
                    }
                    for (String name : PluginList.this.fakedList)
                        output.add(new StringBuilder().append(viewReal ? PluginList.this.colorFaked : PluginList.this.colorEnabled).append(name).toString());
                    for (String name : PluginList.this.fakedDisabledList)
                        output.add(new StringBuilder().append(viewReal ? PluginList.this.colorFaked : PluginList.this.colorDisabled).append(name).toString());
                    Collections.sort(output, this.comparator);

                    for (int i = 0; i < output.size(); i++) {
                        message = new StringBuilder().append(message).append((String) output.get(i)).append(PluginList.this.colorDefault).append(", ").toString();
                    }
                    player.sendMessage(message.substring(0, message.length() - 2));
                }
            } else if (((command.equalsIgnoreCase("ver")) || (command.equalsIgnoreCase("version")) || (command.equalsIgnoreCase("icanhasbukkit")))
                    && (!player.hasPermission("pluginlist.version"))) {
                event.setCancelled(true);
            }
        }
    }
}