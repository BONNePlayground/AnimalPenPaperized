//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpenpaper.events;


import org.bukkit.Location;
import org.bukkit.entity.Player;

import lv.id.bonne.animalpenpaper.data.AnimalData;


/**
 * This event is called right before entity is released from the animal cage or water animal container.
 */
public class AnimalReleaseEvent extends AbstractAnimalPenItemEvent
{
    /**
     * @param player Player who performed releasing
     * @param location Location where entity will be released
     * @param animalData ItemStack stored data
     * @param isAnimalCage The animal cage (true) or water mob container (false)
     */
    public AnimalReleaseEvent(Player player,
        Location location,
        AnimalData animalData,
        boolean isAnimalCage)
    {
        super(player, location, animalData, isAnimalCage);
    }
}
