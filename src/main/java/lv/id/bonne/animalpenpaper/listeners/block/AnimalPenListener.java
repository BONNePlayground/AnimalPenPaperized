//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpenpaper.listeners.block;


import org.bukkit.NamespacedKey;

import io.papermc.paper.datacomponent.item.CustomModelData;
import lv.id.bonne.animalpenpaper.AnimalPenPlugin;
import lv.id.bonne.animalpenpaper.managers.container.AbstractContainerManager;
import lv.id.bonne.animalpenpaper.managers.container.AnimalPenManager;
import net.kyori.adventure.text.Component;


/**
 * This listener manages animal pen interactions.
 */
public class AnimalPenListener extends AbstractStructureListener
{

// ---------------------------------------------------------------------
// Section: Abstract configuration
// ---------------------------------------------------------------------


    @Override
    protected AbstractContainerManager getManager()
    {
        return AnimalPenManager.INSTANCE;
    }


    @Override
    protected NamespacedKey getAttackTag()
    {
        return CAN_ATTACK_PEN;
    }


// ---------------------------------------------------------------------
// Section: Lifecycle hooks
// ---------------------------------------------------------------------


    /**
     * The animal pen has multiple variants (oak, etc.), so the crafted display name is derived from
     * the variant model string rather than a single fixed name.
     */
    @Override
    protected Component getCraftedDisplayName(CustomModelData data)
    {
        if (data.strings().size() >= 2 && data.strings().get(1).startsWith("animal_pen:"))
        {
            return AnimalPenPlugin.translations().
                getTranslatable("item.animal_pen." + data.strings().get(1).split(":")[1]);
        }

        return AnimalPenPlugin.translations().getTranslatable("item.animal_pen.animal_pen_oak");
    }


// ---------------------------------------------------------------------
// Section: Constants
// ---------------------------------------------------------------------


    private static final NamespacedKey CAN_ATTACK_PEN =
        new NamespacedKey("animal_pen", "can_attack_pen");
}