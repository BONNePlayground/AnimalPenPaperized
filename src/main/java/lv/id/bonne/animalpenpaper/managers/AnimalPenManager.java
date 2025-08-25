package lv.id.bonne.animalpenpaper.managers;


import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.damage.CraftDamageSource;
import org.bukkit.craftbukkit.entity.*;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.AxolotlBucketMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.inventory.meta.TropicalFishBucketMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.time.LocalTime;
import java.util.*;

import io.papermc.paper.potion.SuspiciousEffectEntry;
import lv.id.bonne.animalpenpaper.AnimalPenPlugin;
import lv.id.bonne.animalpenpaper.data.AnimalData;
import lv.id.bonne.animalpenpaper.data.AnimalDataType;
import lv.id.bonne.animalpenpaper.data.BlockData;
import lv.id.bonne.animalpenpaper.data.BlockDataType;
import lv.id.bonne.animalpenpaper.util.StyleUtil;
import lv.id.bonne.animalpenpaper.util.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.advancements.CriteriaTriggers;


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

        if (animalData.getVariants().size() <= AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getMaxStoredVariants())
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

        meta.displayName(Component.text("Animal Cage").style(StyleUtil.WHITE));
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
            itemMeta.lore(List.of(Component.empty(),
                Component.text("Right-click on animal to catch it").style(StyleUtil.GRAY)));

            CustomModelDataComponent component = itemMeta.getCustomModelDataComponent();
            component.setStrings(List.of(ANIMAL_CAGE_MODEL));
            itemMeta.setCustomModelDataComponent(component);
        }
        else
        {
            itemMeta.getPersistentDataContainer().
                set(AnimalPenManager.ANIMAL_DATA_KEY, AnimalDataType.INSTANCE, animalData);

            itemMeta.lore(List.of(Component.empty(),
                Component.text("Animal: ").style(StyleUtil.GRAY).
                    append(Component.translatable(animalData.entityType().translationKey())),
                Component.text("Count: ").style(StyleUtil.GRAY).
                    append(Component.text(animalData.entityCount())),
                Component.empty(),
                Component.text("Shift Right-click on block to release it").style(StyleUtil.GRAY)));

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
            block.getLocation().add(0.5, 0.75, 0.5),
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
                    transform.getScale().set(0.99f, 0.99f, 0.99f);
                    display.setTransformation(transform);
                }

                newEntity.getPersistentDataContainer().set(penKey, PersistentDataType.BOOLEAN, true);
            });

        blockData.decorationEntity = decorationEntity.getUniqueId();

        // Crate counter entity.
        Entity countEntity = block.getWorld().spawnEntity(
            block.getLocation().add(Utils.center(blockData.blockFace)),
            EntityType.TEXT_DISPLAY,
            CreatureSpawnEvent.SpawnReason.CUSTOM,
            (newEntity) ->
            {
                newEntity.setPersistent(true);
                newEntity.setRotation(Utils.blockFaceToYaw(blockData.blockFace), 0);

                if (newEntity instanceof TextDisplay display)
                {
                    display.setVisibleByDefault(true);
                    display.setSeeThrough(false);
                    display.text(Component.text(0));
                }

                newEntity.getPersistentDataContainer().set(penKey, PersistentDataType.BOOLEAN, true);
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

            if (attribute != null &&
                livingEntity instanceof Animals)
            {
                if (attribute.getBaseValue() != AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getAnimalSize())
                {
                    attribute.setBaseValue(AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getAnimalSize());
                }

                if (AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().isGrowAnimals())
                {
                    AnimalData animalData = AnimalPenManager.getAnimalData(entity);

                    if (animalData != null)
                    {
                        AttributeModifier modifier = attribute.getModifier(Utils.ANIMAL_SIZE_MODIFIER);
                        float multiplier =
                            AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getGrowthMultiplier() *
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
                    attribute.setBaseValue(AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getAnimalSize());

                    if (AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().isGrowAnimals())
                    {
                        attribute.addModifier(new AttributeModifier(Utils.ANIMAL_SIZE_MODIFIER,
                            AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getGrowthMultiplier() *
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

            if (AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().isGrowAnimals() &&
                entity instanceof LivingEntity livingEntity)
            {
                AttributeInstance attribute = livingEntity.getAttribute(Attribute.SCALE);

                if (attribute != null)
                {
                    attribute.removeModifier(Utils.ANIMAL_SIZE_MODIFIER);

                    attribute.addModifier(new AttributeModifier(Utils.ANIMAL_SIZE_MODIFIER,
                        AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getGrowthMultiplier() * newData.entityCount(),
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

        AnimalPenPlugin.getInstance().task.startTrackingEntity(entity, false);

        entity.getPersistentDataContainer().set(ANIMAL_DATA_KEY,
            AnimalDataType.INSTANCE,
            newData);

        updateCountTextEntity(block, blockData, newData.entityCount(), penKey);
    }


    public static void setAnimalPenData(Entity entity, AnimalData newData)
    {
        if (newData.entityCount() <= 0)
        {
            // Entity is removed. Do propper stuff.
            ItemStack itemStack = AnimalPenManager.createEmptyAnimalCage();
            entity.getWorld().dropItem(entity.getLocation(), itemStack);

            AnimalPenPlugin.getInstance().task.stopTrackingEntity(entity);

            entity.getPersistentDataContainer().remove(ANIMAL_DATA_KEY);
            entity.remove();
        }
        else
        {
            entity.getPersistentDataContainer().set(ANIMAL_DATA_KEY,
                AnimalDataType.INSTANCE,
                newData);

            if (AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().isGrowAnimals() &&
                entity instanceof LivingEntity livingEntity)
            {
                AttributeInstance attribute = livingEntity.getAttribute(Attribute.SCALE);

                if (attribute != null)
                {
                    attribute.removeModifier(Utils.ANIMAL_SIZE_MODIFIER);

                    attribute.addModifier(new AttributeModifier(Utils.ANIMAL_SIZE_MODIFIER,
                        AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getGrowthMultiplier() * newData.entityCount(),
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

        updateCountTextEntity(block, blockData, newData.entityCount(), penKey);
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

        AnimalPenPlugin.getInstance().task.stopTrackingEntity(blockData.entity, block.getWorld());
        AnimalPenManager.removeEntity(block.getWorld(), blockData.entity);

        blockData.entity = null;

        if (keepBlock)
        {
            AnimalPenManager.updateCountTextEntity(block, blockData, 0, penKey);

            block.getWorld().getPersistentDataContainer().set(penKey,
                BlockDataType.INSTANCE,
                blockData);
        }
        else
        {
            AnimalPenManager.removeEntity(block.getWorld(), blockData.countEntity);
            AnimalPenManager.removeEntity(block.getWorld(), blockData.decorationEntity);

            block.getWorld().getPersistentDataContainer().remove(penKey);
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


    public static void removeEntity(World world, @Nullable UUID uuid)
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


    /**
     * Create an animal pen
     */
    public static ItemStack createAnimalPen()
    {
        ItemStack smoothStoneSlab = new ItemStack(Material.SMOOTH_STONE_SLAB);
        ItemMeta meta = smoothStoneSlab.getItemMeta();
        if (meta == null) return smoothStoneSlab;

        CustomModelDataComponent customData = meta.getCustomModelDataComponent();
        customData.setStrings(List.of(ANIMAL_PEN_MODEL, "animal_pen:animal_pen_oak"));
        meta.setCustomModelDataComponent(customData);

        meta.displayName(Component.text("Animal Pen").style(StyleUtil.WHITE));

        meta.lore(List.of(Component.empty(),
            Component.text("Insert Animal Cage to interact with animal.").style(StyleUtil.GRAY)));

        smoothStoneSlab.setItemMeta(meta);

        return smoothStoneSlab;
    }


    private static void updateCountTextEntity(Block block, BlockData blockData, long entityCount, NamespacedKey penKey)
    {
        Entity entity;

        if (blockData.countEntity == null || block.getWorld().getEntity(blockData.countEntity) == null)
        {
            entity = block.getWorld().spawnEntity(
                block.getLocation().add(Utils.center(blockData.blockFace)),
                EntityType.TEXT_DISPLAY,
                CreatureSpawnEvent.SpawnReason.CUSTOM,
                (newEntity) ->
                {
                    newEntity.setPersistent(true);
                    newEntity.setRotation(Utils.blockFaceToYaw(blockData.blockFace), 0);

                    if (newEntity instanceof TextDisplay display)
                    {
                        display.setVisibleByDefault(true);
                        display.setSeeThrough(false);
                    }

                    newEntity.getPersistentDataContainer().set(penKey, PersistentDataType.BOOLEAN, true);
                });

            blockData.countEntity = entity.getUniqueId();

            // Link entity with block
            block.getWorld().getPersistentDataContainer().set(penKey,
                BlockDataType.INSTANCE,
                blockData);
        }
        else
        {
            entity = block.getWorld().getEntity(blockData.countEntity);
        }

        if (entity instanceof TextDisplay display)
        {
            display.text(Component.text(entityCount));
        }
    }


    public static List<Pair<Material, Component>> generateTextMessages(Entity entity, AnimalData animalData, int tick)
    {
        EntityType entityType = entity.getType();

        List<Pair<Material, Component>> lines = new ArrayList<>(4);

        if (AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
            entityType,
            Material.APPLE,
            animalData.entityCount()) != 0)
        {
            // Food Items.

            List<Material> foodItems =
                AnimalPenPlugin.CONFIG_MANAGER.getAnimalFoodConfiguration().getFoodItems(entityType);

            Material foodItem;

            if (foodItems != null && !foodItems.isEmpty())
            {
                Component component;

                if (!animalData.hasCooldown(Material.APPLE))
                {
                    component = Component.text("Ready!           ").
                        style(Style.style().color(TextColor.color(5635925)).build());
                }
                else
                {
                    component = Component.text("Cooldown: ").append(
                        Component.text(LocalTime.of(0, 0, 0).
                            plusSeconds(animalData.getCooldown(Material.APPLE) / 20).
                            format(Utils.DATE_FORMATTER)));
                }

                if (foodItems.size() == 1)
                {
                    foodItem = foodItems.getFirst();
                }
                else
                {
                    int size = foodItems.size();
                    int index = (tick / 100) % size;

                    foodItem = foodItems.get(index);
                }

                lines.add(Pair.of(foodItem, component));
            }
        }

        switch (entityType)
        {
            case ARMADILLO ->
            {
                Pair<Material, Component> textPairMessage = createTextPairMessage(entityType,
                    animalData,
                    Material.BRUSH,
                    Material.ARMADILLO_SCUTE);

                if (textPairMessage != null)
                {
                    lines.add(textPairMessage);
                }
            }
            case AXOLOTL ->
            {
                Pair<Material, Component> textPairMessage = createTextPairMessage(entityType,
                    animalData,
                    Material.WATER_BUCKET,
                    Material.AXOLOTL_BUCKET);

                if (textPairMessage != null)
                {
                    lines.add(textPairMessage);
                }
            }
            case COD ->
            {
                Pair<Material, Component> textPairMessage = createTextPairMessage(entityType,
                    animalData,
                    Material.WATER_BUCKET,
                    Material.COD_BUCKET);

                if (textPairMessage != null)
                {
                    lines.add(textPairMessage);
                }
            }
            case SALMON ->
            {
                Pair<Material, Component> textPairMessage = createTextPairMessage(entityType,
                    animalData,
                    Material.WATER_BUCKET,
                    Material.SALMON_BUCKET);

                if (textPairMessage != null)
                {
                    lines.add(textPairMessage);
                }
            }
            case PUFFERFISH ->
            {
                Pair<Material, Component> textPairMessage = createTextPairMessage(entityType,
                    animalData,
                    Material.WATER_BUCKET,
                    Material.PUFFERFISH_BUCKET);

                if (textPairMessage != null)
                {
                    lines.add(textPairMessage);
                }
            }
            case TROPICAL_FISH ->
            {
                Pair<Material, Component> textPairMessage = createTextPairMessage(entityType,
                    animalData,
                    Material.WATER_BUCKET,
                    Material.TROPICAL_FISH_BUCKET);

                if (textPairMessage != null)
                {
                    lines.add(textPairMessage);
                }
            }
            case TADPOLE ->
            {
                Pair<Material, Component> textPairMessage = createTextPairMessage(entityType,
                    animalData,
                    Material.WATER_BUCKET,
                    Material.TADPOLE_BUCKET);

                if (textPairMessage != null)
                {
                    lines.add(textPairMessage);
                }
            }
            case BEE ->
            {
                Pair<Material, Component> textPairMessage;

                if (!animalData.hasCooldown(Material.SHEARS) &&
                    !animalData.hasCooldown(Material.GLASS_BOTTLE))
                {
                    if ((tick / 100) % 2 == 0)
                    {
                        textPairMessage = createTextPairMessage(entityType,
                            animalData,
                            Material.SHEARS,
                            Material.HONEYCOMB);
                    }
                    else
                    {
                        textPairMessage = createTextPairMessage(entityType,
                            animalData,
                            Material.GLASS_BOTTLE,
                            Material.HONEY_BOTTLE);
                    }
                }
                else if (animalData.hasCooldown(Material.SHEARS))
                {
                    textPairMessage = createTextPairMessage(entityType,
                        animalData,
                        Material.SHEARS,
                        Material.HONEYCOMB);
                }
                else
                {
                    textPairMessage = createTextPairMessage(entityType,
                        animalData,
                        Material.GLASS_BOTTLE,
                        Material.HONEY_BOTTLE);
                }

                lines.add(textPairMessage);
            }
            case CHICKEN ->
            {
                Pair<Material, Component> textPairMessage = createTextPairMessage(entityType,
                    animalData,
                    Material.BUCKET,
                    Material.EGG);

                if (textPairMessage != null)
                {
                    lines.add(textPairMessage);
                }
            }
            case TURTLE ->
            {
                Pair<Material, Component> textPairMessage = createTextPairMessage(entityType,
                    animalData,
                    Material.BUCKET,
                    Material.TURTLE_EGG);

                if (textPairMessage != null)
                {
                    lines.add(textPairMessage);
                }
            }
            case COW, GOAT ->
            {
                Pair<Material, Component> textPairMessage = createTextPairMessage(entityType,
                    animalData,
                    Material.BUCKET,
                    Material.MILK_BUCKET);

                if (textPairMessage != null)
                {
                    lines.add(textPairMessage);
                }
            }
            case FROG ->
            {
                Pair<Material, Component> textPairMessage = createTextPairMessage(entityType,
                    animalData,
                    Material.MAGMA_BLOCK,
                    AnimalPenManager.getFrogLight((Frog) entity));

                if (textPairMessage != null)
                {
                    lines.add(textPairMessage);
                }
            }
            case MOOSHROOM ->
            {
                Pair<Material, Component> textPairMessage = createTextPairMessage(entityType,
                    animalData,
                    Material.BUCKET,
                    Material.MILK_BUCKET);

                if (textPairMessage != null)
                {
                    lines.add(textPairMessage);
                }

                textPairMessage = createTextPairMessage(entityType,
                    animalData,
                    Material.BOWL,
                    Material.MUSHROOM_STEW);

                if (textPairMessage != null)
                {
                    lines.add(textPairMessage);
                }
            }
            case SHEEP ->
            {
                Pair<Material, Component> textPairMessage = createTextPairMessage(entityType,
                    animalData,
                    Material.SHEARS,
                    AnimalPenManager.getWoolMaterial(((Sheep) entity).getColor()));

                if (textPairMessage != null)
                {
                    lines.add(textPairMessage);
                }

                textPairMessage = createTextPairMessage(entityType,
                    animalData,
                    Material.WHITE_DYE,
                    AnimalPenManager.getWoolMaterial(((Sheep) entity).getColor()));

                if (textPairMessage != null)
                {
                    lines.add(textPairMessage);
                }
            }
            case SNIFFER ->
            {
                Pair<Material, Component> textPairMessage = createTextPairMessage(entityType,
                    animalData,
                    Material.BUCKET,
                    Material.SNIFFER_EGG);

                if (textPairMessage != null)
                {
                    lines.add(textPairMessage);
                }

                textPairMessage = createTextPairMessage(entityType,
                    animalData,
                    Material.BOWL,
                    (tick / 100) % 2 == 0 ? Material.TORCHFLOWER_SEEDS : Material.PITCHER_POD);

                if (textPairMessage != null)
                {
                    lines.add(textPairMessage);
                }
            }
        }

        return lines;
    }


    private static Pair<Material, Component> createTextPairMessage(
        EntityType entityType,
        AnimalData animalData,
        Material material,
        Material result)
    {
        if (AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
            entityType,
            material,
            animalData.entityCount()) == 0 ||
            result.isEmpty())
        {
            return null;
        }

        Component component;

        Material icon;

        if (!animalData.hasCooldown(material))
        {
            component = Component.text("Ready!           ").
                style(Style.style().color(TextColor.color(5635925)).build());
            icon = material;
        }
        else
        {
            component = Component.text("Cooldown: ").append(
                Component.text(LocalTime.of(0, 0, 0).
                    plusSeconds(animalData.getCooldown(material) / 20).
                    format(Utils.DATE_FORMATTER)));
            icon = result;
        }

        return Pair.of(icon, component);
    }


// ---------------------------------------------------------------------
// Section: Entity Methods
// ---------------------------------------------------------------------


    public static void handleFood(LivingEntity entity, Player player, ItemStack itemStack)
    {
        AnimalData data = AnimalPenManager.getAnimalData(entity);

        if (data == null)
        {
            return;
        }

        if (data.hasCooldown(Material.APPLE))
        {
            // under cooldown for feeding
            return;
        }

        long maxCount = AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getMaximalAnimalCount();

        if (maxCount > 0 && data.entityCount() >= maxCount)
        {
            // Too many entities already in pen
            return;
        }

        int stackSize = itemStack.getAmount();

        if (entity.getType() == EntityType.AXOLOTL &&
            itemStack.getType() == Material.TROPICAL_FISH_BUCKET &&
            itemStack.getMaxStackSize() == 1)
        {
            // Tropical fishes will be taken from all buckets in player inventory.
            Map<Integer, ? extends ItemStack> all = player.getInventory().all(Material.TROPICAL_FISH_BUCKET);
            stackSize = all.size();
        }

        stackSize = (int) Math.min(data.entityCount(), stackSize);

        if (stackSize < 2)
        {
            // Cannot feed 1 animal only for breeding.
            return;
        }

        stackSize = (int) Math.min((maxCount - data.entityCount()) * 2, stackSize);

        Utils.triggerItemUse(entity, player, itemStack, stackSize % 2 == 1 ? stackSize - 1 : stackSize);

        if (player.getGameMode() != GameMode.CREATIVE)
        {
            if (entity.getType() == EntityType.AXOLOTL &&
                itemStack.getType() == Material.TROPICAL_FISH_BUCKET &&
                itemStack.getMaxStackSize() == 1)
            {
                int removedItems = stackSize % 2 == 1 ? stackSize - 1 : stackSize;

                while (removedItems-- > 0)
                {
                    int slot = player.getInventory().first(Material.TROPICAL_FISH_BUCKET);

                    if (slot != -1)
                    {
                        player.getInventory().setItem(slot, null);
                    }
                }
            }
            else if (stackSize % 2 == 1)
            {
                itemStack.subtract(stackSize - 1);
            }
            else
            {
                itemStack.subtract(stackSize);
            }
        }

        int amount = stackSize / 2;
        data.addEntityCount(amount);

        entity.getWorld().spawnParticle(Particle.HEART,
            entity.getLocation(),
            5,
            0.2, 0.2, 0.2,
            0.05);

        entity.getWorld().playSound(entity,
            entity.getEatingSound(itemStack),
            new Random().nextFloat(0.8f, 1.2f),
            1);

        player.swingMainHand();

        data.setCooldown(Material.APPLE,
            AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                entity.getType(),
                Material.APPLE,
                stackSize));

        if (entity.getType() == EntityType.TURTLE)
        {
            // Handle scutes
            data.setScutes(amount);

            if (AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().isDropScuteAtStart())
            {
                // Drop scutes at the start.
                AnimalPenManager.handleScutes(entity, data);
            }
        }

        // Save data
        AnimalPenManager.setAnimalPenData(entity, data);

        if (!(entity instanceof Breedable))
        {
            // Ignore non-breedable mobs
            return;
        }

        if (AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().isTriggerAdvancements())
        {
            // Trigger event and statistics for breeding.
            for (int i = 0; i < amount; i++)
            {
                CriteriaTriggers.BRED_ANIMALS.trigger(((CraftPlayer) player).getHandle(),
                    ((CraftAnimals) entity).getHandle(),
                    ((CraftAnimals) entity).getHandle(),
                    ((CraftAnimals) entity).getHandle());
            }
        }

        if (AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().isIncreaseStatistics())
        {
            player.incrementStatistic(Statistic.ANIMALS_BRED, amount);
        }
    }


    public static void handleBrush(Entity entity, Player player, ItemStack itemStack)
    {
        if (entity.getType() != EntityType.ARMADILLO)
        {
            // Only armadillo can be interacted with brush
            return;
        }

        AnimalData data = AnimalPenManager.getAnimalData(entity);

        if (data == null)
        {
            return;
        }

        if (data.hasCooldown(Material.BRUSH))
        {
            // under cooldown for feeding
            return;
        }

        Utils.triggerItemUse(entity, player, itemStack, 1);

        if (player.getGameMode() != GameMode.CREATIVE)
        {
            itemStack.damage(16, player);
        }

        Utils.dropItems(entity.getWorld(),
            entity.getLocation().add(0, 1, 0),
            Material.ARMADILLO_SCUTE,
            1);

        entity.getWorld().playSound(entity,
            Sound.ENTITY_ARMADILLO_BRUSH,
            new Random().nextFloat(0.8f, 1.2f),
            1);

        player.swingMainHand();

        data.setCooldown(Material.BRUSH,
            AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                entity.getType(),
                Material.BRUSH,
                data.entityCount()));

        // Save data
        AnimalPenManager.setAnimalPenData(entity, data);
    }


    public static void handleWaterBucket(Entity entity, Player player, ItemStack itemStack)
    {
        if (entity.getType() != EntityType.AXOLOTL &&
            entity.getType() != EntityType.TROPICAL_FISH &&
            entity.getType() != EntityType.SALMON &&
            entity.getType() != EntityType.PUFFERFISH &&
            entity.getType() != EntityType.COD &&
            entity.getType() != EntityType.TADPOLE)
        {
            // Only axolotl and fishes can be interacted with water bucket
            return;
        }

        AnimalData data = AnimalPenManager.getAnimalData(entity);

        if (data == null)
        {
            return;
        }

        if (data.hasCooldown(Material.WATER_BUCKET))
        {
            // under cooldown for feeding
            return;
        }

        ItemStack newBucket;
        Sound sound;

        switch (entity.getType())
        {
            case AXOLOTL ->
            {
                newBucket = new ItemStack(Material.AXOLOTL_BUCKET);
                AxolotlBucketMeta itemMeta = (AxolotlBucketMeta) newBucket.getItemMeta();
                Axolotl axolotl = (Axolotl) entity;
                itemMeta.setVariant(axolotl.getVariant());
                newBucket.setItemMeta(itemMeta);

                sound = Sound.ITEM_BUCKET_FILL_FISH;
            }
            case COD ->
            {
                newBucket = new ItemStack(Material.COD_BUCKET);
                sound = Sound.ITEM_BUCKET_FILL_FISH;
            }
            case PUFFERFISH ->
            {
                newBucket = new ItemStack(Material.PUFFERFISH_BUCKET);
                sound = Sound.ITEM_BUCKET_FILL_FISH;
            }
            case SALMON ->
            {
                newBucket = new ItemStack(Material.SALMON_BUCKET);
                sound = Sound.ITEM_BUCKET_FILL_FISH;
            }
            case TADPOLE ->
            {
                newBucket = new ItemStack(Material.TADPOLE_BUCKET);
                sound = Sound.ITEM_BUCKET_FILL_TADPOLE;
            }
            case TROPICAL_FISH ->
            {
                newBucket = new ItemStack(Material.TROPICAL_FISH_BUCKET);
                TropicalFishBucketMeta itemMeta = (TropicalFishBucketMeta) newBucket.getItemMeta();
                TropicalFish tropicalFish = (TropicalFish) entity;
                itemMeta.setBodyColor(tropicalFish.getBodyColor());
                itemMeta.setPattern(tropicalFish.getPattern());
                itemMeta.setPatternColor(tropicalFish.getPatternColor());
                newBucket.setItemMeta(itemMeta);

                sound = Sound.ITEM_BUCKET_FILL_FISH;
            }
            default ->
            {
                // Should not ever happen.
                return;
            }
        }

        data.reduceEntityCount(1);

        Utils.triggerItemUse(entity, player, itemStack, 1);

        if (player.getGameMode() != GameMode.CREATIVE)
        {
            itemStack.subtract();
        }

        player.getInventory().addItem(newBucket);

        entity.getWorld().playSound(entity,
            sound,
            new Random().nextFloat(0.8f, 1.2f),
            1);

        player.swingMainHand();

        data.setCooldown(Material.WATER_BUCKET,
            AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                entity.getType(),
                Material.WATER_BUCKET,
                data.entityCount()));

        // Save data
        AnimalPenManager.setAnimalPenData(entity, data);

        if (AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().isTriggerAdvancements())
        {
            // Trigger bucket filling
            CriteriaTriggers.FILLED_BUCKET.trigger(((CraftPlayer) player).getHandle(),
                ((CraftItemStack) newBucket).handle);
        }
    }


    public static void handleShears(Entity entity, Player player, ItemStack itemStack)
    {
        if (entity.getType() == EntityType.SHEEP)
        {
            AnimalPenManager.handleShearsWool(entity, player, itemStack);
        }
        else if (entity.getType() == EntityType.BEE)
        {
            AnimalPenManager.handleShearsHoney(entity, player, itemStack);
        }
    }


    public static void handleShearsHoney(Entity entity, Player player, ItemStack itemStack)
    {
        if (entity.getType() != EntityType.BEE)
        {
            // Only sheep can be interacted with shears
            return;
        }

        AnimalData data = AnimalPenManager.getAnimalData(entity);

        if (data == null)
        {
            return;
        }

        if (data.hasCooldown(Material.SHEARS) || data.hasCooldown(Material.GLASS_BOTTLE))
        {
            // under cooldown for feeding
            return;
        }

        Utils.triggerItemUse(entity, player, itemStack, 1);

        if (player.getGameMode() != GameMode.CREATIVE)
        {
            itemStack.damage(1, player);
        }

        Utils.dropItems(entity.getWorld(),
            entity.getLocation().add(0, 1, 0),
            Material.HONEYCOMB,
            3);

        player.swingMainHand();

        entity.getWorld().playSound(entity,
            Sound.BLOCK_BEEHIVE_SHEAR,
            new Random().nextFloat(0.8f, 1.2f),
            1);

        data.setCooldown(Material.SHEARS,
            AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                entity.getType(),
                Material.SHEARS,
                data.entityCount()));

        // Save data
        AnimalPenManager.setAnimalPenData(entity, data);
    }


    public static void handleShearsWool(Entity entity, Player player, ItemStack itemStack)
    {
        if (entity.getType() != EntityType.SHEEP)
        {
            // Only sheep can be interacted with shears
            return;
        }

        AnimalData data = AnimalPenManager.getAnimalData(entity);

        if (data == null)
        {
            return;
        }

        if (data.hasCooldown(Material.SHEARS))
        {
            // under cooldown for feeding
            return;
        }

        Utils.triggerItemUse(entity, player, itemStack, 1);

        if (player.getGameMode() != GameMode.CREATIVE)
        {
            itemStack.damage(1, player);
        }

        Sheep sheep = (Sheep) entity;
        sheep.shear();

        Material woolMaterial = AnimalPenManager.getWoolMaterial(sheep.getColor());

        int woolCount = 1;

        int dropLimits = AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getDropLimits(Material.WHITE_WOOL);

        if (dropLimits <= 0)
        {
            dropLimits = Integer.MAX_VALUE;
        }

        Random random = new Random();

        for (int i = 0; i < data.entityCount() && woolCount < dropLimits; i++)
        {
            woolCount += random.nextInt(3);
        }

        Utils.dropItems(entity.getWorld(),
            entity.getLocation().add(0, 1, 0),
            woolMaterial,
            woolCount);

        player.swingMainHand();

        data.setCooldown(Material.SHEARS,
            AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                entity.getType(),
                Material.SHEARS,
                data.entityCount()));

        // Save data
        AnimalPenManager.setAnimalPenData(entity, data);
    }


    public static void handleBucket(Entity entity, Player player, ItemStack itemStack)
    {
        if (entity.getType() == EntityType.CHICKEN ||
            entity.getType() == EntityType.SNIFFER ||
            entity.getType() == EntityType.TURTLE)
        {
            AnimalPenManager.handleBucketEggs(entity, player, itemStack);
        }
        else if (entity.getType() == EntityType.COW ||
            entity.getType() == EntityType.MOOSHROOM ||
            entity.getType() == EntityType.GOAT)
        {
            AnimalPenManager.handleBucketMilk(entity, player, itemStack);
        }
    }


    public static void handleBucketMilk(Entity entity, Player player, ItemStack itemStack)
    {
        if (entity.getType() != EntityType.COW &&
            entity.getType() != EntityType.MOOSHROOM &&
            entity.getType() != EntityType.GOAT)
        {
            // Only COW, MOOSHROOM and GOAT can be interacted with bucket to get milk
            return;
        }

        AnimalData data = AnimalPenManager.getAnimalData(entity);

        if (data == null)
        {
            return;
        }

        if (data.hasCooldown(Material.BUCKET))
        {
            // under cooldown for feeding
            return;
        }

        Utils.triggerItemUse(entity, player, itemStack, 1);

        if (player.getGameMode() != GameMode.CREATIVE)
        {
            itemStack.subtract();
        }

        player.getInventory().addItem(new ItemStack(Material.MILK_BUCKET));

        player.swingMainHand();

        entity.getWorld().playSound(entity,
            entity.getType() == EntityType.GOAT ? Sound.ENTITY_GOAT_MILK : Sound.ENTITY_COW_MILK,
            new Random().nextFloat(0.8f, 1.2f),
            1);

        data.setCooldown(Material.BUCKET,
            AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                entity.getType(),
                Material.BUCKET,
                data.entityCount()));

        // Save data
        AnimalPenManager.setAnimalPenData(entity, data);
    }


    public static void handleGlassBottle(Entity entity, Player player, ItemStack itemStack)
    {
        if (entity.getType() != EntityType.BEE)
        {
            // Only bee has glass bottle interaction
            return;
        }

        AnimalData data = AnimalPenManager.getAnimalData(entity);

        if (data == null)
        {
            return;
        }

        if (data.hasCooldown(Material.SHEARS) || data.hasCooldown(Material.GLASS_BOTTLE))
        {
            // under cooldown for feeding
            return;
        }

        Utils.triggerItemUse(entity, player, itemStack, 1);

        if (player.getGameMode() != GameMode.CREATIVE)
        {
            itemStack.subtract();
        }

        player.getInventory().addItem(new ItemStack(Material.HONEY_BOTTLE));

        player.swingMainHand();

        entity.getWorld().playSound(entity,
            Sound.BLOCK_BEEHIVE_DRIP,
            new Random().nextFloat(0.8f, 1.2f),
            1);

        data.setCooldown(Material.GLASS_BOTTLE,
            AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                entity.getType(),
                Material.GLASS_BOTTLE,
                data.entityCount()));

        // Save data
        AnimalPenManager.setAnimalPenData(entity, data);
    }


    public static void handleBucketEggs(Entity entity, Player player, ItemStack itemStack)
    {
        if (entity.getType() != EntityType.CHICKEN &&
            entity.getType() != EntityType.SNIFFER &&
            entity.getType() != EntityType.TURTLE)
        {
            // Only chicken, snigger and turtle can be interacted with bucket to get eggs
            return;
        }

        AnimalData data = AnimalPenManager.getAnimalData(entity);

        if (data == null)
        {
            return;
        }

        if (data.hasCooldown(Material.BUCKET))
        {
            // under cooldown for feeding
            return;
        }

        Material material;
        Sound sound;

        switch (entity.getType())
        {
            case CHICKEN ->
            {
                material = Material.EGG;
                sound = Sound.ENTITY_CHICKEN_EGG;
            }
            case SNIFFER ->
            {
                material = Material.SNIFFER_EGG;
                sound = Sound.BLOCK_SNIFFER_EGG_PLOP;
            }
            case TURTLE ->
            {
                material = Material.TURTLE_EGG;
                sound = Sound.ENTITY_TURTLE_LAY_EGG;
            }
            default ->
            {
                return;
            }
        }

        int dropLimits = AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getDropLimits(material);

        if (dropLimits <= 0)
        {
            dropLimits = Integer.MAX_VALUE;
        }

        int itemCount = (int) Math.min(data.entityCount(), dropLimits);

        Utils.dropItems(entity.getWorld(),
            entity.getLocation().add(0, 1, 0),
            material,
            itemCount);

        player.swingMainHand();

        entity.getWorld().playSound(entity,
            sound,
            new Random().nextFloat(0.8f, 1.2f),
            1);

        data.setCooldown(Material.BUCKET,
            AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                entity.getType(),
                Material.BUCKET,
                data.entityCount()));

        // Save data
        AnimalPenManager.setAnimalPenData(entity, data);
    }


    public static void handleDyes(Entity entity, Player player, ItemStack itemStack)
    {
        if (entity.getType() != EntityType.SHEEP)
        {
            // Only sheep can be interacted with shears
            return;
        }

        AnimalData data = AnimalPenManager.getAnimalData(entity);

        if (data == null)
        {
            return;
        }

        if (data.hasCooldown(Material.WHITE_DYE))
        {
            // under cooldown for feeding
            return;
        }

        Utils.triggerItemUse(entity, player, itemStack, 1);

        if (player.getGameMode() != GameMode.CREATIVE)
        {
            itemStack.subtract();
        }

        Sheep sheep = (Sheep) entity;
        sheep.setColor(AnimalPenManager.getDyeColor(itemStack.getType()));

        player.swingMainHand();

        entity.getWorld().playSound(entity,
            Sound.ITEM_DYE_USE,
            new Random().nextFloat(0.8f, 1.2f),
            1);

        data.setCooldown(Material.WHITE_DYE,
            AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                entity.getType(),
                Material.WHITE_DYE,
                data.entityCount()));

        // Save data
        AnimalPenManager.setAnimalPenData(entity, data);
    }


    public static void handleMagmaBlock(Entity entity, Player player, ItemStack itemStack)
    {
        if (entity.getType() != EntityType.FROG)
        {
            return;
        }

        AnimalData data = AnimalPenManager.getAnimalData(entity);

        if (data == null)
        {
            return;
        }

        if (data.hasCooldown(Material.MAGMA_BLOCK))
        {
            // under cooldown for feeding
            return;
        }

        int froglightCount = (int) Math.min(data.entityCount(), itemStack.getAmount());

        int dropLimits =
            AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getDropLimits(Material.PEARLESCENT_FROGLIGHT);

        if (dropLimits > 0)
        {
            froglightCount = Math.min(froglightCount, dropLimits);
        }

        Frog frog = (Frog) entity;
        Material material = AnimalPenManager.getFrogLight(frog);

        if (player.getGameMode() != GameMode.CREATIVE)
        {
            itemStack.subtract(froglightCount);
        }

        Utils.dropItems(entity.getWorld(),
            entity.getLocation().add(0, 1, 0),
            material,
            froglightCount);

        player.swingMainHand();

        entity.getWorld().playSound(entity,
            Sound.ENTITY_FROG_TONGUE,
            new Random().nextFloat(0.8f, 1.2f),
            1);

        data.setCooldown(Material.MAGMA_BLOCK,
            AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                entity.getType(),
                Material.MAGMA_BLOCK,
                data.entityCount()));

        // Save data
        AnimalPenManager.setAnimalPenData(entity, data);
    }


    public static void handleBowl(Entity entity, Player player, ItemStack itemStack)
    {
        if (entity.getType() == EntityType.MOOSHROOM)
        {
            // Only bee has glass bottle interaction
            AnimalPenManager.handleBowlSoup(entity, player, itemStack);
        }
        else if (entity.getType() == EntityType.SNIFFER)
        {
            // Only bee has glass bottle interaction
            AnimalPenManager.handleBowlSeeds(entity, player, itemStack);
        }
    }


    public static void handleBowlSoup(Entity entity, Player player, ItemStack itemStack)
    {
        if (entity.getType() != EntityType.MOOSHROOM)
        {
            // Only bee has glass bottle interaction
            return;
        }

        AnimalData data = AnimalPenManager.getAnimalData(entity);

        if (data == null)
        {
            return;
        }

        if (data.hasCooldown(Material.BOWL))
        {
            // under cooldown for feeding
            return;
        }

        Utils.triggerItemUse(entity, player, itemStack, 1);

        if (player.getGameMode() != GameMode.CREATIVE)
        {
            itemStack.subtract();
        }

        MushroomCow mushroomCow = (MushroomCow) entity;
        List<SuspiciousEffectEntry> effectsForNextStew = mushroomCow.getStewEffects();
        ItemStack stewItem = new ItemStack(Material.MUSHROOM_STEW);

        if (!effectsForNextStew.isEmpty())
        {
            SuspiciousStewMeta itemMeta = (SuspiciousStewMeta) stewItem.getItemMeta();
            effectsForNextStew.forEach(effect -> itemMeta.addCustomEffect(effect, false));
            stewItem.setItemMeta(itemMeta);
        }

        player.getInventory().addItem(stewItem);
        player.swingMainHand();

        entity.getWorld().playSound(entity,
            effectsForNextStew.isEmpty() ? Sound.ENTITY_MOOSHROOM_MILK : Sound.ENTITY_MOOSHROOM_SUSPICIOUS_MILK,
            new Random().nextFloat(0.8f, 1.2f),
            1);

        data.setCooldown(Material.BOWL,
            AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                entity.getType(),
                Material.BOWL,
                data.entityCount()));

        // Save data
        AnimalPenManager.setAnimalPenData(entity, data);
    }


    public static void handleBowlSeeds(Entity entity, Player player, ItemStack itemStack)
    {
        if (entity.getType() != EntityType.SNIFFER)
        {
            return;
        }

        AnimalData data = AnimalPenManager.getAnimalData(entity);

        if (data == null)
        {
            return;
        }

        if (data.hasCooldown(Material.BOWL))
        {
            // under cooldown for feeding
            return;
        }

        LootTable lootTable = LootTables.SNIFFER_DIGGING.getLootTable();
        LootContext lootParams = new LootContext.Builder(entity.getLocation()).lootedEntity(entity).build();

        int dropLimits = AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getDropLimits(Material.TORCHFLOWER_SEEDS);

        if (dropLimits <= 0)
        {
            dropLimits = Integer.MAX_VALUE;
        }

        List<ItemStack> itemStackList = new ArrayList<>();

        int seedCount = (int) Math.min(data.entityCount(), dropLimits);
        Random random = new Random();

        while (seedCount > 0)
        {
            Collection<ItemStack> randomItems = lootTable.populateLoot(random, lootParams);

            if (randomItems.isEmpty())
            {
                // Just a stop on infinite loop
                break;
            }

            seedCount -= randomItems.stream().mapToInt(ItemStack::getAmount).sum();

            randomItems.forEach(item ->
            {
                boolean added = false;

                for (ItemStack stack : itemStackList)
                {
                    if (item.isSimilar(stack) &&
                        stack.getAmount() < stack.getMaxStackSize())
                    {
                        stack.add(item.getAmount());
                        added = true;
                        break;
                    }
                }

                if (!added)
                {
                    itemStackList.add(item);
                }
            });
        }

        itemStackList.forEach(seedStack ->
            entity.getWorld().dropItem(entity.getLocation().add(0, 1, 0), seedStack));

        player.swingMainHand();

        entity.getWorld().playSound(entity,
            Sound.ENTITY_SNIFFER_DROP_SEED,
            new Random().nextFloat(0.8f, 1.2f),
            1);

        data.setCooldown(Material.BOWL,
            AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                entity.getType(),
                Material.BOWL,
                data.entityCount()));

        // Save data
        AnimalPenManager.setAnimalPenData(entity, data);
    }


    public static void handleSmallFlowers(Entity entity, Player player, ItemStack itemStack)
    {
        if (entity.getType() != EntityType.MOOSHROOM)
        {
            // Only bee has glass bottle interaction
            return;
        }

        AnimalData data = AnimalPenManager.getAnimalData(entity);

        if (data == null)
        {
            return;
        }

        MushroomCow mushroomCow = (MushroomCow) entity;

        if (mushroomCow.getVariant() != MushroomCow.Variant.BROWN)
        {
            return;
        }

        if (mushroomCow.hasEffectsForNextStew())
        {
            return;
        }

        SuspiciousEffectEntry suspiciousEffectEntry = FLOWER_EFFECTS.get(itemStack.getType());

        if (suspiciousEffectEntry == null)
        {
            return;
        }

        mushroomCow.addEffectToNextStew(suspiciousEffectEntry, false);

        Utils.triggerItemUse(entity, player, itemStack, 1);

        if (player.getGameMode() != GameMode.CREATIVE)
        {
            itemStack.subtract();
        }

        player.swingMainHand();

        entity.getWorld().playSound(entity,
            Sound.ENTITY_MOOSHROOM_EAT,
            new Random().nextFloat(0.8f, 1.2f),
            1);
    }


    public static void handleScutes(Entity entity, AnimalData animalData)
    {
        if (animalData.scutes() == 0)
        {
            // Nothing to process
            return;
        }

        int scutes = animalData.scutes();
        animalData.setScutes(0);

        Utils.dropItems(entity.getWorld(),
            entity.getLocation().add(0, 1, 0),
            Material.TURTLE_SCUTE,
            scutes);
    }


    public static void handleKilling(LivingEntity entity, Player player, ItemStack itemStack)
    {
        AnimalData data = AnimalPenManager.getAnimalData(entity);

        if (data == null)
        {
            // Something is wrong. No entity on other end.
            return;
        }

        if (AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().isIncreaseStatistics())
        {
            player.incrementStatistic(Statistic.USE_ITEM, itemStack.getType());
        }

        if (player.getGameMode() != GameMode.CREATIVE)
        {
            itemStack.damage(1, player);
        }

        int cooldown = AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getAttackCooldown();

        if (cooldown > 0)
        {
            player.setCooldown(itemStack, cooldown);
        }

        data.reduceEntityCount(1);
        AnimalPenManager.setAnimalPenData(entity, data);

        LootTable lootTable =
            Bukkit.getLootTable(NamespacedKey.minecraft("entities/" + entity.getType().getKey().value()));

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

        Sound deathSound = entity.getDeathSound();

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

        if (AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().isTriggerAdvancements())
        {
            CriteriaTriggers.PLAYER_KILLED_ENTITY.trigger(((CraftPlayer) player).getHandle(),
                ((CraftEntity) entity).getHandle(),
                ((CraftDamageSource) DamageSource.builder(DamageType.PLAYER_ATTACK).build()).getHandle());
        }

        if (AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().isIncreaseStatistics())
        {
            player.incrementStatistic(Statistic.MOB_KILLS);
            player.incrementStatistic(Statistic.KILL_ENTITY, entity.getType());
        }
    }


// ---------------------------------------------------------------------
// Section: Processing methods
// ---------------------------------------------------------------------


    public static void processCooldownFinish(Entity entity, Material key, AnimalData animalData)
    {
        if (entity.getType() == EntityType.TURTLE &&
            key == Material.APPLE &&
            !AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().isDropScuteAtStart())
        {
            AnimalPenManager.handleScutes(entity, animalData);
        }
        else if (entity.getType() == EntityType.SHEEP &&
            key == Material.SHEARS)
        {
            ((Sheep) entity).setSheared(false);
        }
    }


// ---------------------------------------------------------------------
// Section: Private methods
// ---------------------------------------------------------------------


    private static Material getWoolMaterial(DyeColor color)
    {
        try
        {
            String woolName = color.name() + "_WOOL";
            return Material.valueOf(woolName);
        }
        catch (Exception e)
        {
            return Material.WHITE_WOOL;
        }
    }


    private static DyeColor getDyeColor(Material material)
    {
        try
        {
            String color = material.name().replace("_DYE", "");
            return DyeColor.valueOf(color);
        }
        catch (Exception e)
        {
            return DyeColor.WHITE;
        }
    }


    private static Material getFrogLight(Frog frog)
    {
        if (frog.getVariant() == Frog.Variant.WARM)
        {
            return Material.PEARLESCENT_FROGLIGHT;
        }
        else if (frog.getVariant() == Frog.Variant.COLD)
        {
            return Material.VERDANT_FROGLIGHT;
        }
        else if (frog.getVariant() == Frog.Variant.TEMPERATE)
        {
            return Material.OCHRE_FROGLIGHT;
        }
        else
        {
            return Material.PEARLESCENT_FROGLIGHT;
        }
    }


    public final static NamespacedKey ANIMAL_DATA_KEY = new NamespacedKey("animal_pen_plugin", "animal_data");

    private final static String ANIMAL_CAGE_MODEL = "animal_pen:animal_cage";

    private final static String ANIMAL_CAGE_FILLED_MODEL = "animal_pen:animal_cage_filled";

    private final static String ANIMAL_PEN_MODEL = "animal_pen:animal_pen";

    private static final Map<Material, SuspiciousEffectEntry> FLOWER_EFFECTS = Map.ofEntries(
        Map.entry(Material.ALLIUM, SuspiciousEffectEntry.create(PotionEffectType.FIRE_RESISTANCE, 80)),
        Map.entry(Material.AZURE_BLUET, SuspiciousEffectEntry.create(PotionEffectType.BLINDNESS, 160)),
        Map.entry(Material.BLUE_ORCHID, SuspiciousEffectEntry.create(PotionEffectType.SATURATION, 7)),
        Map.entry(Material.DANDELION, SuspiciousEffectEntry.create(PotionEffectType.SATURATION, 7)),
        Map.entry(Material.CORNFLOWER, SuspiciousEffectEntry.create(PotionEffectType.JUMP_BOOST, 120)),
        Map.entry(Material.LILY_OF_THE_VALLEY, SuspiciousEffectEntry.create(PotionEffectType.POISON, 240)),
        Map.entry(Material.OXEYE_DAISY, SuspiciousEffectEntry.create(PotionEffectType.REGENERATION, 160)),
        Map.entry(Material.POPPY, SuspiciousEffectEntry.create(PotionEffectType.NIGHT_VISION, 100)),
        Map.entry(Material.TORCHFLOWER, SuspiciousEffectEntry.create(PotionEffectType.NIGHT_VISION, 100)),
        Map.entry(Material.RED_TULIP, SuspiciousEffectEntry.create(PotionEffectType.WEAKNESS, 180)),
        Map.entry(Material.ORANGE_TULIP, SuspiciousEffectEntry.create(PotionEffectType.WEAKNESS, 180)),
        Map.entry(Material.PINK_TULIP, SuspiciousEffectEntry.create(PotionEffectType.WEAKNESS, 180)),
        Map.entry(Material.WHITE_TULIP, SuspiciousEffectEntry.create(PotionEffectType.WEAKNESS, 180)),
        Map.entry(Material.WITHER_ROSE, SuspiciousEffectEntry.create(PotionEffectType.WITHER, 160))
    );
}