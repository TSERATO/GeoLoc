package dev.tserato.geoloc;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class GeoLocGUIListener extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent event) {
        System.out.println("Event has been called");

        Player player = (Player) event.getWhoClicked();

        ItemStack clickedItem = event.getCurrentItem();
        Material itemType = clickedItem.getType();
        ItemMeta itemMeta = clickedItem.getItemMeta();
        String displayName = itemMeta.getDisplayName();

        System.out.println("Clicked item: " + itemMeta.getDisplayName());
        System.out.println("Clicked item type: " + itemType);
        System.out.println("Clicked item display name: " + displayName);

        if (displayName.equals(ChatColor.RED + "Close") && itemType.equals(Material.BARRIER)) {
            System.out.println("Item has correct display name and material");

            player.closeInventory();
            event.setCancelled(true);
        }
    }
}
