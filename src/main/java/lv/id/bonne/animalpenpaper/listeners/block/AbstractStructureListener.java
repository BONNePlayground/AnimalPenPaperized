package lv.id.bonne.animalpenpaper.listeners.block;


import com.destroystokyo.paper.event.entity.EntityZapEvent;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.block.data.type.Slab;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import lv.id.bonne.animalpenpaper.AnimalPenPlugin;
import lv.id.bonne.animalpenpaper.data.AnimalData;
import lv.id.bonne.animalpenpaper.data.BlockData;
import lv.id.bonne.animalpenpaper.events.block.AnimalBlockAttackEvent;
import lv.id.bonne.animalpenpaper.events.block.AnimalBlockBreakEvent;
import lv.id.bonne.animalpenpaper.events.block.AnimalBlockInteractEvent;
import lv.id.bonne.animalpenpaper.events.block.AnimalBlockPlaceEvent;
import lv.id.bonne.animalpenpaper.managers.container.AbstractContainerManager;
import lv.id.bonne.animalpenpaper.managers.InteractionHandler;
import lv.id.bonne.animalpenpaper.menu.AnimalPenVariantMenu;
import lv.id.bonne.animalpenpaper.util.StyleUtil;
import lv.id.bonne.animalpenpaper.util.Utils;
import net.kyori.adventure.text.Component;


/**
 * Abstract base for listeners that manage the placeable block structure (e.g. Animal Pen, Aquarium)
 * that a container's captured animals live inside: placing/breaking the structure, interacting with
 * or attacking its display entity, and protecting it from the usual list of block/entity hazards.
 *
 * <p>Subclasses supply configuration via abstract methods. The few places where behaviour genuinely
 * differs — waterlogging clean-up on break, water-adjacency checks, and per-variant crafted display
 * names — are covered by protected hooks/overrides that subclasses may use.
 */
public abstract class AbstractStructureListener implements Listener
{

// ---------------------------------------------------------------------
// Section: Abstract configuration — must be provided by every subclass
// ---------------------------------------------------------------------


    /**
     * The manager backing this listener, e.g. {@code AnimalPenManager.INSTANCE}.
     */
    protected abstract AbstractContainerManager getManager();


    /**
     * Tag used to test whether an item can be used to attack this structure's display entity.
     */
    protected abstract NamespacedKey getAttackTag();


// ---------------------------------------------------------------------
// Section: Overridable hooks
// ---------------------------------------------------------------------


    /**
     * Called after a structure block has finished breaking (animal data extracted, but before the
     * structure item is dropped and block data cleared). Subclasses may clean up block-state
     * side-effects here (e.g. removing water from a waterlogged slab).
     *
     * @param block the block being broken
     */
    protected void onStructureBroken(Block block)
    {
        // Default: no-op
    }


    /**
     * Tests whether {@code block} should be treated as part of this structure for water-adjacency
     * protections (bucket use, water spread, dispensers). Default: the block itself is a structure
     * block. The Aquarium overrides this to also treat the block directly below as related, since it
     * sits on a waterlogged slab.
     *
     * @param block the block to test
     */
    protected boolean isRelatedBlock(Block block)
    {
        return this.getManager().isStructureBlock(block);
    }


    /**
     * Resolves the display-name translatable for a freshly crafted structure item. Default: the base
     * name key for the structure. Overridden by {@code AnimalPenListener} to support per-variant names.
     *
     * @param data the crafted item's custom model data
     */
    protected Component getCraftedDisplayName(CustomModelData data)
    {
        return AnimalPenPlugin.translations().getTranslatable(this.getManager().getStructureTranslationName());
    }


// ---------------------------------------------------------------------
// Section: Placement / breaking
// ---------------------------------------------------------------------


    @EventHandler(ignoreCancelled = true)
    public void onStructurePlace(BlockPlaceEvent event)
    {
        AbstractContainerManager manager = this.getManager();

        if (!manager.isStructureItem(event.getItemInHand()))
        {
            // Not this structure
            return;
        }

        if (event.getBlockReplacedState().getType() == Material.SMOOTH_STONE_SLAB)
        {
            // Cannot place structure on another slab.
            event.setCancelled(true);
            return;
        }

        AnimalBlockPlaceEvent placeEvent =
            new AnimalBlockPlaceEvent(event.getPlayer(), event.getBlock().getLocation(), this.getManager().getBlockPrefix());

        if (!placeEvent.callEvent())
        {
            // placing is blocked.
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

        // Empty structure
        BlockFace blockFace = event.getBlockAgainst().getFace(event.getBlock());

        if (blockFace == BlockFace.UP || blockFace == BlockFace.DOWN)
        {
            blockFace = event.getPlayer().getFacing().getOppositeFace();
        }

        BlockData data = new BlockData();
        data.blockFace = blockFace;

        manager.completeStructureCreation(block, data, event.getItemInHand());
    }


    @EventHandler(ignoreCancelled = true)
    public void onStructureBreak(BlockBreakEvent event)
    {
        AbstractContainerManager manager = this.getManager();

        if (!manager.isStructureBlock(event.getBlock()))
        {
            // Not this structure
            return;
        }

        Block block = event.getBlock();

        // Get data.
        AnimalData animalData = manager.getAnimalData(block);

        AnimalBlockBreakEvent breakEvent = new AnimalBlockBreakEvent(event.getPlayer(),
            event.getBlock().getLocation(),
            animalData,
            this.getManager().getBlockPrefix());

        if (!breakEvent.callEvent())
        {
            // block breaking
            event.setCancelled(true);
            return;
        }

        if (animalData != null)
        {
            ItemStack itemStack = manager.createEmptyContainer();
            manager.setContainerData(itemStack, animalData);

            // Drop data
            block.getWorld().dropItem(block.getLocation(), itemStack);
        }

        // Subclass hook: undo block-state side-effects (e.g. remove water)
        this.onStructureBroken(block);

        ItemStack structureItem = manager.getStructureItem(block);

        // Remove entities
        manager.clearBlockData(block, false);

        // Drop proper item
        event.setDropItems(false);
        block.getWorld().dropItem(block.getLocation(), structureItem);
    }


// ---------------------------------------------------------------------
// Section: Entity interaction / attack
// ---------------------------------------------------------------------


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityInteract(PlayerInteractEntityEvent event)
    {
        Entity eventEntity = event.getRightClicked();
        AbstractContainerManager manager = this.getManager();

        if (!manager.isStructureEntity(eventEntity) || !(eventEntity instanceof LivingEntity entity))
        {
            return;
        }

        // I CONTROL IT!!! NO CUSTOM INTERACTIONS HAHAHAHA
        event.setCancelled(true);

        if (event.getHand() != EquipmentSlot.HAND)
        {
            // Prevent interactions with offhand to avoid double triggering.,
            return;
        }

        this.processEntity(event.getPlayer(), event.getHand(), entity);
    }


    @EventHandler(ignoreCancelled = false)
    public void onEntityLootDropping(EntityDamageEvent event)
    {
        Entity eventEntity = event.getEntity();
        AbstractContainerManager manager = this.getManager();

        if (!manager.isStructureEntity(eventEntity) || !(eventEntity instanceof LivingEntity entity))
        {
            return;
        }

        // Structure entities cannot be damaged.
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

        if (!Utils.getTag(this.getAttackTag()).isTagged(attackItem.getType()))
        {
            // Only tagged items can attack.
            return;
        }

        AnimalBlockAttackEvent attackEvent = new AnimalBlockAttackEvent(player,
            attackItem,
            entity.getLocation(),
            manager.getAnimalData(entity),
            manager.getBlockPrefix());

        if (!attackEvent.callEvent())
        {
            // Do nothing. Interaction failed.
            return;
        }

        InteractionHandler.handleKilling(entity,
            player,
            attackItem,
            manager.getAnimalData(entity),
            data -> manager.setStructureData(entity, data));
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockInteract(PlayerInteractEvent event)
    {
        Block clickedBlock = event.getClickedBlock();

        if (clickedBlock == null)
        {
            return;
        }

        AbstractContainerManager manager = this.getManager();

        if (!manager.isStructureBlock(clickedBlock))
        {
            return;
        }

        // I CONTROL IT!!! NO CUSTOM INTERACTIONS HAHAHAHA
        event.setCancelled(true);

        if (event.getHand() != EquipmentSlot.HAND)
        {
            // Prevent interactions with offhand to avoid double triggering.,
            return;
        }

        Entity entity = manager.getStructureEntity(clickedBlock);

        if (entity instanceof LivingEntity livingEntity)
        {
            this.processEntity(event.getPlayer(), event.getHand(), livingEntity);
        }
    }


    private void processEntity(@NotNull Player player,
        @NotNull EquipmentSlot hand,
        @NotNull LivingEntity entity)
    {
        ItemStack itemStack = player.getInventory().getItem(hand);
        AbstractContainerManager manager = this.getManager();

        if (manager.isContainer(itemStack))
        {
            // This does not interact with hand-held containers.
            return;
        }

        // Track on interaction
        AnimalPenPlugin.getInstance().task.startTrackingEntity(entity, false, this.getManager());

        AnimalBlockInteractEvent interactEvent = new AnimalBlockInteractEvent(player,
            itemStack,
            hand,
            entity.getLocation(),
            manager.getAnimalData(entity),
            this.getManager().getBlockPrefix());

        if (!interactEvent.callEvent())
        {
            // Do nothing. Interaction failed.
            return;
        }

        if (itemStack.isEmpty() && hand == EquipmentSlot.HAND)
        {
            AnimalPenVariantMenu.openMenu(entity, player, this.getManager());
            // Not an item.
            return;
        }

        InteractionHandler.handleItemInteraction(entity,
            player,
            itemStack,
            manager.getAnimalData(entity),
            data -> manager.setStructureData(entity, data),
            manager.getBlockPrefix());
    }


// ---------------------------------------------------------------------
// Section: Structure Protections
// ---------------------------------------------------------------------


    @EventHandler
    public void onStructureExplode(BlockExplodeEvent event)
    {
        AbstractContainerManager manager = this.getManager();
        boolean hasStructure = event.blockList().stream().anyMatch(manager::isStructureBlock);

        if (!hasStructure)
        {
            // Not this structure
            return;
        }

        // Prevent structures from exploding.
        event.blockList().removeIf(manager::isStructureBlock);
    }


    @EventHandler
    public void onStructureExplode(EntityExplodeEvent event)
    {
        AbstractContainerManager manager = this.getManager();
        boolean hasStructure = event.blockList().stream().anyMatch(manager::isStructureBlock);

        if (!hasStructure)
        {
            // Not this structure
            return;
        }

        // Prevent structures from exploding.
        event.blockList().removeIf(manager::isStructureBlock);
    }


    @EventHandler(ignoreCancelled = true)
    public void onStructurePush(BlockPistonExtendEvent event)
    {
        if (event.getBlocks().stream().anyMatch(this.getManager()::isStructureBlock))
        {
            // No piston events on structures.
            event.setCancelled(true);
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onStructurePush(BlockPistonRetractEvent event)
    {
        if (event.getBlocks().stream().anyMatch(this.getManager()::isStructureBlock))
        {
            // No piston events on structures.
            event.setCancelled(true);
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onStructurePlaceBlock(BlockCanBuildEvent event)
    {
        if (!this.getManager().isStructureBlock(event.getBlock()))
        {
            // Not this structure
            return;
        }

        // Prevent placing blocks above the structure.
        event.setBuildable(false);
    }


    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event)
    {
        if (!this.getManager().isStructureEntity(event.getEntity()))
        {
            return;
        }

        // Structure entities cannot be damaged.
        event.setCancelled(true);
    }


    @EventHandler(ignoreCancelled = true)
    public void onDamageOtherEntities(EntityDamageEvent event)
    {
        Entity entity = event.getDamageSource().getCausingEntity();

        if (entity == null || !this.getManager().isStructureEntity(entity))
        {
            return;
        }

        // Structure entities cannot damage anything else.
        event.setCancelled(true);
    }


    @EventHandler(ignoreCancelled = true)
    public void onEntityRemoveEvent(EntityDeathEvent event)
    {
        if (this.getManager().isStructureEntity(event.getEntity()))
        {
            // Structure entities cannot be killed.
            event.setCancelled(true);
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onEntityZapEvent(EntityZapEvent event)
    {
        if (this.getManager().isStructureEntity(event.getEntity()))
        {
            // Structure entities cannot be transformed.
            event.setCancelled(true);
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onEntityTransformEvent(EntityTransformEvent event)
    {
        if (this.getManager().isStructureEntity(event.getEntity()))
        {
            // Structure entities cannot be transformed.
            event.setCancelled(true);
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onWaterPlace(PlayerBucketEmptyEvent event)
    {
        if (this.isRelatedBlock(event.getBlock()))
        {
            // Structure cannot be waterlogged
            event.setCancelled(true);
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onWaterSpread(BlockFromToEvent event)
    {
        if (this.isRelatedBlock(event.getToBlock()))
        {
            // Structure cannot be waterlogged
            event.setCancelled(true);
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onEntityTarget(EntityTargetLivingEntityEvent event)
    {
        if (event.getTarget() != null && this.getManager().isStructureEntity(event.getTarget()))
        {
            event.setCancelled(true);
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onDispenseArmor(BlockDispenseArmorEvent event)
    {
        if (this.getManager().isStructureEntity(event.getTargetEntity()))
        {
            event.setCancelled(true);
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onDispenseBlock(BlockDispenseEvent event)
    {
        if (event.getBlock().getBlockData() instanceof Dispenser dispenser)
        {
            if (this.isRelatedBlock(event.getBlock().getRelative(dispenser.getFacing())))
            {
                event.setCancelled(true);
            }
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onEntityDropItems(EntityDropItemEvent event)
    {
        if (this.getManager().isStructureEntity(event.getEntity()))
        {
            event.setCancelled(true);
        }
    }


    @EventHandler(ignoreCancelled = true)
    public void onEntityPickUpItems(EntityPickupItemEvent event)
    {
        if (this.getManager().isStructureEntity(event.getEntity()))
        {
            event.setCancelled(true);
        }
    }


// ---------------------------------------------------------------------
// Section: Crafting
// ---------------------------------------------------------------------


    @EventHandler
    public void onItemCraft(CraftItemEvent event)
    {
        ItemStack result = event.getRecipe().getResult();

        if (!result.hasData(DataComponentTypes.CUSTOM_MODEL_DATA))
        {
            return;
        }

        CustomModelData data = result.getData(DataComponentTypes.CUSTOM_MODEL_DATA);

        if (data.strings().contains(this.getManager().getStructureModel()))
        {
            ItemMeta itemMeta = result.getItemMeta();
            itemMeta.displayName(this.getCraftedDisplayName(data).style(StyleUtil.WHITE));

            String prefix = this.getManager().getStructureTranslationPrefix();
            itemMeta.lore(List.of(
                AnimalPenPlugin.translations().getTranslatable(prefix + ".tip.line1"),
                AnimalPenPlugin.translations().getTranslatable(prefix + ".tip.line2"),
                AnimalPenPlugin.translations().getTranslatable(prefix + ".tip.line3")
            ));

            result.setItemMeta(itemMeta);
            event.setCurrentItem(result);
        }
    }
}