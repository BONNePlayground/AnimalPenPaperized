//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpenpaper.events;


import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lv.id.bonne.animalpenpaper.data.AnimalData;


public abstract class AbstractAnimalPenItemEvent extends Event implements Cancellable
{
    protected AbstractAnimalPenItemEvent(Location location,
        @Nullable AnimalData animalData,
        boolean isAnimalCage)
    {
        this.location = location;
        this.animalData = animalData;
        this.isAnimalCage = isAnimalCage;
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


    public Location getLocation()
    {
        return this.location;
    }


    @Nullable
    private AnimalData getAnimalData()
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


    public boolean isAnimalCage()
    {
        return this.isAnimalCage;
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


    private final Location location;

    @Nullable
    private final AnimalData animalData;

    private final boolean isAnimalCage;

    private boolean cancel = false;

    private static final HandlerList HANDLER_LIST = new HandlerList();
}
