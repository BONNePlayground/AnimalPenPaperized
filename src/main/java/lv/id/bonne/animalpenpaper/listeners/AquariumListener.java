//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpenpaper.listeners;


import com.destroystokyo.paper.event.entity.EntityZapEvent;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Dispenser;
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
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import lv.id.bonne.animalpenpaper.AnimalPenPlugin;
import lv.id.bonne.animalpenpaper.data.AnimalData;
import lv.id.bonne.animalpenpaper.data.BlockData;
import lv.id.bonne.animalpenpaper.managers.AquariumManager;
import lv.id.bonne.animalpenpaper.menu.AnimalPenVariantMenu;
import lv.id.bonne.animalpenpaper.util.StyleUtil;
import lv.id.bonne.animalpenpaper.util.Utils;


/**
 * This listener manages animal pen interactions.
 */
public class AquariumListener implements Listener
{
    @EventHandler(ignoreCancelled = true)
    public void onAnimalPenPlace(BlockPlaceEvent event)
    {
        if (!AquariumManager.isAquarium(event.getItemInHand()))
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

        AquariumManager.completeAquariumCreation(block, data, event.getItemInHand());
    }


    @EventHandler(ignoreCancelled = true)
    public void onAnimalPenBreak(BlockBreakEvent event)
    {
        if (!AquariumManager.isAquarium(event.getBlock()))
        {
            // Not animal pen
            return;
        }

        Block block = event.getBlock();

        // Get data.
        AnimalData animalData = AquariumManager.getAnimalData(block);

        if (animalData != null)
        {
            ItemStack itemStack = AquariumManager.createEmptyWaterContainer();
            AquariumManager.setWaterContainerData(itemStack, animalData);

            // Drop data
            block.getWorld().dropItem(block.getLocation(), itemStack);
        }

        if (block.getBlockData() instanceof Slab slab)
        {
            block.getRelative(BlockFace.UP).setType(Material.AIR);
            slab.setWaterlogged(false);
            block.setBlockData(slab);
        }

        ItemStack animalPenItem = AquariumManager.getAquariumItem(block);

        // Remove entities
        AquariumManager.clearBlockData(block, false);

        // Drop proper item
        event.setDropItems(false);
        block.getWorld().dropItem(block.getLocation(), animalPenItem);
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityInteract(PlayerInteractEntityEvent event)
    {
        Entity eventEntity = event.getRightClicked();

        if (!AquariumManager.isAquarium(eventEntity) || !(eventEntity instanceof LivingEntity entity))
        {
            return;
        }

        // Check for food items
        Player player = event.getPlayer();
        ItemStack itemStack = player.getInventory().getItem(event.getHand());

        if (AquariumManager.isWaterContainer(itemStack))
        {
            // This does not interact with cages.
            return;
        }

        // Track on interaction
        AnimalPenPlugin.getInstance().task.startTrackingEntity(entity, false, false);

        // I CONTROL IT!!! NO CUSTOM INTERACTIONS HAHAHAHA
        event.setCancelled(true);

        if (itemStack.isEmpty() && event.getHand() == EquipmentSlot.HAND)
        {
            AnimalPenVariantMenu.openMenu(entity, player);

            // Not an item.
            return;
        }

        if (AnimalPenPlugin.animalFoodConfiguration().isFoodItem(entity, itemStack))
        {
            AquariumManager.handleFood(entity, player, itemStack);
        }
        else if (itemStack.getType() == Material.WATER_BUCKET)
        {
            AquariumManager.handleWaterBucket(entity, player, itemStack);
        }
    }


    @EventHandler(ignoreCancelled = false)
    public void onEntityLootDropping(EntityDamageEvent event)
    {
        Entity eventEntity = event.getEntity();

        if (!AquariumManager.isAquarium(eventEntity) || !(eventEntity instanceof LivingEntity entity))
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

        AquariumManager.handleKilling(entity, player, attackItem);
    }


// ---------------------------------------------------------------------
// Section: Animal Pen Protections
// ---------------------------------------------------------------------


    @EventHandler
    public void onAnimalPenExplode(BlockExplodeEvent event)
    {
        boolean hasAnimalPen = event.blockList().stream().anyMatch(AquariumManager::isAquarium);

        if (!hasAnimalPen)
        {
            // Not animal pen
            return;
        }

        // Prevent animal pens from explosions.
        event.blockList().removeIf(AquariumManager::isAquarium);
    }


    @EventHandler
    public void onAnimalPenExplode(EntityExplodeEvent event)
    {
        boolean hasAnimalPen = event.blockList().stream().anyMatch(AquariumManager::isAquarium);

        if (!hasAnimalPen)
        {
            // Not animal pen
            return;
        }

        // Prevent animal pens from explosions.
        event.blockList().removeIf(AquariumManager::isAquarium);
    }


    @EventHandler(ignoreCancelled = true)
    public void onAnimalPenPush(BlockPistonExtendEvent event)
    {
        boolean hasAnimalPen = event.getBlocks().stream().anyMatch(AquariumManager::isAquarium);

        if (!hasAnimalPen)
        {
            // Not animal pen
            return;
        }

        // No piston events on animal pens.
        event.setCancelled(true);
    }


    @EventHandler(ignoreCancelled = true)
    public void onAnimalPenPush(BlockPistonRetractEvent event)
    {
        boolean hasAnimalPen = event.getBlocks().stream().anyMatch(AquariumManager::isAquarium);

        if (!hasAnimalPen)
        {
            // Not animal pen
            return;
        }

        // No piston events on animal pens.
        event.setCancelled(true);
    }


    @EventHandler(ignoreCancelled = true)
    public void onAnimalPenPlaceBlock(BlockCanBuildEvent event)
    {
        if (!AquariumManager.isAquarium(event.getBlock()))
        {
            // Not animal pen
            return;
        }

        // Prevent placing blocks above animal pen.
        event.setBuildable(false);
    }


    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event)
    {
        Entity entity = event.getEntity();

        if (!AquariumManager.isAquarium(entity))
        {
            return;
        }

        // Animal pen entities cannot be damaged.
        event.setCancelled(true);
    }


    @EventHandler(ignoreCancelled = true)
    public void onDamageOtherEntities(EntityDamageEvent event)
    {
        Entity entity = event.getDamageSource().getCausingEntity();

        if (entity == null || !AquariumManager.isAquarium(entity))
        {
            return;
        }

        // Animal pen entities cannot damage anything else.
        event.setCancelled(true);
    }


    @EventHandler(ignoreCancelled = true)
    public void onEntityRemoveEvent(EntityDeathEvent event)
    {
        if (AquariumManager.isAquarium(event.getEntity()))
        {
            // Animal pen entities cannot be killed.
            event.setCancelled(true);
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onEntityZapEvent(EntityZapEvent event)
    {
        if (AquariumManager.isAquarium(event.getEntity()))
        {
            // Animal pen entities cannot be transformed.
            event.setCancelled(true);
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onEntityTransformEvent(EntityTransformEvent event)
    {
        if (AquariumManager.isAquarium(event.getEntity()))
        {
            // Animal pen entities cannot be transformed.
            event.setCancelled(true);
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onWaterPlace(PlayerBucketEmptyEvent event)
    {
        if (AquariumManager.isAquarium(event.getBlock()) ||
            AquariumManager.isAquarium(event.getBlock().getRelative(BlockFace.DOWN)))
        {
            // Aquariums cannot be waterlogged
            event.setCancelled(true);
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onWaterPlace(PlayerBucketFillEvent event)
    {
        if (AquariumManager.isAquarium(event.getBlock()) ||
            AquariumManager.isAquarium(event.getBlock().getRelative(BlockFace.DOWN)))
        {
            // Aquariums cannot be removed from water
            event.setCancelled(true);
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onWaterSpread(BlockFromToEvent event)
    {
        if (AquariumManager.isAquarium(event.getToBlock()))
        {
            // Aquarium cannot be waterlogged
            event.setCancelled(true);
        }

        if (AquariumManager.isAquarium(event.getBlock()))
        {
            // Aquarium cannot spread water
            event.setCancelled(true);
        }

        if (AquariumManager.isAquarium(event.getToBlock().getRelative(BlockFace.DOWN)))
        {
            // Aquarium cannot be waterlogged
            event.setCancelled(true);
        }

        if (AquariumManager.isAquarium(event.getBlock().getRelative(BlockFace.DOWN)))
        {
            // Aquarium cannot spread water
            event.setCancelled(true);
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onEntityTarget(EntityTargetLivingEntityEvent event)
    {
        if (event.getTarget() != null && AquariumManager.isAquarium(event.getTarget()))
        {
            event.setCancelled(true);
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onDispenseArmor(BlockDispenseArmorEvent event)
    {
        if (AquariumManager.isAquarium(event.getTargetEntity()))
        {
            event.setCancelled(true);
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onDispenseBlock(BlockDispenseEvent event)
    {
        if (event.getBlock().getBlockData() instanceof Dispenser dispenser)
        {
            // Check if block is aquarium or block bellow is aquarium
            if (AquariumManager.isAquarium(event.getBlock().getRelative(dispenser.getFacing())) ||
                AquariumManager.isAquarium(event.getBlock().getRelative(dispenser.getFacing()).
                    getRelative(BlockFace.DOWN)))
            {
                event.setCancelled(true);
            }
        }
    }


    @EventHandler
    public void onSponge(SpongeAbsorbEvent event)
    {
        // Remove aquarium blocks from sponge.
        event.getBlocks().removeIf(blockState ->
        {
            Block block = blockState.getBlock();
            return AquariumManager.isAquarium(block) ||
                AquariumManager.isAquarium(block.getRelative(BlockFace.DOWN));
        });
    }


    @EventHandler(ignoreCancelled = true)
    public void onEntityDropItems(EntityDropItemEvent event)
    {
        if (AquariumManager.isAquarium(event.getEntity()))
        {
            event.setCancelled(true);
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onEntityPickUpItems(EntityPickupItemEvent event)
    {
        if (AquariumManager.isAquarium(event.getEntity()))
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

        if (data.strings().contains(AquariumManager.AQUARIUM_MODEL))
        {
            ItemMeta itemMeta = result.getItemMeta();
            itemMeta.displayName(AnimalPenPlugin.translations().
                getTranslatable("item.animal_pen.aquarium.name").
                style(StyleUtil.WHITE));

            itemMeta.lore(List.of(
                AnimalPenPlugin.translations().getTranslatable("item.animal_pen.aquarium.tip.line1"),
                AnimalPenPlugin.translations().getTranslatable("item.animal_pen.aquarium.tip.line2"),
                AnimalPenPlugin.translations().getTranslatable("item.animal_pen.aquarium.tip.line3")
            ));

            result.setItemMeta(itemMeta);
            event.setCurrentItem(result);
        }
    }
}
