package com.kubson.mmoclases.data;

import com.kubson.mmoclases.MMOClases;
import com.kubson.mmoclases.Settings;
import com.kubson.mmoclases.ability.Ability;
import com.kubson.mmoclases.api.CoreAttribute;
import com.kubson.mmoclases.api.PlayerClass;
import com.kubson.mmoclases.api.Stat;
import com.kubson.mmoclases.api.event.ClassChangeEvent;
import com.kubson.mmoclases.api.event.ClassLevelUpEvent;
import com.kubson.mmoclases.stats.PlayerStats;
import com.kubson.mmoclases.util.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.List;

/** Elegir clase, ganar XP, subir de nivel y repartir puntos. */
public final class ProgressionService {

    private final MMOClases plugin;

    public ProgressionService(MMOClases plugin) {
        this.plugin = plugin;
    }

    /** @return true si el jugador ahora tiene esa clase. */
    public boolean chooseClass(Player player, PlayerData data, PlayerClass newClass) {
        PlayerClass oldClass = data.currentClass();
        if (oldClass == newClass) {
            plugin.messages().send(player, "clase-ya", Messages.ph("clase", Messages.className(newClass)));
            return true;
        }
        if (oldClass != null && !player.hasPermission("mmoclases.cambiar")) {
            plugin.messages().send(player, "cambio-no-permitido");
            return false;
        }
        ClassChangeEvent event = new ClassChangeEvent(player, oldClass, newClass);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        data.setCastMode(false);
        data.setCurrentClass(newClass);
        data.clearCooldowns();
        data.setSpellCounter(0);
        PlayerStats stats = plugin.stats().compute(player, data);
        data.setResource(newClass.resource().startsFull() ? stats.get(Stat.RECURSO_MAXIMO) : 0);
        plugin.stats().applyHealth(player, data, stats);
        plugin.data().saveAsync(data);

        plugin.messages().send(player, "clase-elegida", Messages.ph("clase", Messages.className(newClass)));
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.7f, 1.2f);
        return true;
    }

    public void giveXp(Player player, PlayerData data, double amount) {
        if (!data.hasClass() || amount <= 0) {
            return;
        }
        Settings settings = plugin.settings();
        ClassProgress progress = data.current();
        if (progress.level() >= settings.maxLevel) {
            return;
        }
        int oldLevel = progress.level();
        progress.setXp(progress.xp() + amount * settings.xpMultiplier);
        while (progress.level() < settings.maxLevel && progress.xp() >= settings.xpToNext(progress.level())) {
            progress.setXp(progress.xp() - settings.xpToNext(progress.level()));
            progress.setLevel(progress.level() + 1);
            progress.setUnspentPoints(progress.unspentPoints() + settings.pointsPerLevel);
        }
        if (progress.level() >= settings.maxLevel) {
            progress.setXp(0);
        }
        if (progress.level() > oldLevel) {
            onLevelUp(player, data, oldLevel, progress.level());
        }
    }

    /** Fija el nivel (comando de admin). Recalcula los puntos disponibles. */
    public void setLevel(Player player, PlayerData data, PlayerClass playerClass, int level) {
        Settings settings = plugin.settings();
        ClassProgress progress = data.progress(playerClass);
        int oldLevel = progress.level();
        int newLevel = Math.max(1, Math.min(settings.maxLevel, level));
        progress.setLevel(newLevel);
        progress.setXp(0);
        int totalPoints = (newLevel - 1) * settings.pointsPerLevel;
        int available = totalPoints - progress.spentPoints();
        if (available < 0) {
            progress.refundAttributes();
            available = totalPoints;
        }
        progress.setUnspentPoints(available);
        if (newLevel > oldLevel && data.currentClass() == playerClass) {
            onLevelUp(player, data, oldLevel, newLevel);
        }
        plugin.data().saveAsync(data);
    }

    /** @return cuántos puntos se asignaron realmente. */
    public int allocate(PlayerData data, CoreAttribute attribute, int amount) {
        if (!data.hasClass() || amount <= 0) {
            return 0;
        }
        ClassProgress progress = data.current();
        int spend = Math.min(amount, progress.unspentPoints());
        if (spend <= 0) {
            return 0;
        }
        progress.setAttribute(attribute, progress.attribute(attribute) + spend);
        progress.setUnspentPoints(progress.unspentPoints() - spend);
        return spend;
    }

    private void onLevelUp(Player player, PlayerData data, int oldLevel, int newLevel) {
        PlayerClass playerClass = data.currentClass();
        if (playerClass == null) {
            return;
        }
        int points = (newLevel - oldLevel) * plugin.settings().pointsPerLevel;
        plugin.messages().send(player, "subida-nivel", Messages.ph("nivel", newLevel), Messages.ph("puntos", points));
        player.showTitle(Title.title(
                Component.text("¡Nivel " + newLevel + "!", NamedTextColor.GOLD),
                Messages.className(playerClass),
                Title.Times.times(Duration.ofMillis(250), Duration.ofMillis(2000), Duration.ofMillis(500))));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0), 40, 0.4, 0.8, 0.4, 0.3);

        List<Ability> abilities = plugin.abilities().abilitiesOf(playerClass);
        for (int i = 0; i < abilities.size(); i++) {
            Ability ability = abilities.get(i);
            int unlock = plugin.abilities().unlockLevel(ability);
            if (unlock > oldLevel && unlock <= newLevel) {
                plugin.messages().send(player, "habilidad-desbloqueada",
                        Messages.ph("habilidad", ability.name()), Messages.ph("tecla", i + 1));
            }
        }

        // Al subir de nivel: vida y recurso al máximo.
        PlayerStats stats = plugin.stats().compute(player, data);
        plugin.stats().applyHealth(player, data, stats);
        if (!player.isDead()) {
            player.setHealth(plugin.stats().maxHealth(player));
        }
        if (playerClass.resource().startsFull()) {
            data.setResource(stats.get(Stat.RECURSO_MAXIMO));
        }
        Bukkit.getPluginManager().callEvent(new ClassLevelUpEvent(player, playerClass, oldLevel, newLevel));
        plugin.data().saveAsync(data);
    }
}
