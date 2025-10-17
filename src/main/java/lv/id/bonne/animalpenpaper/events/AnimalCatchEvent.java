//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpenpaper.events;


import org.bukkit.entity.Entity;

import lv.id.bonne.animalpenpaper.data.AnimalData;


/**
 * This event is called right before entity is added to the animal cage or water animal container.
 */
public class AnimalCatchEvent extends AbstractAnimalPenItemEvent
{
    public AnimalCatchEvent(Entity entity,
        AnimalData animalData,
        boolean isAnimalCage)
    {
        super(entity.getLocation(), animalData, isAnimalCage);
        this.entity = entity;
    }


    public Entity getEntity()
    {
        return this.entity;
    }


    private final Entity entity;
}
