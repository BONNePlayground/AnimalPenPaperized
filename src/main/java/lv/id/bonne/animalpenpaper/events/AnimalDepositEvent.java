//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpenpaper.events;


import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import lv.id.bonne.animalpenpaper.data.AnimalData;


/**
 * This event is called right before animal cage or water container is deposited into animal pen or aquarium.
 */
public class AnimalDepositEvent extends AbstractAnimalPenItemEvent
{
    public AnimalDepositEvent(Location location,
        @Nullable AnimalData itemData,
        @Nullable AnimalData blockData,
        boolean isAnimalCage)
    {
        super(location, itemData, isAnimalCage);
        this.blockData = blockData;
    }


    @Nullable
    public AnimalData getBlockData()
    {
        return this.blockData;
    }


    @Nullable
    private final AnimalData blockData;
}
