package com.kubson.mmoclasses.data;

import com.kubson.mmoclasses.api.PlayerClass;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Datos de clase de un jugador. Los campos "runtime" no se guardan en disco. */
public final class PlayerData {

    private final UUID uuid;
    private @Nullable PlayerClass currentClass;
    private final Map<PlayerClass, ClassProgress> progress = new EnumMap<>(PlayerClass.class);

    // --- runtime ---
    private double resource;
    private final Map<String, Long> cooldowns = new HashMap<>();
    private boolean castMode;
    private int spellCounter;
    private long lastCombatMillis;
    private long fallImmuneUntil;
    private double appliedHealthBonus = Double.NaN;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID uuid() {
        return uuid;
    }

    public @Nullable PlayerClass currentClass() {
        return currentClass;
    }

    public void setCurrentClass(@Nullable PlayerClass currentClass) {
        this.currentClass = currentClass;
    }

    public boolean hasClass() {
        return currentClass != null;
    }

    public ClassProgress progress(PlayerClass playerClass) {
        return progress.computeIfAbsent(playerClass, ignored -> new ClassProgress());
    }

    public @Nullable ClassProgress existingProgress(PlayerClass playerClass) {
        return progress.get(playerClass);
    }

    /** Progreso de la clase actual. Solo llamar si {@link #hasClass()}. */
    public ClassProgress current() {
        if (currentClass == null) {
            throw new IllegalStateException("El jugador no tiene clase");
        }
        return progress(currentClass);
    }

    public int level() {
        return currentClass == null ? 0 : current().level();
    }

    public Map<PlayerClass, ClassProgress> allProgress() {
        return progress;
    }

    public void clearProgress() {
        progress.clear();
        currentClass = null;
    }

    public double resource() {
        return resource;
    }

    public void setResource(double resource) {
        this.resource = Math.max(0, resource);
    }

    public long cooldownEnd(String abilityId) {
        return cooldowns.getOrDefault(abilityId, 0L);
    }

    public void setCooldownEnd(String abilityId, long endMillis) {
        cooldowns.put(abilityId, endMillis);
    }

    public void clearCooldowns() {
        cooldowns.clear();
    }

    public boolean castMode() {
        return castMode;
    }

    public void setCastMode(boolean castMode) {
        this.castMode = castMode;
    }

    public int spellCounter() {
        return spellCounter;
    }

    public void setSpellCounter(int spellCounter) {
        this.spellCounter = spellCounter;
    }

    public long lastCombatMillis() {
        return lastCombatMillis;
    }

    public void markCombat() {
        this.lastCombatMillis = System.currentTimeMillis();
    }

    public boolean isFallImmune() {
        return System.currentTimeMillis() < fallImmuneUntil;
    }

    public void setFallImmuneFor(long millis) {
        this.fallImmuneUntil = System.currentTimeMillis() + millis;
    }

    public double appliedHealthBonus() {
        return appliedHealthBonus;
    }

    public void setAppliedHealthBonus(double appliedHealthBonus) {
        this.appliedHealthBonus = appliedHealthBonus;
    }
}
