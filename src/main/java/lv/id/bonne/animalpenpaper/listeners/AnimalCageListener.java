package lv.id.bonne.animalpenpaper.listeners;


import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Iterator;
import java.util.List;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import io.papermc.paper.potion.SuspiciousEffectEntry;
import lv.id.bonne.animalpenpaper.AnimalPenPlugin;
import lv.id.bonne.animalpenpaper.data.AnimalData;
import lv.id.bonne.animalpenpaper.events.item.AnimalCatchEvent;
import lv.id.bonne.animalpenpaper.events.item.AnimalDepositEvent;
import lv.id.bonne.animalpenpaper.events.item.AnimalReleaseEvent;
import lv.id.bonne.animalpenpaper.events.item.AnimalWithdrawEvent;
import lv.id.bonne.animalpenpaper.managers.AnimalPenManager;
import lv.id.bonne.animalpenpaper.util.StyleUtil;
import lv.id.bonne.animalpenpaper.util.Utils;
import net.kyori.adventure.text.Component;
import net.minecraft.world.entity.OwnableEntity;


/**
 * This listener manages animal cage interactions.
 */
public class AnimalCageListener implements Listener
{
    /**
     * This listener checks if player can catch clicked entity with animal cage.
     */
    @EventHandler(ignoreCancelled = true)
    public void onEntityCatch(PlayerInteractEntityEvent event)
    {
        Player player = event.getPlayer();

        if (event.getPlayer().isSneaking())
        {
            return;
        }

        Entity entity = event.getRightClicked();
        ItemStack item = player.getInventory().getItem(event.getHand());

        if (!AnimalPenManager.isAnimalCage(item))
        {
            return;
        }

        event.setCancelled(true);

        if (!(entity instanceof Animals animal))
        {
            player.sendMessage(AnimalPenPlugin.translations().
                getTranslatable("item.animal_pen.animal_cage.error.not_animal"));

            return;
        }

        if (animal.isDead() || !animal.hasAI())
        {
            // Silent death
            return;
        }

        if (!animal.isAdult())
        {
            player.sendMessage(AnimalPenPlugin.translations().
                getTranslatable("item.animal_pen.animal_cage.error.baby"));

            return;
        }

        if (entity instanceof Tameable tameable && tameable.getOwnerUniqueId() != null ||
            entity instanceof OwnableEntity ownable && ownable.getOwner() != null)
        {
            player.sendMessage(AnimalPenPlugin.translations().
                getTranslatable("item.animal_pen.animal_cage.error.tame"));

            return;
        }

        if (animal.isLeashed())
        {
            player.sendMessage(AnimalPenPlugin.translations().
                getTranslatable("item.animal_pen.animal_cage.error.leashed"));

            return;
        }

        if (!animal.isEmpty())
        {
            // Eject all passengers
            animal.eject();
        }

        // Drop all equipment
        EntityEquipment equipment = animal.getEquipment();

        for (EquipmentSlot slot : EquipmentSlot.values())
        {
            ItemStack itemStack = equipment.getItem(slot);

            if (Math.random() <= equipment.getDropChance(slot))
            {
                entity.getWorld().dropItemNaturally(entity.getLocation(), itemStack);
            }

            equipment.setDropChance(slot, 0);
        }

        // Check blocked types
        if (AnimalPenPlugin.configuration().isBlocked(animal.getType()))
        {
            player.sendMessage(AnimalPenPlugin.translations().
                getTranslatable("item.animal_pen.animal_cage.error.blocked"));

            return;
        }

        EntityType entityType = animal.getType();
        AnimalData storedData = AnimalPenManager.getAnimalData(item);

        // Check if item already contains another type
        if (storedData != null && storedData.entityType() != entityType)
        {
            player.sendMessage(AnimalPenPlugin.translations().
                getTranslatable("item.animal_pen.animal_cage.error.wrong"));

            return;
        }
        else if (storedData == null)
        {
            storedData = new AnimalData(entityType, entity.createSnapshot(), 0);
        }

        long maxAmount = AnimalPenPlugin.configuration().getMaximalAnimalCount();

        if (maxAmount > 0 && storedData.entityCount() + 1 > maxAmount)
        {
            player.sendMessage(AnimalPenPlugin.translations().
                getTranslatable("item.animal_pen.animal_cage.error.full"));

            return;
        }

        AnimalCatchEvent animalCatchEvent =
            new AnimalCatchEvent(player,
                entity,
                storedData,
                true);

        if (!animalCatchEvent.callEvent())
        {
            player.sendMessage(AnimalPenPlugin.translations().
                getTranslatable("item.animal_pen.animal_cage.error.unknown",
                    Component.translatable(entity.getType().translationKey())));
            return;
        }

        AnimalPenManager.addAnimal(item, entityType, entity.createSnapshot(), 1);

        entity.remove();
        player.swingMainHand();

        player.sendMessage(AnimalPenPlugin.translations().
            getTranslatable("item.animal_pen.animal_cage.captured",
                Component.translatable(entity.getType().translationKey())));
    }


    /**
     * This listener checks if player can release entity from animal cage.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityRelease(PlayerInteractEvent event)
    {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() == null)
        {
            return;
        }

        if (!event.getPlayer().isSneaking())
        {
            return;
        }

        Block block = event.getClickedBlock();
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getHand());

        if (!AnimalPenManager.isAnimalCage(item))
        {
            return;
        }

        event.setCancelled(true);

        AnimalData storedData = AnimalPenManager.getAnimalData(item);

        if (storedData == null)
        {
            return;
        }

        EntityType entityType = storedData.entityType();

        Location spawnLoc = block.getLocation().add(0.5, 1, 0.5);
        World world = player.getWorld();

        AnimalReleaseEvent animalReleaseEvent = new AnimalReleaseEvent(player,
            spawnLoc,
            storedData,
            true);

        if (!animalReleaseEvent.callEvent())
        {
            player.sendMessage(AnimalPenPlugin.translations().
                getTranslatable("item.animal_pen.animal_cage.error.release"));
            return;
        }

        Entity entity;

        if (storedData.entitySnapshot() != null)
        {
            entity = storedData.entitySnapshot().createEntity(spawnLoc);
        }
        else
        {
            entity = world.spawnEntity(spawnLoc, entityType, CreatureSpawnEvent.SpawnReason.CUSTOM);
        }

        boolean updateSnapshot = storedData.getAppliedFlag().isPresent() ||
            storedData.getAppliedMaterial().isPresent();

        if (entity instanceof Sheep sheep)
        {
            storedData.getAppliedFlag().ifPresent(shared -> {
                sheep.setSheared(shared);
                storedData.setAppliedFlag(null);
            });

            storedData.getAppliedMaterial().ifPresent(dye -> {
                sheep.setColor(Utils.getDyeColor(dye));
                storedData.setAppliedMaterial(null);
            });
        }
        else if (entity instanceof MushroomCow cow)
        {
            storedData.getAppliedMaterial().ifPresent(dye -> {
                SuspiciousEffectEntry suspiciousEffectEntry = Utils.FLOWER_EFFECTS.get(dye);

                if (suspiciousEffectEntry != null)
                {
                    cow.addEffectToNextStew(suspiciousEffectEntry, true);
                }

                storedData.setAppliedMaterial(null);
            });
        }

        if (updateSnapshot || storedData.entitySnapshot() == null)
        {
            // Update snapshot based on created entity.
            storedData.setEntitySnapshot(entity.createSnapshot());
        }

        if (!(entity instanceof Animals animal))
        {
            return;
        }

        // Clear all equipment to avoid its dropping.
        animal.getEquipment().clear();

        AnimalPenManager.removeAnimal(item, 1);

        player.swingMainHand();

        player.sendMessage(AnimalPenPlugin.translations().
            getTranslatable("item.animal_pen.animal_cage.released",
                Component.translatable(entity.getType().translationKey())));
    }


    /**
     * This listener cheks is player can interact with animal cage on animal pen
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInteractWithPenWithAnimalCage(PlayerInteractEvent event)
    {
        if (event.getHand() == null)
        {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getHand());

        if (!AnimalPenManager.isAnimalCage(item))
        {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
        {
            return;
        }

        Block block = event.getClickedBlock();

        if (!AnimalPenManager.isAnimalPen(block))
        {
            return;
        }

        event.setCancelled(true);

        AnimalData penData = AnimalPenManager.getAnimalData(block);
        AnimalData itemData = AnimalPenManager.getAnimalData(item);

        if (itemData == null && penData == null)
        {
            // Both are empty
            return;
        }

        if (penData == null)
        {
            AnimalDepositEvent animalDepositEvent = new AnimalDepositEvent(player,
                block.getLocation(),
                itemData,
                null,
                true);

            if (!animalDepositEvent.callEvent())
            {
                player.sendMessage(AnimalPenPlugin.translations().
                    getTranslatable("item.animal_pen.animal_cage.error.deposit"));
                return;
            }

            // Animal pen data is null.
            AnimalPenManager.setAnimalPenData(block, itemData);

            item.setAmount(-1);
            player.getInventory().setItem(event.getHand(), item);
            player.swingMainHand();

            player.sendMessage(AnimalPenPlugin.translations().
                getTranslatable("item.animal_pen.animal_cage.inserted"));

            return;
        }

        if (itemData == null)
        {
            if (!player.isSneaking() || penData.entityCount() < 2)
            {
                // Only on sneaking or there is something to split
                return;
            }

            // Clone half of data to new item
            itemData = new AnimalData(penData.entityType(), penData.entitySnapshot(), penData.entityCount() / 2);
            itemData.getCooldowns().putAll(penData.getCooldowns());
            itemData.setScutes(penData.scutes() / 2);
            penData.setScutes(penData.scutes() - itemData.scutes());

            // save applied data
            penData.getAppliedMaterial().ifPresent(itemData::setAppliedMaterial);
            penData.getAppliedFlag().ifPresent(itemData::setAppliedFlag);

            AnimalWithdrawEvent animalWithdrawEvent = new AnimalWithdrawEvent(player,
                block.getLocation(),
                itemData,
                penData,
                true);

            if (!animalWithdrawEvent.callEvent())
            {
                event.getPlayer().sendMessage(AnimalPenPlugin.translations().
                    getTranslatable("item.animal_pen.animal_cage.error.withdrawn"));
                return;
            }

            AnimalPenManager.setAnimalCageData(item, itemData);

            penData.reduceEntityCount(itemData.entityCount());

            AnimalPenManager.setAnimalPenData(block, penData);

            player.sendMessage(AnimalPenPlugin.translations().
                getTranslatable("item.animal_pen.animal_cage.withdrawn", itemData.entityCount()));

            return;
        }

        if (penData.entityType() != itemData.entityType())
        {
            // Cannot merge different entities
            return;
        }

        AnimalDepositEvent animalDepositEvent = new AnimalDepositEvent(player,
            block.getLocation(),
            itemData,
            penData,
            true);

        if (!animalDepositEvent.callEvent())
        {
            player.sendMessage(AnimalPenPlugin.translations().
                getTranslatable("item.animal_pen.animal_cage.error.deposit"));
            return;
        }

        // Now just combine both data, and clear item.
        penData.addEntityCount(itemData.entityCount());

        // Merge cooldowns
        itemData.getCooldowns().forEach((key, value) -> penData.getCooldowns().merge(key, value, Math::max));
        itemData.getCooldowns().clear();

        // Merge scute data
        penData.setScutes(penData.scutes() + itemData.scutes());
        itemData.setScutes(0);

        // Check variants
        final int maxStoredVariants = AnimalPenPlugin.configuration().getMaxStoredVariants();
        long amount = itemData.entityCount();

        if (itemData.entityCount() > 1 &&
            penData.getVariants().size() + itemData.getVariants().size() > maxStoredVariants)
        {
            player.sendMessage(AnimalPenPlugin.translations().
                getTranslatable("item.animal_pen.animal_cage.error.too_many_variants"));

            penData.reduceEntityCount(1);
            itemData.setEntityCount(1);

            // Save reduced item data
            AnimalPenManager.setAnimalCageData(item, itemData);

            amount--;
        }
        else
        {
            int size = penData.getVariants().size();
            Iterator<EntitySnapshot> iterator = itemData.getVariants().iterator();

            while (size < maxStoredVariants && iterator.hasNext())
            {
                penData.addVariant(iterator.next());
                size++;
            }

            // just clear remining ones.
            itemData.getVariants().clear();

            // Clear item data
            AnimalPenManager.setAnimalCageData(item, null);
        }

        AnimalPenManager.setAnimalPenData(block, penData);

        player.sendMessage(AnimalPenPlugin.translations().
            getTranslatable("item.animal_pen.animal_cage.deposited", amount));

    }


    /**
     * This listener checks if player can interact with animal pen having empty hand
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInteractWithPenWithEmptyHand(PlayerInteractEvent event)
    {
        if (event.getHand() == null ||
            event.getItem() != null && !event.getItem().getType().isAir() ||
            event.getHand() == EquipmentSlot.OFF_HAND)
        {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || !event.getPlayer().isSneaking())
        {
            return;
        }

        Block block = event.getClickedBlock();

        if (!AnimalPenManager.isAnimalPen(block))
        {
            return;
        }

        event.setCancelled(true);

        AnimalData penData = AnimalPenManager.getAnimalData(block);

        if (penData == null)
        {
            return;
        }

        AnimalWithdrawEvent animalWithdrawEvent = new AnimalWithdrawEvent(event.getPlayer(),
            block.getLocation(),
            null,
            penData,
            true);

        if (!animalWithdrawEvent.callEvent())
        {
            event.getPlayer().sendMessage(AnimalPenPlugin.translations().
                getTranslatable("item.animal_pen.animal_cage.error.withdrawn"));
            return;
        }

        ItemStack itemStack = AnimalPenManager.createEmptyAnimalCage();
        AnimalPenManager.setAnimalCageData(itemStack, penData);

        AnimalPenManager.clearBlockData(block, true);

        event.getPlayer().getInventory().
            setItem(event.getHand() == null ? EquipmentSlot.HAND : event.getHand(), itemStack);

        event.getPlayer().sendMessage(AnimalPenPlugin.translations().
            getTranslatable("item.animal_pen.animal_cage.taken"));
    }


    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGHEST)
    public void onProtectionOfUsage(PlayerInteractEntityEvent event)
    {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getHand());

        if (!AnimalPenManager.isAnimalCage(item))
        {
            return;
        }

        event.setCancelled(true);
    }


    @EventHandler(ignoreCancelled = false, priority = EventPriority.HIGHEST)
    public void onProtectionOfUsage(PlayerInteractEvent event)
    {
        if (!AnimalPenManager.isAnimalCage(event.getItem()))
        {
            return;
        }

        event.setCancelled(true);
    }


    @EventHandler
    public void onItemCraft(CraftItemEvent event)
    {
        ItemStack result = event.getRecipe().getResult();

        if (!result.hasData(DataComponentTypes.CUSTOM_MODEL_DATA))
        {
            return;
        }

        CustomModelData data = result.getData(DataComponentTypes.CUSTOM_MODEL_DATA);

        if (data.strings().contains(AnimalPenManager.ANIMAL_CAGE_MODEL))
        {
            ItemMeta itemMeta = result.getItemMeta();
            itemMeta.displayName(AnimalPenPlugin.translations().
                getTranslatable("item.animal_pen.animal_cage.name").
                style(StyleUtil.WHITE));

            itemMeta.lore(List.of(
                AnimalPenPlugin.translations().getTranslatable("item.animal_pen.animal_cage.catch_tip.line1"),
                AnimalPenPlugin.translations().getTranslatable("item.animal_pen.animal_cage.catch_tip.line2"),
                AnimalPenPlugin.translations().getTranslatable("item.animal_pen.animal_cage.catch_tip.line3")
            ));

            result.setItemMeta(itemMeta);
            event.setCurrentItem(result);
        }
    }
}