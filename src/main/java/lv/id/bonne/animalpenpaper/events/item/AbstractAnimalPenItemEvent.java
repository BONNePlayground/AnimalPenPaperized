//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpenpaper.events.item;


import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lv.id.bonne.animalpenpaper.data.AnimalData;


public abstract class AbstractAnimalPenItemEvent extends Event implements Cancellable
{
    /**
     * @param player The player who performed event
     * @param location The location of event
     * @param animalData The data stored inside item
     * @param itemKey The animal cage (true) or water mob container (false)
     */
    protected AbstractAnimalPenItemEvent(Player player,
        Location location,
        @Nullable AnimalData animalData,
        String itemKey)
    {
        this.player = player;
        this.location = location;
        this.animalData = animalData;
        this.itemKey = itemKey;
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


    public Location location()
    {
        return this.location;
    }


    @Nullable
    public AnimalData animalData()
    {
        return this.animalData;
    }


    @Nullable
    public EntityType entityType()
    {
        return this.animalData == null ? null : this.animalData.entityType();
    }


    public long animalCount()
    {
        return this.animalData == null ? 0 : this.animalData.entityCount();
    }


    public String itemKey()
    {
        return this.itemKey;
    }


    public Player player()
    {
        return this.player;
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------

    private final Player player;

    private final Location location;

    @Nullable
    private final AnimalData animalData;

    private final String itemKey;

    private boolean cancel = false;

    private static final HandlerList HANDLER_LIST = new HandlerList();
}
