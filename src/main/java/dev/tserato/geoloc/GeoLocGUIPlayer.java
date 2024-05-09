package dev.tserato.geoloc;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class GeoLocGUIPlayer extends JavaPlugin {

    public static void openPlayerGUI(Player player, Player targetPlayer) {
        Inventory playerGUI = Bukkit.createInventory(null, 27, targetPlayer.getName() + "'s Profile");

        ItemStack teleportItem = new ItemStack(Material.COMPASS);
        ItemMeta teleportMeta = teleportItem.getItemMeta();
        teleportMeta.setDisplayName(ChatColor.GREEN + "Teleport to " + targetPlayer.getName());
        teleportItem.setItemMeta(teleportMeta);

        ItemStack killItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta killMeta = killItem.getItemMeta();
        killMeta.setDisplayName(ChatColor.RED + "Kill " + targetPlayer.getName());
        killItem.setItemMeta(killMeta);

        ItemStack healItem = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE);
        ItemMeta healMeta = healItem.getItemMeta();
        healMeta.setDisplayName(ChatColor.BLUE + "Heal " + targetPlayer.getName());
        healItem.setItemMeta(healMeta);

        for (int i = 0; i < playerGUI.getSize(); i++) {
            if (i == 11) {
                playerGUI.setItem(11, teleportItem);
            } else if (i == 13) {
                playerGUI.setItem(13, killItem);
            } else if (i == 15) {
                playerGUI.setItem(15, healItem);
            } else {
                playerGUI.setItem(i, createGrayPane());
            }
        }

        player.openInventory(playerGUI);
    }

    private static ItemStack createGrayPane() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.setDisplayName("");
        pane.setItemMeta(meta);
        return pane;
    }

}
