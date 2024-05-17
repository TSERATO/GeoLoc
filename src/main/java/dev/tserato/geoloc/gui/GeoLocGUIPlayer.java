package dev.tserato.geoloc.gui;

import dev.tserato.geoloc.GeoLocSpigot;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class GeoLocGUIPlayer {

    public static void openPlayerGUI(Player player, Player targetPlayer) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Inventory playerGUI = Bukkit.createInventory(null, 27, targetPlayer.getName() + "'s Profile");

                ItemStack teleportItem = createItem(Material.COMPASS, ChatColor.GREEN + "Teleport to " + targetPlayer.getName());
                ItemStack killItem = createItem(Material.DIAMOND_SWORD, ChatColor.RED + "Kill " + targetPlayer.getName());
                ItemStack healItem = createItem(Material.ENCHANTED_GOLDEN_APPLE, ChatColor.BLUE + "Heal " + targetPlayer.getName());

                fillInventory(playerGUI, teleportItem, killItem, healItem);

                Bukkit.getScheduler().runTask(GeoLocSpigot.getInstance(), () -> player.openInventory(playerGUI));
            }
        }.runTaskAsynchronously(GeoLocSpigot.getInstance());
    }

    private static ItemStack createItem(Material material, String displayName) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(displayName);
        item.setItemMeta(meta);
        return item;
    }

    private static void fillInventory(Inventory inventory, ItemStack... specialItems) {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (i == 11) {
                inventory.setItem(11, specialItems[0]);
            } else if (i == 13) {
                inventory.setItem(13, specialItems[1]);
            } else if (i == 15) {
                inventory.setItem(15, specialItems[2]);
            } else {
                inventory.setItem(i, createGrayPane());
            }
        }
    }

    private static ItemStack createGrayPane() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.setDisplayName("");
        pane.setItemMeta(meta);
        return pane;
    }
}
