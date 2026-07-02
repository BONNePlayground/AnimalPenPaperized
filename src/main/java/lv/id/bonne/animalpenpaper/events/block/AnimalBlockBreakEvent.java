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
import org.jetbrains.annotations.Nullable;

import lv.id.bonne.animalpenpaper.data.AnimalData;


/**
 * This event is called right before animal pen/aquarium is broken in world.
 */
public class AnimalBlockBreakEvent extends Event implements Cancellable
{
    /**
     * @param player The player who places animal pen/aquarium
     * @param location The location of animal pen/aquarium
     * @param animalData The data stored in animal pen/aquarium
     */
    public AnimalBlockBreakEvent(Player player,
        Location location,
        @Nullable AnimalData animalData,
        String blockKey)
    {
        this.player = player;
        this.location = location;
        this.animalData = animalData;
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


    public Location location()
    {
        return this.location;
    }


    public String blockKey()
    {
        return this.blockKey;
    }


    @Nullable
    public AnimalData animalData()
    {
        return this.animalData;
    }


    private final Player player;

    private final Location location;

    private final String blockKey;

    @Nullable
    private final AnimalData animalData;

    private boolean cancel = false;

    private static final HandlerList HANDLER_LIST = new HandlerList();
}
