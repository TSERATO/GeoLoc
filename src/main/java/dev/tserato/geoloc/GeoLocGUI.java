package dev.tserato.geoloc;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class GeoLocGUI extends JavaPlugin implements Listener {

    private static final int MAX_PLAYERS_PER_PAGE = 45; // Adjust as needed

    private Player searchingPlayer;

    public static void openGUI(Player player, int page) {
        Inventory gui = Bukkit.createInventory(null, 54, "GeoLocGUI - Page " + page);

        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        List<Player> playerList = new ArrayList<>(onlinePlayers);
        int startIndex = (page - 1) * MAX_PLAYERS_PER_PAGE;
        int endIndex = Math.min(startIndex + MAX_PLAYERS_PER_PAGE, playerList.size());

        for (int i = startIndex; i < endIndex; i++) {
            Player onlinePlayer = playerList.get(i);
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) playerHead.getItemMeta();
            meta.setOwningPlayer(onlinePlayer);
            meta.setDisplayName(onlinePlayer.getName());
            String ipAddress = onlinePlayer.getAddress().getAddress().getHostAddress();
            String geoLocation = getGeoLocation(ipAddress);
            meta.setLore(Arrays.asList(
                    "UUID: " + onlinePlayer.getUniqueId(),
                    "Joined: " + onlinePlayer.getFirstPlayed(),
                    "GeoLocation: " + geoLocation
            ));
            playerHead.setItemMeta(meta);

            gui.addItem(playerHead);
        }

        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeButton.getItemMeta();
        closeMeta.setDisplayName(ChatColor.RED + "Close");
        closeButton.setItemMeta(closeMeta);
        gui.setItem(45, closeButton);

        ItemStack searchButton = new ItemStack(Material.COMPASS);
        ItemMeta searchMeta = searchButton.getItemMeta();
        searchMeta.setDisplayName("Search");
        searchButton.setItemMeta(searchMeta);
        gui.setItem(53, searchButton);

        if (endIndex < playerList.size()) {
            ItemStack nextPageButton = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextPageButton.getItemMeta();
            nextMeta.setDisplayName("Next Page");
            nextPageButton.setItemMeta(nextMeta);
            gui.setItem(51, nextPageButton);
        }

        if (page > 1) {
            ItemStack prevPageButton = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevPageButton.getItemMeta();
            prevMeta.setDisplayName("Previous Page");
            prevPageButton.setItemMeta(prevMeta);
            gui.setItem(47, prevPageButton);
        }

        player.openInventory(gui);
    }
    private static String getGeoLocation(String ipAddress) {
        try {
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
                return geoLocation.getCountry() + ", " + geoLocation.getRegion() + ", " + geoLocation.getCity();
            }
            connection.disconnect();
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
