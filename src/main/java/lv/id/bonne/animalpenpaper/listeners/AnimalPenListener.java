//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpenpaper.listeners;


import com.destroystokyo.paper.MaterialTags;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Slab;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;

import java.util.Collection;
import java.util.Random;

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

        // Check for food items
        Player player = event.getPlayer();
        ItemStack itemStack = player.getInventory().getItem(event.getHand());

        if (AnimalPenManager.isAnimalCage(itemStack))
        {
            // This does not interact with cages.
            return;
        }

        // Track on interaction
        AnimalPenPlugin.getInstance().task.startTrackingEntity(entity);

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
        else if (AnimalPenListener.getTag(NamespacedKey.minecraft("small_flowers")).isTagged(itemStack.getType()))
        {
            AnimalPenManager.handleSmallFlowers(entity, player, itemStack);
        }
    }


    private static Tag<Material> getTag(NamespacedKey tagKey)
    {
        return Bukkit.getTag(Tag.REGISTRY_ITEMS, tagKey, Material.class);
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

        if (!AnimalPenListener.getTag(NamespacedKey.minecraft("swords")).isTagged(attackItem.getType()) &&
            !AnimalPenListener.getTag(NamespacedKey.minecraft("axes")).isTagged(attackItem.getType()))
        {
            // Only swords and axes can attack.
            return;
        }

        AnimalData data = AnimalPenManager.getAnimalData(entity);

        if (data == null)
        {
            // Something is wrong. No entity on other end.
            return;
        }

        data.entityCount -= 1;
        AnimalPenManager.setAnimalPenData(entity, data);

        LootTable lootTable = Bukkit.getLootTable(NamespacedKey.minecraft("entities/" + entity.getType().getKey().value()));

        if (lootTable != null)
        {
            Collection<ItemStack> itemStacks = lootTable.populateLoot(new Random(),
                new LootContext.Builder(entity.getLocation()).
                    killer(player).
                    lootedEntity(entity).
                    build());

            Location location = entity.getLocation().add(0, 1, 0);
            itemStacks.forEach(item -> entity.getWorld().dropItemNaturally(location, item));
        }

        if (entity instanceof LivingEntity livingEntity)
        {
            Sound deathSound = livingEntity.getDeathSound();

            if (deathSound != null)
            {
                entity.getWorld().playSound(entity.getLocation(),
                    deathSound,
                    new Random().nextFloat(0.5f, 1f),
                    1f);
            }
            else
            {
                entity.getWorld().playSound(entity.getLocation(),
                    Sound.ENTITY_GENERIC_DEATH,
                    new Random().nextFloat(0.5f, 1f),
                    1f);
            }
        }

        entity.getWorld().spawnParticle(Particle.SMOKE,
            entity.getLocation().add(0, 0.5, 0),
            10,
            0.3,
            0.3,
            0.3,
            0.01);
        entity.getWorld().spawnParticle(Particle.ANGRY_VILLAGER,
            entity.getLocation().add(0, 0.5, 0),
            2,
            0.2,
            0.2,
            0.2,
            0);
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
