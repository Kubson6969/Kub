package com.kubson.mmoclases.api;

import org.jetbrains.annotations.Nullable;

/**
 * Stats derivadas de un jugador. Son también las stats que un arma de
 * MMOWeaponary puede aportar (ver {@link WeaponInfo}).
 */
public enum Stat {
    VIDA_MAXIMA("vida_maxima", "Vida máxima", false),
    RECURSO_MAXIMO("recurso_maximo", "Recurso máximo", false),
    REGEN_RECURSO("regen_recurso", "Regeneración de recurso/s", false),
    DANO_FISICO("dano_fisico", "Daño físico", true),
    DANO_DISTANCIA("dano_distancia", "Daño a distancia", true),
    PODER_MAGICO("poder_magico", "Poder mágico", true),
    PODER_CURACION("poder_curacion", "Poder de curación", true),
    CRITICO_PROB("critico_prob", "Probabilidad de crítico", true),
    CRITICO_DANO("critico_dano", "Daño crítico", true),
    DEFENSA("defensa", "Defensa", false),
    ROBO_VIDA("robo_vida", "Robo de vida", true),
    REDUCCION_ENFRIAMIENTO("reduccion_enfriamiento", "Reducción de enfriamiento", true);

    private final String id;
    private final String displayName;
    private final boolean percent;

    Stat(String id, String displayName, boolean percent) {
        this.id = id;
        this.displayName = displayName;
        this.percent = percent;
    }

    /** Id usado en config.yml y en el PDC de las armas. */
    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public boolean isPercent() {
        return percent;
    }

    public static @Nullable Stat fromId(String id) {
        for (Stat stat : values()) {
            if (stat.id.equalsIgnoreCase(id) || stat.name().equalsIgnoreCase(id)) {
                return stat;
            }
        }
        return null;
    }
}
