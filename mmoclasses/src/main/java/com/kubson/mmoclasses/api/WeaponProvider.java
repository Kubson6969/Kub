package com.kubson.mmoclasses.api;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Puente para que MMOWeaponary le diga a MMOClasses qué es cada arma.
 *
 * <p>Desde MMOWeaponary (con MMOClasses como {@code softdepend} y dependencia {@code provided}):
 * <pre>{@code
 * MMOClassesAPI.registerWeaponProvider(this, item -> {
 *     MiArma arma = MMOWeaponary.getArma(item);
 *     if (arma == null) return null;
 *     return new WeaponInfo(arma.getTipo(), Map.of(Stat.PODER_MAGICO, arma.getPoderMagico()), null, arma.getNivel());
 * });
 * }</pre>
 */
@FunctionalInterface
public interface WeaponProvider {

    /** @return la información del arma, o {@code null} si el item no es un arma de MMOWeaponary. */
    @Nullable WeaponInfo getWeapon(ItemStack item);
}
