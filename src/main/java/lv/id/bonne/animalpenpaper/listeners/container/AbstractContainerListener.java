package lv.id.bonne.animalpenpaper.listeners.container;


import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.List;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import io.papermc.paper.potion.SuspiciousEffectEntry;
import lv.id.bonne.animalpenpaper.AnimalPenPlugin;
import lv.id.bonne.animalpenpaper.data.AnimalData;
import lv.id.bonne.animalpenpaper.events.item.AnimalCatchEvent;
import lv.id.bonne.animalpenpaper.events.item.AnimalDepositEvent;
import lv.id.bonne.animalpenpaper.events.item.AnimalReleaseEvent;
import lv.id.bonne.animalpenpaper.events.item.AnimalWithdrawEvent;
import lv.id.bonne.animalpenpaper.managers.container.AbstractContainerManager;
import lv.id.bonne.animalpenpaper.util.StyleUtil;
import lv.id.bonne.animalpenpaper.util.Utils;
import net.kyori.adventure.text.Component;
import net.minecraft.world.entity.OwnableEntity;


/**
 * Abstract base for listeners that let players catch, release, deposit and withdraw animals using a
 * hand-held container (e.g. Animal Cage, Water Animal Container) together with its matching block
 * structure (e.g. Animal Pen, Aquarium).
 *
 * <p>Subclasses supply configuration via abstract methods. The two areas where behaviour genuinely
 * differs — cosmetic state applied on release (e.g. sheep colour, mooshroom stew effects) and extra
 * per-item state carried over on a withdraw split (e.g. scutes) — are covered by protected hooks that
 * subclasses may override.
 */
public abstract class AbstractContainerListener implements Listener
{

// ---------------------------------------------------------------------
// Section: Abstract configuration — must be provided by every subclass
// ---------------------------------------------------------------------


    /**
     * The manager backing this listener, e.g. {@code AnimalPenManager.INSTANCE}.
     */
    protected abstract AbstractContainerManager getManager();
    

    /**
     * Translation-key suffix used when the right-clicked entity cannot be caught by this container at
     * all, e.g. {@code "error.not_animal"} or {@code "error.not_water_animal"}.
     */
    protected abstract String getNotAnimalErrorKey();


    /**
     * Translation-key suffix used when the right-clicked entity is owned/tamed by someone, e.g.
     * {@code "error.tame"} or {@code "error.owned"}.
     */
    protected abstract String getOwnershipErrorKey();


// ---------------------------------------------------------------------
// Section: Overridable lifecycle hooks
// ---------------------------------------------------------------------


    /**
     * Called right after a stored entity has been recreated at release time, before its equipment is
     * cleared. Subclasses may apply species-specific cosmetic state here (e.g. sheep colour/shear
     * state, mooshroom stew effects).
     *
     * @param entity the freshly (re)spawned entity
     * @param storedData the animal data that was released
     * @return {@code true} if applied state means the stored snapshot should be refreshed even though
     *         one already existed
     */
    protected boolean onEntityReleased(Entity entity, AnimalData storedData)
    {
        boolean updateSnapshot = storedData.getAppliedFlag().isPresent() ||
            storedData.getAppliedMaterial().isPresent();

        if (entity instanceof Sheep sheep)
        {
            storedData.getAppliedFlag().ifPresent(shared -> {
                sheep.setSheared(shared);
                storedData.setAppliedFlag(null);
            });

            storedData.getAppliedMaterial().ifPresent(dye -> {
                sheep.setColor(Utils.getDyeColor(dye));
                storedData.setAppliedMaterial(null);
            });
        }
        else if (entity instanceof MushroomCow cow)
        {
            storedData.getAppliedMaterial().ifPresent(dye -> {
                SuspiciousEffectEntry suspiciousEffectEntry = Utils.FLOWER_EFFECTS.get(dye);

                if (suspiciousEffectEntry != null)
                {
                    cow.addEffectToNextStew(suspiciousEffectEntry, true);
                }

                storedData.setAppliedMaterial(null);
            });
        }

        return updateSnapshot;
    }


    /**
     * Called while splitting {@code penData} into a freshly created {@code itemData} on withdraw,
     * letting subclasses carry over extra per-item state that isn't handled generically (e.g. scutes,
     * applied dye/shear state).
     *
     * @param penData the data remaining in the structure (not yet reduced)
     * @param itemData the freshly created data that will be written to the withdrawn item
     */
    protected void onWithdrawSplit(AnimalData penData, AnimalData itemData)
    {
        itemData.setScutes(penData.scutes() / 2);
        penData.setScutes(penData.scutes() - itemData.scutes());

        penData.getAppliedMaterial().ifPresent(itemData::setAppliedMaterial);
        penData.getAppliedFlag().ifPresent(itemData::setAppliedFlag);
    }


// ---------------------------------------------------------------------
// Section: Catch
// ---------------------------------------------------------------------


    /**
     * This listener checks if player can catch clicked entity with a hand-held container.
     */
    @EventHandler(ignoreCancelled = true)
    public void onEntityCatch(PlayerInteractEntityEvent event)
    {
        Player player = event.getPlayer();

        if (player.isSneaking())
        {
            return;
        }

        Entity entity = event.getRightClicked();
        ItemStack item = player.getInventory().getItem(event.getHand());
        AbstractContainerManager manager = this.getManager();

        if (!manager.isContainer(item))
        {
            return;
        }

        event.setCancelled(true);

        if (!Utils.getTagEntity(this.getManager().getPickableTag()).isTagged(entity.getType()))
        {
            player.sendMessage(AnimalPenPlugin.translations().
                getTranslatable(this.getManager().getContainerTranslationPrefix() + "." + this.getNotAnimalErrorKey()));

            return;
        }

        if (!(entity instanceof LivingEntity animal))
        {
            player.sendMessage(AnimalPenPlugin.translations().
                getTranslatable(this.getManager().getContainerTranslationPrefix() + "." + this.getNotAnimalErrorKey()));

            return;
        }

        if (animal.isDead() || !animal.hasAI())
        {
            // Silent death
            return;
        }

        if (animal instanceof Ageable ageable && !ageable.isAdult())
        {
            player.sendMessage(AnimalPenPlugin.translations().
                getTranslatable(this.getManager().getContainerTranslationPrefix() + ".error.baby"));

            return;
        }

        if (entity instanceof Tameable tameable && tameable.getOwnerUniqueId() != null ||
            entity instanceof OwnableEntity ownable && ownable.getOwner() != null)
        {
            player.sendMessage(AnimalPenPlugin.translations().
                getTranslatable(this.getManager().getContainerTranslationPrefix() + "." + this.getOwnershipErrorKey()));

            return;
        }

        if (animal.isLeashed())
        {
            player.sendMessage(AnimalPenPlugin.translations().
                getTranslatable(this.getManager().getContainerTranslationPrefix() + ".error.leashed"));

            return;
        }

        if (!animal.isEmpty())
        {
            // Eject all passengers
            animal.eject();
        }

        // Drop all equipment
        EntityEquipment equipment = animal.getEquipment();

        for (EquipmentSlot slot : EquipmentSlot.values())
        {
            ItemStack itemStack = equipment.getItem(slot);

            if (Math.random() <= equipment.getDropChance(slot))
            {
                entity.getWorld().dropItemNaturally(entity.getLocation(), itemStack);
            }

            equipment.setDropChance(slot, 0);
        }

        // Check blocked types
        if (AnimalPenPlugin.configuration().isBlocked(animal.getType()))
        {
            player.sendMessage(AnimalPenPlugin.translations().
                getTranslatable(this.getManager().getContainerTranslationPrefix() + ".error.blocked"));

            return;
        }

        EntityType entityType = animal.getType();
        AnimalData storedData = manager.getAnimalData(item);

        // Check if item already contains another type
        if (storedData != null && storedData.entityType() != entityType)
        {
            player.sendMessage(AnimalPenPlugin.translations().
                getTranslatable(this.getManager().getContainerTranslationPrefix() + ".error.wrong"));

            return;
        }
        else if (storedData == null)
        {
            storedData = new AnimalData(entityType, entity.createSnapshot(), 0);
        }

        long maxAmount = AnimalPenPlugin.configuration().getMaximalAnimalCount();

        if (maxAmount > 0 && storedData.entityCount() + 1 > maxAmount)
        {
            player.sendMessage(AnimalPenPlugin.translations().
                getTranslatable(this.getManager().getContainerTranslationPrefix() + ".error.full"));

            return;
        }

        AnimalCatchEvent animalCatchEvent =
            new AnimalCatchEvent(player,
                entity,
                storedData,
                this.getManager().getItemPrefix());

        if (!animalCatchEvent.callEvent())
        {
            player.sendMessage(AnimalPenPlugin.translations().
                getTranslatable(this.getManager().getContainerTranslationPrefix() + ".error.unknown",
                    Component.translatable(entity.getType().translationKey())));
            return;
        }

        manager.addAnimal(item, entityType, entity.createSnapshot(), 1);

        entity.remove();
        player.swingMainHand();

        player.sendMessage(AnimalPenPlugin.translations().
            getTranslatable(this.getManager().getContainerTranslationPrefix() + ".captured",
                Component.translatable(entity.getType().translationKey())));
    }


// ---------------------------------------------------------------------
// Section: Release
// ---------------------------------------------------------------------


    /**
     * This listener checks if player can release entity from a hand-held container.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityRelease(PlayerInteractEvent event)
    {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() == null)
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
        AbstractContainerManager manager = this.getManager();

        if (!manager.isContainer(item))
        {
            return;
        }

        event.setCancelled(true);

        AnimalData storedData = manager.getAnimalData(item);

        if (storedData == null)
        {
            return;
        }

        EntityType entityType = storedData.entityType();

        Location spawnLoc = block.getLocation().add(event.getBlockFace().getDirection().add(MIDDLE_BLOCK));
        World world = player.getWorld();

        AnimalReleaseEvent animalReleaseEvent = new AnimalReleaseEvent(player,
            spawnLoc,
            storedData,
            this.getManager().getItemPrefix());

        if (!animalReleaseEvent.callEvent())
        {
            player.sendMessage(AnimalPenPlugin.translations().
                getTranslatable(this.getManager().getContainerTranslationPrefix() + ".error.release"));
            return;
        }

        Entity entity;

        if (storedData.entitySnapshot() != null)
        {
            entity = storedData.entitySnapshot().createEntity(spawnLoc);
        }
        else
        {
            entity = world.spawnEntity(spawnLoc, entityType, CreatureSpawnEvent.SpawnReason.CUSTOM);
        }

        boolean updateSnapshot = onEntityReleased(entity, storedData);

        if (updateSnapshot || storedData.entitySnapshot() == null)
        {
            // Update snapshot based on created entity.
            storedData.setEntitySnapshot(entity.createSnapshot());
        }

        if (!(entity instanceof LivingEntity animal))
        {
            return;
        }

        // Clear all equipment to avoid its dropping.
        if (animal.getEquipment() != null)
        {
            animal.getEquipment().clear();
        }

        manager.removeAnimal(item, 1);

        player.swingMainHand();

        player.sendMessage(AnimalPenPlugin.translations().
            getTranslatable(this.getManager().getContainerTranslationPrefix() + ".released",
                Component.translatable(entity.getType().translationKey())));
    }


// ---------------------------------------------------------------------
// Section: Deposit / withdraw via structure interaction
// ---------------------------------------------------------------------


    /**
     * This listener checks if player can interact with the structure using a hand-held container.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInteractWithStructureEntityWithContainer(PlayerInteractEntityEvent event)
    {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getHand());
        AbstractContainerManager manager = this.getManager();

        if (!manager.isContainer(item))
        {
            return;
        }

        if (!(event.getRightClicked() instanceof LivingEntity livingEntity))
        {
            return;
        }

        if (!manager.isStructureEntity(livingEntity))
        {
            return;
        }

        event.setCancelled(true);

        AnimalData penData = manager.getAnimalData(livingEntity);
        AnimalData itemData = manager.getAnimalData(item);

        if (itemData == null && penData == null)
        {
            // Both are empty
            return;
        }

        if (penData == null)
        {
            AnimalDepositEvent animalDepositEvent = new AnimalDepositEvent(player,
                livingEntity.getLocation(),
                itemData,
                null,
                this.getManager().getItemPrefix());

            if (!animalDepositEvent.callEvent())
            {
                player.sendMessage(AnimalPenPlugin.translations().
                    getTranslatable(this.getManager().getContainerTranslationPrefix() + ".error.deposit"));
                return;
            }

            // Structure data is null.
            manager.setStructureData(livingEntity, itemData);

            item.setAmount(-1);
            player.getInventory().setItem(event.getHand(), item);
            player.swingMainHand();

            player.sendMessage(AnimalPenPlugin.translations().
                getTranslatable(this.getManager().getContainerTranslationPrefix() + ".inserted"));

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

            this.onWithdrawSplit(penData, itemData);

            AnimalWithdrawEvent animalWithdrawEvent = new AnimalWithdrawEvent(player,
                livingEntity.getLocation(),
                itemData,
                penData,
                this.getManager().getItemPrefix());

            if (!animalWithdrawEvent.callEvent())
            {
                event.getPlayer().sendMessage(AnimalPenPlugin.translations().
                    getTranslatable(this.getManager().getContainerTranslationPrefix() + ".error.withdrawn"));
                return;
            }

            manager.setContainerData(item, itemData);

            penData.reduceEntityCount(itemData.entityCount());

            manager.setStructureData(livingEntity, penData);

            player.sendMessage(AnimalPenPlugin.translations().
                getTranslatable(this.getManager().getContainerTranslationPrefix() + ".withdrawn", itemData.entityCount()));

            return;
        }

        if (penData.entityType() != itemData.entityType())
        {
            // Cannot merge different entities
            return;
        }

        AnimalDepositEvent animalDepositEvent = new AnimalDepositEvent(player,
            livingEntity.getLocation(),
            itemData,
            penData,
            this.getManager().getItemPrefix());

        if (!animalDepositEvent.callEvent())
        {
            player.sendMessage(AnimalPenPlugin.translations().
                getTranslatable(this.getManager().getContainerTranslationPrefix() + ".error.deposit"));
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
        final int maxStoredVariants = AnimalPenPlugin.configuration().getMaxStoredVariants();
        long amount = itemData.entityCount();

        if (itemData.entityCount() > 1 &&
            penData.getVariants().size() + itemData.getVariants().size() > maxStoredVariants)
        {
            player.sendMessage(AnimalPenPlugin.translations().
                getTranslatable(this.getManager().getContainerTranslationPrefix() + ".error.too_many_variants"));

            penData.reduceEntityCount(1);
            itemData.setEntityCount(1);

            // Save reduced item data
            manager.setContainerData(item, itemData);

            amount--;
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
            manager.setContainerData(item, null);
        }

        manager.setStructureData(livingEntity, penData);

        player.sendMessage(AnimalPenPlugin.translations().
            getTranslatable(this.getManager().getContainerTranslationPrefix() + ".deposited", amount));
    }


    /**
     * This listener checks if player can interact with the structure using a hand-held container.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInteractWithStructureWithContainer(PlayerInteractEvent event)
    {
        if (event.getHand() == null)
        {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getHand());
        AbstractContainerManager manager = this.getManager();

        if (!manager.isContainer(item))
        {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
        {
            return;
        }

        Block block = event.getClickedBlock();

        if (!manager.isStructureBlock(block))
        {
            return;
        }

        event.setCancelled(true);

        AnimalData penData = manager.getAnimalData(block);
        AnimalData itemData = manager.getAnimalData(item);

        if (itemData == null && penData == null)
        {
            // Both are empty
            return;
        }

        if (penData == null)
        {
            AnimalDepositEvent animalDepositEvent = new AnimalDepositEvent(player,
                block.getLocation(),
                itemData,
                null,
                this.getManager().getItemPrefix());

            if (!animalDepositEvent.callEvent())
            {
                player.sendMessage(AnimalPenPlugin.translations().
                    getTranslatable(this.getManager().getContainerTranslationPrefix() + ".error.deposit"));
                return;
            }

            // Structure data is null.
            manager.setStructureData(block, itemData);

            item.setAmount(-1);
            player.getInventory().setItem(event.getHand(), item);
            player.swingMainHand();

            player.sendMessage(AnimalPenPlugin.translations().
                getTranslatable(this.getManager().getContainerTranslationPrefix() + ".inserted"));

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

            this.onWithdrawSplit(penData, itemData);

            AnimalWithdrawEvent animalWithdrawEvent = new AnimalWithdrawEvent(player,
                block.getLocation(),
                itemData,
                penData,
                this.getManager().getItemPrefix());

            if (!animalWithdrawEvent.callEvent())
            {
                event.getPlayer().sendMessage(AnimalPenPlugin.translations().
                    getTranslatable(this.getManager().getContainerTranslationPrefix() + ".error.withdrawn"));
                return;
            }

            manager.setContainerData(item, itemData);

            penData.reduceEntityCount(itemData.entityCount());

            manager.setStructureData(block, penData);

            player.sendMessage(AnimalPenPlugin.translations().
                getTranslatable(this.getManager().getContainerTranslationPrefix() + ".withdrawn", itemData.entityCount()));

            return;
        }

        if (penData.entityType() != itemData.entityType())
        {
            // Cannot merge different entities
            return;
        }

        AnimalDepositEvent animalDepositEvent = new AnimalDepositEvent(player,
            block.getLocation(),
            itemData,
            penData,
            this.getManager().getItemPrefix());

        if (!animalDepositEvent.callEvent())
        {
            player.sendMessage(AnimalPenPlugin.translations().
                getTranslatable(this.getManager().getContainerTranslationPrefix() + ".error.deposit"));
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
        final int maxStoredVariants = AnimalPenPlugin.configuration().getMaxStoredVariants();
        long amount = itemData.entityCount();

        if (itemData.entityCount() > 1 &&
            penData.getVariants().size() + itemData.getVariants().size() > maxStoredVariants)
        {
            player.sendMessage(AnimalPenPlugin.translations().
                getTranslatable(this.getManager().getContainerTranslationPrefix() + ".error.too_many_variants"));

            penData.reduceEntityCount(1);
            itemData.setEntityCount(1);

            // Save reduced item data
            manager.setContainerData(item, itemData);

            amount--;
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
            manager.setContainerData(item, null);
        }

        manager.setStructureData(block, penData);

        player.sendMessage(AnimalPenPlugin.translations().
            getTranslatable(this.getManager().getContainerTranslationPrefix() + ".deposited", amount));
    }


    /**
     * This listener checks if player can interact with the structure having an empty hand.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInteractWithStructureWithEmptyHand(PlayerInteractEvent event)
    {
        if (event.getHand() == null ||
            event.getItem() != null && !event.getItem().getType().isAir() ||
            event.getHand() == EquipmentSlot.OFF_HAND)
        {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || !event.getPlayer().isSneaking())
        {
            return;
        }

        Block block = event.getClickedBlock();
        AbstractContainerManager manager = this.getManager();

        if (!manager.isStructureBlock(block))
        {
            return;
        }

        event.setCancelled(true);

        AnimalData penData = manager.getAnimalData(block);

        if (penData == null)
        {
            return;
        }

        AnimalWithdrawEvent animalWithdrawEvent = new AnimalWithdrawEvent(event.getPlayer(),
            block.getLocation(),
            null,
            penData,
            this.getManager().getItemPrefix());

        if (!animalWithdrawEvent.callEvent())
        {
            event.getPlayer().sendMessage(AnimalPenPlugin.translations().
                getTranslatable(this.getManager().getContainerTranslationPrefix() + ".error.withdrawn"));
            return;
        }

        ItemStack itemStack = manager.createEmptyContainer();
        manager.setContainerData(itemStack, penData);

        manager.clearBlockData(block, true);

        event.getPlayer().getInventory().
            setItem(event.getHand() == null ? EquipmentSlot.HAND : event.getHand(), itemStack);

        event.getPlayer().sendMessage(AnimalPenPlugin.translations().
            getTranslatable(this.getManager().getContainerTranslationPrefix() + ".taken"));
    }


// ---------------------------------------------------------------------
// Section: Protection / crafting
// ---------------------------------------------------------------------


    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGHEST)
    public void onProtectionOfUsage(PlayerInteractEntityEvent event)
    {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getHand());

        if (!this.getManager().isContainer(item))
        {
            return;
        }

        event.setCancelled(true);
    }


    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGHEST)
    public void onProtectionOfUsage(PlayerInteractEvent event)
    {
        if (!this.getManager().isContainer(event.getItem()))
        {
            return;
        }

        event.setCancelled(true);
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

        if (data.strings().contains(this.getManager().getEmptyContainerModel()))
        {
            ItemMeta itemMeta = result.getItemMeta();
            itemMeta.displayName(AnimalPenPlugin.translations().
                getTranslatable(this.getManager().getContainerTranslationPrefix() + ".name").
                style(StyleUtil.WHITE));

            itemMeta.lore(List.of(
                AnimalPenPlugin.translations().getTranslatable(this.getManager().getContainerTranslationPrefix() + ".catch_tip.line1"),
                AnimalPenPlugin.translations().getTranslatable(this.getManager().getContainerTranslationPrefix() + ".catch_tip.line2"),
                AnimalPenPlugin.translations().getTranslatable(this.getManager().getContainerTranslationPrefix() + ".catch_tip.line3")
            ));

            result.setItemMeta(itemMeta);
            event.setCurrentItem(result);
        }
    }


    private static final Vector MIDDLE_BLOCK = new Vector(0.5, 0, 0.5);
}