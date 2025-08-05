package lv.id.bonne.animalpenpaper.listeners;


import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import lv.id.bonne.animalpenpaper.managers.AnimalPenManager;
import lv.id.bonne.animalpenpaper.AnimalPenPlugin;
import lv.id.bonne.animalpenpaper.data.AnimalData;
import lv.id.bonne.animalpenpaper.util.StyleUtil;
import net.kyori.adventure.text.Component;


/**
 * This listener manages animal cage interactions.
 */
public class AnimalCageListener implements Listener
{
    /**
     * This listener checks if player can catch clicked entity with animal cage.
     */
    @EventHandler
    public void onEntityCatch(PlayerInteractEntityEvent event)
    {
        Player player = event.getPlayer();

        if (event.getPlayer().isSneaking())
        {
            return;
        }

        Entity entity = event.getRightClicked();
        ItemStack item = player.getInventory().getItem(event.getHand());

        if (!AnimalPenManager.isAnimalCage(item))
        {
            return;
        }

        event.setCancelled(true);

        if (!(entity instanceof Animals animal))
        {
            player.sendMessage(Component.text("This is not a valid animal.").
                style(StyleUtil.RED_COLOR));
            return;
        }

        if (animal.isDead() || !animal.isAdult())
        {
            player.sendMessage(Component.text("Cannot capture dead or baby animals.").
                style(StyleUtil.RED_COLOR));
            return;
        }

        if (entity instanceof Tameable tameable && tameable.isTamed() ||
            entity instanceof AbstractHorse horse && horse.isTamed())
        {
            player.sendMessage(Component.text("Cannot capture tamed animals.").
                style(StyleUtil.RED_COLOR));
            return;
        }

        // Check blocked types
        if (AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().isBlocked(animal.getType()))
        {
            player.sendMessage(Component.text("This animal is blocked from being captured.").
                style(StyleUtil.RED_COLOR));
            return;
        }

        EntityType entityType = animal.getType();
        AnimalData storedData = AnimalPenManager.getAnimalData(item);

        // Check if item already contains another type
        if (storedData != null && storedData.entityType != entityType)
        {
            player.sendMessage(Component.text("This cage contains a different animal.").
                style(StyleUtil.RED_COLOR));
            return;
        }
        else
        {
            storedData = new AnimalData(entityType, 0);
        }

        long maxAmount = AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getMaximalAnimalCount();

        if (maxAmount > 0 && storedData.entityCount + 1 > maxAmount)
        {
            player.sendMessage(Component.text("Cage is full.").
                style(StyleUtil.RED_COLOR));
            return;
        }

        AnimalPenManager.addAnimal(item, entityType, 1);

        entity.remove();
        player.swingMainHand();
        player.sendMessage(Component.text("Captured " + entity.getType().name()).
            style(StyleUtil.GREEN_COLOR));
    }


    /**
     * This listener checks if player can release entity from animal cage.
     */
    @EventHandler
    public void onEntityRelease(PlayerInteractEvent event)
    {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
        {
            return;
        }

        if (!event.getPlayer().isSneaking())
        {
            return;
        }

        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getHand());

        if (!AnimalPenManager.isAnimalCage(item))
        {
            return;
        }

        event.setCancelled(true);

        AnimalData storedData = AnimalPenManager.getAnimalData(item);

        if (storedData == null)
        {
            return;
        }

        EntityType entityType = storedData.entityType;

        Location spawnLoc = block.getLocation().add(0.5, 1, 0.5);
        World world = player.getWorld();

        Entity entity = world.spawnEntity(spawnLoc,
            entityType,
            CreatureSpawnEvent.SpawnReason.CUSTOM,
            spawnedEntity -> {
                // TODO: apply variants for entities.
            });

        if (!(entity instanceof Animals))
        {
            return;
        }

        AnimalPenManager.removeAnimal(item, 1);

        player.swingMainHand();
        player.sendMessage(Component.text("Released " + entity.getType().name()).
            style(StyleUtil.GREEN_COLOR));
    }
}