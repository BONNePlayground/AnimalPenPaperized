//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpenpaper.events;


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
     * @param isAnimalPen The indication if block is animal pen (true) or aquarium (false)
     */
    public AnimalBlockPlaceEvent(Player player, Location location, boolean isAnimalPen)
    {
        this.player = player;
        this.location = location;
        this.isAnimalPen = isAnimalPen;
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
    public @NotNull HandlerList getHandlers()
    {
        return HANDLER_LIST;
    }


    public Player getPlayer()
    {
        return player;
    }


    public Location getLocation()
    {
        return location;
    }


    public boolean isAnimalPen()
    {
        return isAnimalPen;
    }


    private final Player player;

    private final Location location;

    private final boolean isAnimalPen;

    private boolean cancel = false;

    private static final HandlerList HANDLER_LIST = new HandlerList();
}
