package com.kubson.mmoclasses.ability;

import org.bukkit.Material;

import java.util.List;

/**
 * Una habilidad de clase. Nivel, coste y enfriamiento se leen de
 * {@code habilidades.<id>} en config.yml; los valores de aquí son los de por defecto.
 */
public abstract class Ability {

    private final String id;
    private final String name;
    private final Material icon;
    private final List<String> description;
    private final int defaultLevel;
    private final double defaultCost;
    private final double defaultCooldown;
    private final boolean ultimate;

    protected Ability(String id, String name, Material icon, List<String> description,
                      int defaultLevel, double defaultCost, double defaultCooldown, boolean ultimate) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.description = description;
        this.defaultLevel = defaultLevel;
        this.defaultCost = defaultCost;
        this.defaultCooldown = defaultCooldown;
        this.ultimate = ultimate;
    }

    /**
     * Ejecuta la habilidad. Nivel, enfriamiento y recurso ya están comprobados.
     * @return false si no se pudo lanzar (no se cobra ni entra en enfriamiento).
     */
    public abstract boolean cast(AbilityContext ctx);

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Material icon() {
        return icon;
    }

    public List<String> description() {
        return description;
    }

    public int defaultLevel() {
        return defaultLevel;
    }

    public double defaultCost() {
        return defaultCost;
    }

    public double defaultCooldown() {
        return defaultCooldown;
    }

    public boolean isUltimate() {
        return ultimate;
    }
}
