//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpenpaper.listeners;


import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import lv.id.bonne.animalpenpaper.AnimalPenPlugin;
import lv.id.bonne.animalpenpaper.data.AnimalData;
import lv.id.bonne.animalpenpaper.data.BlockData;
import lv.id.bonne.animalpenpaper.data.BlockDataType;
import lv.id.bonne.animalpenpaper.managers.AnimalPenManager;


/**
 * This listener manages animal pen interactions.
 */
public class AnimalPenListener implements Listener
{
    @EventHandler(ignoreCancelled = true)
    public void onAnimalPenPlace(BlockPlaceEvent event)
    {
        if (!AnimalPenManager.isAnimalPen(event.getItemInHand()))
        {
            // Not animal pen
            return;
        }

        if (event.getBlockReplacedState().getType() == Material.SMOOTH_STONE_SLAB)
        {
            // Cannot place animal pen on another slab.
            event.setCancelled(true);
            return;
        }

        Block block = event.getBlock();

        if (event.getBlock().getBlockData() instanceof Slab slab)
        {
            slab.setType(Slab.Type.BOTTOM);
            slab.setWaterlogged(false);
            block.setBlockData(slab);
        }

        NamespacedKey penKey = new NamespacedKey(AnimalPenPlugin.getInstance(),
            block.getX() + "_" + block.getY() + "_" + block.getZ() + "_animal_pen");

        // Empty pen
        BlockFace blockFace = event.getBlockAgainst().getFace(event.getBlock());

        if (blockFace == BlockFace.UP || blockFace == BlockFace.DOWN)
        {
            blockFace = event.getPlayer().getFacing().getOppositeFace();
        }

        BlockData data = new BlockData();
        data.blockFace = blockFace;

        block.getWorld().getPersistentDataContainer().set(penKey, BlockDataType.INSTANCE, data);
    }


    @EventHandler
    public void onAnimalPenBreak(BlockBreakEvent event)
    {
        if (!AnimalPenManager.isAnimalPen(event.getBlock()))
        {
            // Not animal pen
            return;
        }

        Block block = event.getBlock();

        // Get data.
        AnimalData animalData = AnimalPenManager.getAnimalData(block);

        if (animalData != null)
        {
            ItemStack itemStack = AnimalPenManager.createEmptyAnimalCage();
            AnimalPenManager.setItemData(itemStack, animalData);

            // Drop data
            block.getWorld().dropItem(block.getLocation(), itemStack);
        }

        NamespacedKey penKey = new NamespacedKey(AnimalPenPlugin.getInstance(),
            block.getX() + "_" + block.getY() + "_" + block.getZ() + "_animal_pen");

        // Remove entity
        BlockData blockData = block.getWorld().getPersistentDataContainer().get(penKey, BlockDataType.INSTANCE);

        if (blockData != null)
        {
            if (blockData.entity != null)
            {
                Entity entity = block.getWorld().getEntity(blockData.entity);

                if (entity != null)
                {
                    entity.getPersistentDataContainer().remove(AnimalPenManager.ANIMAL_DATA_KEY);
                    entity.remove();
                }
            }

            AnimalPenListener.removeEntity(block.getWorld(), blockData.countEntity);

            blockData.cooldowns.forEach(text -> {
                AnimalPenListener.removeEntity(block.getWorld(), text.text);
                AnimalPenListener.removeEntity(block.getWorld(), text.icon);
            });
        }

        // Remove saved data key
        block.getWorld().getPersistentDataContainer().remove(penKey);

        // Drop proper item
        event.setDropItems(false);
        block.getWorld().dropItem(block.getLocation(), AnimalPenManager.createAnimalPen());
    }


    private static void removeEntity(World world, @Nullable UUID uuid)
    {
        if (uuid != null)
        {
            Entity entity = world.getEntity(uuid);

            if (entity != null)
            {
                entity.remove();
            }
        }
    }


// ---------------------------------------------------------------------
// Section: Animal Pen Protections
// ---------------------------------------------------------------------


    @EventHandler
    public void onAnimalPenExplode(BlockExplodeEvent event)
    {
        boolean hasAnimalPen = event.blockList().stream().anyMatch(AnimalPenManager::isAnimalPen);

        if (!hasAnimalPen)
        {
            // Not animal pen
            return;
        }

        // Prevent animal pens from explosions.
        event.blockList().removeIf(AnimalPenManager::isAnimalPen);
    }


    @EventHandler
    public void onAnimalPenExplode(EntityExplodeEvent event)
    {
        boolean hasAnimalPen = event.blockList().stream().anyMatch(AnimalPenManager::isAnimalPen);

        if (!hasAnimalPen)
        {
            // Not animal pen
            return;
        }

        // Prevent animal pens from explosions.
        event.blockList().removeIf(AnimalPenManager::isAnimalPen);
    }


    @EventHandler
    public void onAnimalPenPush(BlockPistonExtendEvent event)
    {
        boolean hasAnimalPen = event.getBlocks().stream().anyMatch(AnimalPenManager::isAnimalPen);

        if (!hasAnimalPen)
        {
            // Not animal pen
            return;
        }

        // No piston events on animal pens.
        event.setCancelled(true);
    }


    @EventHandler
    public void onAnimalPenPush(BlockPistonRetractEvent event)
    {
        boolean hasAnimalPen = event.getBlocks().stream().anyMatch(AnimalPenManager::isAnimalPen);

        if (!hasAnimalPen)
        {
            // Not animal pen
            return;
        }

        // No piston events on animal pens.
        event.setCancelled(true);
    }


    @EventHandler
    public void onAnimalPenPlaceBlock(BlockCanBuildEvent event)
    {
        if (!AnimalPenManager.isAnimalPen(event.getBlock()))
        {
            // Not animal pen
            return;
        }

        // Prevent placing blocks above animal pen.
        event.setBuildable(false);
    }


    @EventHandler
    public void onEntityDamage(EntityDamageEvent event)
    {
        if (!AnimalPenManager.isAnimalPen(event.getEntity()))
        {
            return;
        }

        // Animal pen entities cannot be damaged.
        event.setCancelled(true);
    }


    @EventHandler
    public void onEntityRemoveEvent(EntityDeathEvent event)
    {
        if (AnimalPenManager.isAnimalPen(event.getEntity()))
        {
            // Animal pen entities cannot be killed.
            event.setCancelled(true);
        }
    }
}
