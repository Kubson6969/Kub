package com.kubson.mmoclases.hud;

import com.kubson.mmoclases.MMOClases;
import com.kubson.mmoclases.Settings;
import com.kubson.mmoclases.ability.Ability;
import com.kubson.mmoclases.api.PlayerClass;
import com.kubson.mmoclases.api.ResourceType;
import com.kubson.mmoclases.api.Stat;
import com.kubson.mmoclases.data.ClassProgress;
import com.kubson.mmoclases.data.PlayerData;
import com.kubson.mmoclases.stats.PlayerStats;
import com.kubson.mmoclases.util.Format;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Cada medio segundo: regenera el recurso, actualiza la vida máxima,
 * sale del modo habilidades si ya no llevas el arma y dibuja el HUD en la action bar.
 */
public final class HudTask implements Runnable {

    /** Segundos entre ejecuciones (la tarea corre cada 10 ticks). */
    public static final double INTERVAL_SECONDS = 0.5;

    private final MMOClases plugin;

    public HudTask(MMOClases plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            PlayerData data = plugin.data().getIfLoaded(player.getUniqueId());
            if (data == null) {
                continue;
            }
            tick(player, data);
        }
    }

    private void tick(Player player, PlayerData data) {
        PlayerClass playerClass = data.currentClass();
        PlayerStats stats = plugin.stats().compute(player, data);
        plugin.stats().applyHealth(player, data, stats);
        if (playerClass == null || player.isDead()) {
            return;
        }
        regenerate(data, playerClass.resource(), stats);

        if (data.castMode() && plugin.settings().castRequiresWeapon && !plugin.weapons().check(player, data).valid()) {
            plugin.castMode().setCastMode(player, data, false);
            return;
        }
        if (plugin.settings().showHud) {
            player.sendActionBar(render(player, data, playerClass, stats));
        }
    }

    private void regenerate(PlayerData data, ResourceType resource, PlayerStats stats) {
        Settings settings = plugin.settings();
        double max = stats.get(Stat.RECURSO_MAXIMO);
        double value = data.resource();
        if (resource == ResourceType.FURIA) {
            long outOfCombat = System.currentTimeMillis() - data.lastCombatMillis();
            if (outOfCombat > settings.rageOutOfCombatSeconds * 1000) {
                value -= settings.rageDecay * INTERVAL_SECONDS;
            }
        } else {
            value += stats.get(Stat.REGEN_RECURSO) * INTERVAL_SECONDS;
        }
        data.setResource(Math.max(0, Math.min(max, value)));
    }

    /** Redibuja el HUD al momento (p. ej. al entrar o salir del modo habilidades). */
    public void refresh(Player player, PlayerData data) {
        PlayerClass playerClass = data.currentClass();
        if (playerClass == null || !plugin.settings().showHud) {
            return;
        }
        player.sendActionBar(render(player, data, playerClass, plugin.stats().compute(player, data)));
    }

    private Component render(Player player, PlayerData data, PlayerClass playerClass, PlayerStats stats) {
        ResourceType resource = playerClass.resource();
        Component health = Component.text("❤ " + Math.round(player.getHealth()) + "/" + Math.round(plugin.stats().maxHealth(player)), NamedTextColor.RED);
        Component resourceText = Component.text(resource.symbol() + " " + (int) data.resource() + "/" + (int) stats.get(Stat.RECURSO_MAXIMO), resource.color());
        Component separator = Component.text("   ", NamedTextColor.DARK_GRAY);

        if (!data.castMode()) {
            ClassProgress progress = data.current();
            String levelText = playerClass.displayName() + " Nv." + progress.level();
            if (progress.level() < plugin.settings().maxLevel) {
                double fraction = progress.xp() / plugin.settings().xpToNext(progress.level());
                levelText += " (" + Math.round(fraction * 100) + "%)";
            }
            return Component.empty()
                    .append(health).append(separator)
                    .append(resourceText).append(separator)
                    .append(Component.text(levelText, playerClass.color()));
        }

        TextComponent.Builder builder = Component.text();
        List<Ability> abilities = plugin.abilities().abilitiesOf(playerClass);
        for (int i = 0; i < abilities.size(); i++) {
            Ability ability = abilities.get(i);
            builder.append(Component.text("[" + (i + 1) + "] ", NamedTextColor.GRAY));
            int unlock = plugin.abilities().unlockLevel(ability);
            double remaining = plugin.abilities().remainingCooldown(data, ability);
            if (data.level() < unlock) {
                builder.append(Component.text("✖ Nv" + unlock, NamedTextColor.DARK_GRAY));
            } else if (remaining > 0) {
                builder.append(Component.text(ability.name() + " " + Format.number(Math.ceil(remaining)) + "s", NamedTextColor.RED));
            } else if (data.resource() < plugin.abilities().cost(ability)) {
                builder.append(Component.text(ability.name(), NamedTextColor.GOLD));
            } else {
                builder.append(Component.text(ability.name(), NamedTextColor.GREEN));
            }
            builder.append(Component.text("  "));
        }
        return builder.append(resourceText).build();
    }
}
