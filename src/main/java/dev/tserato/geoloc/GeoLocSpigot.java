package dev.tserato.geoloc;

import com.google.gson.Gson;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GeoLocSpigot extends JavaPlugin implements Listener {
    private String prefix;
    private final Pattern colorPattern = Pattern.compile("(?i)&([0-9A-FK-OR])");

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new GeoLocGUIListener(), this);
        getServer().getPluginManager().registerEvents(new GeoLocGUIPlayerListener(), this);
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new GeoLocPAPIExpansion().register();
        }
        getLogger().info("GeoLoc has been enabled!");
        saveDefaultConfig();
        loadPrefix();
        checkForUpdates();
        int pluginId = 21836;
        Metrics metrics = new Metrics(this, pluginId);
    }

    @Override
    public void onDisable() {
        getLogger().info("GeoLoc has been disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("geoloc")) {
            if (args.length == 0) {
                sender.sendMessage(replaceColorCodes(prefix) + "Usage: /geoloc <player>");
                return true;
            }else if (args.length == 1 && args[0].equalsIgnoreCase("gui")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(replaceColorCodes(prefix) + "You must be a player to use this command!");
                    return true;
                }
                if (sender.hasPermission("geoloc.gui")) {
                    GeoLocGUI.openGUI((Player) sender, 1);
                    return true;
                }
            } else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("geoloc.reload")) {
                    reloadConfig();
                    loadPrefix();
                    sender.sendMessage(replaceColorCodes(prefix) + "Config reloaded.");
                    return true;
                } else {
                    sender.sendMessage(replaceColorCodes(prefix) + "No Permission!");
                    return true;
                }
            } else if (args.length == 1 && args[0].equalsIgnoreCase("toggle")) {
                if (sender.hasPermission("geoloc.toggle")) {
                    FileConfiguration config = getConfig();
                    boolean autoRunOnJoin = config.getBoolean("auto-run-on-join");
                    config.set("auto-run-on-join", !autoRunOnJoin);
                    saveConfig();
                    sender.sendMessage(replaceColorCodes(prefix) + "Auto-Message " + (autoRunOnJoin ? "off." : "on."));
                    return true;
                } else {
                    sender.sendMessage(replaceColorCodes(prefix) + "No Permission!");
                    return true;
                }
            }else if (sender.hasPermission("geoloc.use") && args.length == 1) {
                if (sender instanceof Player) {

                    Player target = getServer().getPlayer(args[0]);
                    Player player = (Player) sender;
                    if (target != null) {
                        String ipAddress = target.getAddress().getAddress().getHostAddress();
                        sender.sendMessage(replaceColorCodes(prefix) + "Geolocation for " + target.getName() + ":");
                        sender.sendMessage(replaceColorCodes(prefix) + target.getName() + "'s IP address: " + ipAddress);
                        sendGeoLocationMessage(player, target);
                        return true;
                    } else {
                        sender.sendMessage(replaceColorCodes(prefix) + "Player not found or not online.");
                        return true;
                    }
                } else {
                    sender.sendMessage(replaceColorCodes(prefix) + "You must be a player to use this command!");
                }
            } else {
                sender.sendMessage(replaceColorCodes(prefix) + "No Permission!");
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (cmd.getName().equalsIgnoreCase("geoloc") && args.length == 1) {
            completions.add("reload");
            completions.add("toggle");
            completions.add("gui");
        }
        return completions;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joinedPlayer = event.getPlayer();
        Iterator<Player> iterator = (Iterator<Player>) getServer().getOnlinePlayers().iterator();
        while (iterator.hasNext()) {
            Player player = iterator.next();
            if (player.hasPermission("geoloc.use")) {
                FileConfiguration config = getConfig();
                if (config.getBoolean("auto-run-on-join")) {
                    String ipAddress = joinedPlayer.getAddress().getAddress().getHostAddress();
                    player.sendMessage(replaceColorCodes(prefix) + "Geolocation for " + joinedPlayer.getName() + ":");
                    player.sendMessage(replaceColorCodes(prefix) + joinedPlayer.getName() + "'s IP address: " + ipAddress);
                    sendGeoLocationMessage(player, joinedPlayer);
                }
            }
        }
        if (joinedPlayer.hasPermission("geoloc.use")) {
            checkForUpdates(joinedPlayer);
        }
    }

    private void loadPrefix() {
        FileConfiguration config = getConfig();
        prefix = config.getString("prefix", "");
    }

    private String replaceColorCodes(String message) {
        Matcher matcher = colorPattern.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, "§$1");
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private void sendGeoLocationMessage(Player sender, Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            if (player != null) {
                try {
                    String ipAddress = player.getAddress().getAddress().getHostAddress();
                    String urlString = "http://ip-api.com/json/" + ipAddress;
                    URL url = new URL(urlString);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    int responseCode = connection.getResponseCode();
                    if (responseCode == 200) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();
                        GeoLocation geoLocation = new Gson().fromJson(response.toString(), GeoLocation.class);
                        if (sender.hasPermission("geoloc.use")) {
                            sender.sendMessage(replaceColorCodes(prefix) + "Geolocation for " + player.getName() + ": " + geoLocation.getCountry() + ", " + geoLocation.getRegion() + ", " + geoLocation.getCity());
                        }
                        getLogger().info(replaceColorCodes(prefix) + "Geolocation for " + player.getName() + ": " + geoLocation.getCountry() + ", " + geoLocation.getRegion() + ", " + geoLocation.getCity());
                    }
                    connection.disconnect();
                } catch (IOException e) {
                    getLogger().warning("Failed to retrieve geolocation data: " + e.getMessage());
                }
            } else {
                getLogger().warning("Player is null!");
            }
        });
    }



    private void checkForUpdates() {
        try {
            int resourceId = 116496; // Replace with your actual resource ID
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
                if (compareVersions(currentVersion, version) < 0) {
                    getLogger().warning("A new version of GeoLoc (v" + version + ") is available! You are currently running v" + currentVersion + ". Update at: https://www.spigotmc.org/resources/" + resourceId);
                } else {
                    getLogger().info("You are running the latest version of GeoLoc.");
                }
            }
            connection.disconnect();
        } catch (IOException e) {
            getLogger().warning("Failed to check for updates: " + e.getMessage());
        }
    }

    private void checkForUpdates(Player player) {
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
                if (compareVersions(currentVersion, version) < 0) {
                    player.sendMessage("§6A new version of GeoLoc (v" + version + ") is available! You are currently running v" + currentVersion + ". Update at: §9https://www.spigotmc.org/resources/" + resourceId);
                }
            }
            connection.disconnect();
        } catch (IOException e) {
            getLogger().warning("Failed to check for updates: " + e.getMessage());
        }
    }

    private int compareVersions(String version1, String version2) {
        String[] parts1 = version1.split("\\.");
        String[] parts2 = version2.split("\\.");

        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            int part1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int part2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;
            if (part1 < part2) return -1;
            if (part1 > part2) return 1;
        }
        return 0;
    }

    static class GeoLocation {
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
