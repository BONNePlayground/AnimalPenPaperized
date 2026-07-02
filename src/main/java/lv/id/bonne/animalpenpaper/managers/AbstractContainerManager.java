package lv.id.bonne.animalpenpaper.managers;


import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.List;

import lv.id.bonne.animalpenpaper.AnimalPenPlugin;
import lv.id.bonne.animalpenpaper.data.AnimalData;
import lv.id.bonne.animalpenpaper.data.AnimalDataType;
import lv.id.bonne.animalpenpaper.data.BlockData;
import lv.id.bonne.animalpenpaper.data.BlockDataType;
import lv.id.bonne.animalpenpaper.menu.AnimalPenVariantMenu;
import lv.id.bonne.animalpenpaper.util.StyleUtil;
import lv.id.bonne.animalpenpaper.util.Utils;
import net.kyori.adventure.text.Component;


/**
 * Abstract base for block-based animal container managers (e.g. Animal Pen, Aquarium).
 *
 * <p>Subclasses supply configuration via abstract methods. The two main areas
 * where behaviour genuinely differs — entity placement / removal and block-state side-effects (e.g. waterlogging) — are
 * covered by protected hooks that subclasses may override.
 */
public abstract class AbstractContainerManager
{

// ---------------------------------------------------------------------
// Section: Abstract configuration — must be provided by every subclass
// ---------------------------------------------------------------------


    /**
     * PersistentDataContainer key used to store {@link AnimalData} on entities and items.
     */
    protected abstract NamespacedKey getDataKey();


    /**
     * Suffix appended to the coordinate-based world PDC key, e.g. {@code "animal_pen"} or {@code "aquarium"}.  Must
     * be unique per manager type.
     */
    public abstract String getBlockPrefix();


    /**
     * Suffix appended to the coordinate-based world PDC key, e.g. {@code "animal_cage"} or {@code "water_aniaml_container"}.  Must
     * be unique per manager type.
     */
    public abstract String getItemPrefix();


    /**
     * Tag used to test whether an entity type can be captured by this container.
     */
    public abstract NamespacedKey getPickableTag();


    /**
     * Model-data string for the empty hand-held container item (e.g. glass bottle).
     */
    public abstract String getEmptyContainerModel();


    /**
     * Model-data string for a filled hand-held container item.
     */
    protected abstract String getFilledContainerModel();


    /**
     * Model-data string for the placeable block item.
     */
    public abstract String getStructureModel();


    /**
     * Material of the hand-held container item. Defaults to {@link Material#GLASS_BOTTLE}; override if different.
     */
    protected Material getContainerMaterial()
    {
        return Material.GLASS_BOTTLE;
    }


    /**
     * Material of the placeable block. Defaults to {@link Material#SMOOTH_STONE_SLAB}; override if different.
     */
    protected Material getStructureMaterial()
    {
        return Material.SMOOTH_STONE_SLAB;
    }


    /**
     * Size applied to the display entity's SCALE attribute.
     */
    protected abstract double getAnimalSize();


    /**
     * Whether animals inside this container type should grow with count.
     */
    protected abstract boolean isGrowEnabled();


    /**
     * Y-offset added to the block location when spawning the display entity. Animal Pen uses {@code 0.5}; Aquarium uses
     * {@code 1.0}.
     */
    protected abstract double getEntityYOffset();


// ---------------------------------------------------------------------
// Section: Translation Keys
// ---------------------------------------------------------------------


    public String getContainerTranslationPrefix()
    {
        return "item.animal_pen." + this.getItemPrefix();
    }


    public String getContainerTranslationName()
    {
        return this.getContainerTranslationPrefix() + ".name";
    }


    public String getStructureTranslationPrefix()
    {
        return "item.animal_pen." + this.getBlockPrefix();
    }


    public String getStructureTranslationName()
    {
        return this.getStructureTranslationPrefix() + ".name";
    }


// ---------------------------------------------------------------------
// Section: Overridable lifecycle hooks
// ---------------------------------------------------------------------


    /**
     * Called after a brand-new display entity has been spawned and configured for the given block.  Subclasses may
     * perform additional entity setup here (e.g. sheep colour, mooshroom stew effects).
     *
     * @param block the block this structure occupies
     * @param entity the freshly spawned display entity
     * @param newData the animal data being applied
     * @param blockData the block's persistent data record
     */
    protected void onEntitySpawned(Block block, Entity entity, AnimalData newData, BlockData blockData)
    {
        // Default: no-op
    }


    /**
     * Called when the last animal is removed from a structure entity, just before the entity itself is removed.
     * Subclasses may undo block-state side-effects here (e.g. removing water from a waterlogged slab).
     *
     * @param entity the entity being removed
     */
    protected void onEntityRemoved(Entity entity)
    {
        // Default: no-op
    }


    /**
     * Called at the end of {@link #clearBlockData} regardless of whether the block itself is being kept.  Subclasses
     * may clean up block-state here.
     *
     * @param block the block being cleared
     * @param keepBlock whether the structure block itself is kept
     */
    protected void onBlockCleared(Block block, boolean keepBlock)
    {
        // Default: no-op
    }


    /**
     * Called inside {@link #setStructureData(Block, AnimalData)} immediately after the display entity has been placed
     * for the first time, allowing subclasses to modify the block itself (e.g. waterlogging).
     *
     * @param block the block that now hosts the structure
     */
    protected void onFirstEntityPlaced(Block block)
    {
        // Default: no-op
    }


// ---------------------------------------------------------------------
// Section: World PDC key helper
// ---------------------------------------------------------------------


    protected final NamespacedKey penKey(Block block)
    {
        return new NamespacedKey(AnimalPenPlugin.getInstance(),
            block.getX() + "_" + block.getY() + "_" + block.getZ() + "_" + this.getBlockPrefix());
    }


// ---------------------------------------------------------------------
// Section: Hand-held container (e.g. glass bottle) methods
// ---------------------------------------------------------------------


    public boolean isContainer(ItemStack item)
    {
        if (item == null || item.getType() != this.getContainerMaterial())
        {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null || !meta.hasCustomModelDataComponent())
        {
            return false;
        }

        List<String> strings = meta.getCustomModelDataComponent().getStrings();
        return strings.contains(getEmptyContainerModel()) ||
            strings.contains(getFilledContainerModel());
    }


    public AnimalData addAnimal(ItemStack handItem,
        @NotNull EntityType type,
        EntitySnapshot entitySnapshot,
        long amount)
    {
        ItemMeta itemMeta = handItem.getItemMeta();
        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();

        AnimalData animalData = pdc.getOrDefault(this.getDataKey(),
            AnimalDataType.INSTANCE,
            new AnimalData(type, entitySnapshot, 0));

        animalData.addEntityCount(amount);

        if (animalData.getVariants().size() <= AnimalPenPlugin.configuration().getMaxStoredVariants())
        {
            animalData.addVariant(entitySnapshot);
        }

        this.updateContainerItemMeta(animalData, itemMeta);
        handItem.setItemMeta(itemMeta);

        return animalData;
    }


    public ItemStack removeAnimal(ItemStack item, long amount)
    {
        ItemMeta itemMeta = item.getItemMeta();
        PersistentDataContainer pdc = itemMeta.getPersistentDataContainer();

        AnimalData animalData = pdc.get(this.getDataKey(), AnimalDataType.INSTANCE);

        if (animalData == null)
        {
            return item;
        }

        animalData.reduceEntityCount(amount);
        this.updateContainerItemMeta(animalData, itemMeta);
        item.setItemMeta(itemMeta);

        return item;
    }


    @Nullable
    public AnimalData getAnimalData(ItemStack item)
    {
        if (!this.isContainer(item))
        {
            return null;
        }

        return item.getItemMeta()
            .getPersistentDataContainer()
            .get(getDataKey(), AnimalDataType.INSTANCE);
    }


    public void setContainerData(ItemStack item, @Nullable AnimalData animalData)
    {
        ItemMeta itemMeta = item.getItemMeta();
        this.updateContainerItemMeta(animalData, itemMeta);
        item.setItemMeta(itemMeta);
    }


    /**
     * Creates an empty hand-held container item.
     */
    public ItemStack createEmptyContainer()
    {
        ItemStack bottle = new ItemStack(this.getContainerMaterial());
        ItemMeta meta = bottle.getItemMeta();
        if (meta == null)
        {
            return bottle;
        }

        meta.displayName(AnimalPenPlugin.translations()
            .getTranslatable(getContainerTranslationName())
            .style(StyleUtil.WHITE));

        this.updateContainerItemMeta(null, meta);
        meta.setMaxStackSize(1);
        bottle.setItemMeta(meta);

        return bottle;
    }


    private void updateContainerItemMeta(@Nullable AnimalData animalData, ItemMeta itemMeta)
    {
        String prefix = this.getContainerTranslationPrefix();

        if (animalData == null || animalData.entityCount() <= 0)
        {
            itemMeta.getPersistentDataContainer().remove(this.getDataKey());

            itemMeta.lore(List.of(
                AnimalPenPlugin.translations().getTranslatable(prefix + ".catch_tip.line1"),
                AnimalPenPlugin.translations().getTranslatable(prefix + ".catch_tip.line2"),
                AnimalPenPlugin.translations().getTranslatable(prefix + ".catch_tip.line3")
            ));

            CustomModelDataComponent component = itemMeta.getCustomModelDataComponent();
            component.setStrings(List.of(this.getEmptyContainerModel()));
            itemMeta.setCustomModelDataComponent(component);
        }
        else
        {
            itemMeta.getPersistentDataContainer().set(this.getDataKey(), AnimalDataType.INSTANCE, animalData);

            itemMeta.lore(List.of(
                AnimalPenPlugin.translations().getTranslatable(prefix + ".description.top"),
                AnimalPenPlugin.translations().getTranslatable(prefix + ".description.entity",
                    Component.translatable(animalData.entityType().translationKey()).style(StyleUtil.YELLOW)),
                AnimalPenPlugin.translations().getTranslatable(prefix + ".description.amount",
                    Component.text(animalData.entityCount()).style(StyleUtil.YELLOW)),
                AnimalPenPlugin.translations().getTranslatable(prefix + ".catch_tip.line1"),
                AnimalPenPlugin.translations().getTranslatable(prefix + ".catch_tip.line2"),
                AnimalPenPlugin.translations().getTranslatable(prefix + ".catch_tip.line3"),
                AnimalPenPlugin.translations().getTranslatable(prefix + ".release_tip.line1"),
                AnimalPenPlugin.translations().getTranslatable(prefix + ".release_tip.line2")
            ));

            CustomModelDataComponent component = itemMeta.getCustomModelDataComponent();

            if (!component.getStrings().contains(this.getFilledContainerModel()))
            {
                component.setStrings(List.of(
                    this.getFilledContainerModel(),
                    animalData.entityType().key().asString()));
                itemMeta.setCustomModelDataComponent(component);
            }
        }
    }


// ---------------------------------------------------------------------
// Section: Placeable structure (block) item methods
// ---------------------------------------------------------------------


    public boolean isStructureItem(@NotNull ItemStack item)
    {
        if (item.getType() != this.getStructureMaterial())
        {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null || !meta.hasCustomModelDataComponent())
        {
            return false;
        }

        return meta.getCustomModelDataComponent().getStrings().contains(this.getStructureModel());
    }


    public boolean isStructureBlock(@Nullable Block block)
    {
        if (block == null || block.getType() != this.getStructureMaterial())
        {
            return false;
        }

        return block.getWorld().getPersistentDataContainer().has(this.penKey(block), BlockDataType.INSTANCE);
    }


    public boolean isStructureEntity(@NotNull Entity entity)
    {
        return entity.getPersistentDataContainer().has(this.getDataKey(), AnimalDataType.INSTANCE);
    }


    public void completeStructureCreation(Block block, BlockData blockData, @NotNull ItemStack itemInHand)
    {
        NamespacedKey key = this.penKey(block);

        // Decoration entity (ItemDisplay showing the structure item)
        Entity decorationEntity = block.getWorld().spawnEntity(
            block.getLocation().add(0.5, 0.501, 0.5),
            EntityType.ITEM_DISPLAY,
            CreatureSpawnEvent.SpawnReason.CUSTOM,
            newEntity ->
            {
                newEntity.setPersistent(true);
                newEntity.setRotation(Utils.blockFaceToYaw(blockData.blockFace), 0);

                if (newEntity instanceof ItemDisplay display)
                {
                    display.setVisibleByDefault(true);

                    ItemStack displayStack = new ItemStack(itemInHand);
                    displayStack.setAmount(1);
                    display.setItemStack(displayStack);

                    Transformation transform = display.getTransformation();
                    transform.getScale().set(1.001f, 1f, 1.001f);
                    display.setTransformation(transform);
                }

                newEntity.getPersistentDataContainer().set(
                    Helper.DECORATION_ENTITY_KEY,
                    PersistentDataType.STRING,
                    key.getKey());
            });

        blockData.decorationEntity = decorationEntity.getUniqueId();

        // Counter entity (TextDisplay showing the animal count)
        Entity countEntity = block.getWorld().spawnEntity(
            block.getLocation().add(Utils.center(blockData.blockFace)),
            EntityType.TEXT_DISPLAY,
            CreatureSpawnEvent.SpawnReason.CUSTOM,
            newEntity ->
            {
                newEntity.setPersistent(false);
                newEntity.setRotation(Utils.blockFaceToYaw(blockData.blockFace), 0);

                if (newEntity instanceof TextDisplay display)
                {
                    display.setVisibleByDefault(true);
                    display.setSeeThrough(false);
                    display.text(Component.text(0));
                }

                newEntity.getPersistentDataContainer().set(
                    Helper.COUNTER_ENTITY_KEY,
                    PersistentDataType.STRING,
                    key.getKey());
            });

        blockData.countEntity = countEntity.getUniqueId();

        block.getWorld().getPersistentDataContainer().set(key, BlockDataType.INSTANCE, blockData);
    }


    public void validateStructure(@NotNull Entity entity)
    {
        Block block = entity.getLocation().add(0, -0.5, 0).getBlock();

        if (block.getType() != this.getStructureMaterial())
        {
            return;
        }

        NamespacedKey key = this.penKey(block);
        BlockData blockData = block.getWorld().getPersistentDataContainer().get(key, BlockDataType.INSTANCE);

        if (blockData == null)
        {
            AnimalPenPlugin.getInstance().getLogger()
                .warning("Failed to load " + this.getBlockPrefix() + " block. Restoring...");
            blockData = new BlockData();
        }

        if (blockData.entity == null)
        {
            blockData.entity = entity.getUniqueId();
            blockData.blockFace = entity.getFacing();

            // Restore counter entity reference
            Collection<Entity> nearby = block.getWorld().getNearbyEntities(
                block.getBoundingBox().expand(1),
                e -> e.getType() == EntityType.TEXT_DISPLAY &&
                    e.getFacing() == entity.getFacing() &&
                    e.getPersistentDataContainer().has(key));

            if (!nearby.isEmpty())
            {
                blockData.countEntity = nearby.iterator().next().getUniqueId();
            }

            // Restore decoration entity reference
            nearby = block.getWorld().getNearbyEntities(
                block.getBoundingBox().expand(1),
                e -> e.getType() == EntityType.ITEM_DISPLAY &&
                    e.getFacing() == entity.getFacing() &&
                    e.getPersistentDataContainer().has(key));

            if (!nearby.isEmpty())
            {
                blockData.decorationEntity = nearby.iterator().next().getUniqueId();
            }

            block.getWorld().getPersistentDataContainer().set(key, BlockDataType.INSTANCE, blockData);
        }
        else if (!blockData.entity.equals(entity.getUniqueId()))
        {
            Entity oldEntity = block.getWorld().getEntity(blockData.entity);

            if (oldEntity == null)
            {
                blockData.entity = entity.getUniqueId();
                block.getWorld().getPersistentDataContainer().set(key, BlockDataType.INSTANCE, blockData);
            }
        }

        if (entity instanceof LivingEntity livingEntity)
        {
            AttributeInstance attribute = livingEntity.getAttribute(Attribute.SCALE);

            if (attribute != null && Utils.getTagEntity(getPickableTag()).isTagged(entity.getType()))
            {
                if (attribute.getBaseValue() != getAnimalSize())
                {
                    attribute.setBaseValue(getAnimalSize());
                }

                if (isGrowEnabled())
                {
                    AnimalData animalData = this.getAnimalData(entity);

                    if (animalData != null)
                    {
                        AttributeModifier modifier = attribute.getModifier(Utils.ANIMAL_SIZE_MODIFIER);
                        float multiplier = AnimalPenPlugin.configuration().getGrowthMultiplier()
                            * animalData.entityCount();

                        if (modifier != null && modifier.getAmount() != multiplier)
                        {
                            attribute.addModifier(new AttributeModifier(
                                Utils.ANIMAL_SIZE_MODIFIER,
                                multiplier,
                                AttributeModifier.Operation.ADD_NUMBER));
                        }
                    }
                }
            }

            livingEntity.setPose(livingEntity.getPose());
        }
    }


// ---------------------------------------------------------------------
// Section: Animal data — block / entity read-write
// ---------------------------------------------------------------------


    @Nullable
    public AnimalData getAnimalData(Block block)
    {
        NamespacedKey key = this.penKey(block);
        BlockData blockData = block.getWorld().getPersistentDataContainer().get(key, BlockDataType.INSTANCE);

        if (blockData == null || blockData.entity == null)
        {
            return null;
        }

        return getAnimalData(block.getWorld().getEntity(blockData.entity));
    }


    @Nullable
    public AnimalData getAnimalData(Entity entity)
    {
        if (entity == null)
        {
            AnimalPenPlugin.getInstance().getLogger()
                .severe(this.getBlockPrefix() + " entity is removed! Cannot access data!");
            return null;
        }

        return entity.getPersistentDataContainer().get(this.getDataKey(), AnimalDataType.INSTANCE);
    }


    /**
     * Writes {@code newData} to the structure at {@code block}, spawning the display entity if it does not yet exist.
     */
    public void setStructureData(Block block, AnimalData newData)
    {
        NamespacedKey key = this.penKey(block);
        BlockData blockData = block.getWorld().getPersistentDataContainer()
            .getOrDefault(key, BlockDataType.INSTANCE, new BlockData());

        Entity entity;

        if (blockData.entity == null || block.getWorld().getEntity(blockData.entity) == null)
        {
            // Spawn brand-new display entity
            if (newData.entitySnapshot() != null)
            {
                entity = newData.entitySnapshot().createEntity(
                    block.getLocation().add(0.5, getEntityYOffset(), 0.5));
            }
            else
            {
                entity = block.getWorld().spawnEntity(
                    block.getLocation().add(0.5, getEntityYOffset(), 0.5),
                    newData.entityType(),
                    CreatureSpawnEvent.SpawnReason.CUSTOM);
            }

            entity.setGravity(false);
            entity.setNoPhysics(true);
            entity.setPersistent(true);

            if (entity instanceof LivingEntity livingEntity)
            {
                livingEntity.setCollidable(false);
                livingEntity.setAI(false);
                livingEntity.setRemoveWhenFarAway(false);
                livingEntity.setRotation(Utils.blockFaceToYaw(blockData.blockFace), 0);

                AttributeInstance attribute = livingEntity.getAttribute(Attribute.SCALE);

                if (attribute != null)
                {
                    attribute.setBaseValue(getAnimalSize());

                    if (this.isGrowEnabled())
                    {
                        attribute.addModifier(new AttributeModifier(
                            Utils.ANIMAL_SIZE_MODIFIER,
                            AnimalPenPlugin.configuration().getGrowthMultiplier() * newData.entityCount(),
                            AttributeModifier.Operation.ADD_NUMBER));
                    }
                }
            }

            // Subclass hook: sheep colouring, waterlogging, etc.
            this.onEntitySpawned(block, entity, newData, blockData);
            this.onFirstEntityPlaced(block);

            blockData.entity = entity.getUniqueId();
            block.getWorld().getPersistentDataContainer().set(key, BlockDataType.INSTANCE, blockData);
        }
        else
        {
            entity = block.getWorld().getEntity(blockData.entity);

            if (isGrowEnabled() && entity instanceof LivingEntity livingEntity)
            {
                AttributeInstance attribute = livingEntity.getAttribute(Attribute.SCALE);

                if (attribute != null)
                {
                    attribute.removeModifier(Utils.ANIMAL_SIZE_MODIFIER);
                    attribute.addModifier(new AttributeModifier(
                        Utils.ANIMAL_SIZE_MODIFIER,
                        AnimalPenPlugin.configuration().getGrowthMultiplier() * newData.entityCount(),
                        AttributeModifier.Operation.ADD_NUMBER));
                }
            }
        }

        if (entity == null)
        {
            AnimalPenPlugin.getInstance().getLogger()
                .severe(this.getBlockPrefix() + " entity is removed! Cannot access data!");
            return;
        }

        AnimalPenPlugin.getInstance().task.startTrackingEntity(entity, false, this);

        entity.getPersistentDataContainer().set(getDataKey(), AnimalDataType.INSTANCE, newData);
        Helper.updateCountTextEntity(block, blockData, newData.entityCount(), key);
    }


    /**
     * Writes {@code newData} to the structure entity directly (used when animals are added / removed via right-click on
     * the placed structure).
     */
    public void setStructureData(Entity entity, AnimalData newData)
    {
        if (newData.entityCount() <= 0)
        {
            ItemStack emptyContainer = this.createEmptyContainer();
            entity.getWorld().dropItem(entity.getLocation(), emptyContainer);

            // Subclass hook: undo block-state side-effects (e.g. remove water)
            this.onEntityRemoved(entity);

            AnimalPenPlugin.getInstance().task.stopTrackingEntity(entity, this);

            entity.getPersistentDataContainer().remove(getDataKey());
            entity.remove();
        }
        else
        {
            entity.getPersistentDataContainer().set(getDataKey(), AnimalDataType.INSTANCE, newData);

            if (isGrowEnabled() && entity instanceof LivingEntity livingEntity)
            {
                AttributeInstance attribute = livingEntity.getAttribute(Attribute.SCALE);

                if (attribute != null)
                {
                    attribute.removeModifier(Utils.ANIMAL_SIZE_MODIFIER);
                    attribute.addModifier(new AttributeModifier(
                        Utils.ANIMAL_SIZE_MODIFIER,
                        AnimalPenPlugin.configuration().getGrowthMultiplier() * newData.entityCount(),
                        AttributeModifier.Operation.ADD_NUMBER));
                }
            }
        }

        Block block = entity.getLocation().add(0, -0.5, 0).getBlock();
        NamespacedKey key = this.penKey(block);
        BlockData blockData = block.getWorld().getPersistentDataContainer()
            .getOrDefault(key, BlockDataType.INSTANCE, new BlockData());

        Helper.updateCountTextEntity(block, blockData, newData.entityCount(), key);
    }


    public void clearBlockData(Block block, boolean keepBlock)
    {
        NamespacedKey key = this.penKey(block);
        BlockData blockData = block.getWorld().getPersistentDataContainer().get(key, BlockDataType.INSTANCE);

        if (blockData == null)
        {
            return;
        }

        AnimalPenPlugin.getInstance().task.stopTrackingEntity(blockData.entity, block.getWorld(), this);
        Helper.removeEntity(block.getWorld(), blockData.entity);

        this.onBlockCleared(block, keepBlock);

        blockData.entity = null;

        if (keepBlock)
        {
            Helper.updateCountTextEntity(block, blockData, 0, key);
            block.getWorld().getPersistentDataContainer().set(key, BlockDataType.INSTANCE, blockData);
        }
        else
        {
            Helper.removeEntity(block.getWorld(), blockData.countEntity);
            Helper.removeEntity(block.getWorld(), blockData.decorationEntity);

            block.getWorld().getPersistentDataContainer().remove(key);

            block.getWorld().getNearbyEntitiesByType(Display.class, block.getLocation(), 1)
                .forEach(display ->
                {
                    if (display.getPersistentDataContainer().has(key, PersistentDataType.BOOLEAN))
                    {
                        display.remove();
                    }
                });
        }
    }


    public ItemStack getStructureItem(Block block)
    {
        NamespacedKey key = this.penKey(block);
        BlockData blockData = block.getWorld().getPersistentDataContainer().get(key, BlockDataType.INSTANCE);

        if (blockData == null || blockData.decorationEntity == null)
        {
            return this.createDefaultStructureItem();
        }

        Entity entity = block.getWorld().getEntity(blockData.decorationEntity);

        if (!(entity instanceof ItemDisplay display))
        {
            return this.createDefaultStructureItem();
        }

        return display.getItemStack();
    }


    /**
     * Creates the default (oak / plain) variant of the placeable structure item.
     */
    public abstract ItemStack createDefaultStructureItem();


// ---------------------------------------------------------------------
// Section: Variant application
// ---------------------------------------------------------------------


    public void applyVariant(Entity entity, EntitySnapshot selectedVariant)
    {
        if (selectedVariant == null)
        {
            return;
        }

        Block block = entity.getLocation().add(0, -0.5, 0).getBlock();
        NamespacedKey key = this.penKey(block);
        BlockData blockData = block.getWorld().getPersistentDataContainer().get(key, BlockDataType.INSTANCE);

        if (blockData == null)
        {
            return;
        }

        AnimalData animalData = this.getAnimalData(entity);

        if (animalData == null)
        {
            return;
        }

        animalData.setAppliedMaterial(null);
        animalData.setAppliedFlag(null);
        animalData.setEntitySnapshot(selectedVariant);

        blockData.entity = null;

        block.getWorld().getPersistentDataContainer().set(key, BlockDataType.INSTANCE, blockData);

        AnimalPenPlugin.getInstance().task.stopTrackingEntity(entity, this);
        entity.getPersistentDataContainer().remove(getDataKey());
        AnimalPenVariantMenu.close(entity);
        entity.remove();

        setStructureData(block, animalData);
    }
}