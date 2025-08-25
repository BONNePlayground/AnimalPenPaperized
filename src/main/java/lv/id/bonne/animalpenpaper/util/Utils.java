//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpenpaper.util;


import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import lv.id.bonne.animalpenpaper.AnimalPenPlugin;
import net.minecraft.advancements.CriteriaTriggers;

import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;


public class Utils
{
    public final static NamespacedKey UNIQUE_DATA_KEY = new NamespacedKey("animal_pen_plugin", "unique_key");

    public final static NamespacedKey ANIMAL_SIZE_MODIFIER = new NamespacedKey("animal_pen_plugin", "animal_size");

    public static DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder().
        appendValue(MINUTE_OF_HOUR, 2).
        optionalStart().
        appendLiteral(':').
        appendValue(SECOND_OF_MINUTE, 2).
        toFormatter();


// ---------------------------------------------------------------------
// Section: Item methods
// ---------------------------------------------------------------------


    /**
     * This method drops item stacks of given material in given amount naturally at the given location.
     */
    public static void dropItems(World world, Location location, Material material, int amount)
    {
        while (amount > 0)
        {
            ItemStack dropItem = new ItemStack(material);

            if (amount > dropItem.getMaxStackSize())
            {
                dropItem.setAmount(dropItem.getMaxStackSize());
                amount -= dropItem.getMaxStackSize();
            }
            else
            {
                dropItem.setAmount(amount);
                amount = 0;
            }

            world.dropItemNaturally(location, dropItem);
        }
    }


    public static void triggerItemUse(Entity entity, Player player, ItemStack itemStack, int amount)
    {
        if (AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().isTriggerAdvancements())
        {
            CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY.trigger(((CraftPlayer) player).getHandle(),
                ((CraftItemStack) itemStack).handle,
                ((CraftEntity) entity).getHandle());
        }

        if (AnimalPenPlugin.CONFIG_MANAGER.getConfiguration().isIncreaseStatistics())
        {
            player.incrementStatistic(Statistic.USE_ITEM, itemStack.getType(), amount);
        }
    }


// ---------------------------------------------------------------------
// Section: Text Entity Related methods
// ---------------------------------------------------------------------


    public static float blockFaceToYaw(BlockFace blockFace)
    {
        return switch (blockFace)
        {
            case NORTH -> 180f;
            case SOUTH -> 0f;
            case EAST -> 270f;
            case WEST -> 90f;
            default -> 180f;
        };
    }


    public static Vector center(BlockFace blockFace)
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
