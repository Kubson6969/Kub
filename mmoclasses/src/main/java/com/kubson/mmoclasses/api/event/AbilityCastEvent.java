package com.kubson.mmoclasses.api.event;

import com.kubson.mmoclasses.api.PlayerClass;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Se lanza justo antes de ejecutar una habilidad (ya comprobados nivel, enfriamiento y recurso).
 * Se puede cancelar o cambiar el coste; útil para que MMOWeaponary añada efectos por arma.
 */
public class AbilityCastEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final PlayerClass playerClass;
    private final String abilityId;
    private double cost;
    private boolean cancelled;

    public AbilityCastEvent(Player player, PlayerClass playerClass, String abilityId, double cost) {
        super(player);
        this.playerClass = playerClass;
        this.abilityId = abilityId;
        this.cost = cost;
    }

    public PlayerClass getPlayerClass() {
        return playerClass;
    }

    public String getAbilityId() {
        return abilityId;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = Math.max(0, cost);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
