//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpenpaper.managers;


import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import lv.id.bonne.animalpenpaper.AnimalPenPlugin;
import lv.id.bonne.animalpenpaper.data.AnimalData;
import net.kyori.adventure.text.Component;


public class DisplayTextManager
{
    public void startTrackingEntity(Entity entity, boolean replace, AbstractContainerManager manager)
    {
        EntityReference reference = new EntityReference(entity.getUniqueId(), entity.getWorld().getUID(), manager);

        if (!cache.containsKey(reference))
        {
            cache.put(reference, new ArrayList<>());
        }
        else if (replace)
        {
            this.stopTrackingEntity(entity, manager);
            cache.put(reference, new ArrayList<>());
        }
    }


    public void stopTrackingEntity(UUID entityUUID, World world, AbstractContainerManager manager)
    {
        if (entityUUID == null)
        {
            return;
        }

        stopTrackingEntity(new EntityReference(entityUUID, world.getUID(), manager));
    }


    public void stopTrackingEntity(Entity entity, AbstractContainerManager manager)
    {
        stopTrackingEntity(entity.getUniqueId(), entity.getWorld(), manager);
    }


    private void stopTrackingEntity(EntityReference entityReference)
    {
        List<Display> leftOverEntities = cache.remove(entityReference);

        if (leftOverEntities != null)
        {
            leftOverEntities.forEach(Entity::remove);
        }
    }


    public void runTask()
    {
        AtomicInteger tick = new AtomicInteger();

        this.bukkitTask = new BukkitRunnable()
        {
            @Override
            public void run()
            {
                List<EntityReference> entityReferenceList = new ArrayList<>(cache.keySet());

                entityReferenceList.forEach(entityReference ->
                {
                    Entity entity = getEntity(entityReference);

                    if (entity != null)
                    {
                        AnimalData animalData = entityReference.manager().getAnimalData(entity);

                        if (animalData != null)
                        {
                            List<Display> displayList = cache.get(entityReference);

                            // Reduce cooldowns by 20
                            animalData.getCooldowns().replaceAll((key, value) ->
                            {
                                int newValue = Math.max(0, value - 20);

                                if (newValue == 0 && value > 0)
                                {
                                    InteractionHandler.handleCooldownFinish(entity, key, animalData);
                                }

                                return newValue;
                            });

                            // Update data
                            entityReference.manager().setStructureData(entity, animalData);

                            // Draw text
                            List<Pair<Material, Component>> generatedTextMessages =
                                Helper.generateTextMessages(entity, animalData, tick.get());

                            int neededCount = generatedTextMessages.size() * 2;

                            if (displayList.size() != neededCount)
                            {
                                // kill everything.
                                displayList.forEach(Display::remove);
                                displayList.clear();

                                double yOffset = 1.2;

                                while (displayList.size() < neededCount)
                                {
                                    displayList.add((Display) entity.getWorld().spawnEntity(
                                        entity.getLocation().add(0, yOffset + 0.0625, 0),
                                        EntityType.ITEM_DISPLAY,
                                        CreatureSpawnEvent.SpawnReason.CUSTOM,
                                        newEntity ->
                                        {
                                            Display display = (Display) newEntity;

                                            Transformation transform = display.getTransformation();
                                            transform.getScale().set(0.125f, 0.125f, 0.125f);
                                            transform.getTranslation().set(-0.45f, 0f, 0f);
                                            display.setTransformation(transform);
                                            display.setPersistent(false);

                                            if (AnimalPenPlugin.configuration().isShowCooldownsOnlyOnShift())
                                            {
                                                display.setVisibleByDefault(false);
                                            }
                                            else
                                            {
                                                display.setViewRange(0.05f);
                                            }
                                        }));
                                    displayList.add((Display) entity.getWorld().spawnEntity(
                                        entity.getLocation().add(0, yOffset, 0),
                                        EntityType.TEXT_DISPLAY,
                                        CreatureSpawnEvent.SpawnReason.CUSTOM,
                                        newEntity ->
                                        {
                                            TextDisplay display = (TextDisplay) newEntity;

                                            Transformation transform = display.getTransformation();
                                            transform.getScale().set(0.4f, 0.4f, 0.4f);
                                            display.setTransformation(transform);
                                            display.setPersistent(false);
                                            display.setDefaultBackground(false);
                                            display.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));

                                            if (AnimalPenPlugin.configuration().isShowCooldownsOnlyOnShift())
                                            {
                                                display.setVisibleByDefault(false);
                                            }
                                            else
                                            {
                                                display.setViewRange(0.05f);
                                            }
                                        }));

                                    yOffset += 0.125;
                                }
                            }

                            int displayIndex = 0;

                            for (Pair<Material, Component> messagePair : generatedTextMessages.reversed())
                            {
                                Display item = displayList.get(displayIndex++);

                                Component message = messagePair.getValue();

                                if (item instanceof ItemDisplay itemDisplay)
                                {
                                    itemDisplay.setItemStack(new ItemStack(messagePair.getKey()));
                                }

                                Display text = displayList.get(displayIndex++);

                                if (text instanceof TextDisplay textDisplay)
                                {
                                    textDisplay.text(message);
                                }
                            }
                        }
                        else
                        {
                            // Remove reference as data is not present.
                            stopTrackingEntity(entityReference);
                        }
                    }
                    else
                    {
                        // Remove reference as entity is not present.
                        stopTrackingEntity(entityReference);
                    }
                });

                tick.addAndGet(20);
            }
        }.runTaskTimer(AnimalPenPlugin.getInstance(), 0L, 20L);
    }


    public Entity getEntity(EntityReference ref)
    {
        World world = Bukkit.getWorld(ref.worldId());
        return world != null ? world.getEntity(ref.entityId()) : null;
    }


    public void hideEntities(Player player)
    {
        cache.values().stream().flatMap(List::stream).
            forEach(display -> player.hideEntity(AnimalPenPlugin.getInstance(), display));
    }


    public void showEntities(Player player)
    {
        cache.values().stream().flatMap(List::stream).
            filter(display -> display.getLocation().distanceSquared(player.getLocation()) <= 100).
            forEach(display -> player.showEntity(AnimalPenPlugin.getInstance(), display));
    }


    public record EntityReference(UUID entityId, UUID worldId, AbstractContainerManager manager)
    {
    }


    public BukkitTask bukkitTask;

    final static Map<EntityReference, List<Display>> cache = new ConcurrentHashMap<>(10);
}
