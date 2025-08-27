//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpenpaper.listeners;


import com.destroystokyo.paper.MaterialTags;
import com.destroystokyo.paper.event.entity.EntityZapEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Slab;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import lv.id.bonne.animalpenpaper.AnimalPenPlugin;
import lv.id.bonne.animalpenpaper.data.AnimalData;
import lv.id.bonne.animalpenpaper.data.BlockData;
import lv.id.bonne.animalpenpaper.managers.AnimalPenManager;
import lv.id.bonne.animalpenpaper.util.StyleUtil;
import lv.id.bonne.animalpenpaper.util.Utils;


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

        // Empty pen
        BlockFace blockFace = event.getBlockAgainst().getFace(event.getBlock());

        if (blockFace == BlockFace.UP || blockFace == BlockFace.DOWN)
        {
            blockFace = event.getPlayer().getFacing().getOppositeFace();
        }

        BlockData data = new BlockData();
        data.blockFace = blockFace;

        AnimalPenManager.completePenCreation(block, data, event.getItemInHand());
    }


    @EventHandler(ignoreCancelled = true)
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
            AnimalPenManager.setAnimalCageData(itemStack, animalData);

            // Drop data
            block.getWorld().dropItem(block.getLocation(), itemStack);
        }

        ItemStack animalPenItem = AnimalPenManager.getAnimalPenItem(block);

        // Remove entities
        AnimalPenManager.clearBlockData(block, false);

        // Drop proper item
        event.setDropItems(false);
        block.getWorld().dropItem(block.getLocation(), animalPenItem);
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityInteract(PlayerInteractEntityEvent event)
    {
        Entity eventEntity = event.getRightClicked();

        if (!AnimalPenManager.isAnimalPen(eventEntity) || !(eventEntity instanceof LivingEntity entity))
        {
            return;
        }

        // Check for food items
        Player player = event.getPlayer();
        ItemStack itemStack = player.getInventory().getItem(event.getHand());

        if (AnimalPenManager.isAnimalCage(itemStack))
        {
            // This does not interact with cages.
            return;
        }

        // Track on interaction
        AnimalPenPlugin.getInstance().task.startTrackingEntity(entity, false, true);

        // I CONTROL IT!!! NO CUSTOM INTERACTIONS HAHAHAHA
        event.setCancelled(true);

        if (itemStack.isEmpty())
        {
            // Not an item.
            return;
        }

        if (AnimalPenPlugin.CONFIG_MANAGER.getAnimalFoodConfiguration().isFoodItem(entity, itemStack))
        {
            AnimalPenManager.handleFood(entity, player, itemStack);
        }
        else if (itemStack.getType() == Material.BRUSH)
        {
            AnimalPenManager.handleBrush(entity, player, itemStack);
        }
        else if (itemStack.getType() == Material.WATER_BUCKET)
        {
            AnimalPenManager.handleWaterBucket(entity, player, itemStack);
        }
        else if (itemStack.getType() == Material.SHEARS)
        {
            AnimalPenManager.handleShears(entity, player, itemStack);
        }
        else if (MaterialTags.DYES.isTagged(itemStack))
        {
            AnimalPenManager.handleDyes(entity, player, itemStack);
        }
        else if (itemStack.getType() == Material.BUCKET)
        {
            AnimalPenManager.handleBucket(entity, player, itemStack);
        }
        else if (itemStack.getType() == Material.GLASS_BOTTLE)
        {
            AnimalPenManager.handleGlassBottle(entity, player, itemStack);
        }
        else if (itemStack.getType() == Material.MAGMA_BLOCK)
        {
            AnimalPenManager.handleMagmaBlock(entity, player, itemStack);
        }
        else if (itemStack.getType() == Material.BOWL)
        {
            AnimalPenManager.handleBowl(entity, player, itemStack);
        }
        else if (Utils.getTag(NamespacedKey.minecraft("small_flowers")).isTagged(itemStack.getType()))
        {
            AnimalPenManager.handleSmallFlowers(entity, player, itemStack);
        }
    }


    @EventHandler(ignoreCancelled = false)
    public void onEntityLootDropping(EntityDamageEvent event)
    {
        Entity eventEntity = event.getEntity();

        if (!AnimalPenManager.isAnimalPen(eventEntity) || !(eventEntity instanceof LivingEntity entity))
        {
            return;
        }

        // Animal pen entities cannot be damaged.
        event.setCancelled(true);

        Entity directEntity = event.getDamageSource().getDirectEntity();

        if (directEntity == null || directEntity.getType() != EntityType.PLAYER)
        {
            // Only player can attack.
            return;
        }

        if (event.getDamageSource().getDamageType() != DamageType.PLAYER_ATTACK)
        {
            // Not a direct attack
            return;
        }

        Player player = (Player) directEntity;

        ItemStack attackItem = player.getInventory().getItemInMainHand();

        if (player.hasCooldown(attackItem))
        {
            // Under cooldown
            return;
        }

        if (!Utils.getTag(NamespacedKey.minecraft("swords")).isTagged(attackItem.getType()) &&
            !Utils.getTag(NamespacedKey.minecraft("axes")).isTagged(attackItem.getType()))
        {
            // Only swords and axes can attack.
            return;
        }

        AnimalPenManager.handleKilling(entity, player, attackItem);
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
        Entity entity = event.getEntity();

        if (!AnimalPenManager.isAnimalPen(entity))
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


    @EventHandler
    public void onEntityZapEvent(EntityZapEvent event)
    {
        if (AnimalPenManager.isAnimalPen(event.getEntity()))
        {
            // Animal pen entities cannot be transformed.
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onEntityTransformEvent(EntityTransformEvent event)
    {
        if (AnimalPenManager.isAnimalPen(event.getEntity()))
        {
            // Animal pen entities cannot be transformed.
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onWaterPlace(PlayerBucketEmptyEvent event)
    {
        if (AnimalPenManager.isAnimalPen(event.getBlock()))
        {
            // Animal pen cannot be waterlogged
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onWaterSpread(BlockFromToEvent event)
    {
        if (AnimalPenManager.isAnimalPen(event.getToBlock()))
        {
            // Animal pen cannot be waterlogged
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetLivingEntityEvent event)
    {
        if (event.getTarget() != null && AnimalPenManager.isAnimalPen(event.getTarget()))
        {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onItemCraft(CraftItemEvent event)
    {
        ItemStack result = event.getRecipe().getResult();

        if (!result.hasData(DataComponentTypes.CUSTOM_MODEL_DATA))
        {
            return;
        }

        CustomModelData data = result.getData(DataComponentTypes.CUSTOM_MODEL_DATA);

        if (data.strings().contains(AnimalPenManager.ANIMAL_PEN_MODEL))
        {
            ItemMeta itemMeta = result.getItemMeta();

            if (data.strings().size() >= 2 && data.strings().get(1).startsWith("animal_pen:"))
            {
                itemMeta.displayName(AnimalPenPlugin.translations().
                    getTranslatable("item.animal_pen." + data.strings().get(1).split(":")[1]).
                    style(StyleUtil.WHITE));
            }
            else
            {
                itemMeta.displayName(AnimalPenPlugin.translations().
                    getTranslatable("item.animal_pen.animal_pen_oak").
                    style(StyleUtil.WHITE));
            }

            itemMeta.lore(List.of(
                AnimalPenPlugin.translations().getTranslatable("item.animal_pen.animal_pen.tip.line1"),
                AnimalPenPlugin.translations().getTranslatable("item.animal_pen.animal_pen.tip.line2"),
                AnimalPenPlugin.translations().getTranslatable("item.animal_pen.animal_pen.tip.line3")
            ));

            result.setItemMeta(itemMeta);
            event.setCurrentItem(result);
        }
    }
}
