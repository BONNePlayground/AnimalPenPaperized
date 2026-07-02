//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpenpaper.listeners.block;


import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Slab;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.SpongeAbsorbEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

import lv.id.bonne.animalpenpaper.managers.container.AbstractContainerManager;
import lv.id.bonne.animalpenpaper.managers.container.AviaryManager;


/**
 * This listener manages aquarium interactions.
 */
public class AviaryListener extends AbstractStructureListener
{

// ---------------------------------------------------------------------
// Section: Abstract configuration
// ---------------------------------------------------------------------


    @Override
    protected AbstractContainerManager getManager()
    {
        return AviaryManager.INSTANCE;
    }


    @Override
    protected NamespacedKey getAttackTag()
    {
        return CAN_ATTACK_AVIARY;
    }


// ---------------------------------------------------------------------
// Section: Lifecycle hooks
// ---------------------------------------------------------------------


    /**
     * Undo waterlogging when the aquarium block is broken.
     */
    @Override
    protected void onStructureBroken(Block block)
    {
        if (block.getBlockData() instanceof Slab slab)
        {
            block.getRelative(BlockFace.UP).setType(Material.AIR);
            slab.setWaterlogged(false);
            block.setBlockData(slab);
        }
    }


    /**
     * The aquarium sits on a waterlogged slab, so any block-below is treated as related for water
     * adjacency protections (bucket use, water spread, dispensers).
     */
    @Override
    protected boolean isRelatedBlock(Block block)
    {
        return this.getManager().isStructureBlock(block) ||
            this.getManager().isStructureBlock(block.getRelative(BlockFace.DOWN));
    }


    /**
     * The aquarium also needs to check the source block of a water spread, not just the destination,
     * since it sits directly beside/below flowing water.
     */
    @Override
    @EventHandler(ignoreCancelled = true)
    public void onWaterSpread(BlockFromToEvent event)
    {
        if (this.isRelatedBlock(event.getToBlock()) || this.isRelatedBlock(event.getBlock()))
        {
            // Aquarium cannot be waterlogged / cannot spread water
            event.setCancelled(true);
        }
    }


// ---------------------------------------------------------------------
// Section: Aquarium-only protections
// ---------------------------------------------------------------------


    @EventHandler(ignoreCancelled = true)
    public void onWaterPlace(PlayerBucketFillEvent event)
    {
        if (this.isRelatedBlock(event.getBlock()))
        {
            // Aquariums cannot be removed from water
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onSponge(SpongeAbsorbEvent event)
    {
        // Remove aquarium blocks from sponge.
        event.getBlocks().removeIf(blockState -> this.isRelatedBlock(blockState.getBlock()));
    }


// ---------------------------------------------------------------------
// Section: Constants
// ---------------------------------------------------------------------


    private static final NamespacedKey CAN_ATTACK_AVIARY =
        new NamespacedKey("animal_pen", "can_attack_aviary");
}