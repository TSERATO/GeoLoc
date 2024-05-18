package dev.tserato.geoloc;

import com.google.gson.Gson;
import dev.tserato.geoloc.extentions.GeoLocPAPIExpansion;
import dev.tserato.geoloc.extentions.LogToDiscord;
import dev.tserato.geoloc.extentions.Metrics;
import dev.tserato.geoloc.gui.GeoLocGUI;
import dev.tserato.geoloc.gui.GeoLocGUIListener;
import dev.tserato.geoloc.gui.GeoLocGUIPlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GeoLocSpigot extends JavaPlugin implements Listener {
    private String prefix;
    private final Pattern colorPattern = Pattern.compile("(?i)&([0-9A-FK-OR])");
    private static GeoLocSpigot instance;

    @Override
    public void onEnable() {
        Objects.requireNonNull(getCommand("geoloc")).setExecutor(this);
        Objects.requireNonNull(getCommand("geoloc")).setTabCompleter(this);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new GeoLocGUIListener(), this);
        getServer().getPluginManager().registerEvents(new GeoLocGUIPlayerListener(), this);
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new GeoLocPAPIExpansion().register();
        }
        getLogger().info("GeoLoc has been enabled!");
        saveDefaultConfig();
        loadPrefix();
        checkForUpdates(null);
        instance = this;
        int pluginId = 21836;
        new Metrics(this, pluginId);

        FileConfiguration config = getConfig();
        getServer().getPluginManager().registerEvents(new LogToDiscord(config), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("GeoLoc has been disabled!");
    }

    public static GeoLocSpigot getInstance() {
        return instance;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("geoloc")) {
            List<String> completions = new ArrayList<>();
            if (args.length == 1) {
                if (sender.hasPermission("geoloc.use")) {
                    completions.add("gui");
                    completions.add("reload");
                    completions.add("toggle");
                    completions.add("version");
                    completions.add("v");
                    completions.add("player");
                    completions.add("webhook");
                }
            } else if (args.length == 2 && args[0].equalsIgnoreCase("player") && sender.hasPermission("geoloc.use")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
            return completions.stream()
                    .filter(completion -> completion.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("geoloc")) {
            return false;
        }

        if (args.length == 0) {
            sender.sendMessage(replaceColorCodes(prefix) + "Usage: /geoloc <player|gui|reload|toggle|version|v>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "gui":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(replaceColorCodes(prefix) + "You must be a player to use this command!");
                    return true;
                }
                if (sender.hasPermission("geoloc.gui")) {
                    GeoLocGUI.openGUI((Player) sender, 1);
                }
                break;

            case "reload":
                if (sender.hasPermission("geoloc.reload")) {
                    reloadConfig();
                    loadPrefix();
                    new LogToDiscord(getConfig()); // Reload LogToDiscord settings
                    sender.sendMessage(replaceColorCodes(prefix) + "Config reloaded.");
                } else {
                    sender.sendMessage(replaceColorCodes(prefix) + "No Permission!");
                }
                break;

            case "webhook":
                if (sender.hasPermission("geoloc.use")) {
                    if (args.length == 2) {
                        String webhookUrl = args[1];
                        FileConfiguration config = getConfig();
                        config.set("webhook-url", webhookUrl);
                        saveConfig();
                        sender.sendMessage(replaceColorCodes(prefix) + "Webhook added. Do /geoloc reload to apply changes.");
                    }
                } else {
                    sender.sendMessage(replaceColorCodes(prefix) + "No Permission!");
                }
                break;

            case "toggle":
                if (sender.hasPermission("geoloc.toggle")) {
                    boolean useDiscord = LogToDiscord.isUseDiscord();
                    FileConfiguration config = getConfig();
                    config.set("use-discord", !useDiscord);
                    saveConfig();
                    sender.sendMessage(replaceColorCodes(prefix) + "Discord Integration has been set to " + !useDiscord + ". Do /geoloc reload to apply changes.");
                } else {
                    sender.sendMessage(replaceColorCodes(prefix) + "No Permission!");
                }
                break;

            case "version":
            case "v":
                if (sender.hasPermission("geoloc.use")) {
                    sender.sendMessage(replaceColorCodes(prefix) + "GeoLoc Version: " + getDescription().getVersion());
                } else {
                    sender.sendMessage(replaceColorCodes(prefix) + "No Permission!");
                }
                break;

            case "player":
                if (args.length == 2 && sender.hasPermission("geoloc.use")) {
                    String playerName = args[1];
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            String ip = getIpOfPlayer(playerName);
                            if (ip == null) {
                                sender.sendMessage(replaceColorCodes(prefix) + "Player not found.");
                            } else {
                                String geoLocation = getGeoLocation(ip);
                                sender.sendMessage(replaceColorCodes(prefix) + geoLocation);
                            }
                        }
                    }.runTaskAsynchronously(this);
                } else {
                    sender.sendMessage(replaceColorCodes(prefix) + "No Permission or usage: /geoloc player <name>");
                }
                break;

            default:
                sender.sendMessage(replaceColorCodes(prefix) + "Usage: /geoloc <player|gui|reload|toggle|version|v>");
                break;
        }
        return true;
    }

    private String replaceColorCodes(String message) {
        Matcher matcher = colorPattern.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, "\u00a7" + matcher.group(1));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private void loadPrefix() {
        prefix = getConfig().getString("prefix", "&c&lGeoloc&7&l>> &r ");
    }

    private void checkForUpdates(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    int resourceId = 116496;
                    String updateCheckUrl = "https://api.spigotmc.org/legacy/update.php?resource=" + resourceId;
                    URL url = new URL(updateCheckUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    int responseCode = connection.getResponseCode();
                    if (responseCode == 200) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String version = in.readLine();
                        in.close();
                        String currentVersion = getDescription().getVersion();
                        if (!version.equals(currentVersion)) {
                            String updateMessage = replaceColorCodes(prefix + "§6GeoLoc §ais out of date! §6There is a new version available: §a" + version + "§6!");
                            if (player == null) {
                                getLogger().warning(updateMessage);
                            } else if (player.hasPermission("geoloc.use")) {
                                player.sendMessage(updateMessage);
                            }
                        }
                    }
                    connection.disconnect();
                } catch (IOException e) {
                    getLogger().warning("Failed to check for updates: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(this);
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.isOnline()) {
            checkForUpdates(player);

            if (getConfig().getBoolean("auto-message-on-join", false)) {
                String ip = player.getAddress().getAddress().getHostAddress();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        String geoLocation = getGeoLocation(ip);
                        String message = replaceColorCodes(prefix + player.getName() + " joined from " + geoLocation);
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (p.hasPermission("geoloc.use")) {
                                p.sendMessage(message);
                            }
                        }
                    }
                }.runTaskAsynchronously(this);
            }
        }
    }

    private String getIpOfPlayer(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player != null) {
            return player.getAddress().getAddress().getHostAddress();
        }
        return null;
    }

    private static String getGeoLocation(String ipAddress) {
        try {
            String urlString = "http://ip-api.com/json/" + ipAddress;
            HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() == 200) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    GeoLocation geoLocation = new Gson().fromJson(in.readLine(), GeoLocation.class);
                    return geoLocation.getCountry() + ", " + geoLocation.getRegion() + ", " + geoLocation.getCity();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    private static class GeoLocation {
        private String country;
        private String region;
        private String city;

        public String getCountry() {
            return country;
        }

        public String getRegion() {
            return region;
        }

        public String getCity() {
            return city;
        }
    }
}
