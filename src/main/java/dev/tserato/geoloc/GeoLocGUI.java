package dev.tserato.geoloc;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class GeoLocGUI extends JavaPlugin {

    private static final int MAX_PLAYERS_PER_PAGE = 45; // Adjust as needed

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
                    "Joined: " + formatLastLogin(onlinePlayer.getLastPlayed()),
                    "GeoLocation: " + geoLocation
            ));
            playerHead.setItemMeta(meta);

            gui.addItem(playerHead);
        }

        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeButton.getItemMeta();
        closeMeta.setDisplayName(ChatColor.RED + "Close");
        closeMeta.addEnchant(Enchantment.LURE, 1, false); // You can use any enchantment, but LURE is commonly used for this purpose
        closeMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        closeButton.setItemMeta(closeMeta);
        gui.setItem(49, closeButton);

        ItemStack FillMaterial = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta FillMeta = FillMaterial.getItemMeta();
        FillMeta.setDisplayName("");
        FillMaterial.setItemMeta(FillMeta);
        gui.setItem(45, FillMaterial);
        gui.setItem(46, FillMaterial);
        gui.setItem(47, FillMaterial);
        gui.setItem(51, FillMaterial);
        gui.setItem(52, FillMaterial);
        gui.setItem(53, FillMaterial);

        ItemStack nextPageButton = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = nextPageButton.getItemMeta();
        nextMeta.setDisplayName("Next Page");
        nextPageButton.setItemMeta(nextMeta);
        nextMeta.addEnchant(Enchantment.LURE, 1, false);
        nextMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        nextPageButton.setItemMeta(nextMeta);
        gui.setItem(50, nextPageButton);

        ItemStack prevPageButton = new ItemStack(Material.ARROW);
        ItemMeta prevMeta = prevPageButton.getItemMeta();
        prevMeta.setDisplayName("Previous Page");
        prevPageButton.setItemMeta(prevMeta);
        prevMeta.addEnchant(Enchantment.LURE, 1, false);
        prevMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        prevPageButton.setItemMeta(prevMeta);
        gui.setItem(48, prevPageButton);

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

    private static String formatLastLogin(long lastLogin) {
        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - lastLogin;
        long seconds = timeDifference / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        if (days > 0) {
            return days + " days ago";
        } else if (hours > 0) {
            return hours + " hours ago";
        } else if (minutes > 0) {
            return minutes + " minutes ago";
        } else {
            return seconds + " seconds ago";
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