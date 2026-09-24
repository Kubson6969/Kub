package com.kubson.mmoclasses.listener;

import com.kubson.mmoclasses.gui.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

/** Bloquea mover items en los menús del plugin y reenvía los clics. */
public final class MenuListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof Menu menu)) {
            return;
        }
        event.setCancelled(true);
        if (event.getWhoClicked() instanceof Player player
                && event.getRawSlot() >= 0 && event.getRawSlot() < event.getInventory().getSize()) {
            menu.onClick(player, event.getRawSlot(), event.getClick());
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof Menu) {
            event.setCancelled(true);
        }
    }
}
