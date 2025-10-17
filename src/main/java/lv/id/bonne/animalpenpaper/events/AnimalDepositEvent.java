//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpenpaper.events;


import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import lv.id.bonne.animalpenpaper.data.AnimalData;


/**
 * This event is called right before animal cage or water container is deposited into animal pen or aquarium.
 */
public class AnimalDepositEvent extends AbstractAnimalPenItemEvent
{
    /**
     * @param player The player who performed event
     * @param location The location of event
     * @param itemData The data stored inside item
     * @param blockData The data stored inside animal pen/aquarium block
     * @param isAnimalCage The animal cage (true) or water mob container (false)
     */
    public AnimalDepositEvent(Player player,
        Location location,
        @Nullable AnimalData itemData,
        @Nullable AnimalData blockData,
        boolean isAnimalCage)
    {
        super(player, location, itemData, isAnimalCage);
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
