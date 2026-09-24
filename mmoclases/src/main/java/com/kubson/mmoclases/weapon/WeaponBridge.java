package com.kubson.mmoclases.weapon;

import com.kubson.mmoclases.MMOClases;
import com.kubson.mmoclases.api.PlayerClass;
import com.kubson.mmoclases.api.Stat;
import com.kubson.mmoclases.api.WeaponInfo;
import com.kubson.mmoclases.api.WeaponProvider;
import com.kubson.mmoclases.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

/**
 * Identifica armas de MMOWeaponary. Orden:
 * 1) {@link WeaponProvider} registrado en el ServicesManager,
 * 2) claves del PersistentDataContainer,
 * 3) materiales vanilla.
 */
public final class WeaponBridge {

    public enum Status { NONE, VALID, WRONG_CLASS, LEVEL_TOO_LOW }

    public record Check(Status status, @Nullable WeaponInfo weapon) {
        public boolean valid() {
            return status == Status.VALID;
        }
    }

    private static final Check NO_WEAPON = new Check(Status.NONE, null);

    private final MMOClases plugin;
    private boolean providerErrorLogged;

    private boolean pdcEnabled;
    private @Nullable NamespacedKey typeKey;
    private @Nullable NamespacedKey classKey;
    private @Nullable NamespacedKey levelKey;
    private final Map<Stat, NamespacedKey> statKeys = new EnumMap<>(Stat.class);

    private boolean vanillaEnabled;
    private final Map<Material, String> vanillaExact = new EnumMap<>(Material.class);
    private final List<Map.Entry<String, String>> vanillaSuffixes = new ArrayList<>();

    public WeaponBridge(MMOClases plugin) {
        this.plugin = plugin;
    }

    public void reload(FileConfiguration config) {
        pdcEnabled = config.getBoolean("mmoweaponary.pdc.activado", true);
        String namespace = config.getString("mmoweaponary.pdc.namespace", "mmoweaponary");
        typeKey = key(namespace, config.getString("mmoweaponary.pdc.clave-tipo", "tipo"));
        classKey = key(namespace, config.getString("mmoweaponary.pdc.clave-clase", "clase"));
        levelKey = key(namespace, config.getString("mmoweaponary.pdc.clave-nivel", "nivel"));
        statKeys.clear();
        ConfigurationSection stats = config.getConfigurationSection("mmoweaponary.pdc.stats");
        if (stats != null) {
            for (String statId : stats.getKeys(false)) {
                Stat stat = Stat.fromId(statId);
                NamespacedKey statKey = key(namespace, stats.getString(statId));
                if (stat != null && statKey != null) {
                    statKeys.put(stat, statKey);
                }
            }
        }

        vanillaEnabled = config.getBoolean("mmoweaponary.vanilla.activado", true);
        vanillaExact.clear();
        vanillaSuffixes.clear();
        ConfigurationSection types = config.getConfigurationSection("mmoweaponary.vanilla.tipos");
        if (types != null) {
            for (String materialName : types.getKeys(false)) {
                String type = types.getString(materialName, "").toUpperCase(Locale.ROOT);
                if (materialName.startsWith("*")) {
                    vanillaSuffixes.add(Map.entry(materialName.substring(1).toUpperCase(Locale.ROOT), type));
                    continue;
                }
                Material material = Material.matchMaterial(materialName);
                if (material == null) {
                    plugin.getLogger().warning("Material desconocido en mmoweaponary.vanilla.tipos: " + materialName);
                } else {
                    vanillaExact.put(material, type);
                }
            }
        }
    }

    private @Nullable NamespacedKey key(@Nullable String namespace, @Nullable String key) {
        if (namespace == null || key == null || key.isBlank()) {
            return null;
        }
        return NamespacedKey.fromString(namespace.toLowerCase(Locale.ROOT) + ":" + key.toLowerCase(Locale.ROOT));
    }

    /** @return la información del arma o {@code null} si el item no es un arma reconocida. */
    public @Nullable WeaponInfo identify(@Nullable ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return null;
        }
        WeaponProvider provider = Bukkit.getServicesManager().load(WeaponProvider.class);
        if (provider != null) {
            try {
                WeaponInfo info = provider.getWeapon(item);
                if (info != null) {
                    return info;
                }
            } catch (RuntimeException e) {
                if (!providerErrorLogged) {
                    providerErrorLogged = true;
                    plugin.getLogger().log(Level.WARNING, "El WeaponProvider de MMOWeaponary lanzó un error", e);
                }
            }
        }
        if (pdcEnabled) {
            WeaponInfo info = readPdc(item);
            if (info != null) {
                return info;
            }
        }
        if (vanillaEnabled) {
            String type = vanillaType(item.getType());
            if (type != null) {
                return WeaponInfo.ofType(type);
            }
        }
        return null;
    }

    /** Comprueba el arma de la mano principal contra la clase y el nivel del jugador. */
    public Check check(Player player, PlayerData data) {
        PlayerClass playerClass = data.currentClass();
        if (playerClass == null) {
            return NO_WEAPON;
        }
        return check(identify(player.getInventory().getItemInMainHand()), playerClass, data.level());
    }

    /** Comprueba un arma contra una clase y un nivel. */
    public Check check(@Nullable WeaponInfo info, PlayerClass playerClass, int level) {
        if (info == null) {
            return NO_WEAPON;
        }
        boolean allowed = info.requiredClass() != null
                ? info.requiredClass() == playerClass
                : plugin.settings().classSettings(playerClass).weapons().contains(info.type());
        if (!allowed) {
            return new Check(Status.WRONG_CLASS, info);
        }
        if (info.requiredLevel() > level) {
            return new Check(Status.LEVEL_TOO_LOW, info);
        }
        return new Check(Status.VALID, info);
    }

    /** Primera clase que admite este tipo de arma (para los mensajes). */
    public @Nullable PlayerClass classFor(WeaponInfo info) {
        if (info.requiredClass() != null) {
            return info.requiredClass();
        }
        for (PlayerClass playerClass : PlayerClass.values()) {
            if (plugin.settings().classSettings(playerClass).weapons().contains(info.type())) {
                return playerClass;
            }
        }
        return null;
    }

    private @Nullable WeaponInfo readPdc(ItemStack item) {
        if (typeKey == null || !item.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(typeKey, PersistentDataType.STRING)) {
            return null;
        }
        String type = pdc.get(typeKey, PersistentDataType.STRING);
        if (type == null || type.isBlank()) {
            return null;
        }
        Map<Stat, Double> stats = new EnumMap<>(Stat.class);
        for (Map.Entry<Stat, NamespacedKey> entry : statKeys.entrySet()) {
            Double value = readNumber(pdc, entry.getValue());
            if (value != null && value != 0) {
                stats.put(entry.getKey(), value);
            }
        }
        PlayerClass requiredClass = null;
        if (classKey != null && pdc.has(classKey, PersistentDataType.STRING)) {
            String classId = pdc.get(classKey, PersistentDataType.STRING);
            requiredClass = classId == null ? null : PlayerClass.fromId(classId);
        }
        Double level = levelKey == null ? null : readNumber(pdc, levelKey);
        return new WeaponInfo(type, stats, requiredClass, level == null ? 0 : level.intValue());
    }

    /** Lee un número sin importar con qué tipo lo guardó MMOWeaponary. */
    private static @Nullable Double readNumber(PersistentDataContainer pdc, NamespacedKey key) {
        if (pdc.has(key, PersistentDataType.DOUBLE)) {
            return pdc.get(key, PersistentDataType.DOUBLE);
        }
        if (pdc.has(key, PersistentDataType.INTEGER)) {
            Integer value = pdc.get(key, PersistentDataType.INTEGER);
            return value == null ? null : value.doubleValue();
        }
        if (pdc.has(key, PersistentDataType.FLOAT)) {
            Float value = pdc.get(key, PersistentDataType.FLOAT);
            return value == null ? null : value.doubleValue();
        }
        if (pdc.has(key, PersistentDataType.LONG)) {
            Long value = pdc.get(key, PersistentDataType.LONG);
            return value == null ? null : value.doubleValue();
        }
        if (pdc.has(key, PersistentDataType.STRING)) {
            try {
                return Double.parseDouble(pdc.get(key, PersistentDataType.STRING));
            } catch (NumberFormatException | NullPointerException e) {
                return null;
            }
        }
        return null;
    }

    private @Nullable String vanillaType(Material material) {
        String exact = vanillaExact.get(material);
        if (exact != null) {
            return exact;
        }
        String name = material.name();
        for (Map.Entry<String, String> entry : vanillaSuffixes) {
            if (name.endsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }
}
