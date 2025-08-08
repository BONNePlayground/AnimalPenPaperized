package lv.id.bonne.animalpenpaper.managers;


import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.AxolotlBucketMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.inventory.meta.TropicalFishBucketMeta;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;

import io.papermc.paper.potion.SuspiciousEffectEntry;
import lv.id.bonne.animalpenpaper.AnimalPenPlugin;
import lv.id.bonne.animalpenpaper.data.AnimalData;
import lv.id.bonne.animalpenpaper.data.AnimalDataType;
import lv.id.bonne.animalpenpaper.data.BlockData;
import lv.id.bonne.animalpenpaper.data.BlockDataType;
import lv.id.bonne.animalpenpaper.util.StyleUtil;
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

        if (meta == null || !meta.hasCustomModelData())
        {
            return false;
        }

        return meta.getCustomModelData() == ANIMAL_CAGE_MODEL;
    }


    public static AnimalData addAnimal(ItemStack handItem, @NotNull EntityType type, long amount)
    {
        ItemMeta itemMeta = handItem.getItemMeta();

        PersistentDataContainer dataContainer = itemMeta.getPersistentDataContainer();
        AnimalData animalData = dataContainer.getOrDefault(AnimalPenManager.ANIMAL_DATA_KEY,
            AnimalDataType.INSTANCE,
            new AnimalData(type, 0));

        animalData.entityCount += amount;

        dataContainer.set(AnimalPenManager.ANIMAL_DATA_KEY, AnimalDataType.INSTANCE, animalData);

        itemMeta.lore(List.of(Component.empty(),
            Component.text("Animal: ").style(StyleUtil.GRAY).append(Component.translatable(animalData.entityType.translationKey())),
            Component.text("Count: ").style(StyleUtil.GRAY).append(Component.text(animalData.entityCount)),
            Component.empty(),
            Component.text("Shift Right-click on block to release it").style(StyleUtil.GRAY)));

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

        animalData.entityCount -= amount;

        if (animalData.entityCount <= 0)
        {
            dataContainer.remove(AnimalPenManager.ANIMAL_DATA_KEY);
            itemMeta.lore(List.of(Component.empty(),
                Component.text("Right-click on animal to catch it").style(StyleUtil.GRAY)));
        }
        else
        {
            dataContainer.set(AnimalPenManager.ANIMAL_DATA_KEY, AnimalDataType.INSTANCE, animalData);

            itemMeta.lore(List.of(Component.empty(),
                Component.text("Animal: ").style(StyleUtil.GRAY).append(Component.translatable(animalData.entityType.translationKey())),
                Component.text("Count: ").style(StyleUtil.GRAY).append(Component.text(animalData.entityCount)),
                Component.empty(),
                Component.text("Shift Right-click on block to release it").style(StyleUtil.GRAY)));
        }

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


    public static void setItemData(ItemStack item, @Nullable AnimalData animalData)
    {
        ItemMeta itemMeta = item.getItemMeta();

        if (animalData == null)
        {
            itemMeta.getPersistentDataContainer().remove(AnimalPenManager.ANIMAL_DATA_KEY);
            itemMeta.lore(List.of(Component.empty(),
                Component.text("Right-click on animal to catch it").style(StyleUtil.GRAY)));
        }
        else
        {
            itemMeta.getPersistentDataContainer().set(AnimalPenManager.ANIMAL_DATA_KEY, AnimalDataType.INSTANCE, animalData);

            itemMeta.lore(List.of(Component.empty(),
                Component.text("Animal: ").style(StyleUtil.GRAY).append(Component.translatable(animalData.entityType.translationKey())),
                Component.text("Count: ").style(StyleUtil.GRAY).append(Component.text(animalData.entityCount)),
                Component.empty(),
                Component.text("Shift Right-click on block to release it").style(StyleUtil.GRAY)));
        }

        item.setItemMeta(itemMeta);
    }


    /**
     * Create an empty animal cage
     */
    public static ItemStack createEmptyAnimalCage() {
        ItemStack bottle = new ItemStack(Material.GLASS_BOTTLE);
        ItemMeta meta = bottle.getItemMeta();
        if (meta == null) return bottle;

        meta.setCustomModelData(ANIMAL_CAGE_MODEL);
        meta.displayName(Component.text("Animal Cage").style(StyleUtil.WHITE));

        meta.lore(List.of(Component.empty(),
            Component.text("Right-click on animal to catch it").style(StyleUtil.GRAY)));

        // Anti Stacking
        meta.getPersistentDataContainer().set(
            UNIQUE_DATA_KEY,
            PersistentDataType.STRING,
            UUID.randomUUID().toString());

        bottle.setItemMeta(meta);

        return bottle;
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

        if (meta == null || !meta.hasCustomModelData())
        {
            return false;
        }

        return meta.getCustomModelData() == ANIMAL_PEN_MODEL;
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
        if (blockData.entity == null)
        {
            entity = block.getWorld().spawnEntity(block.getLocation().add(0.5, 0.5, 0.5),
                newData.entityType,
                CreatureSpawnEvent.SpawnReason.CUSTOM,
                (newEntity) -> {
                    newEntity.setGravity(false);
                    newEntity.setNoPhysics(true);
                    newEntity.setPersistent(true);

                    if (newEntity instanceof LivingEntity livingEntity)
                    {
                        livingEntity.setAI(false);
                        livingEntity.setRemoveWhenFarAway(false);
                        livingEntity.setRotation(AnimalPenManager.blockFaceToYaw(blockData.blockFace), 0);

                        AttributeInstance attribute = livingEntity.getAttribute(Attribute.GENERIC_SCALE);

                        if (attribute != null) attribute.setBaseValue(0.5d);
                    }
                });

            blockData.entity = entity.getUniqueId();

            // Link entity with block
            block.getWorld().getPersistentDataContainer().set(penKey,
                BlockDataType.INSTANCE,
                blockData);
        }
        else
        {
            entity = block.getWorld().getEntity(blockData.entity);
        }

        if (entity == null)
        {
            AnimalPenPlugin.getInstance().getLogger().severe("Animal Pen entity is removed! Cannot access data!");
            return;
        }

        entity.getPersistentDataContainer().set(ANIMAL_DATA_KEY,
            AnimalDataType.INSTANCE,
            newData);

        updateCountTextEntity(block, blockData, newData.entityCount, penKey);
    }


    public static void setAnimalPenData(Entity entity, AnimalData newData)
    {
        entity.getPersistentDataContainer().set(ANIMAL_DATA_KEY,
            AnimalDataType.INSTANCE,
            newData);

        Block block = entity.getLocation().getBlock();

        NamespacedKey penKey = new NamespacedKey(AnimalPenPlugin.getInstance(),
            block.getX() + "_" + block.getY() + "_" + block.getZ() + "_animal_pen");

        BlockData blockData = block.getWorld().getPersistentDataContainer().getOrDefault(penKey,
            BlockDataType.INSTANCE,
            new BlockData());

        updateCountTextEntity(block, blockData, newData.entityCount, penKey);
    }


    public static void clearBlockData(Block block, boolean withSave)
    {
        NamespacedKey penKey = new NamespacedKey(AnimalPenPlugin.getInstance(),
            block.getX() + "_" + block.getY() + "_" + block.getZ() + "_animal_pen");

        BlockData blockData = block.getWorld().getPersistentDataContainer().get(penKey, BlockDataType.INSTANCE);

        if (blockData == null)
        {
            return;
        }

        AnimalPenManager.removeEntity(block.getWorld(), blockData.entity);
        AnimalPenManager.removeEntity(block.getWorld(), blockData.countEntity);

        blockData.cooldowns.forEach(text -> {
            AnimalPenManager.removeEntity(block.getWorld(), text.text);
            AnimalPenManager.removeEntity(block.getWorld(), text.icon);
        });

        blockData.entity = null;
        blockData.countEntity = null;
        blockData.cooldowns.clear();

        if (withSave)
        {
            block.getWorld().getPersistentDataContainer().set(penKey,
                BlockDataType.INSTANCE,
                blockData);
        }
        else
        {
            block.getWorld().getPersistentDataContainer().remove(penKey);
        }
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
    public static ItemStack createAnimalPen() {
        ItemStack smoothStoneSlab = new ItemStack(Material.SMOOTH_STONE_SLAB);
        ItemMeta meta = smoothStoneSlab.getItemMeta();
        if (meta == null) return smoothStoneSlab;

        meta.setCustomModelData(ANIMAL_PEN_MODEL);
        meta.displayName(Component.text("Animal Pen").style(StyleUtil.WHITE));

        meta.lore(List.of(Component.empty(),
            Component.text("Insert Animal Cage to interact with animal.").style(StyleUtil.GRAY)));

        smoothStoneSlab.setItemMeta(meta);

        return smoothStoneSlab;
    }


    private static void updateCountTextEntity(Block block, BlockData blockData, long entityCount, NamespacedKey penKey)
    {
        Entity entity;

        if (blockData.countEntity == null)
        {
            entity = block.getWorld().spawnEntity(
                block.getLocation().add(AnimalPenManager.center(blockData.blockFace)),
                EntityType.TEXT_DISPLAY,
                CreatureSpawnEvent.SpawnReason.CUSTOM,
                (newEntity) -> {
                    newEntity.setPersistent(true);
                    newEntity.setRotation(AnimalPenManager.blockFaceToYaw(blockData.blockFace), 0);

                    if (newEntity instanceof TextDisplay display)
                    {
                        display.setVisibleByDefault(true);
                        display.setSeeThrough(false);
                    }
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


// ---------------------------------------------------------------------
// Section: Entity Methods
// ---------------------------------------------------------------------


    public static void handleFood(Entity entity, Player player, ItemStack itemStack)
    {
        AnimalData data = AnimalPenManager.getAnimalData(entity);

        if (data == null)
        {
            return;
        }

        if (data.hasCooldown(AnimalData.Interaction.FOOD))
        {
            // under cooldown for feeding
            return;
        }

        long maxCount = AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getMaximalAnimalCount();

        if (maxCount > 0 && data.entityCount >= maxCount)
        {
            // Too many entities already in pen
            return;
        }

        int stackSize = itemStack.getAmount();
        stackSize = (int) Math.min(data.entityCount, stackSize);

        if (stackSize < 2)
        {
            // Cannot feed 1 animal only for breeding.
            return;
        }

        stackSize = (int) Math.min((maxCount - data.entityCount) * 2, stackSize);

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

        data.entityCount += stackSize / 2;

        entity.getWorld().spawnParticle(Particle.HEART,
            entity.getLocation(),
            5,
            0.2, 0.2, 0.2,
            0.05);

        entity.getWorld().playSound(entity,
            Sound.ENTITY_GENERIC_EAT,
            new Random().nextFloat(0.8f, 1.2f),
            1);

        player.swingMainHand();

        data.setCooldown(AnimalData.Interaction.FOOD,
            AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                entity.getType(),
                Material.APPLE,
                stackSize));

        // Save data
        AnimalPenManager.setAnimalPenData(entity, data);
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

        if (data.hasCooldown(AnimalData.Interaction.BRUSH))
        {
            // under cooldown for feeding
            return;
        }

        itemStack.damage(16, player);

        entity.getWorld().dropItem(entity.getLocation().add(0, 1, 0),
            new ItemStack(Material.ARMADILLO_SCUTE));

        entity.getWorld().playSound(entity,
            Sound.ENTITY_ARMADILLO_BRUSH,
            new Random().nextFloat(0.8f, 1.2f),
            1);

        player.swingMainHand();

        data.setCooldown(AnimalData.Interaction.BRUSH,
            AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                entity.getType(),
                Material.BRUSH,
                data.entityCount));

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

        if (data.hasCooldown(AnimalData.Interaction.WATER_BUCKET))
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
                newBucket =  new ItemStack(Material.COD_BUCKET);
                sound = Sound.ITEM_BUCKET_FILL_FISH;
            }
            case PUFFERFISH ->
            {
                newBucket =  new ItemStack(Material.PUFFERFISH_BUCKET);
                sound = Sound.ITEM_BUCKET_FILL_FISH;
            }
            case SALMON ->
            {
                newBucket =  new ItemStack(Material.SALMON_BUCKET);
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

        data.entityCount -= 1;

        itemStack.subtract();
        player.getInventory().addItem(newBucket);

        entity.getWorld().playSound(entity,
            sound,
            new Random().nextFloat(0.8f, 1.2f),
            1);

        player.swingMainHand();

        data.setCooldown(AnimalData.Interaction.WATER_BUCKET,
            AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                entity.getType(),
                Material.WATER_BUCKET,
                data.entityCount));

        // Save data
        AnimalPenManager.setAnimalPenData(entity, data);
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

        if (data.hasCooldown(AnimalData.Interaction.SHEARS))
        {
            // under cooldown for feeding
            return;
        }

        itemStack.damage(1, player);

        int dropLimits = AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getDropLimits(Material.HONEYCOMB);

        if (dropLimits <= 0)
        {
            dropLimits = Integer.MAX_VALUE;
        }

        int itemCount = (int) Math.min(data.entityCount, dropLimits);

        while (itemCount > 0)
        {
            ItemStack dropStack = new ItemStack(Material.HONEYCOMB);

            if (itemCount > dropStack.getMaxStackSize())
            {
                dropStack.setAmount(dropStack.getMaxStackSize());
                itemCount -= dropStack.getMaxStackSize();
            }
            else
            {
                dropStack.setAmount(itemCount);
                itemCount = 0;
            }

            entity.getWorld().dropItem(entity.getLocation().add(0, 1, 0), dropStack);
        }

        player.swingMainHand();

        entity.getWorld().playSound(entity,
            Sound.BLOCK_BEEHIVE_SHEAR,
            new Random().nextFloat(0.8f, 1.2f),
            1);

        data.setCooldown(AnimalData.Interaction.SHEARS,
            AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                entity.getType(),
                Material.SHEARS,
                data.entityCount));

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

        if (data.hasCooldown(AnimalData.Interaction.SHEARS))
        {
            // under cooldown for feeding
            return;
        }

        itemStack.damage(1, player);

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

        for (int i = 0; i < data.entityCount && woolCount < dropLimits; i++)
        {
            woolCount += random.nextInt(3);
        }

        while (woolCount > 0)
        {
            ItemStack woolStack = new ItemStack(woolMaterial);

            if (woolCount > 64)
            {
                woolStack.setAmount(64);
                woolCount -= 64;
            }
            else
            {
                woolStack.setAmount(woolCount);
                woolCount = 0;
            }

            entity.getWorld().dropItem(entity.getLocation().add(0, 1, 0), woolStack);
        }

        player.swingMainHand();

        data.setCooldown(AnimalData.Interaction.SHEARS,
            AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                entity.getType(),
                Material.SHEARS,
                data.entityCount));

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

        if (data.hasCooldown(AnimalData.Interaction.BUCKET))
        {
            // under cooldown for feeding
            return;
        }

        itemStack.subtract();
        player.getInventory().addItem(new ItemStack(Material.MILK_BUCKET));

        player.swingMainHand();

        entity.getWorld().playSound(entity,
            entity.getType() == EntityType.GOAT ? Sound.ENTITY_GOAT_MILK : Sound.ENTITY_COW_MILK,
            new Random().nextFloat(0.8f, 1.2f),
            1);

        data.setCooldown(AnimalData.Interaction.BUCKET,
            AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                entity.getType(),
                Material.BUCKET,
                data.entityCount));

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

        if (data.hasCooldown(AnimalData.Interaction.GLASS_BOTTLE))
        {
            // under cooldown for feeding
            return;
        }

        itemStack.subtract();
        player.getInventory().addItem(new ItemStack(Material.HONEY_BOTTLE));

        player.swingMainHand();

        entity.getWorld().playSound(entity,
            Sound.BLOCK_BEEHIVE_DRIP,
            new Random().nextFloat(0.8f, 1.2f),
            1);

        data.setCooldown(AnimalData.Interaction.GLASS_BOTTLE,
            AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                entity.getType(),
                Material.GLASS_BOTTLE,
                data.entityCount));

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

        if (data.hasCooldown(AnimalData.Interaction.BUCKET))
        {
            // under cooldown for feeding
            return;
        }

        Material material;
        Sound sound;

        switch (entity.getType())
        {
            case CHICKEN -> {
                material = Material.EGG;
                sound = Sound.ENTITY_CHICKEN_EGG;
            }
            case SNIFFER -> {
                material = Material.SNIFFER_EGG;
                sound = Sound.BLOCK_SNIFFER_EGG_PLOP;
            }
            case TURTLE -> {
                material = Material.TURTLE_EGG;
                sound = Sound.ENTITY_TURTLE_LAY_EGG;
            }
            default -> {
                return;
            }
        }

        int dropLimits = AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getDropLimits(material);

        if (dropLimits <= 0)
        {
            dropLimits = Integer.MAX_VALUE;
        }

        int itemCount = (int) Math.min(data.entityCount, dropLimits);

        while (itemCount > 0)
        {
            ItemStack dropStack = new ItemStack(material);

            if (itemCount > dropStack.getMaxStackSize())
            {
                dropStack.setAmount(dropStack.getMaxStackSize());
                itemCount -= dropStack.getMaxStackSize();
            }
            else
            {
                dropStack.setAmount(itemCount);
                itemCount = 0;
            }

            entity.getWorld().dropItem(entity.getLocation().add(0, 1, 0), dropStack);
        }

        player.swingMainHand();

        entity.getWorld().playSound(entity,
            sound,
            new Random().nextFloat(0.8f, 1.2f),
            1);

        data.setCooldown(AnimalData.Interaction.BUCKET,
            AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                entity.getType(),
                Material.BUCKET,
                data.entityCount));

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

        if (data.hasCooldown(AnimalData.Interaction.DYES))
        {
            // under cooldown for feeding
            return;
        }

        itemStack.subtract();

        Sheep sheep = (Sheep) entity;
        sheep.setColor(AnimalPenManager.getDyeColor(itemStack.getType()));

        player.swingMainHand();

        entity.getWorld().playSound(entity,
            Sound.ITEM_DYE_USE,
            new Random().nextFloat(0.8f, 1.2f),
            1);

        data.setCooldown(AnimalData.Interaction.DYES,
            AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                entity.getType(),
                Material.WHITE_DYE,
                data.entityCount));

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

        if (data.hasCooldown(AnimalData.Interaction.MAGMA_BLOCK))
        {
            // under cooldown for feeding
            return;
        }

        int froglightCount = (int) Math.min(data.entityCount, itemStack.getAmount());

        int dropLimits = AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getDropLimits(Material.PEARLESCENT_FROGLIGHT);

        if (dropLimits > 0)
        {
            froglightCount = Math.min(froglightCount, dropLimits);
        }

        Frog frog = (Frog) entity;
        Material material;

        if (frog.getVariant() == Frog.Variant.WARM)
        {
            material = Material.PEARLESCENT_FROGLIGHT;
        }
        else if (frog.getVariant() == Frog.Variant.COLD)
        {
            material = Material.VERDANT_FROGLIGHT;
        }
        else if (frog.getVariant() == Frog.Variant.TEMPERATE)
        {
            material = Material.OCHRE_FROGLIGHT;
        }
        else
        {
            return;
        }

        itemStack.subtract(froglightCount);

        while (froglightCount > 0)
        {
            ItemStack frogLight = new ItemStack(material);

            if (froglightCount > frogLight.getMaxStackSize())
            {
                frogLight.setAmount(frogLight.getMaxStackSize());
                froglightCount -= frogLight.getMaxStackSize();
            }
            else
            {
                frogLight.setAmount(froglightCount);
                froglightCount = 0;
            }

            entity.getWorld().dropItem(entity.getLocation().add(0, 1, 0), frogLight);
        }

        player.swingMainHand();

        entity.getWorld().playSound(entity,
            Sound.ENTITY_FROG_TONGUE,
            new Random().nextFloat(0.8f, 1.2f),
            1);

        data.setCooldown(AnimalData.Interaction.MAGMA_BLOCK,
            AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                entity.getType(),
                Material.MAGMA_BLOCK,
                data.entityCount));

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

        if (data.hasCooldown(AnimalData.Interaction.BOWL))
        {
            // under cooldown for feeding
            return;
        }

        itemStack.subtract();

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

        data.setCooldown(AnimalData.Interaction.BOWL,
            AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                entity.getType(),
                Material.BOWL,
                data.entityCount));

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

        if (data.hasCooldown(AnimalData.Interaction.BOWL))
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

        int seedCount = (int) Math.min(data.entityCount, dropLimits);
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

            randomItems.forEach(item -> {
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

        data.setCooldown(AnimalData.Interaction.BOWL,
            AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().getEntityCooldown(
                entity.getType(),
                Material.BOWL,
                data.entityCount));

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

        itemStack.subtract();
        player.swingMainHand();

        entity.getWorld().playSound(entity,
            Sound.ENTITY_MOOSHROOM_EAT,
            new Random().nextFloat(0.8f, 1.2f),
            1);
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


    private static float blockFaceToYaw(BlockFace blockFace) {
        return switch (blockFace)
        {
            case NORTH -> 180f;
            case SOUTH -> 0f;
            case EAST -> 270f;
            case WEST -> 90f;
            default -> 180f;
        };
    }


    private static Vector center(BlockFace blockFace)
    {
        return switch (blockFace)
        {
            case NORTH -> NORTH_CENTER;
            case SOUTH -> SOUTH_CENTER;
            case EAST -> EAST_CENTER;
            case WEST -> WEST_CENTER;
            default -> NORTH_CENTER;
        };
    }

    public final static NamespacedKey ANIMAL_DATA_KEY = new NamespacedKey("animal_pen_plugin", "animal_data");

    private static final Vector NORTH_CENTER = new Vector(0.5, 0.125, 0);

    private static final Vector SOUTH_CENTER = new Vector(0.5, 0.125, 1);

    private static final Vector EAST_CENTER = new Vector(1, 0.125, 0.5);

    private static final Vector WEST_CENTER = new Vector(0, 0.125, 0.5);

    private final static int ANIMAL_CAGE_MODEL = 1000;

    private final static int ANIMAL_PEN_MODEL = 1001;

    private final static NamespacedKey UNIQUE_DATA_KEY = new NamespacedKey("animal_pen_plugin", "unique_key");

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