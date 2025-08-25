package lv.id.bonne.animalpenpaper.listeners;


import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import java.util.Iterator;

import lv.id.bonne.animalpenpaper.AnimalPenPlugin;
import lv.id.bonne.animalpenpaper.data.AnimalData;
import lv.id.bonne.animalpenpaper.managers.AquariumManager;
import lv.id.bonne.animalpenpaper.util.StyleUtil;
import net.kyori.adventure.text.Component;
import net.minecraft.world.entity.OwnableEntity;


/**
 * This listener manages animal cage interactions.
 */
public class WaterAnimalContainerListener implements Listener
{
    /**
     * This listener checks if player can catch clicked entity with animal cage.
     */
    @EventHandler(ignoreCancelled = true)
    public void onEntityCatch(PlayerInteractEntityEvent event)
    {
        Player player = event.getPlayer();

        if (event.getPlayer().isSneaking())
        {
            return;
        }

        Entity entity = event.getRightClicked();
        ItemStack item = player.getInventory().getItem(event.getHand());

        if (!AquariumManager.isWaterContainer(item))
        {
            return;
        }

        event.setCancelled(true);

        if (!(entity instanceof WaterMob animal))
        {
            player.sendMessage(Component.text("This is not a valid water mob.").style(StyleUtil.RED_COLOR));
            return;
        }

        if (animal.isDead() || animal instanceof Ageable ageable && !ageable.isAdult() || !animal.hasAI())
        {
            player.sendMessage(Component.text("Cannot capture dead or baby mobs.").style(StyleUtil.RED_COLOR));
            return;
        }

        if (entity instanceof OwnableEntity ownableEntity && ownableEntity.getOwner() != null)
        {
            player.sendMessage(Component.text("Cannot capture owned mobs.").style(StyleUtil.RED_COLOR));
            return;
        }

        // Check blocked types
        if (AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().isBlocked(animal.getType()))
        {
            player.sendMessage(Component.text("This mob is blocked from being captured.").
                style(StyleUtil.RED_COLOR));
            return;
        }

        EntityType entityType = animal.getType();
        AnimalData storedData = AquariumManager.getAnimalData(item);

        // Check if item already contains another type
        if (storedData != null && storedData.entityType() != entityType)
        {
            player.sendMessage(Component.text("This container contains a different mob.").style(StyleUtil.RED_COLOR));
            return;
        }
        else
        {
            storedData = new AnimalData(entityType, entity.createSnapshot(), 0);
        }

        long maxAmount = AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getMaximalAnimalCount();

        if (maxAmount > 0 && storedData.entityCount() + 1 > maxAmount)
        {
            player.sendMessage(Component.text("Container is full.").style(StyleUtil.RED_COLOR));
            return;
        }

        AquariumManager.addAnimal(item, entityType, entity.createSnapshot(), 1);

        entity.remove();
        player.swingMainHand();
        player.sendMessage(Component.text("Captured " + entity.getType().name()).style(StyleUtil.GREEN_COLOR));
    }


    /**
     * This listener checks if player can release entity from animal cage.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
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

        if (!AquariumManager.isWaterContainer(item))
        {
            return;
        }

        event.setCancelled(true);

        AnimalData storedData = AquariumManager.getAnimalData(item);

        if (storedData == null)
        {
            return;
        }

        EntityType entityType = storedData.entityType();

        Location spawnLoc = block.getLocation().add(0.5, 1, 0.5);
        World world = player.getWorld();

        Entity entity;

        if (storedData.entitySnapshot() != null)
        {
            entity = storedData.entitySnapshot().createEntity(spawnLoc);
        }
        else
        {
            entity = world.spawnEntity(spawnLoc, entityType, CreatureSpawnEvent.SpawnReason.CUSTOM);
        }

        if (!(entity instanceof WaterMob))
        {
            return;
        }

        AquariumManager.removeAnimal(item, 1);

        player.swingMainHand();
        player.sendMessage(Component.text("Released " + entity.getType().name()).style(StyleUtil.GREEN_COLOR));
    }


    /**
     * This listener cheks is player can interact with animal cage on animal pen
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInteractWithPenWithAnimalCage(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getHand());

        if (!AquariumManager.isWaterContainer(item))
        {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
        {
            return;
        }

        Block block = event.getClickedBlock();

        if (!AquariumManager.isAquarium(block))
        {
            return;
        }

        event.setCancelled(true);

        AnimalData penData = AquariumManager.getAnimalData(block);
        AnimalData itemData = AquariumManager.getAnimalData(item);

        if (itemData == null && penData == null)
        {
            // Both are empty
            return;
        }

        if (penData == null)
        {
            // Animal pen data is null.
            AquariumManager.setAquariumData(block, itemData);

            item.setAmount(-1);
            player.getInventory().setItem(event.getHand(), item);
            player.swingMainHand();
            player.sendMessage(Component.text("Inserted into animal pen").style(StyleUtil.GREEN_COLOR));

            return;
        }

        if (itemData == null)
        {
            if (!player.isSneaking() || penData.entityCount() < 2)
            {
                // Only on sneaking or there is something to split
                return;
            }

            // Clone half of data to new item
            itemData = new AnimalData(penData.entityType(), penData.entitySnapshot(), penData.entityCount() / 2);
            itemData.getCooldowns().putAll(penData.getCooldowns());
            itemData.setScutes(penData.scutes());

            AquariumManager.setWaterContainerData(item, itemData);

            penData.reduceEntityCount(itemData.entityCount());

            AquariumManager.setAquariumData(block, penData);

            return;
        }

        if (penData.entityType() != itemData.entityType())
        {
            // Cannot merge different entities
            return;
        }

        // Now just combine both data, and clear item.
        penData.addEntityCount(itemData.entityCount());

        // Merge cooldowns
        itemData.getCooldowns().forEach((key, value) -> penData.getCooldowns().merge(key, value, Math::max));
        itemData.getCooldowns().clear();

        // Merge scute data
        penData.setScutes(penData.scutes() + itemData.scutes());
        itemData.setScutes(0);

        // Check variants
        final int maxStoredVariants = AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getMaxStoredVariants();

        if (itemData.entityCount() > 1 &&
            penData.getVariants().size() + itemData.getVariants().size() > maxStoredVariants)
        {
            player.sendMessage(Component.text("Cannot store all variants into aquarium."));
            penData.reduceEntityCount(1);
            itemData.setEntityCount(1);

            // Save reduced item data
            AquariumManager.setWaterContainerData(item, itemData);
        }
        else
        {
            int size = penData.getVariants().size();
            Iterator<EntitySnapshot> iterator = itemData.getVariants().iterator();

            while (size < maxStoredVariants && iterator.hasNext())
            {
                penData.addVariant(iterator.next());
                size++;
            }

            // just clear remining ones.
            itemData.getVariants().clear();

            // Clear item data
            AquariumManager.setWaterContainerData(item, null);
        }

        AquariumManager.setAquariumData(block, penData);
    }


    /**
     * This listener checks if player can interact with animal pen having empty hand
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInteractWithPenWithEmptyHand(PlayerInteractEvent event)
    {
        if (event.getItem() != null && !event.getItem().getType().isAir() || event.getHand() == EquipmentSlot.OFF_HAND)
        {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || !event.getPlayer().isSneaking())
        {
            return;
        }

        Block block = event.getClickedBlock();

        if (!AquariumManager.isAquarium(block))
        {
            return;
        }

        event.setCancelled(true);

        AnimalData penData = AquariumManager.getAnimalData(block);

        if (penData == null)
        {
            return;
        }

        ItemStack itemStack = AquariumManager.createEmptyWaterContainer();
        AquariumManager.setWaterContainerData(itemStack, penData);

        AquariumManager.clearBlockData(block, true);

        event.getPlayer().getInventory().
            setItem(event.getHand() == null ? EquipmentSlot.HAND : event.getHand(), itemStack);
    }


    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGHEST)
    public void onProtectionOfUsage(PlayerInteractEntityEvent event)
    {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getHand());

        if (!AquariumManager.isWaterContainer(item))
        {
            return;
        }

        event.setCancelled(true);
    }


    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGHEST)
    public void onProtectionOfUsage(PlayerInteractEvent event)
    {
        if (!AquariumManager.isWaterContainer(event.getItem()))
        {
            return;
        }

        event.setCancelled(true);
    }
}