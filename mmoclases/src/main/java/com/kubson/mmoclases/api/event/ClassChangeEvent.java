package com.kubson.mmoclases.api.event;

import com.kubson.mmoclases.api.PlayerClass;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Se lanza antes de que un jugador elija o cambie de clase. Cancelable. */
public class ClassChangeEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final @Nullable PlayerClass oldClass;
    private final PlayerClass newClass;
    private boolean cancelled;

    public ClassChangeEvent(Player player, @Nullable PlayerClass oldClass, PlayerClass newClass) {
        super(player);
        this.oldClass = oldClass;
        this.newClass = newClass;
    }

    public @Nullable PlayerClass getOldClass() {
        return oldClass;
    }

    public PlayerClass getNewClass() {
        return newClass;
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
