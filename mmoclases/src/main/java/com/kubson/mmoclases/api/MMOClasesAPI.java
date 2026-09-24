package com.kubson.mmoclases.api;

import com.kubson.mmoclases.MMOClases;
import com.kubson.mmoclases.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * API pública para otros plugins (pensada para MMOWeaponary).
 * Añade MMOClases como {@code softdepend} y comprueba que esté activo antes de usarla.
 */
public final class MMOClasesAPI {

    private MMOClasesAPI() {
    }

    /** Registra el proveedor de armas de tu plugin. Llamar en onEnable. */
    public static void registerWeaponProvider(Plugin owner, WeaponProvider provider) {
        Bukkit.getServicesManager().register(WeaponProvider.class, provider, owner, ServicePriority.Normal);
    }

    public static @Nullable PlayerClass getPlayerClass(Player player) {
        return data(player).currentClass();
    }

    /** Nivel en la clase actual (0 si no tiene clase). */
    public static int getLevel(Player player) {
        return data(player).level();
    }

    public static double getStat(Player player, Stat stat) {
        return MMOClases.get().stats().compute(player, data(player)).get(stat);
    }

    public static Map<Stat, Double> getStats(Player player) {
        return MMOClases.get().stats().compute(player, data(player)).asMap();
    }

    public static double getResource(Player player) {
        return data(player).resource();
    }

    public static void setResource(Player player, double value) {
        data(player).setResource(value);
    }

    public static void giveExperience(Player player, double amount) {
        MMOClases.get().progression().giveXp(player, data(player), amount);
    }

    /** ¿Puede el jugador usar este item como arma de su clase ahora mismo? */
    public static boolean canUseWeapon(Player player, ItemStack item) {
        MMOClases plugin = MMOClases.get();
        PlayerData data = data(player);
        PlayerClass playerClass = data.currentClass();
        return playerClass != null
                && plugin.weapons().check(plugin.weapons().identify(item), playerClass, data.level()).valid();
    }

    /** Lanza la habilidad 1-4 como si el jugador pulsara la tecla. */
    public static void castAbility(Player player, int number) {
        MMOClases.get().abilities().cast(player, number - 1);
    }

    private static PlayerData data(Player player) {
        return MMOClases.get().data().get(player);
    }
}
