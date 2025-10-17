//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpenpaper.events.item;


import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import lv.id.bonne.animalpenpaper.data.AnimalData;


/**
 * This event is called right before entity is added to the animal cage or water animal container.
 */
public class AnimalCatchEvent extends AbstractAnimalPenItemEvent
{
    /**
     * @param player Player who performed catching
     * @param entity Entity that was caught
     * @param animalData ItemStack stored data
     * @param isAnimalCage The animal cage (true) or water mob container (false)
     */
    public AnimalCatchEvent(Player player,
        Entity entity,
        AnimalData animalData,
        boolean isAnimalCage)
    {
        super(player, entity.getLocation(), animalData, isAnimalCage);
        this.entity = entity;
    }


    public Entity entity()
    {
        return this.entity;
    }


    private final Entity entity;
}
