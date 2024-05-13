package dev.tserato.geoloc;

import com.google.gson.Gson;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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
        return "1.7";
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer != null && offlinePlayer.isOnline()) {
            Player player = offlinePlayer.getPlayer();
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
                        if (params.equalsIgnoreCase("geoloc_city")) {
                            return geoLocation.getCity();
                        } else if (params.equalsIgnoreCase("geoloc_country")) {
                            return geoLocation.getCountry();
                        } else if (params.equalsIgnoreCase("geoloc_region")) {
                            return geoLocation.getRegion();
                        }
                    }
                    connection.disconnect();
                } catch (IOException e) {
                    player.sendMessage(ChatColor.RED + "An error occurred, while trying to get the GeoLocation");
                }
            }
        }
        return null;
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
