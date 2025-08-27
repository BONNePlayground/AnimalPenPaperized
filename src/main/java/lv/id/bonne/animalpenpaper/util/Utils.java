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
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import java.util.Map;

import io.papermc.paper.potion.SuspiciousEffectEntry;
import lv.id.bonne.animalpenpaper.AnimalPenPlugin;
import net.minecraft.advancements.CriteriaTriggers;


public class Utils
{
    public final static NamespacedKey ANIMAL_SIZE_MODIFIER = new NamespacedKey("animal_pen_plugin", "animal_size");


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


    public static Tag<Material> getTag(NamespacedKey tagKey)
    {
        return Bukkit.getTag(Tag.REGISTRY_ITEMS, tagKey, Material.class);
    }


    public static Material getWoolMaterial(DyeColor color)
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


    public static Material getFrogLight(Frog frog)
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


    public static DyeColor getDyeColor(Material material)
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


    public static final Map<Material, SuspiciousEffectEntry> FLOWER_EFFECTS = Map.ofEntries(
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


    private static final Vector NORTH_CENTER = new Vector(0.502, 0.125, 0);

    private static final Vector SOUTH_CENTER = new Vector(0.502, 0.125, 1);

    private static final Vector EAST_CENTER = new Vector(1, 0.125, 0.5025);

    private static final Vector WEST_CENTER = new Vector(0, 0.125, 0.502);
}
