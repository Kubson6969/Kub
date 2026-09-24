package com.kubson.mmoclases.api;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

/** Recurso que consumen las habilidades de cada clase. */
public enum ResourceType {
    /** Se regenera de forma constante. */
    MANA("Maná", "✦", NamedTextColor.AQUA, true),
    /** Pool pequeño que se regenera muy rápido. */
    ENERGIA("Energía", "⚡", NamedTextColor.YELLOW, true),
    /** Empieza en 0, se gana golpeando y recibiendo daño y decae fuera de combate. */
    FURIA("Furia", "✹", NamedTextColor.RED, false);

    private final String displayName;
    private final String symbol;
    private final TextColor color;
    private final boolean startsFull;

    ResourceType(String displayName, String symbol, TextColor color, boolean startsFull) {
        this.displayName = displayName;
        this.symbol = symbol;
        this.color = color;
        this.startsFull = startsFull;
    }

    public String displayName() {
        return displayName;
    }

    public String symbol() {
        return symbol;
    }

    public TextColor color() {
        return color;
    }

    public boolean startsFull() {
        return startsFull;
    }
}
