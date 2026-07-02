//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpenpaper.events.block;


import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import lv.id.bonne.animalpenpaper.data.AnimalData;


/**
 * This event is triggered when player is attacking animal pen/aquarium.
 */
public class AnimalBlockAttackEvent extends AbstractAnimalPenBlockEvent
{
    /**
     * @param player The player who performed event.
     * @param location The location of animal pen/aquarium
     * @param animalData The animal data
     * @param itemStack The item stack used for attacking
     */
    public AnimalBlockAttackEvent(Player player,
        ItemStack itemStack,
        Location location,
        @Nullable AnimalData animalData,
        String blockKey)
    {
        super(player, location, animalData, blockKey);
        this.itemStack = itemStack;
    }


    public ItemStack itemStack()
    {
        return this.itemStack;
    }


    private final ItemStack itemStack;
}
