package com.kubson.mmoclases.ability;

import com.kubson.mmoclases.MMOClases;
import com.kubson.mmoclases.combat.CombatService;
import com.kubson.mmoclases.combat.DamageType;
import com.kubson.mmoclases.data.PlayerData;
import com.kubson.mmoclases.stats.PlayerStats;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Todo lo que necesita una habilidad al lanzarse. Las stats son las del momento del lanzamiento. */
public record AbilityContext(MMOClases plugin, Player player, PlayerData data, PlayerStats stats,
                             @Nullable ConfigurationSection section) {

    /** Lee {@code habilidades.<id>.<key>} de config.yml. */
    public double num(String key, double def) {
        return section == null ? def : section.getDouble(key, def);
    }

    public CombatService combat() {
        return plugin.combat();
    }

    public double damage(LivingEntity target, double base, DamageType type) {
        return combat().damage(player, stats, target, base, type);
    }

    public double heal(LivingEntity target, double base) {
        return combat().heal(stats, target, base);
    }

    public List<LivingEntity> enemiesNear(Location center, double radius) {
        return combat().enemiesNear(player, center, radius);
    }

    public List<Player> alliesNear(Location center, double radius) {
        return combat().alliesNear(player, center, radius);
    }
}
