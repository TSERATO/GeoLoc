package dev.tserato.geoloc.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GeoLocGUIPlayerListener implements Listener {
    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().endsWith("'s Profile")) {
            Player player = (Player) event.getWhoClicked();
            if (event.getCurrentItem() == null) return;
            if (event.getCurrentItem().getItemMeta() == null) return;

            if (event.getCurrentItem().getItemMeta().getDisplayName().startsWith(ChatColor.GREEN + "Teleport to ") && event.getCurrentItem().getType().equals(Material.COMPASS)) {
                event.setCancelled(true);
                Player targetPlayer = Bukkit.getPlayer(event.getView().getTitle().replace("'s Profile", ""));
                if (targetPlayer != null) {
                    player.teleport(targetPlayer.getLocation());
                    player.sendMessage(ChatColor.GREEN + "Teleported to " + targetPlayer.getName());
                    player.closeInventory();
                } else {
                    player.sendMessage(ChatColor.RED + "Player not found.");
                }
            } else if (event.getCurrentItem().getItemMeta().getDisplayName().startsWith(ChatColor.RED + "Kill") && event.getCurrentItem().getType().equals(Material.DIAMOND_SWORD)) {
                event.setCancelled(true);
                Player targetPlayer = Bukkit.getPlayer(event.getView().getTitle().replace("'s Profile", ""));
                if (targetPlayer != null) {
                    targetPlayer.setHealth(0);
                    player.sendMessage(ChatColor.RED + "Killed " + targetPlayer.getName());
                    player.closeInventory();
                } else {
                    player.sendMessage(ChatColor.RED + "Player not found.");
                }
            } else if (event.getCurrentItem().getItemMeta().getDisplayName().startsWith(ChatColor.BLUE + "Heal") && event.getCurrentItem().getType().equals(Material.ENCHANTED_GOLDEN_APPLE)) {
                event.setCancelled(true);
                Player targetPlayer = Bukkit.getPlayer(event.getView().getTitle().replace("'s Profile", ""));
                if (targetPlayer != null) {
                    targetPlayer.setHealth(targetPlayer.getMaxHealth());
                    targetPlayer.setFoodLevel(20);
                    targetPlayer.setSaturation(20);
                    player.sendMessage(ChatColor.BLUE + "Healed " + targetPlayer.getName());
                    player.closeInventory();
                } else {
                    player.sendMessage(ChatColor.RED + "Player not found.");
                }
            } else {
                event.setCancelled(true);
            }
        }
    }
}
