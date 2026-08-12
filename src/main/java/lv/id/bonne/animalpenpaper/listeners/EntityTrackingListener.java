//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpenpaper.listeners;


import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;

import lv.id.bonne.animalpenpaper.AnimalPenPlugin;
import lv.id.bonne.animalpenpaper.managers.container.AbstractContainerManager;


public class EntityTrackingListener implements Listener
{
    private final AbstractContainerManager manager;


    public EntityTrackingListener(AbstractContainerManager manager)
    {
        this.manager = manager;
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityLoading(EntitiesLoadEvent event)
    {
        for (Entity entity : event.getEntities())
        {
            if (this.manager.isStructureEntity(entity))
            {
                Block block = entity.getLocation().add(0, -0.5, 0).getBlock();

                if (this.manager.isStructureBlock(block))
                {
                    this.manager.validateStructure(entity);
                    AnimalPenPlugin.getInstance().task.startTrackingEntity(entity, true, this.manager);
                }
                else
                {
                    entity.remove();
                    this.manager.clearBlockData(block, false);
                }
            }
        }
    }


    @EventHandler
    public void onEntityUnloading(EntitiesUnloadEvent event)
    {
        for (Entity entity : event.getEntities())
        {
            if (this.manager.isStructureEntity(entity))
            {
                AnimalPenPlugin.getInstance().task.stopTrackingEntity(entity, this.manager);
            }
        }
    }
}
