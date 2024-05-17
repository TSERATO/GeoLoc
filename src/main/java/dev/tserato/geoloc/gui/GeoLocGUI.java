package dev.tserato.geoloc.gui;

import com.google.gson.Gson;
import dev.tserato.geoloc.GeoLocSpigot;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class GeoLocGUI {

    private static final int MAX_PLAYERS_PER_PAGE = 45;

    public static void openGUI(Player player, int page) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Inventory gui = Bukkit.createInventory(null, 54, "GeoLocGUI - Page " + page);

                List<Player> playerList = new ArrayList<>(Bukkit.getOnlinePlayers());
                int startIndex = (page - 1) * MAX_PLAYERS_PER_PAGE;
                int endIndex = Math.min(startIndex + MAX_PLAYERS_PER_PAGE, playerList.size());

                for (int i = startIndex; i < endIndex; i++) {
                    Player onlinePlayer = playerList.get(i);
                    ItemStack playerHead = createPlayerHead(onlinePlayer);
                    gui.addItem(playerHead);
                }

                setupNavigationButtons(gui, page);

                Bukkit.getScheduler().runTask(GeoLocSpigot.getInstance(), () -> player.openInventory(gui));
            }
        }.runTaskAsynchronously(GeoLocSpigot.getInstance());
    }

    private static ItemStack createPlayerHead(Player onlinePlayer) {
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
        return playerHead;
    }

    private static void setupNavigationButtons(Inventory gui, int page) {
        gui.setItem(49, createButton(Material.BARRIER, ChatColor.RED + "Close"));
        fillWithPane(gui, 45, 46, 47, 51, 52, 53);
        gui.setItem(50, createButton(Material.ARROW, "Next Page"));
        gui.setItem(48, createButton(Material.ARROW, "Previous Page"));
    }

    private static ItemStack createButton(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.addEnchant(Enchantment.LURE, 1, false);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }

    private static void fillWithPane(Inventory gui, int... slots) {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.setDisplayName("");
        pane.setItemMeta(meta);
        for (int slot : slots) {
            gui.setItem(slot, pane);
        }
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

    private static String formatLastLogin(long lastLogin) {
        long timeDifference = System.currentTimeMillis() - lastLogin;
        long seconds = timeDifference / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        if (days > 0) return days + " days ago";
        if (hours > 0) return hours + " hours ago";
        if (minutes > 0) return minutes + " minutes ago";
        return seconds + " seconds ago";
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
