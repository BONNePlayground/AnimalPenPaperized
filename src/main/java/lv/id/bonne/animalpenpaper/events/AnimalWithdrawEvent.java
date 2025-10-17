//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpenpaper.events;


import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lv.id.bonne.animalpenpaper.data.AnimalData;


/**
 * This event is called right before animal cage or water container is withdrawn from animal pen or aquarium.
 */
public class AnimalWithdrawEvent extends AbstractAnimalPenItemEvent
{
    /**
     * @param player The player who performed event
     * @param location The location of event
     * @param itemData The data stored inside item
     * @param blockData The data stored inside animal pen/aquarium block
     * @param isAnimalCage The animal cage (true) or water mob container (false)
     */
    public AnimalWithdrawEvent(Player player,
        Location location,
        @Nullable AnimalData itemData,
        @NotNull AnimalData blockData,
        boolean isAnimalCage)
    {
        super(player, location, itemData, isAnimalCage);
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
