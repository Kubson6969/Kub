package com.kubson.mmoclasses.listener;

import com.kubson.mmoclasses.MMOClasses;
import com.kubson.mmoclasses.data.PlayerData;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

/**
 * Modo habilidades: F (cambiar de mano) con el arma de clase lo activa/desactiva.
 * Dentro del modo, las teclas 1-4 lanzan habilidades en vez de cambiar de ranura.
 */
public final class CastModeListener implements Listener {

    private final MMOClasses plugin;

    public CastModeListener(MMOClasses plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSwap(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.data().get(player);
        if (!data.hasClass() || !player.hasPermission("mmoclasses.use")) {
            return;
        }
        if (!data.castMode()) {
            if (plugin.settings().castRequiresSneak && !player.isSneaking()) {
                return;
            }
            if (plugin.settings().castRequiresWeapon && !plugin.weapons().check(player, data).valid()) {
                return;
            }
        }
        event.setCancelled(true);
        setCastMode(player, data, !data.castMode());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHotbar(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.data().getIfLoaded(player.getUniqueId());
        if (data == null || !data.castMode()) {
            return;
        }
        event.setCancelled(true);
        int slot = event.getNewSlot();
        if (slot >= 0 && slot < 4) {
            plugin.abilities().cast(player, slot);
        }
    }

    public void setCastMode(Player player, PlayerData data, boolean enabled) {
        if (data.castMode() == enabled) {
            return;
        }
        data.setCastMode(enabled);
        plugin.messages().send(player, enabled ? "modo-activado" : "modo-desactivado");
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, enabled ? 1.4f : 0.8f);
        plugin.hud().refresh(player, data);
    }
}
