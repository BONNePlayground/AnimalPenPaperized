//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpenpaper.listeners;


import org.bukkit.NamespacedKey;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;
import org.bukkit.persistence.PersistentDataType;

import lv.id.bonne.animalpenpaper.AnimalPenPlugin;
import lv.id.bonne.animalpenpaper.data.BlockData;
import lv.id.bonne.animalpenpaper.data.BlockDataType;
import lv.id.bonne.animalpenpaper.managers.Helper;


public class GeneralDisplayListener implements Listener
{
    @EventHandler
    public void onEntityLoading(EntitiesLoadEvent event)
    {
        for (Entity entity : event.getEntities())
        {
            if (entity instanceof ItemDisplay display)
            {
                if (display.getPersistentDataContainer().has(Helper.DECORATION_ENTITY_KEY,
                    PersistentDataType.STRING))
                {
                    String key = display.getPersistentDataContainer().get(Helper.DECORATION_ENTITY_KEY,
                        PersistentDataType.STRING);
                    NamespacedKey penKey = new NamespacedKey(AnimalPenPlugin.getInstance(), key);

                    // I need this code to generate 0 for empty pens/aquariums
                    BlockData blockData = display.getWorld().getPersistentDataContainer().
                        get(penKey, BlockDataType.INSTANCE);

                    if (blockData != null && blockData.entity == null)
                    {
                        Helper.updateCountTextEntity(display.getLocation().getBlock(),
                            blockData,
                            0,
                            penKey);
                    }
                }
            }
        }
    }


    @EventHandler
    public void onPlayerCrouching(PlayerToggleSneakEvent event)
    {
        if (!AnimalPenPlugin.configuration().isShowCooldownsOnlyOnShift())
        {
            return;
        }

        if (!event.isSneaking())
        {
            AnimalPenPlugin.getInstance().task.hideEntities(event.getPlayer());
        }
        else
        {
            AnimalPenPlugin.getInstance().task.showEntities(event.getPlayer());
        }
    }


    @EventHandler
    public void onEntityUnloading(EntitiesUnloadEvent event)
    {
        for (Entity entity : event.getEntities())
        {
            if (entity instanceof Display display &&
                display.getPersistentDataContainer().has(Helper.COUNTER_ENTITY_KEY,
                    PersistentDataType.STRING))
            {
                // Remove entity on unloading it. Prevents chunk loading issues.
                display.remove();
            }
        }
    }
}
