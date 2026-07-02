//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpenpaper.listeners;


import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
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


    @EventHandler
    public void onEntityLoading(EntitiesLoadEvent event)
    {
        for (Entity entity : event.getEntities())
        {
            if (this.manager.isStructureEntity(entity))
            {
                this.manager.validateStructure(entity);
                AnimalPenPlugin.getInstance().task.startTrackingEntity(entity, true, this.manager);
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
