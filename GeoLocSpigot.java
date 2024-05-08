package dev.tserato.geoloc;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class GeoLocSpigot extends JavaPlugin implements Listener {
    private static String ipAddress;
    private String prefix;
    private final Pattern colorPattern = Pattern.compile("(?i)&([0-9A-FK-OR])");
    private final String UPDATE_CHECK_URL = "https://api.spigotmc.org/legacy/update.php?resource=RESOURCE_ID";

    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getLogger().info("GeoLoc has been enabled!");
        this.saveDefaultConfig();
        this.loadPrefix();
        this.checkForUpdates();
        int pluginId = 21836;
        Metrics metrics = new Metrics(this, pluginId);
    }

    public void onDisable() {
        this.getLogger().info("GeoLoc has been disabled!");
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("geoloc")) {
            if (args.length == 0) {
                sender.sendMessage(this.replaceColorCodes(this.prefix) + "Usage: /geoloc <player>");
                return true;
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("geoloc.reload")) {
                    this.reloadConfig();
                    this.loadPrefix();
                    sender.sendMessage(this.replaceColorCodes(this.prefix) + "Config reloaded.");
                    return true;
                }

                sender.sendMessage(this.replaceColorCodes(this.prefix) + "No Permission!");
                return true;
            }

            String var10001;
            if (args.length == 1 && args[0].equalsIgnoreCase("toggle")) {
                if (sender.hasPermission("geoloc.toggle")) {
                    FileConfiguration config = this.getConfig();
                    boolean autoRunOnJoin = config.getBoolean("auto-run-on-join");
                    config.set("auto-run-on-join", !autoRunOnJoin);
                    this.saveConfig();
                    var10001 = this.replaceColorCodes(this.prefix);
                    sender.sendMessage(var10001 + "Auto-Message " + (autoRunOnJoin ? "off" + "." : "on" + "."));
                    return true;
                }

                sender.sendMessage(this.replaceColorCodes(this.prefix) + "No Permission!");
                return true;
            }

            if (args.length == 1 && !args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("geoloc.use")) {
                    Player target = this.getServer().getPlayer(args[0]);
                    if (target != null) {
                        ipAddress = target.getAddress().getAddress().getHostAddress();
                        var10001 = this.replaceColorCodes(this.prefix);
                        sender.sendMessage(var10001 + "Geolocation for " + target.getName() + ":");
                        var10001 = this.replaceColorCodes(this.prefix);
                        sender.sendMessage(var10001 + target.getName() + "'s IP address: " + ipAddress);
                        this.sendGeoLocationMessage(target);
                        return true;
                    }

                    sender.sendMessage(this.replaceColorCodes(this.prefix) + "Player not found or not online.");
                    return true;
                }

                sender.sendMessage(this.replaceColorCodes(this.prefix) + "No Permission!");
                return true;
            }
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (cmd.getName().equalsIgnoreCase("geoloc")) {
            if (args.length == 1) {
                completions.add("reload");
                completions.add("toggle");
            }
        }

        return completions;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joinedPlayer = event.getPlayer();
        Iterator var3 = this.getServer().getOnlinePlayers().iterator();

        while(var3.hasNext()) {
            Player player = (Player)var3.next();
            if (player.hasPermission("geoloc.use")) {
                FileConfiguration config = this.getConfig();
                if (config.getBoolean("auto-run-on-join")) {
                    ipAddress = joinedPlayer.getAddress().getAddress().getHostAddress();
                    String var10001 = this.replaceColorCodes(this.prefix);
                    player.sendMessage(var10001 + "Geolocation for " + joinedPlayer.getName() + ":");
                    var10001 = this.replaceColorCodes(this.prefix);
                    player.sendMessage(var10001 + joinedPlayer.getName() + "'s IP address: " + ipAddress);
                    this.sendGeoLocationMessage(player);
                }
            }
        }

        if (joinedPlayer.hasPermission("geoloc.use")) {
            this.checkForUpdates(joinedPlayer);
        }

    }

    private void loadPrefix() {
        FileConfiguration config = this.getConfig();
        this.prefix = config.getString("prefix", "");
    }

    private String replaceColorCodes(String message) {
        Matcher matcher = this.colorPattern.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while(matcher.find()) {
            matcher.appendReplacement(buffer, "§$1");
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private void sendGeoLocationMessage(Player player) {
        if (player != null) {
            try {
                ipAddress = player.getAddress().getAddress().getHostAddress();
                String urlString = "http://ip-api.com/json/" + ipAddress;
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("GET");
                int responseCode = connection.getResponseCode();
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();

                String inputLine;
                while((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                in.close();
                GeoLocation geoLocation = (GeoLocation)(new Gson()).fromJson(response.toString(), GeoLocation.class);
                String var10001 = this.replaceColorCodes(this.prefix);
                player.sendMessage(var10001 + "Geolocation: " + geoLocation.getCountry() + ", " + geoLocation.getRegion() + ", " + geoLocation.getCity());
                getLogger().info(var10001 + "Geolocation: " + geoLocation.getCountry() + ", " + geoLocation.getRegion() + ", " + geoLocation.getCity());
                connection.disconnect();
            } catch (Exception var10) {
                Exception e = var10;
                e.printStackTrace();
            }
        } else {
            this.getLogger().warning("Player is null!");
        }

    }

    private void checkForUpdates() {
        try {
            int resourceId = 116496;
            String updateCheckUrl = "https://api.spigotmc.org/legacy/update.php?resource=RESOURCE_ID".replace("RESOURCE_ID", String.valueOf(resourceId));
            URL url = new URL(updateCheckUrl);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String version = in.readLine();
                in.close();
                String currentVersion = this.getDescription().getVersion();
                if (!currentVersion.equalsIgnoreCase(version)) {
                    this.getLogger().warning("A new version of GeoLoc (v" + version + ") is available! You are currently running v" + currentVersion + ". Update at: https://www.spigotmc.org/resources/" + resourceId);
                } else {
                    this.getLogger().info("You are running the latest version of GeoLoc.");
                }
            }

            connection.disconnect();
        } catch (Exception var9) {
            Exception e = var9;
            this.getLogger().warning("Failed to check for updates: " + e.getMessage());
        }

    }

    private void checkForUpdates(Player player) {
        try {
            int resourceId = 116496;
            String updateCheckUrl = "https://api.spigotmc.org/legacy/update.php?resource=RESOURCE_ID".replace("RESOURCE_ID", String.valueOf(resourceId));
            URL url = new URL(updateCheckUrl);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String version = in.readLine();
                in.close();
                String currentVersion = this.getDescription().getVersion();
                if (!currentVersion.equalsIgnoreCase(version)) {
                    player.sendMessage("§6A new version of GeoLoc (v" + version + ") is available! You are currently running v" + currentVersion + ". Update at: §9https://www.spigotmc.org/resources/" + resourceId);
                }
            }

            connection.disconnect();
        } catch (Exception var10) {
            Exception e = var10;
            this.getLogger().warning("Failed to check for updates: " + e.getMessage());
        }

    }

    private static class GeoLocation {
        private String country;
        private String region;
        private String city;

        private GeoLocation() {
        }

        public String getCountry() {
            return this.country;
        }

        public String getRegion() {
            return this.region;
        }

        public String getCity() {
            return this.city;
        }
    }
}
