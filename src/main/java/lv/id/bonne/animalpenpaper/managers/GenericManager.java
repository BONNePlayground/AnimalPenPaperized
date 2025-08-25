//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpenpaper.managers;


import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lv.id.bonne.animalpenpaper.AnimalPenPlugin;
import lv.id.bonne.animalpenpaper.data.AnimalData;
import lv.id.bonne.animalpenpaper.data.BlockData;
import lv.id.bonne.animalpenpaper.data.BlockDataType;
import lv.id.bonne.animalpenpaper.util.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;


public class GenericManager
{
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

    static void updateCountTextEntity(Block block, BlockData blockData, long entityCount, NamespacedKey penKey)
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
                    Utils.getFrogLight((Frog) entity));

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
                    Utils.getWoolMaterial(((Sheep) entity).getColor()));

                if (textPairMessage != null)
                {
                    lines.add(textPairMessage);
                }

                textPairMessage = createTextPairMessage(entityType,
                    animalData,
                    Material.WHITE_DYE,
                    Utils.getWoolMaterial(((Sheep) entity).getColor()));

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
}
