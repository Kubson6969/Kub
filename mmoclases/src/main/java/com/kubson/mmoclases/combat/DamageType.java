package com.kubson.mmoclases.combat;

import com.kubson.mmoclases.api.Stat;

/** Tipo de daño de una habilidad: decide qué stat lo escala. */
public enum DamageType {
    FISICO(Stat.DANO_FISICO),
    DISTANCIA(Stat.DANO_DISTANCIA),
    MAGICO(Stat.PODER_MAGICO),
    /** Mágico del clérigo; hace daño extra a no-muertos. */
    SAGRADO(Stat.PODER_MAGICO);

    private final Stat scalingStat;

    DamageType(Stat scalingStat) {
        this.scalingStat = scalingStat;
    }

    public Stat scalingStat() {
        return scalingStat;
    }
}
