package com.kubson.mmoclasses.stats;

import com.kubson.mmoclasses.MMOClasses;
import com.kubson.mmoclasses.Settings;
import com.kubson.mmoclasses.api.CoreAttribute;
import com.kubson.mmoclasses.api.PlayerClass;
import com.kubson.mmoclasses.api.Stat;
import com.kubson.mmoclasses.api.WeaponInfo;
import com.kubson.mmoclasses.data.ClassProgress;
import com.kubson.mmoclasses.data.PlayerData;
import com.kubson.mmoclasses.weapon.WeaponBridge;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

/**
 * Calcula las stats: base de clase + crecimiento por nivel + atributos + arma de MMOWeaponary.
 * También aplica la vida máxima como modificador de atributo.
 */
public final class StatService {

    private final MMOClasses plugin;
    private final NamespacedKey healthKey;

    public StatService(MMOClasses plugin) {
        this.plugin = plugin;
        this.healthKey = new NamespacedKey(plugin, "vida_clase");
    }

    public PlayerStats compute(Player player, PlayerData data) {
        PlayerClass playerClass = data.currentClass();
        if (playerClass == null) {
            return PlayerStats.EMPTY;
        }
        WeaponBridge.Check check = plugin.weapons().check(player, data);
        return compute(playerClass, data.current(), check.valid() ? check.weapon() : null);
    }

    public PlayerStats compute(PlayerClass playerClass, ClassProgress progress, @Nullable WeaponInfo weapon) {
        Settings settings = plugin.settings();
        Settings.ClassSettings classSettings = settings.classSettings(playerClass);
        Map<Stat, Double> values = new EnumMap<>(Stat.class);

        classSettings.base().forEach((stat, value) -> values.merge(stat, value, Double::sum));
        int levelsGained = progress.level() - 1;
        classSettings.perLevel().forEach((stat, value) -> values.merge(stat, value * levelsGained, Double::sum));

        for (CoreAttribute attribute : CoreAttribute.values()) {
            int points = progress.attribute(attribute);
            if (points > 0) {
                settings.attributeEffects(attribute).forEach((stat, value) -> values.merge(stat, value * points, Double::sum));
            }
        }

        if (weapon != null) {
            weapon.stats().forEach((stat, value) -> values.merge(stat, value, Double::sum));
        }

        clamp(values, Stat.CRITICO_PROB, 0, 100);
        clamp(values, Stat.REDUCCION_ENFRIAMIENTO, 0, settings.maxCooldownReduction);
        clamp(values, Stat.RECURSO_MAXIMO, 1, Double.MAX_VALUE);
        clamp(values, Stat.CRITICO_DANO, 100, Double.MAX_VALUE);
        return new PlayerStats(values);
    }

    private static void clamp(Map<Stat, Double> values, Stat stat, double min, double max) {
        values.put(stat, Math.max(min, Math.min(max, values.getOrDefault(stat, 0.0))));
    }

    /** Aplica la stat vida_maxima como modificador de MAX_HEALTH (solo si cambió). */
    public void applyHealth(Player player, PlayerData data, PlayerStats stats) {
        double bonus = data.hasClass() ? stats.get(Stat.VIDA_MAXIMA) : 0;
        if (bonus == data.appliedHealthBonus()) {
            return;
        }
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth == null) {
            return;
        }
        for (AttributeModifier modifier : new ArrayList<>(maxHealth.getModifiers())) {
            if (healthKey.equals(modifier.getKey())) {
                maxHealth.removeModifier(modifier);
            }
        }
        if (bonus != 0) {
            maxHealth.addModifier(new AttributeModifier(healthKey, bonus,
                    AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.ANY));
        }
        data.setAppliedHealthBonus(bonus);

        double max = maxHealth.getValue();
        if (player.getHealth() > max) {
            player.setHealth(max);
        }
        if (plugin.settings().scaleHearts && data.hasClass()) {
            player.setHealthScale(20.0);
            player.setHealthScaled(true);
        } else {
            player.setHealthScaled(false);
        }
    }

    public double maxHealth(LivingEntity entity) {
        AttributeInstance maxHealth = entity.getAttribute(Attribute.MAX_HEALTH);
        return maxHealth == null ? 20.0 : maxHealth.getValue();
    }
}
