package com.kubson.mmoclasses.listener;

import com.kubson.mmoclasses.MMOClasses;
import com.kubson.mmoclasses.api.PlayerClass;
import com.kubson.mmoclasses.api.Stat;
import com.kubson.mmoclasses.data.PlayerData;
import com.kubson.mmoclasses.stats.PlayerStats;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/** Carga/guardado de datos, XP por matar mobs e inmunidad a caída de habilidades. */
public final class PlayerListener implements Listener {

    private final MMOClasses plugin;

    public PlayerListener(MMOClasses plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.data().get(player);
        setup(player, data);
        if (!data.hasClass()) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    plugin.messages().send(player, "sin-clase");
                }
            }, 40L);
        }
    }

    /** Prepara recurso y vida al entrar (también se usa al recargar el plugin). */
    public void setup(Player player, PlayerData data) {
        PlayerClass playerClass = data.currentClass();
        PlayerStats stats = plugin.stats().compute(player, data);
        if (playerClass != null && playerClass.resource().startsFull()) {
            data.setResource(stats.get(Stat.RECURSO_MAXIMO));
        }
        plugin.stats().applyHealth(player, data, stats);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.data().unload(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        PlayerData data = plugin.data().getIfLoaded(event.getEntity().getUniqueId());
        if (data != null) {
            data.setCastMode(false);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        PlayerData data = plugin.data().getIfLoaded(event.getPlayer().getUniqueId());
        if (data == null || !data.hasClass()) {
            return;
        }
        PlayerClass playerClass = data.currentClass();
        if (playerClass != null) {
            data.setResource(playerClass.resource().startsFull()
                    ? plugin.stats().compute(event.getPlayer(), data).get(Stat.RECURSO_MAXIMO)
                    : 0);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onFall(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL || !(event.getEntity() instanceof Player player)) {
            return;
        }
        PlayerData data = plugin.data().getIfLoaded(player.getUniqueId());
        if (data != null && data.isFallImmune()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onKill(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        if (killer == null || entity instanceof Player) {
            return;
        }
        PlayerData data = plugin.data().get(killer);
        if (!data.hasClass()) {
            return;
        }
        double xp = plugin.settings().mobXp(entity.getType(), entity instanceof Enemy);
        plugin.progression().giveXp(killer, data, xp);
    }
}
