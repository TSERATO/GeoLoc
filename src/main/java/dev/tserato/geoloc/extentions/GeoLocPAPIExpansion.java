package dev.tserato.geoloc.extentions;

import com.google.gson.Gson;
import dev.tserato.geoloc.GeoLocSpigot;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class GeoLocPAPIExpansion extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "GeoLoc";
    }

    @Override
    public @NotNull String getAuthor() {
        return "TSERATO";
    }

    @Override
    public @NotNull String getVersion() {
        return "2.0";
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer == null || !offlinePlayer.isOnline()) {
            return null;
        }

        Player player = offlinePlayer.getPlayer();
        if (player == null) {
            return null;
        }

        String metadataKey = "GeoLoc_" + params;
        List<MetadataValue> metadataValues = player.getMetadata(metadataKey);
        if (!metadataValues.isEmpty()) {
            return metadataValues.get(0).asString();
        }

        Bukkit.getScheduler().runTaskAsynchronously(GeoLocSpigot.getInstance(), new GeoLocTask(player, params));
        return "Loading...";
    }

    private static class GeoLocTask implements Runnable {
        private final Player player;
        private final String params;

        public GeoLocTask(Player player, String params) {
            this.player = player;
            this.params = params;
        }

        @Override
        public void run() {
            try {
                String ipAddress = player.getAddress().getAddress().getHostAddress();
                String urlString = "http://ip-api.com/json/" + ipAddress;
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                if (connection.getResponseCode() == 200) {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        StringBuilder response = new StringBuilder();
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }

                        GeoLocation geoLocation = new Gson().fromJson(response.toString(), GeoLocation.class);
                        String locationResult = getLocationResult(geoLocation, params);

                        player.setMetadata("GeoLoc_" + params, new FixedMetadataValue(GeoLocSpigot.getInstance(), locationResult));
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Failed to fetch GeoLocation.");
                }

                connection.disconnect();
            } catch (IOException e) {
                player.sendMessage(ChatColor.RED + "An error occurred while trying to get the GeoLocation.");
            }
        }

        private String getLocationResult(GeoLocation geoLocation, String params) {
            switch (params.toLowerCase()) {
                case "city":
                    return geoLocation.getCity();
                case "country":
                    return geoLocation.getCountry();
                case "region":
                    return geoLocation.getRegion();
                case "all":
                    return geoLocation.getCountry() + ", " + geoLocation.getRegion() + ", " + geoLocation.getCity();
                default:
                    return "";
            }
        }
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
