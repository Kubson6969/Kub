package com.kubson.mmoclases.api;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;

/**
 * Lo que MMOClases necesita saber de un arma.
 *
 * @param type          tipo de arma en mayúsculas (BACULO, ARCO, ESPADA...). Se compara con
 *                      la lista {@code clases.<clase>.armas} de config.yml.
 * @param stats         stats que el arma suma al jugador mientras la lleva en la mano principal.
 * @param requiredClass clase que puede usarla, o {@code null} si vale cualquier clase que admita el tipo.
 * @param requiredLevel nivel de clase mínimo (0 o 1 = sin requisito).
 */
public record WeaponInfo(String type, Map<Stat, Double> stats, @Nullable PlayerClass requiredClass, int requiredLevel) {

    public WeaponInfo {
        type = type.toUpperCase(Locale.ROOT);
        stats = Map.copyOf(stats);
    }

    public static WeaponInfo ofType(String type) {
        return new WeaponInfo(type, Map.of(), null, 0);
    }
}
