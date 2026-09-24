package com.kubson.mmoclases.api.event;

import com.kubson.mmoclases.api.PlayerClass;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/** Se lanza después de que un jugador sube de nivel en una clase. */
public class ClassLevelUpEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final PlayerClass playerClass;
    private final int oldLevel;
    private final int newLevel;

    public ClassLevelUpEvent(Player player, PlayerClass playerClass, int oldLevel, int newLevel) {
        super(player);
        this.playerClass = playerClass;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    public PlayerClass getPlayerClass() {
        return playerClass;
    }

    public int getOldLevel() {
        return oldLevel;
    }

    public int getNewLevel() {
        return newLevel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
