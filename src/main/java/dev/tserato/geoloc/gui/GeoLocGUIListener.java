package dev.tserato.geoloc.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GeoLocGUIListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (title.startsWith("GeoLocGUI")) {
            handleGeoLocGUIClick(event, player, title);
        }
    }

    private void handleGeoLocGUIClick(InventoryClickEvent event, Player player, String title) {
        ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null || !currentItem.hasItemMeta()) {
            event.setCancelled(true);
            return;
        }

        ItemMeta itemMeta = currentItem.getItemMeta();
        String displayName = itemMeta.getDisplayName();
        Material itemType = currentItem.getType();

        if (displayName.equals(ChatColor.RED + "Close") && itemType == Material.BARRIER) {
            player.closeInventory();
        } else if (displayName.equals("Next Page") && itemType == Material.ARROW) {
            int currentPage = getCurrentPage(title);
            GeoLocGUI.openGUI(player, currentPage + 1);
        } else if (displayName.equals("Previous Page") && itemType == Material.ARROW) {
            int currentPage = getCurrentPage(title);
            if (currentPage > 1) {
                GeoLocGUI.openGUI(player, currentPage - 1);
            }
        } else if (itemType == Material.PLAYER_HEAD) {
            Player targetPlayer = Bukkit.getPlayer(displayName);
            if (targetPlayer != null) {
                player.closeInventory();
                GeoLocGUIPlayer.openPlayerGUI(player, targetPlayer);
            }
        }
        event.setCancelled(true);
    }

    private int getCurrentPage(String title) {
        String[] split = title.split(" ");
        return Integer.parseInt(split[split.length - 1]);
    }
}
