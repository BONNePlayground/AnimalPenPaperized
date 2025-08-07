//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpenpaper.listeners;


import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

import lv.id.bonne.animalpenpaper.AnimalPenPlugin;
import lv.id.bonne.animalpenpaper.data.AnimalData;
import lv.id.bonne.animalpenpaper.data.BlockData;
import lv.id.bonne.animalpenpaper.data.BlockDataType;
import lv.id.bonne.animalpenpaper.managers.AnimalPenManager;
import net.kyori.adventure.sound.Sound;


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

        // Remove entities
        AnimalPenManager.clearBlockData(block, false);

        // Drop proper item
        event.setDropItems(false);
        block.getWorld().dropItem(block.getLocation(), AnimalPenManager.createAnimalPen());
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityInteract(PlayerInteractEntityEvent event)
    {
        Entity entity = event.getRightClicked();
        boolean isAnimalPen = AnimalPenManager.isAnimalPen(entity);

        if (!isAnimalPen)
        {
            // Not animal pen
            return;
        }

        // I CONTROL IT!!! NO CUSTOM INTERACTIONS HAHAHAHA
        event.setCancelled(true);

        // Check for food items
        Player player = event.getPlayer();
        ItemStack itemStack = player.getInventory().getItem(event.getHand());

        if (itemStack.isEmpty())
        {
            // Not an item.
            return;
        }

        if (AnimalPenPlugin.CONFIG_MANAGER.getAnimalFoodConfiguration().isFoodItem(entity, itemStack))
        {
            AnimalPenManager.handleFood(entity, player, itemStack);
            return;
        }
        else if (itemStack.getType() == Material.BRUSH)
        {
            AnimalPenManager.handleBrush(entity, player, itemStack);
            return;
        }
        else if (itemStack.getType() == Material.WATER_BUCKET)
        {
            AnimalPenManager.handleWaterBucket(entity, player, itemStack);
        }

        // HANDLE BEES

        else if (itemStack.getType() == Material.SHEARS)
        {
            AnimalPenManager.handleShears(entity, player, itemStack);
        }
        else if (AnimalPenListener.isDye(itemStack.getType()))
        {
            AnimalPenManager.handleDyes(entity, player, itemStack);
        }
        else if (itemStack.getType() == Material.BUCKET)
        {
            AnimalPenManager.handleBucket(entity, player, itemStack);
        }
    }


    private static boolean isDye(Material material)
    {
        return switch (material)
        {
            case BLACK_DYE,
                 BLUE_DYE,
                 BROWN_DYE,
                 CYAN_DYE,
                 GRAY_DYE,
                 GREEN_DYE,
                 LIGHT_BLUE_DYE,
                 LIGHT_GRAY_DYE,
                 LIME_DYE,
                 MAGENTA_DYE,
                 ORANGE_DYE,
                 PINK_DYE,
                 PURPLE_DYE,
                 RED_DYE,
                 WHITE_DYE,
                 YELLOW_DYE -> true;
            default -> false;
        };
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
