package com.kubson.mmoclasses;

import com.kubson.mmoclasses.api.CoreAttribute;
import com.kubson.mmoclasses.api.PlayerClass;
import com.kubson.mmoclasses.api.Stat;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/** Valores de config.yml ya leídos y validados. Se recrea en cada recarga. */
public final class Settings {

    public record ClassSettings(Set<String> weapons, Map<Stat, Double> base, Map<Stat, Double> perLevel) {
    }

    private final Map<PlayerClass, ClassSettings> classes = new EnumMap<>(PlayerClass.class);
    private final Map<CoreAttribute, Map<Stat, Double>> attributeEffects = new EnumMap<>(CoreAttribute.class);
    private final Map<EntityType, Double> mobXp = new EnumMap<>(EntityType.class);
    private final @Nullable ConfigurationSection abilities;
    private final FileConfiguration config;

    public final boolean scaleHearts;
    public final boolean pvpAbilities;
    public final boolean aoeHitsAnimals;
    public final double defenseConstant;
    public final double maxCooldownReduction;
    public final double wrongWeaponMultiplier;
    public final int autosaveMinutes;
    public final boolean showHud;

    public final boolean castRequiresSneak;
    public final boolean castRequiresWeapon;

    public final int maxLevel;
    public final double xpBase;
    public final double xpExponent;
    public final int pointsPerLevel;
    public final double xpMultiplier;
    public final double defaultMobXp;

    public final double rageOnHit;
    public final double rageOnDamaged;
    public final double rageDecay;
    public final double rageOutOfCombatSeconds;

    public Settings(FileConfiguration config, Logger logger) {
        this.config = config;
        scaleHearts = config.getBoolean("general.escalar-corazones", true);
        pvpAbilities = config.getBoolean("general.pvp-habilidades", false);
        aoeHitsAnimals = config.getBoolean("general.habilidades-danan-animales", false);
        defenseConstant = Math.max(1, config.getDouble("general.defensa-constante", 60));
        maxCooldownReduction = config.getDouble("general.reduccion-enfriamiento-maxima", 40);
        wrongWeaponMultiplier = config.getDouble("general.multiplicador-arma-incorrecta", 0.5);
        autosaveMinutes = config.getInt("general.autoguardado-minutos", 5);
        showHud = config.getBoolean("general.mostrar-hud", true);

        castRequiresSneak = config.getBoolean("modo-habilidades.requiere-agacharse", false);
        castRequiresWeapon = config.getBoolean("modo-habilidades.requiere-arma-de-clase", true);

        maxLevel = Math.max(1, config.getInt("experiencia.nivel-maximo", 50));
        xpBase = config.getDouble("experiencia.base", 50);
        xpExponent = config.getDouble("experiencia.exponente", 1.3);
        pointsPerLevel = config.getInt("experiencia.puntos-por-nivel", 3);
        xpMultiplier = config.getDouble("experiencia.multiplicador", 1.0);
        defaultMobXp = config.getDouble("experiencia.por-defecto", 5);
        ConfigurationSection mobs = config.getConfigurationSection("experiencia.mobs");
        if (mobs != null) {
            for (String key : mobs.getKeys(false)) {
                try {
                    mobXp.put(EntityType.valueOf(key.toUpperCase(Locale.ROOT)), mobs.getDouble(key));
                } catch (IllegalArgumentException e) {
                    logger.warning("Mob desconocido en experiencia.mobs: " + key);
                }
            }
        }

        rageOnHit = config.getDouble("recursos.furia.por-golpe", 8);
        rageOnDamaged = config.getDouble("recursos.furia.por-dano-recibido", 4);
        rageDecay = config.getDouble("recursos.furia.decaimiento", 3);
        rageOutOfCombatSeconds = config.getDouble("recursos.furia.segundos-fuera-combate", 5);

        for (CoreAttribute attribute : CoreAttribute.values()) {
            attributeEffects.put(attribute, readStats(config.getConfigurationSection("atributos." + attribute.id()), logger));
        }

        for (PlayerClass playerClass : PlayerClass.values()) {
            String path = "clases." + playerClass.id();
            Set<String> weapons = new LinkedHashSet<>();
            for (String weapon : config.getStringList(path + ".armas")) {
                weapons.add(weapon.toUpperCase(Locale.ROOT));
            }
            classes.put(playerClass, new ClassSettings(
                    Collections.unmodifiableSet(weapons),
                    readStats(config.getConfigurationSection(path + ".base"), logger),
                    readStats(config.getConfigurationSection(path + ".por-nivel"), logger)));
        }

        abilities = config.getConfigurationSection("habilidades");
    }

    private static Map<Stat, Double> readStats(@Nullable ConfigurationSection section, Logger logger) {
        Map<Stat, Double> stats = new EnumMap<>(Stat.class);
        if (section == null) {
            return stats;
        }
        for (String key : section.getKeys(false)) {
            Stat stat = Stat.fromId(key);
            if (stat == null) {
                logger.warning("Stat desconocida en " + section.getCurrentPath() + ": " + key);
                continue;
            }
            stats.put(stat, section.getDouble(key));
        }
        return stats;
    }

    public ClassSettings classSettings(PlayerClass playerClass) {
        return classes.get(playerClass);
    }

    public Map<Stat, Double> attributeEffects(CoreAttribute attribute) {
        return attributeEffects.get(attribute);
    }

    /** XP necesaria para pasar de {@code level} a {@code level + 1}. */
    public double xpToNext(int level) {
        return Math.max(1, Math.round(xpBase * Math.pow(level, xpExponent)));
    }

    /** XP por matar a un mob; los mobs no hostiles solo dan XP si están en la lista. */
    public double mobXp(EntityType type, boolean hostile) {
        Double value = mobXp.get(type);
        if (value != null) {
            return value;
        }
        return hostile ? defaultMobXp : 0;
    }

    public @Nullable ConfigurationSection abilitySection(String abilityId) {
        return abilities == null ? null : abilities.getConfigurationSection(abilityId);
    }

    public double passive(PlayerClass playerClass, String key, double def) {
        return config.getDouble("pasivas." + playerClass.id() + "." + key, def);
    }
}
