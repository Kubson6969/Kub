package com.kubson.mmoclases.api;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Las clases jugables. Los números de balance (stats base, armas permitidas...)
 * viven en config.yml; aquí solo está la identidad de cada clase.
 */
public enum PlayerClass {
    MAGO("mago", "Mago", TextColor.color(0x9B6BFF), Material.BLAZE_ROD, ResourceType.MANA,
            "Daño mágico a distancia y control de masas",
            List.of(CoreAttribute.INTELIGENCIA, CoreAttribute.ESPIRITU),
            List.of("bola_fuego", "parpadeo", "nova_escarcha", "meteoro"),
            "Sobrecarga Arcana", "Cada 4º hechizo no cuesta maná."),
    ARQUERO("arquero", "Arquero", TextColor.color(0x55C04A), Material.BOW, ResourceType.ENERGIA,
            "Daño físico a distancia y movilidad",
            List.of(CoreAttribute.DESTREZA, CoreAttribute.VITALIDAD),
            List.of("disparo_triple", "salto_evasivo", "flecha_explosiva", "lluvia_flechas"),
            "Ojo de Halcón", "Tus flechas hacen más daño cuanto más lejos está el objetivo."),
    GUERRERO("guerrero", "Guerrero", TextColor.color(0xE0533D), Material.IRON_SWORD, ResourceType.FURIA,
            "Tanque cuerpo a cuerpo: aguanta, provoca y protege",
            List.of(CoreAttribute.FUERZA, CoreAttribute.VITALIDAD),
            List.of("tajo_giratorio", "carga", "grito_guerra", "golpe_titan"),
            "Voluntad de Hierro", "Con poca vida recibes menos daño."),
    CLERIGO("clerigo", "Clérigo", TextColor.color(0xF2D15C), Material.TOTEM_OF_UNDYING, ResourceType.MANA,
            "Sanación, escudos y daño sagrado",
            List.of(CoreAttribute.ESPIRITU, CoreAttribute.INTELIGENCIA),
            List.of("luz_sagrada", "circulo_sanacion", "escudo_divino", "juicio_celestial"),
            "Luz Purificadora", "Tu daño sagrado es más fuerte contra no-muertos.");

    private final String id;
    private final String displayName;
    private final TextColor color;
    private final Material icon;
    private final ResourceType resource;
    private final String role;
    private final List<CoreAttribute> mainAttributes;
    private final List<String> abilityIds;
    private final String passiveName;
    private final String passiveDescription;

    PlayerClass(String id, String displayName, TextColor color, Material icon, ResourceType resource,
                String role, List<CoreAttribute> mainAttributes, List<String> abilityIds,
                String passiveName, String passiveDescription) {
        this.id = id;
        this.displayName = displayName;
        this.color = color;
        this.icon = icon;
        this.resource = resource;
        this.role = role;
        this.mainAttributes = mainAttributes;
        this.abilityIds = abilityIds;
        this.passiveName = passiveName;
        this.passiveDescription = passiveDescription;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public TextColor color() {
        return color;
    }

    public Material icon() {
        return icon;
    }

    public ResourceType resource() {
        return resource;
    }

    public String role() {
        return role;
    }

    public List<CoreAttribute> mainAttributes() {
        return mainAttributes;
    }

    /** Ids de habilidad en el orden de las teclas 1-4. */
    public List<String> abilityIds() {
        return abilityIds;
    }

    public String passiveName() {
        return passiveName;
    }

    public String passiveDescription() {
        return passiveDescription;
    }

    public static @Nullable PlayerClass fromId(String id) {
        for (PlayerClass playerClass : values()) {
            if (playerClass.id.equalsIgnoreCase(id) || playerClass.name().equalsIgnoreCase(id)) {
                return playerClass;
            }
        }
        return null;
    }
}
