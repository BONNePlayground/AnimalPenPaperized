package lv.id.bonne.animalpenpaper.managers;


import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;
import java.util.UUID;

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
    private final static int ANIMAL_CAGE_MODEL = 1000;
    private final static int ANIMAL_PEN_MODEL = 1001;

    public final static NamespacedKey ANIMAL_DATA_KEY = new NamespacedKey("animal_pen_plugin", "animal_data");
    private final static NamespacedKey UNIQUE_DATA_KEY = new NamespacedKey("animal_pen_plugin", "unique_key");

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


// ---------------------------------------------------------------------
// Section: Private methods
// ---------------------------------------------------------------------


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

    private static final Vector NORTH_CENTER = new Vector(0.5, 0.125, 0);
    private static final Vector SOUTH_CENTER = new Vector(0.5, 0.125, 1);
    private static final Vector EAST_CENTER = new Vector(1, 0.125, 0.5);
    private static final Vector WEST_CENTER = new Vector(0, 0.125, 0.5);
}