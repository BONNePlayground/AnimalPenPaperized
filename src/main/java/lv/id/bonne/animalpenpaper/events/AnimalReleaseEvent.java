//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpenpaper.events;


import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import lv.id.bonne.animalpenpaper.data.AnimalData;


/**
 * This event is called right before entity is released from the animal cage or water animal container.
 */
public class AnimalReleaseEvent extends AbstractAnimalPenItemEvent
{
    public AnimalReleaseEvent(Location location,
        AnimalData animalData,
        boolean isAnimalCage)
    {
        super(location, animalData, isAnimalCage);
    }
}
