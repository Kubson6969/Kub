package com.kubson.mmoclases.data;

import com.kubson.mmoclases.api.CoreAttribute;

import java.util.EnumMap;
import java.util.Map;

/** Progreso de un jugador en una clase concreta (cada clase guarda su propio nivel). */
public final class ClassProgress {

    private int level = 1;
    private double xp;
    private int unspentPoints;
    private final Map<CoreAttribute, Integer> attributes = new EnumMap<>(CoreAttribute.class);

    public int level() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, level);
    }

    public double xp() {
        return xp;
    }

    public void setXp(double xp) {
        this.xp = Math.max(0, xp);
    }

    public int unspentPoints() {
        return unspentPoints;
    }

    public void setUnspentPoints(int unspentPoints) {
        this.unspentPoints = Math.max(0, unspentPoints);
    }

    public int attribute(CoreAttribute attribute) {
        return attributes.getOrDefault(attribute, 0);
    }

    public void setAttribute(CoreAttribute attribute, int value) {
        attributes.put(attribute, Math.max(0, value));
    }

    public int spentPoints() {
        int total = 0;
        for (int value : attributes.values()) {
            total += value;
        }
        return total;
    }

    /** Devuelve todos los puntos gastados a la reserva. */
    public void refundAttributes() {
        unspentPoints += spentPoints();
        attributes.clear();
    }
}
