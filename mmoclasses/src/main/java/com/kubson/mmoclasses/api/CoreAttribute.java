package com.kubson.mmoclasses.api;

import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

/** Atributos principales en los que el jugador reparte sus puntos al subir de nivel. */
public enum CoreAttribute {
    FUERZA("fuerza", "Fuerza", Material.IRON_SWORD),
    DESTREZA("destreza", "Destreza", Material.FEATHER),
    INTELIGENCIA("inteligencia", "Inteligencia", Material.LAPIS_LAZULI),
    VITALIDAD("vitalidad", "Vitalidad", Material.GOLDEN_APPLE),
    ESPIRITU("espiritu", "Espíritu", Material.GHAST_TEAR);

    private final String id;
    private final String displayName;
    private final Material icon;

    CoreAttribute(String id, String displayName, Material icon) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public Material icon() {
        return icon;
    }

    public static @Nullable CoreAttribute fromId(String id) {
        for (CoreAttribute attribute : values()) {
            if (attribute.id.equalsIgnoreCase(id) || attribute.name().equalsIgnoreCase(id)) {
                return attribute;
            }
        }
        return null;
    }
}
