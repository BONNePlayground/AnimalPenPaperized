//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpenpaper.events.block;


import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;


/**
 * This event is called right before animal pen/aquarium is constructed in world.
 */
public class AnimalBlockPlaceEvent extends Event implements Cancellable
{
    /**
     * @param player The player who places animal pen/aquarium
     * @param location The location of animal pen/aquarium
     */
    public AnimalBlockPlaceEvent(Player player, Location location, String blockKey)
    {
        this.player = player;
        this.location = location;
        this.blockKey = blockKey;
    }


    @Override
    public boolean isCancelled()
    {
        return this.cancel;
    }


    @Override
    public void setCancelled(boolean cancel)
    {
        this.cancel = cancel;
    }


    @Override
    @NotNull
    public HandlerList getHandlers()
    {
        return HANDLER_LIST;
    }


    @NotNull
    public static HandlerList getHandlerList()
    {
        return HANDLER_LIST;
    }


    public Player player()
    {
        return this.player;
    }


    public String blockKey()
    {
        return this.blockKey;
    }


    public Location location()
    {
        return this.location;
    }


    private final Player player;

    private final Location location;

    private final String blockKey;

    private boolean cancel = false;

    private static final HandlerList HANDLER_LIST = new HandlerList();
}
