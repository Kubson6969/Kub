package com.kubson.mmoclases.ability;

import com.kubson.mmoclases.MMOClases;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

/** Marcas que llevan las flechas lanzadas por habilidades del arquero. */
public final class AbilityArrows {

    private final NamespacedKey damageKey;
    private final NamespacedKey explosiveRadiusKey;
    private final NamespacedKey temporaryKey;
    private final NamespacedKey explodedKey;

    public AbilityArrows(MMOClases plugin) {
        this.damageKey = new NamespacedKey(plugin, "dano_habilidad");
        this.explosiveRadiusKey = new NamespacedKey(plugin, "radio_explosion");
        this.temporaryKey = new NamespacedKey(plugin, "flecha_temporal");
        this.explodedKey = new NamespacedKey(plugin, "flecha_explotada");
    }

    /** Marca una flecha de habilidad: daño base (se escala con dano_distancia al impactar). */
    public void tag(AbstractArrow arrow, double baseDamage) {
        arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
        PersistentDataContainer pdc = arrow.getPersistentDataContainer();
        pdc.set(damageKey, PersistentDataType.DOUBLE, baseDamage);
        pdc.set(temporaryKey, PersistentDataType.BYTE, (byte) 1);
    }

    public void tagExplosive(AbstractArrow arrow, double baseDamage, double radius) {
        tag(arrow, baseDamage);
        arrow.getPersistentDataContainer().set(explosiveRadiusKey, PersistentDataType.DOUBLE, radius);
    }

    public @Nullable Double baseDamage(Entity entity) {
        return entity.getPersistentDataContainer().get(damageKey, PersistentDataType.DOUBLE);
    }

    public @Nullable Double explosiveRadius(Entity entity) {
        return entity.getPersistentDataContainer().get(explosiveRadiusKey, PersistentDataType.DOUBLE);
    }

    /**
     * Marca la flecha como ya explotada (una flecha que rebota no explota dos veces).
     * @return false si ya había explotado
     */
    public boolean markExploded(Entity entity) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        if (pdc.has(explodedKey, PersistentDataType.BYTE)) {
            return false;
        }
        pdc.set(explodedKey, PersistentDataType.BYTE, (byte) 1);
        return true;
    }

    public boolean isTemporary(Entity entity) {
        return entity.getPersistentDataContainer().has(temporaryKey, PersistentDataType.BYTE);
    }
}
