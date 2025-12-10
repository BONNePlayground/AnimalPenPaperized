package lv.id.bonne.animalpenpaper.managers;


import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.*;
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
import java.util.*;

import io.papermc.paper.potion.SuspiciousEffectEntry;
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
 * Clean storage class using the custom PersistentDataType
 */
public class AnimalPenManager
{
    public static boolean isAnimalCage(ItemStack item)
    {
        if (item == null || item.getType() != Material.GLASS_BOTTLE)
        {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null || !meta.hasCustomModelDataComponent())
        {
            return false;
        }

        List<String> dataComponents = meta.getCustomModelDataComponent().getStrings();

        return dataComponents.contains(ANIMAL_CAGE_MODEL) ||
            dataComponents.contains(ANIMAL_CAGE_FILLED_MODEL);
    }


    public static AnimalData addAnimal(ItemStack handItem,
        @NotNull EntityType type,
        EntitySnapshot entitySnapshot,
        long amount)
    {
        ItemMeta itemMeta = handItem.getItemMeta();

        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        AnimalData animalData = dataContainer.getOrDefault(AnimalPenManager.ANIMAL_DATA_KEY,
            AnimalDataType.INSTANCE,
            new AnimalData(type, entitySnapshot, 0));

        animalData.addEntityCount(amount);

        if (animalData.getVariants().size() <= AnimalPenPlugin.configuration().getMaxStoredVariants())
        {
            animalData.addVariant(entitySnapshot);
        }

        AnimalPenManager.updateAnimalCageItemMeta(animalData, itemMeta);

        handItem.setItemMeta(itemMeta);

        return animalData;
    }


    public static ItemStack removeAnimal(ItemStack item, long amount)
    {
        ItemMeta itemMeta = item.getItemMeta();

        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        AnimalData animalData = dataContainer.get(AnimalPenManager.ANIMAL_DATA_KEY, AnimalDataType.INSTANCE);

        if (animalData == null)
        {
            return item;
        }

        animalData.reduceEntityCount(amount);

        AnimalPenManager.updateAnimalCageItemMeta(animalData, itemMeta);

        item.setItemMeta(itemMeta);

        return item;
    }


    @Nullable
    public static AnimalData getAnimalData(ItemStack item)
    {
        if (!AnimalPenManager.isAnimalCage(item))
        {
            return null;
        }


        return item.getItemMeta().getPersistentDataContainer().
            get(AnimalPenManager.ANIMAL_DATA_KEY, AnimalDataType.INSTANCE);
    }


    public static void setAnimalCageData(ItemStack item, @Nullable AnimalData animalData)
    {
        ItemMeta itemMeta = item.getItemMeta();
        AnimalPenManager.updateAnimalCageItemMeta(animalData, itemMeta);
        item.setItemMeta(itemMeta);
    }


    /**
     * Create an empty animal cage
     */
    public static ItemStack createEmptyAnimalCage()
    {
        ItemStack bottle = new ItemStack(Material.GLASS_BOTTLE);
        ItemMeta meta = bottle.getItemMeta();
        if (meta == null) return bottle;

        meta.displayName(AnimalPenPlugin.translations().
            getTranslatable("item.animal_pen.animal_cage.name").
            style(StyleUtil.WHITE));

        AnimalPenManager.updateAnimalCageItemMeta(null, meta);

        // Anti Stacking
        meta.setMaxStackSize(1);

        bottle.setItemMeta(meta);

        return bottle;
    }


    private static void updateAnimalCageItemMeta(@Nullable AnimalData animalData, ItemMeta itemMeta)
    {
        if (animalData == null || animalData.entityCount() <= 0)
        {
            itemMeta.getPersistentDataContainer().remove(AnimalPenManager.ANIMAL_DATA_KEY);

            itemMeta.lore(List.of(
                AnimalPenPlugin.translations().getTranslatable("item.animal_pen.animal_cage.catch_tip.line1"),
                AnimalPenPlugin.translations().getTranslatable("item.animal_pen.animal_cage.catch_tip.line2"),
                AnimalPenPlugin.translations().getTranslatable("item.animal_pen.animal_cage.catch_tip.line3")
            ));

            CustomModelDataComponent component = itemMeta.getCustomModelDataComponent();
            component.setStrings(List.of(ANIMAL_CAGE_MODEL));
            itemMeta.setCustomModelDataComponent(component);
        }
        else
        {
            itemMeta.getPersistentDataContainer().
                set(AnimalPenManager.ANIMAL_DATA_KEY, AnimalDataType.INSTANCE, animalData);

            itemMeta.lore(List.of(
                AnimalPenPlugin.translations().getTranslatable("item.animal_pen.animal_cage.description.top"),
                AnimalPenPlugin.translations().getTranslatable("item.animal_pen.animal_cage.description.entity",
                    Component.translatable(animalData.entityType().translationKey()).style(StyleUtil.YELLOW)),
                AnimalPenPlugin.translations().getTranslatable("item.animal_pen.animal_cage.description.amount",
                    Component.text(animalData.entityCount()).style(StyleUtil.YELLOW)),
                AnimalPenPlugin.translations().getTranslatable("item.animal_pen.animal_cage.catch_tip.line1"),
                AnimalPenPlugin.translations().getTranslatable("item.animal_pen.animal_cage.catch_tip.line2"),
                AnimalPenPlugin.translations().getTranslatable("item.animal_pen.animal_cage.catch_tip.line3"),
                AnimalPenPlugin.translations().getTranslatable("item.animal_pen.animal_cage.release_tip.line1"),
                AnimalPenPlugin.translations().getTranslatable("item.animal_pen.animal_cage.release_tip.line2")
            ));

            CustomModelDataComponent component = itemMeta.getCustomModelDataComponent();

            if (!component.getStrings().contains(AnimalPenManager.ANIMAL_CAGE_FILLED_MODEL))
            {
                component.setStrings(List.of(AnimalPenManager.ANIMAL_CAGE_FILLED_MODEL,
                    animalData.entityType().key().asString()));
                itemMeta.setCustomModelDataComponent(component);
            }
        }
    }


// ---------------------------------------------------------------------
// Section: Animal Pen related methods
// ---------------------------------------------------------------------


    /**
     * Return if given item is animal pen item.
     */
    public static boolean isAnimalPen(@NotNull ItemStack item)
    {
        if (item.getType() != Material.SMOOTH_STONE_SLAB)
        {
            return false;
        }

        ItemMeta meta = item.getItemMeta();

        if (meta == null || !meta.hasCustomModelDataComponent())
        {
            return false;
        }

        return meta.getCustomModelDataComponent().getStrings().contains(ANIMAL_PEN_MODEL);
    }


    /**
     * Return if given block is animal pen.
     */
    public static boolean isAnimalPen(@Nullable Block block)
    {
        if (block == null || block.getType() != Material.SMOOTH_STONE_SLAB)
        {
            return false;
        }

        NamespacedKey penKey = new NamespacedKey(AnimalPenPlugin.getInstance(),
            block.getX() + "_" + block.getY() + "_" + block.getZ() + "_animal_pen");

        return block.getWorld().getPersistentDataContainer().has(penKey, BlockDataType.INSTANCE);
    }


    public static boolean isAnimalPen(@NotNull Entity entity)
    {
        return entity.getPersistentDataContainer().has(ANIMAL_DATA_KEY,
            AnimalDataType.INSTANCE);
    }


    public static void completePenCreation(Block block, BlockData blockData, @NotNull ItemStack itemInHand)
    {
        NamespacedKey penKey = new NamespacedKey(AnimalPenPlugin.getInstance(),
            block.getX() + "_" + block.getY() + "_" + block.getZ() + "_animal_pen");

        // Create decoration entity
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

                    ItemStack itemStack = new ItemStack(itemInHand);
                    itemStack.setAmount(1);
                    display.setItemStack(itemStack);

                    Transformation transform = display.getTransformation();
                    transform.getScale().set(1.001f, 1f, 1.001f);
                    display.setTransformation(transform);
                }

                newEntity.getPersistentDataContainer().set(Helper.DECORATION_ENTITY_KEY,
                    PersistentDataType.STRING,
                    penKey.getKey());
            });

        blockData.decorationEntity = decorationEntity.getUniqueId();

        // Crate counter entity.
        Entity countEntity = block.getWorld().spawnEntity(
            block.getLocation().add(Utils.center(blockData.blockFace)),
            EntityType.TEXT_DISPLAY,
            CreatureSpawnEvent.SpawnReason.CUSTOM,
            (newEntity) ->
            {
                newEntity.setPersistent(false);
                newEntity.setRotation(Utils.blockFaceToYaw(blockData.blockFace), 0);

                if (newEntity instanceof TextDisplay display)
                {
                    display.setVisibleByDefault(true);
                    display.setSeeThrough(false);
                    display.text(Component.text(0));
                }

                newEntity.getPersistentDataContainer().set(Helper.COUNTER_ENTITY_KEY,
                    PersistentDataType.STRING,
                    penKey.getKey());
            });

        blockData.countEntity = countEntity.getUniqueId();

        // Save data.
        block.getWorld().getPersistentDataContainer().set(penKey, BlockDataType.INSTANCE, blockData);
    }


    public static void validateAnimalPen(@NotNull Entity entity)
    {
        Block block = entity.getLocation().getBlock();

        if (block.getType() != Material.SMOOTH_STONE_SLAB)
        {
            return;
        }

        NamespacedKey penKey = new NamespacedKey(AnimalPenPlugin.getInstance(),
            block.getX() + "_" + block.getY() + "_" + block.getZ() + "_animal_pen");

        BlockData blockData = block.getWorld().getPersistentDataContainer().get(penKey, BlockDataType.INSTANCE);

        if (blockData == null)
        {
            AnimalPenPlugin.getInstance().getLogger().warning("Failed to load animal pen block. Restoring...");
            blockData = new BlockData();
        }

        if (blockData.entity == null)
        {
            // fix
            blockData.entity = entity.getUniqueId();
            blockData.blockFace = entity.getFacing();

            Collection<Entity> nearbyEntities = block.getWorld().
                getNearbyEntities(block.getBoundingBox().expand(1),
                    findEntity -> findEntity.getType() == EntityType.TEXT_DISPLAY &&
                        findEntity.getFacing() == entity.getFacing() &&
                        findEntity.getPersistentDataContainer().has(penKey));

            if (!nearbyEntities.isEmpty())
            {
                blockData.countEntity = nearbyEntities.iterator().next().getUniqueId();
            }

            nearbyEntities = block.getWorld().
                getNearbyEntities(block.getBoundingBox().expand(1),
                    findEntity -> findEntity.getType() == EntityType.ITEM_DISPLAY &&
                        findEntity.getFacing() == entity.getFacing() &&
                        findEntity.getPersistentDataContainer().has(penKey));

            if (!nearbyEntities.isEmpty())
            {
                blockData.decorationEntity = nearbyEntities.iterator().next().getUniqueId();
            }

            block.getWorld().getPersistentDataContainer().set(penKey, BlockDataType.INSTANCE, blockData);
        }

        if (entity instanceof LivingEntity livingEntity)
        {
            // Validate attributes
            AttributeInstance attribute = livingEntity.getAttribute(Attribute.SCALE);

            if (attribute != null && Utils.getTagEntity(ANIMAL_CAGE_PICKABLE).isTagged(entity.getType()))
            {
                if (attribute.getBaseValue() != AnimalPenPlugin.configuration().getAnimalSize())
                {
                    attribute.setBaseValue(AnimalPenPlugin.configuration().getAnimalSize());
                }

                if (AnimalPenPlugin.configuration().isGrowAnimals())
                {
                    AnimalData animalData = AnimalPenManager.getAnimalData(entity);

                    if (animalData != null)
                    {
                        AttributeModifier modifier = attribute.getModifier(Utils.ANIMAL_SIZE_MODIFIER);
                        float multiplier =
                            AnimalPenPlugin.configuration().getGrowthMultiplier() *
                                animalData.entityCount();

                        if (modifier != null && modifier.getAmount() != multiplier)
                        {
                            attribute.getModifier(Utils.ANIMAL_SIZE_MODIFIER);

                            attribute.addModifier(new AttributeModifier(Utils.ANIMAL_SIZE_MODIFIER,
                                multiplier,
                                AttributeModifier.Operation.ADD_NUMBER
                            ));
                        }
                    }
                }
            }

            // Trigger pose update
            livingEntity.setPose(livingEntity.getPose());
        }
    }


    /**
     * Returns animal data associated with given block.
     */
    public static AnimalData getAnimalData(Block block)
    {
        NamespacedKey penKey = new NamespacedKey(AnimalPenPlugin.getInstance(),
            block.getX() + "_" + block.getY() + "_" + block.getZ() + "_animal_pen");

        BlockData blockData = block.getWorld().getPersistentDataContainer().get(penKey, BlockDataType.INSTANCE);

        if (blockData == null || blockData.entity == null)
        {
            // No data from animal pen.
            return null;
        }

        return AnimalPenManager.getAnimalData(block.getWorld().getEntity(blockData.entity));
    }


    /**
     * Returns animal data associated with given entity.
     */
    public static AnimalData getAnimalData(Entity entity)
    {
        if (entity == null)
        {
            AnimalPenPlugin.getInstance().getLogger().severe("Animal Pen entity is removed! Cannot access data!");
            return null;
        }

        return entity.getPersistentDataContainer().get(ANIMAL_DATA_KEY, AnimalDataType.INSTANCE);
    }


    public static void setAnimalPenData(Block block, AnimalData newData)
    {
        NamespacedKey penKey = new NamespacedKey(AnimalPenPlugin.getInstance(),
            block.getX() + "_" + block.getY() + "_" + block.getZ() + "_animal_pen");

        BlockData blockData = block.getWorld().getPersistentDataContainer().getOrDefault(penKey,
            BlockDataType.INSTANCE,
            new BlockData());

        Entity entity;

        // Entity to display
        if (blockData.entity == null || block.getWorld().getEntity(blockData.entity) == null)
        {
            if (newData.entitySnapshot() != null)
            {
                entity = newData.entitySnapshot().createEntity(block.getLocation().add(0.5, 0.5, 0.5));
            }
            else
            {
                entity = block.getWorld().spawnEntity(block.getLocation().add(0.5, 0.5, 0.5),
                    newData.entityType(),
                    CreatureSpawnEvent.SpawnReason.CUSTOM);
            }

            if (entity instanceof Sheep sheep)
            {
                newData.getAppliedFlag().ifPresent(sheep::setSheared);
                newData.getAppliedMaterial().ifPresent(dye -> sheep.setColor(Utils.getDyeColor(dye)));
            }
            else if (entity instanceof MushroomCow cow)
            {
                newData.getAppliedMaterial().ifPresent(dye -> {
                    SuspiciousEffectEntry suspiciousEffectEntry = Utils.FLOWER_EFFECTS.get(dye);

                    if (suspiciousEffectEntry != null)
                    {
                        cow.addEffectToNextStew(suspiciousEffectEntry, true);
                    }
                });
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
                    attribute.setBaseValue(AnimalPenPlugin.configuration().getAnimalSize());

                    if (AnimalPenPlugin.configuration().isGrowAnimals())
                    {
                        attribute.addModifier(new AttributeModifier(Utils.ANIMAL_SIZE_MODIFIER,
                            AnimalPenPlugin.configuration().getGrowthMultiplier() *
                                newData.entityCount(),
                            AttributeModifier.Operation.ADD_NUMBER
                        ));
                    }
                }
            }

            blockData.entity = entity.getUniqueId();

            // Link entity with block
            block.getWorld().getPersistentDataContainer().set(penKey,
                BlockDataType.INSTANCE,
                blockData);
        }
        else
        {
            entity = block.getWorld().getEntity(blockData.entity);

            if (AnimalPenPlugin.configuration().isGrowAnimals() &&
                entity instanceof LivingEntity livingEntity)
            {
                AttributeInstance attribute = livingEntity.getAttribute(Attribute.SCALE);

                if (attribute != null)
                {
                    attribute.removeModifier(Utils.ANIMAL_SIZE_MODIFIER);

                    attribute.addModifier(new AttributeModifier(Utils.ANIMAL_SIZE_MODIFIER,
                        AnimalPenPlugin.configuration().getGrowthMultiplier() * newData.entityCount(),
                        AttributeModifier.Operation.ADD_NUMBER
                    ));
                }
            }
        }

        if (entity == null)
        {
            AnimalPenPlugin.getInstance().getLogger().severe("Animal Pen entity is removed! Cannot access data!");
            return;
        }

        AnimalPenPlugin.getInstance().task.startTrackingEntity(entity, false, true);

        entity.getPersistentDataContainer().set(ANIMAL_DATA_KEY,
            AnimalDataType.INSTANCE,
            newData);

        Helper.updateCountTextEntity(block, blockData, newData.entityCount(), penKey);
    }


    public static void setAnimalPenData(Entity entity, AnimalData newData)
    {
        if (newData.entityCount() <= 0)
        {
            // Entity is removed. Do propper stuff.
            ItemStack itemStack = AnimalPenManager.createEmptyAnimalCage();
            entity.getWorld().dropItem(entity.getLocation(), itemStack);

            AnimalPenPlugin.getInstance().task.stopTrackingEntity(entity, true);

            entity.getPersistentDataContainer().remove(ANIMAL_DATA_KEY);
            entity.remove();
        }
        else
        {
            entity.getPersistentDataContainer().set(ANIMAL_DATA_KEY,
                AnimalDataType.INSTANCE,
                newData);

            if (AnimalPenPlugin.configuration().isGrowAnimals() &&
                entity instanceof LivingEntity livingEntity)
            {
                AttributeInstance attribute = livingEntity.getAttribute(Attribute.SCALE);

                if (attribute != null)
                {
                    attribute.removeModifier(Utils.ANIMAL_SIZE_MODIFIER);

                    attribute.addModifier(new AttributeModifier(Utils.ANIMAL_SIZE_MODIFIER,
                        AnimalPenPlugin.configuration().getGrowthMultiplier() * newData.entityCount(),
                        AttributeModifier.Operation.ADD_NUMBER
                    ));
                }
            }
        }

        Block block = entity.getLocation().getBlock();

        NamespacedKey penKey = new NamespacedKey(AnimalPenPlugin.getInstance(),
            block.getX() + "_" + block.getY() + "_" + block.getZ() + "_animal_pen");

        BlockData blockData = block.getWorld().getPersistentDataContainer().getOrDefault(penKey,
            BlockDataType.INSTANCE,
            new BlockData());

        Helper.updateCountTextEntity(block, blockData, newData.entityCount(), penKey);
    }


    public static void clearBlockData(Block block, boolean keepBlock)
    {
        NamespacedKey penKey = new NamespacedKey(AnimalPenPlugin.getInstance(),
            block.getX() + "_" + block.getY() + "_" + block.getZ() + "_animal_pen");

        BlockData blockData = block.getWorld().getPersistentDataContainer().get(penKey, BlockDataType.INSTANCE);

        if (blockData == null)
        {
            return;
        }

        AnimalPenPlugin.getInstance().task.stopTrackingEntity(blockData.entity, block.getWorld(), true);
        Helper.removeEntity(block.getWorld(), blockData.entity);

        blockData.entity = null;

        if (keepBlock)
        {
            Helper.updateCountTextEntity(block, blockData, 0, penKey);

            block.getWorld().getPersistentDataContainer().set(penKey,
                BlockDataType.INSTANCE,
                blockData);
        }
        else
        {
            Helper.removeEntity(block.getWorld(), blockData.countEntity);
            Helper.removeEntity(block.getWorld(), blockData.decorationEntity);

            block.getWorld().getPersistentDataContainer().remove(penKey);

            // BugFix - remove all display entities within 1 block
            block.getWorld().getNearbyEntitiesByType(Display.class, block.getLocation(), 1).
                forEach(display ->
                {
                    if (display.getPersistentDataContainer().has(penKey, PersistentDataType.BOOLEAN))
                    {
                        display.remove();
                    }
                });
        }
    }


    public static ItemStack getAnimalPenItem(Block block)
    {
        NamespacedKey penKey = new NamespacedKey(AnimalPenPlugin.getInstance(),
            block.getX() + "_" + block.getY() + "_" + block.getZ() + "_animal_pen");

        BlockData blockData = block.getWorld().getPersistentDataContainer().get(penKey, BlockDataType.INSTANCE);

        if (blockData == null || blockData.decorationEntity == null)
        {
            return AnimalPenManager.createAnimalPen();
        }

        Entity entity = block.getWorld().getEntity(blockData.decorationEntity);

        if (!(entity instanceof ItemDisplay display))
        {
            return AnimalPenManager.createAnimalPen();
        }

        return display.getItemStack();
    }


    /**
     * Create an oak animal pen
     */
    public static ItemStack createAnimalPen()
    {
        return AnimalPenManager.createAnimalPen("animal_pen_oak");
    }


    /**
     * Create an animal pen
     * @param type wood type
     */
    public static ItemStack createAnimalPen(String type)
    {
        ItemStack smoothStoneSlab = new ItemStack(Material.SMOOTH_STONE_SLAB);
        ItemMeta meta = smoothStoneSlab.getItemMeta();
        if (meta == null) return smoothStoneSlab;

        CustomModelDataComponent customData = meta.getCustomModelDataComponent();
        customData.setStrings(List.of(ANIMAL_PEN_MODEL, "animal_pen:" + type));
        meta.setCustomModelDataComponent(customData);

        meta.displayName(AnimalPenPlugin.translations().
            getTranslatable("item.animal_pen." + type).
            style(StyleUtil.WHITE));

        meta.lore(List.of(
            AnimalPenPlugin.translations().getTranslatable("item.animal_pen.animal_pen.tip.line1"),
            AnimalPenPlugin.translations().getTranslatable("item.animal_pen.animal_pen.tip.line2"),
            AnimalPenPlugin.translations().getTranslatable("item.animal_pen.animal_pen.tip.line3")
        ));

        smoothStoneSlab.setItemMeta(meta);

        return smoothStoneSlab;
    }


// ---------------------------------------------------------------------
// Section: Processing methods
// ---------------------------------------------------------------------


    public static void applyVariant(Entity entity, EntitySnapshot selectedVariant)
    {
        if (selectedVariant == null)
        {
            return;
        }

        Block block = entity.getLocation().add(0, -0.5, 0).getBlock();
        NamespacedKey penKey = new NamespacedKey(AnimalPenPlugin.getInstance(),
            block.getX() + "_" + block.getY() + "_" + block.getZ() + "_animal_pen");

        BlockData blockData = block.getWorld().getPersistentDataContainer().get(penKey, BlockDataType.INSTANCE);

        if (blockData == null)
        {
            return;
        }

        AnimalData animalData = AnimalPenManager.getAnimalData(entity);

        if (animalData == null)
        {
            return;
        }

        animalData.setAppliedMaterial(null);
        animalData.setAppliedFlag(null);

        animalData.setEntitySnapshot(selectedVariant);
        blockData.entity = null;

        // Update animal pen by removing saved entity reference.
        block.getWorld().getPersistentDataContainer().set(penKey, BlockDataType.INSTANCE, blockData);

        // Remove entity from world
        AnimalPenPlugin.getInstance().task.stopTrackingEntity(entity, false);
        entity.getPersistentDataContainer().remove(ANIMAL_DATA_KEY);
        AnimalPenVariantMenu.close(entity);
        entity.remove();

        // Trigger new entity creation
        AnimalPenManager.setAnimalPenData(block, animalData);
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


    public final static NamespacedKey ANIMAL_DATA_KEY = new NamespacedKey("animal_pen", "animal_pen_data");

    public final static NamespacedKey ANIMAL_CAGE_PICKABLE = new NamespacedKey("animal_pen", "animal_cage_pickable");

    public final static String ANIMAL_CAGE_MODEL = "animal_pen:animal_cage";

    private final static String ANIMAL_CAGE_FILLED_MODEL = "animal_pen:animal_cage_filled";

    public final static String ANIMAL_PEN_MODEL = "animal_pen:animal_pen";
}