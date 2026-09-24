package com.kubson.mmoclases.stats;

import com.kubson.mmoclases.api.Stat;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/** Foto de las stats de un jugador en un momento dado. */
public final class PlayerStats {

    public static final PlayerStats EMPTY = new PlayerStats(new EnumMap<>(Stat.class));

    private final Map<Stat, Double> values;

    PlayerStats(Map<Stat, Double> values) {
        this.values = values;
    }

    public double get(Stat stat) {
        return values.getOrDefault(stat, 0.0);
    }

    /** 1 + stat/100, para stats porcentuales como dano_fisico o poder_magico. */
    public double multiplier(Stat stat) {
        return 1 + get(stat) / 100.0;
    }

    public Map<Stat, Double> asMap() {
        return Collections.unmodifiableMap(values);
    }
}
