package dev.tserato.geoloc;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GeoLocGUIListener implements Listener {
    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (event.getView().getTitle().startsWith("GeoLocGUI")) {
            if (event.getCurrentItem().getItemMeta().getDisplayName().equals(ChatColor.RED + "Close") && event.getCurrentItem().getType().equals(Material.BARRIER)) {
                player.closeInventory();
                event.setCancelled(true);
            } else if (event.getCurrentItem().getItemMeta().getDisplayName().equals("Next Page") && event.getCurrentItem().getType().equals(Material.ARROW)) {
                String title = event.getView().getTitle();
                String[] split = title.split(" ");
                int currentPage = Integer.parseInt(split[split.length - 1]);
                GeoLocGUI.openGUI(player, currentPage + 1);
                event.setCancelled(true);
            } else if (event.getCurrentItem().getItemMeta().getDisplayName().equals("Previous Page") && event.getCurrentItem().getType().equals(Material.ARROW)) {
                String title = event.getView().getTitle();
                String[] split = title.split(" ");
                int currentPage = Integer.parseInt(split[split.length - 1]);
                if (currentPage > 1) {
                    GeoLocGUI.openGUI(player, currentPage - 1);
                }
                event.setCancelled(true);
            } else if (event.getCurrentItem().getType().equals(Material.PLAYER_HEAD)) {
                Player targetPlayer = Bukkit.getPlayer(event.getCurrentItem().getItemMeta().getDisplayName());
                if (targetPlayer != null) {
                    player.closeInventory();
                    GeoLocGUIPlayer.openPlayerGUI(player, targetPlayer);
                }
            } else {
                event.setCancelled(true);
            }
        }
    }
}
