package lv.id.bonne.animalpenpaper.managers;


import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Slab;
import org.bukkit.craftbukkit.damage.CraftDamageSource;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.TropicalFishBucketMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;

import lv.id.bonne.animalpenpaper.AnimalPenPlugin;
import lv.id.bonne.animalpenpaper.data.AnimalData;
import lv.id.bonne.animalpenpaper.data.AnimalDataType;
import lv.id.bonne.animalpenpaper.data.BlockData;
import lv.id.bonne.animalpenpaper.data.BlockDataType;
import lv.id.bonne.animalpenpaper.menu.AnimalPenVariantMenu;
import lv.id.bonne.animalpenpaper.util.StyleUtil;
import lv.id.bonne.animalpenpaper.util.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.TriState;
import net.minecraft.advancements.CriteriaTriggers;


/**
 * Clean storage class using the custom PersistentDataType
 */
public class AquariumManager
{
    public static boolean isWaterContainer(ItemStack item)
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

        return dataComponents.contains(WATER_CONTAINER_MODEL) ||
            dataComponents.contains(WATER_CONTAINER_FILLED_MODEL);
    }


    public static AnimalData addAnimal(ItemStack handItem,
        @NotNull EntityType type,
        EntitySnapshot entitySnapshot,
        long amount)
    {
        ItemMeta itemMeta = handItem.getItemMeta();

        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        AnimalData animalData = dataContainer.getOrDefault(AquariumManager.AQUARIUM_DATA_KEY,
            AnimalDataType.INSTANCE,
            new AnimalData(type, entitySnapshot, 0));

        animalData.addEntityCount(amount);

        if (animalData.getVariants().size() <= AnimalPenPlugin.configuration().getMaxStoredVariants())
        {
            animalData.addVariant(entitySnapshot);
        }

        AquariumManager.updateWaterContainerItemMeta(animalData, itemMeta);

        handItem.setItemMeta(itemMeta);

        return animalData;
    }


    public static ItemStack removeAnimal(ItemStack item, long amount)
    {
        ItemMeta itemMeta = item.getItemMeta();

        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        AnimalData animalData = dataContainer.get(AquariumManager.AQUARIUM_DATA_KEY, AnimalDataType.INSTANCE);

        if (animalData == null)
        {
            return item;
        }

        animalData.reduceEntityCount(amount);

        AquariumManager.updateWaterContainerItemMeta(animalData, itemMeta);

        item.setItemMeta(itemMeta);

        return item;
    }


    @Nullable
    public static AnimalData getAnimalData(ItemStack item)
    {
        if (!AquariumManager.isWaterContainer(item))
        {
            return null;
        }


        return item.getItemMeta().getPersistentDataContainer().
            get(AquariumManager.AQUARIUM_DATA_KEY, AnimalDataType.INSTANCE);
    }


    public static void setWaterContainerData(ItemStack item, @Nullable AnimalData animalData)
    {
        ItemMeta itemMeta = item.getItemMeta();
        AquariumManager.updateWaterContainerItemMeta(animalData, itemMeta);
        item.setItemMeta(itemMeta);
    }


    /**
     * Create an empty animal cage
     */
    public static ItemStack createEmptyWaterContainer()
    {
        ItemStack bottle = new ItemStack(Material.GLASS_BOTTLE);
        ItemMeta meta = bottle.getItemMeta();
        if (meta == null) return bottle;

        meta.displayName(AnimalPenPlugin.translations().
            getTranslatable("item.animal_pen.water_animal_container.name").
            style(StyleUtil.WHITE));

        AquariumManager.updateWaterContainerItemMeta(null, meta);

        // Anti Stacking
        meta.setMaxStackSize(1);

        bottle.setItemMeta(meta);

        return bottle;
    }


    private static void updateWaterContainerItemMeta(@Nullable AnimalData animalData, ItemMeta itemMeta)
    {
        if (animalData == null || animalData.entityCount() <= 0)
        {
            itemMeta.getPersistentDataContainer().remove(AquariumManager.AQUARIUM_DATA_KEY);

            itemMeta.lore(List.of(
                AnimalPenPlugin.translations().getTranslatable("item.animal_pen.water_animal_container.catch_tip.line1"),
                AnimalPenPlugin.translations().getTranslatable("item.animal_pen.water_animal_container.catch_tip.line2"),
                AnimalPenPlugin.translations().getTranslatable("item.animal_pen.water_animal_container.catch_tip.line3")
            ));

            CustomModelDataComponent component = itemMeta.getCustomModelDataComponent();
            component.setStrings(List.of(WATER_CONTAINER_MODEL));
            itemMeta.setCustomModelDataComponent(component);
        }
        else
        {
            itemMeta.getPersistentDataContainer().
                set(AquariumManager.AQUARIUM_DATA_KEY, AnimalDataType.INSTANCE, animalData);

            itemMeta.lore(List.of(
                AnimalPenPlugin.translations().getTranslatable("item.animal_pen.water_animal_container.description.top"),
                AnimalPenPlugin.translations().getTranslatable("item.animal_pen.water_animal_container.description.entity",
                    Component.translatable(animalData.entityType().translationKey()).style(StyleUtil.YELLOW)),
                AnimalPenPlugin.translations().getTranslatable("item.animal_pen.water_animal_container.description.amount",
                    Component.text(animalData.entityCount()).style(StyleUtil.YELLOW)),
                AnimalPenPlugin.translations().getTranslatable("item.animal_pen.water_animal_container.catch_tip.line1"),
                AnimalPenPlugin.translations().getTranslatable("item.animal_pen.water_animal_container.catch_tip.line2"),
                AnimalPenPlugin.translations().getTranslatable("item.animal_pen.water_animal_container.catch_tip.line3"),
                AnimalPenPlugin.translations().getTranslatable("item.animal_pen.water_animal_container.release_tip.line1"),
                AnimalPenPlugin.translations().getTranslatable("item.animal_pen.water_animal_container.release_tip.line2")
            ));

            CustomModelDataComponent component = itemMeta.getCustomModelDataComponent();

            if (!component.getStrings().contains(AquariumManager.WATER_CONTAINER_FILLED_MODEL))
            {
                component.setStrings(List.of(AquariumManager.WATER_CONTAINER_FILLED_MODEL,
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
    public static boolean isAquarium(@NotNull ItemStack item)
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

        return meta.getCustomModelDataComponent().getStrings().contains(AQUARIUM_MODEL);
    }


    /**
     * Return if given block is animal pen.
     */
    public static boolean isAquarium(@Nullable Block block)
    {
        if (block == null || block.getType() != Material.SMOOTH_STONE_SLAB)
        {
            return false;
        }

        NamespacedKey penKey = new NamespacedKey(AnimalPenPlugin.getInstance(),
            block.getX() + "_" + block.getY() + "_" + block.getZ() + "_aquarium");

        return block.getWorld().getPersistentDataContainer().has(penKey, BlockDataType.INSTANCE);
    }


    public static boolean isAquarium(@NotNull Entity entity)
    {
        return entity.getPersistentDataContainer().has(AQUARIUM_DATA_KEY,
            AnimalDataType.INSTANCE);
    }


    public static void completeAquariumCreation(Block block, BlockData blockData, @NotNull ItemStack itemInHand)
    {
        NamespacedKey penKey = new NamespacedKey(AnimalPenPlugin.getInstance(),
            block.getX() + "_" + block.getY() + "_" + block.getZ() + "_aquarium");

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


    public static void validateAquarium(@NotNull Entity entity)
    {
        Block block = entity.getLocation().add(0, -0.5, 0).getBlock();

        if (block.getType() != Material.SMOOTH_STONE_SLAB)
        {
            return;
        }

        NamespacedKey penKey = new NamespacedKey(AnimalPenPlugin.getInstance(),
            block.getX() + "_" + block.getY() + "_" + block.getZ() + "_aquarium");

        BlockData blockData = block.getWorld().getPersistentDataContainer().get(penKey, BlockDataType.INSTANCE);

        if (blockData == null)
        {
            AnimalPenPlugin.getInstance().getLogger().warning("Failed to load aquarium block. Restoring...");
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

            if (attribute != null && livingEntity instanceof WaterMob)
            {
                if (attribute.getBaseValue() != AnimalPenPlugin.configuration().getWaterAnimalSize())
                {
                    attribute.setBaseValue(AnimalPenPlugin.configuration().getWaterAnimalSize());
                }

                if (AnimalPenPlugin.configuration().isGrowWaterAnimals())
                {
                    AnimalData animalData = AquariumManager.getAnimalData(entity);

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
            block.getX() + "_" + block.getY() + "_" + block.getZ() + "_aquarium");

        BlockData blockData = block.getWorld().getPersistentDataContainer().get(penKey, BlockDataType.INSTANCE);

        if (blockData == null || blockData.entity == null)
        {
            // No data from animal pen.
            return null;
        }

        return AquariumManager.getAnimalData(block.getWorld().getEntity(blockData.entity));
    }


    /**
     * Returns animal data associated with given entity.
     */
    public static AnimalData getAnimalData(Entity entity)
    {
        if (entity == null)
        {
            AnimalPenPlugin.getInstance().getLogger().severe("Aquarium entity is removed! Cannot access data!");
            return null;
        }

        return entity.getPersistentDataContainer().get(AQUARIUM_DATA_KEY, AnimalDataType.INSTANCE);
    }


    public static void setAquariumData(Block block, AnimalData newData)
    {
        NamespacedKey penKey = new NamespacedKey(AnimalPenPlugin.getInstance(),
            block.getX() + "_" + block.getY() + "_" + block.getZ() + "_aquarium");

        BlockData blockData = block.getWorld().getPersistentDataContainer().getOrDefault(penKey,
            BlockDataType.INSTANCE,
            new BlockData());

        Entity entity;

        // Entity to display
        if (blockData.entity == null || block.getWorld().getEntity(blockData.entity) == null)
        {
            if (newData.entitySnapshot() != null)
            {
                entity = newData.entitySnapshot().createEntity(block.getLocation().add(0.5, 1, 0.5));
            }
            else
            {
                entity = block.getWorld().spawnEntity(block.getLocation().add(0.5, 1, 0.5),
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

            if (block.getBlockData() instanceof Slab slab)
            {
                block.getRelative(BlockFace.UP).setType(Material.WATER);
                slab.setWaterlogged(true);
                block.setBlockData(slab);
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
            AnimalPenPlugin.getInstance().getLogger().severe("Aquarium entity is removed! Cannot access data!");
            return;
        }

        AnimalPenPlugin.getInstance().task.startTrackingEntity(entity, false, false);

        entity.getPersistentDataContainer().set(AQUARIUM_DATA_KEY,
            AnimalDataType.INSTANCE,
            newData);

        Helper.updateCountTextEntity(block, blockData, newData.entityCount(), penKey);
    }


    public static void setAquariumData(Entity entity, AnimalData newData)
    {
        Block block = entity.getLocation().add(0, -0.5, 0).getBlock();

        if (newData.entityCount() <= 0)
        {
            // Entity is removed. Do propper stuff.
            ItemStack itemStack = AquariumManager.createEmptyWaterContainer();
            entity.getWorld().dropItem(entity.getLocation(), itemStack);

            AnimalPenPlugin.getInstance().task.stopTrackingEntity(entity, false);

            entity.getPersistentDataContainer().remove(AQUARIUM_DATA_KEY);
            entity.remove();

            if (block.getBlockData() instanceof Slab slab)
            {
                block.getRelative(BlockFace.UP).setType(Material.AIR);
                slab.setWaterlogged(false);
                block.setBlockData(slab);
            }
        }
        else
        {
            entity.getPersistentDataContainer().set(AQUARIUM_DATA_KEY,
                AnimalDataType.INSTANCE,
                newData);

            if (AnimalPenPlugin.configuration().isGrowWaterAnimals() &&
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

        NamespacedKey penKey = new NamespacedKey(AnimalPenPlugin.getInstance(),
            block.getX() + "_" + block.getY() + "_" + block.getZ() + "_aquarium");

        BlockData blockData = block.getWorld().getPersistentDataContainer().getOrDefault(penKey,
            BlockDataType.INSTANCE,
            new BlockData());

        Helper.updateCountTextEntity(block, blockData, newData.entityCount(), penKey);
    }


    public static void clearBlockData(Block block, boolean keepBlock)
    {
        NamespacedKey penKey = new NamespacedKey(AnimalPenPlugin.getInstance(),
            block.getX() + "_" + block.getY() + "_" + block.getZ() + "_aquarium");

        BlockData blockData = block.getWorld().getPersistentDataContainer().get(penKey, BlockDataType.INSTANCE);

        if (blockData == null)
        {
            return;
        }

        AnimalPenPlugin.getInstance().task.stopTrackingEntity(blockData.entity, block.getWorld(), false);
        Helper.removeEntity(block.getWorld(), blockData.entity);

        if (block.getBlockData() instanceof Slab slab)
        {
            block.getRelative(BlockFace.UP).setType(Material.AIR);
            slab.setWaterlogged(false);
            block.setBlockData(slab);
        }

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
        }
    }


    public static ItemStack getAquariumItem(Block block)
    {
        NamespacedKey penKey = new NamespacedKey(AnimalPenPlugin.getInstance(),
            block.getX() + "_" + block.getY() + "_" + block.getZ() + "_aquarium");

        BlockData blockData = block.getWorld().getPersistentDataContainer().get(penKey, BlockDataType.INSTANCE);

        if (blockData == null || blockData.decorationEntity == null)
        {
            return AquariumManager.createAquarium();
        }

        Entity entity = block.getWorld().getEntity(blockData.decorationEntity);

        if (!(entity instanceof ItemDisplay display))
        {
            return AquariumManager.createAquarium();
        }

        return display.getItemStack();
    }


    /**
     * Create an aquarium
     */
    public static ItemStack createAquarium()
    {
        ItemStack smoothStoneSlab = new ItemStack(Material.SMOOTH_STONE_SLAB);
        ItemMeta meta = smoothStoneSlab.getItemMeta();
        if (meta == null) return smoothStoneSlab;

        CustomModelDataComponent customData = meta.getCustomModelDataComponent();
        customData.setStrings(List.of(AQUARIUM_MODEL));
        meta.setCustomModelDataComponent(customData);

        meta.displayName(AnimalPenPlugin.translations().
            getTranslatable("item.animal_pen.aquarium.name").
            style(StyleUtil.WHITE));

        meta.lore(List.of(
            AnimalPenPlugin.translations().getTranslatable("item.animal_pen.aquarium.tip.line1"),
            AnimalPenPlugin.translations().getTranslatable("item.animal_pen.aquarium.tip.line2"),
            AnimalPenPlugin.translations().getTranslatable("item.animal_pen.aquarium.tip.line3")));

        smoothStoneSlab.setItemMeta(meta);

        return smoothStoneSlab;
    }


// ---------------------------------------------------------------------
// Section: Entity Methods
// ---------------------------------------------------------------------


    public static void handleFood(LivingEntity entity, Player player, ItemStack itemStack)
    {
        AnimalData data = AquariumManager.getAnimalData(entity);

        if (data == null)
        {
            return;
        }

        if (data.hasCooldown(Material.APPLE))
        {
            // under cooldown for feeding
            return;
        }

        long maxCount = AnimalPenPlugin.configuration().getMaximalAnimalCount();

        if (maxCount > 0 && data.entityCount() >= maxCount)
        {
            // Too many entities already in pen
            return;
        }

        int stackSize = itemStack.getAmount();
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
            if (stackSize % 2 == 1)
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
            AnimalPenPlugin.configuration().getEntityCooldown(
                entity.getType(),
                Material.APPLE,
                stackSize));

        // Save data
        AquariumManager.setAquariumData(entity, data);
    }


    public static void handleWaterBucket(Entity entity, Player player, ItemStack itemStack)
    {
        if (entity.getType() != EntityType.TROPICAL_FISH &&
            entity.getType() != EntityType.SALMON &&
            entity.getType() != EntityType.PUFFERFISH &&
            entity.getType() != EntityType.COD &&
            entity.getType() != EntityType.TADPOLE)
        {
            // Only axolotl and fishes can be interacted with water bucket
            return;
        }

        AnimalData data = AquariumManager.getAnimalData(entity);

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
            AnimalPenPlugin.configuration().getEntityCooldown(
                entity.getType(),
                Material.WATER_BUCKET,
                data.entityCount()));

        // Save data
        AquariumManager.setAquariumData(entity, data);

        if (AnimalPenPlugin.configuration().isTriggerAdvancements())
        {
            // Trigger bucket filling
            CriteriaTriggers.FILLED_BUCKET.trigger(((CraftPlayer) player).getHandle(),
                ((CraftItemStack) newBucket).handle);
        }
    }


    public static void handleKilling(LivingEntity entity, Player player, ItemStack itemStack)
    {
        AnimalData data = AquariumManager.getAnimalData(entity);

        if (data == null)
        {
            // Something is wrong. No entity on other end.
            return;
        }

        if (AnimalPenPlugin.configuration().isIncreaseStatistics())
        {
            player.incrementStatistic(Statistic.USE_ITEM, itemStack.getType());
        }

        if (player.getGameMode() != GameMode.CREATIVE)
        {
            itemStack.damage(1, player);
        }

        int cooldown = AnimalPenPlugin.configuration().getAttackCooldown();

        if (cooldown > 0)
        {
            player.setCooldown(itemStack, cooldown);
        }

        data.reduceEntityCount(1);
        AquariumManager.setAquariumData(entity, data);

        LootTable lootTable =
            Bukkit.getLootTable(NamespacedKey.minecraft("entities/" + entity.getType().getKey().value()));

        if (lootTable != null)
        {
            if (player.getInventory().getItemInMainHand().containsEnchantment(Enchantment.FIRE_ASPECT))
            {
                entity.setFireTicks(1);
                entity.setVisualFire(TriState.FALSE);
            }

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

        if (AnimalPenPlugin.configuration().isTriggerAdvancements())
        {
            CriteriaTriggers.PLAYER_KILLED_ENTITY.trigger(((CraftPlayer) player).getHandle(),
                ((CraftEntity) entity).getHandle(),
                ((CraftDamageSource) DamageSource.builder(DamageType.PLAYER_ATTACK).build()).getHandle());
        }

        if (AnimalPenPlugin.configuration().isIncreaseStatistics())
        {
            player.incrementStatistic(Statistic.MOB_KILLS);
            player.incrementStatistic(Statistic.KILL_ENTITY, entity.getType());
        }
    }


    public static void applyVariant(Entity entity, EntitySnapshot selectedVariant)
    {
        if (selectedVariant == null)
        {
            return;
        }

        Block block = entity.getLocation().add(0, -0.5, 0).getBlock();
        NamespacedKey penKey = new NamespacedKey(AnimalPenPlugin.getInstance(),
            block.getX() + "_" + block.getY() + "_" + block.getZ() + "_aquarium");

        BlockData blockData = block.getWorld().getPersistentDataContainer().get(penKey, BlockDataType.INSTANCE);

        if (blockData == null)
        {
            return;
        }

        AnimalData animalData = AquariumManager.getAnimalData(entity);

        if (animalData == null)
        {
            return;
        }

        animalData.setAppliedMaterial(null);
        animalData.setAppliedFlag(null);

        animalData.setEntitySnapshot(selectedVariant);

        blockData.entity = null;

        // Update aquarium by removing saved entity reference.
        block.getWorld().getPersistentDataContainer().set(penKey, BlockDataType.INSTANCE, blockData);

        // Remove entity from world
        AnimalPenPlugin.getInstance().task.stopTrackingEntity(entity, false);
        entity.getPersistentDataContainer().remove(AQUARIUM_DATA_KEY);
        AnimalPenVariantMenu.close(entity);
        entity.remove();

        // Trigger new entity creation
        AquariumManager.setAquariumData(block, animalData);
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


    public final static NamespacedKey AQUARIUM_DATA_KEY = new NamespacedKey("animal_pen", "aquarium_data");

    public final static String WATER_CONTAINER_MODEL = "animal_pen:water_animal_container";

    private final static String WATER_CONTAINER_FILLED_MODEL = "animal_pen:water_animal_container_filled";

    public final static String AQUARIUM_MODEL = "animal_pen:aquarium";
}