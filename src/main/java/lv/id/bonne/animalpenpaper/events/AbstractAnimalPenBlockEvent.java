//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpenpaper.events;


import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lv.id.bonne.animalpenpaper.data.AnimalData;


public abstract class AbstractAnimalPenBlockEvent extends Event implements Cancellable
{
    /**
     * @param player The player who performed event.
     * @param location The location of animal pen/aquarium
     * @param animalData The animal data
     * @param isAnimalPen The indication if block is animal pen (true) or aquarium (false)
     */
    protected AbstractAnimalPenBlockEvent(Player player,
        Location location,
        @Nullable AnimalData animalData,
        boolean isAnimalPen)
    {
        this.player = player;
        this.location = location;
        this.animalData = animalData;
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


    public Location getLocation()
    {
        return this.location;
    }


    @Nullable
    public AnimalData getAnimalData()
    {
        return this.animalData;
    }


    @Nullable
    public EntityType getEntityType()
    {
        return this.animalData == null ? null : this.animalData.entityType();
    }


    public long getAnimalCount()
    {
        return this.animalData == null ? 0 : this.animalData.entityCount();
    }


    public boolean isAnimalPen()
    {
        return this.isAnimalPen;
    }


    public Player player()
    {
        return this.player;
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


    private final Location location;

    @Nullable
    private final AnimalData animalData;

    private final Player player;

    private final boolean isAnimalPen;

    private boolean cancel = false;

    private static final HandlerList HANDLER_LIST = new HandlerList();
}