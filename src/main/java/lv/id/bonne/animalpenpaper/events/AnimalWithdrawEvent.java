//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpenpaper.events;


import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lv.id.bonne.animalpenpaper.data.AnimalData;


/**
 * This event is called right before animal cage or water container is withdrawn from animal pen or aquarium.
 */
public class AnimalWithdrawEvent extends AbstractAnimalPenItemEvent
{
    public AnimalWithdrawEvent(Location location,
        @Nullable AnimalData itemData,
        @NotNull AnimalData blockData,
        boolean isAnimalCage)
    {
        super(location, itemData, isAnimalCage);
        this.blockData = blockData;
    }


    @NotNull
    public AnimalData getBlockData()
    {
        return this.blockData;
    }


    @NotNull
    private final AnimalData blockData;
}
