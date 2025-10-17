//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpenpaper.events.block;


import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import lv.id.bonne.animalpenpaper.data.AnimalData;


/**
 * This event is triggered when player is interacting with animal pen/aquarium.
 */
public class AnimalBlockInteractEvent extends AbstractAnimalPenBlockEvent
{
    /**
     * @param player The player who performed event.
     * @param location The location of animal pen/aquarium
     * @param animalData The animal data
     * @param isAnimalPen The indication if block is animal pen (true) or aquarium (false)
     * @param itemStack The item stack used for interaction
     * @param interactionHand The hand used for interaction
     */
    public AnimalBlockInteractEvent(Player player,
        ItemStack itemStack,
        EquipmentSlot interactionHand,
        Location location,
        @Nullable AnimalData animalData,
        boolean isAnimalPen)
    {
        super(player, location, animalData, isAnimalPen);
        this.itemStack = itemStack;
        this.interactionHand = interactionHand;
    }


    public ItemStack getItemStack()
    {
        return this.itemStack;
    }


    public EquipmentSlot getInteractionHand()
    {
        return this.interactionHand;
    }

    private final ItemStack itemStack;

    private final EquipmentSlot interactionHand;
}
