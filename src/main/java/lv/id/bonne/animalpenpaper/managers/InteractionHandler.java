//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpenpaper.managers;


import com.destroystokyo.paper.MaterialTags;
import org.bukkit.*;
import org.bukkit.craftbukkit.damage.CraftDamageSource;
import org.bukkit.craftbukkit.entity.CraftAnimals;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;
import java.util.*;
import java.util.function.Consumer;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.entity.Bucketable;
import io.papermc.paper.potion.SuspiciousEffectEntry;
import lv.id.bonne.animalpenpaper.AnimalPenPlugin;
import lv.id.bonne.animalpenpaper.data.AnimalData;
import lv.id.bonne.animalpenpaper.events.block.AnimalBlockBreedEvent;
import lv.id.bonne.animalpenpaper.util.Utils;
import net.kyori.adventure.util.TriState;
import net.minecraft.advancements.CriteriaTriggers;


public class InteractionHandler
{
    public static void handleKilling(LivingEntity entity,
        Player player,
        ItemStack itemStack,
        AnimalData data,
        Consumer<AnimalData> dataApply)
    {
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
        dataApply.accept(data);

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

            int reward = ((Mob) entity).getPossibleExperienceReward();
            entity.getWorld().spawnEntity(location,
                EntityType.EXPERIENCE_ORB,
                CreatureSpawnEvent.SpawnReason.CUSTOM,
                orb -> ((ExperienceOrb) orb).setExperience(reward));
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


    public static void handleCooldownFinish(Entity entity, Material key, AnimalData animalData)
    {
        if (entity.getType() == EntityType.TURTLE &&
            key == Material.APPLE &&
            !AnimalPenPlugin.configuration().isDropScuteAtStart())
        {
            InteractionHandler.handleScutes(entity, animalData);
        }
        else if (entity.getType() == EntityType.SHEEP &&
            key == Material.SHEARS)
        {
            ((Sheep) entity).setSheared(false);

            // Remember shear value for snapshot
            animalData.setAppliedFlag(false);
        }
    }


    public static void handleItemInteraction(LivingEntity entity,
        Player player,
        ItemStack itemStack,
        AnimalData data,
        Consumer<AnimalData> dataApply,
        boolean isAnimalPen)
    {
        if (AnimalPenPlugin.animalFoodConfiguration().isFoodItem(entity, itemStack))
        {
            InteractionHandler.handleFood(entity,
                player,
                itemStack,
                data,
                dataApply,
                isAnimalPen);
        }
        else if (itemStack.getType() == Material.BRUSH)
        {
            InteractionHandler.handleBrush(entity, player, itemStack, data, dataApply);
        }
        else if (itemStack.getType() == Material.WATER_BUCKET)
        {
            InteractionHandler.handleWaterBucket(entity, player, itemStack, data, dataApply);
        }
        else if (itemStack.getType() == Material.SHEARS)
        {
            InteractionHandler.handleShears(entity, player, itemStack, data, dataApply);
        }
        else if (MaterialTags.DYES.isTagged(itemStack))
        {
            InteractionHandler.handleDyes(entity, player, itemStack, data, dataApply);
        }
        else if (itemStack.getType() == Material.BUCKET)
        {
            InteractionHandler.handleBucket(entity, player, itemStack, data, dataApply);
        }
        else if (itemStack.getType() == Material.GLASS_BOTTLE)
        {
            InteractionHandler.handleGlassBottle(entity, player, itemStack, data, dataApply);
        }
        else if (itemStack.getType() == Material.MAGMA_BLOCK)
        {
            InteractionHandler.handleMagmaBlock(entity, player, itemStack, data, dataApply);
        }
        else if (itemStack.getType() == Material.BOWL)
        {
            InteractionHandler.handleBowl(entity, player, itemStack, data, dataApply);
        }
        else if (Utils.getTag(NamespacedKey.minecraft("small_flowers")).isTagged(itemStack.getType()))
        {
            InteractionHandler.handleSmallFlowers(entity, player, itemStack, data, dataApply);
        }
    }


// ---------------------------------------------------------------------
// Section: Entity Methods
// ---------------------------------------------------------------------
    
    
    private static void handleFood(LivingEntity entity,
        Player player,
        ItemStack itemStack,
        AnimalData data,
        Consumer<AnimalData> dataApply,
        boolean isAnimalPen)
    {
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

        if (itemStack.getMaxStackSize() == 1)
        {
            // Tropical fishes will be taken from all buckets in player inventory.
            Map<Integer, ? extends ItemStack> all = player.getInventory().all(itemStack.getType());
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
            if (itemStack.getMaxStackSize() == 1)
            {
                int removedItems = stackSize % 2 == 1 ? stackSize - 1 : stackSize;

                while (removedItems-- > 0)
                {
                    int slot = player.getInventory().first(itemStack.getType());

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

        AnimalBlockBreedEvent breedEvent = new AnimalBlockBreedEvent(player,
            entity.getLocation(),
            entity.getType(),
            data.entityCount(),
            amount,
            isAnimalPen);

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

        if (entity.getType() == EntityType.TURTLE)
        {
            // Handle scutes
            data.setScutes(amount);

            if (AnimalPenPlugin.configuration().isDropScuteAtStart())
            {
                // Drop scutes at the start.
                InteractionHandler.handleScutes(entity, data);
            }
        }

        // Save data
        dataApply.accept(data);

        breedEvent.callEvent();
        
        if (!(entity instanceof Breedable))
        {
            // Ignore non-breedable mobs
            return;
        }

        // Use gaussian spread to get random amount.
        entity.getWorld().spawnEntity(entity.getLocation().add(0, 0.5, 0),
            EntityType.EXPERIENCE_ORB,
            CreatureSpawnEvent.SpawnReason.CUSTOM,
            orb -> ((ExperienceOrb) orb).setExperience((int)
                Math.round(amount * 4 + new Random().nextGaussian() * Math.sqrt(amount * 4))));

        if (AnimalPenPlugin.configuration().isTriggerAdvancements() && entity instanceof Animals)
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

        if (AnimalPenPlugin.configuration().isIncreaseStatistics())
        {
            player.incrementStatistic(Statistic.ANIMALS_BRED, amount);
        }
    }

    
    private static void handleScutes(Entity entity, AnimalData animalData)
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
    

    private static void handleBrush(Entity entity, 
        Player player, 
        ItemStack itemStack, 
        AnimalData data,
        Consumer<AnimalData> dataApply)
    {
        if (entity.getType() != EntityType.ARMADILLO)
        {
            // Only armadillo can be interacted with brush
            return;
        }
        
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
            AnimalPenPlugin.configuration().getEntityCooldown(
                entity.getType(),
                Material.BRUSH,
                data.entityCount()));

        // Save data
        dataApply.accept(data);
    }


    private static void handleWaterBucket(Entity entity,
        Player player, 
        ItemStack itemStack, 
        AnimalData data,
        Consumer<AnimalData> dataApply)
    {
        if (!(entity instanceof Bucketable bucketable))
        {
            // Not bucketable
            return;
        }
        
        if (data == null)
        {
            return;
        }

        if (data.hasCooldown(Material.WATER_BUCKET))
        {
            // under cooldown for feeding
            return;
        }

        ItemStack newBucket = bucketable.getBaseBucketItem();

        switch (entity.getType())
        {
            case AXOLOTL ->
            {
                Axolotl axolotl = (Axolotl) entity;

                newBucket.setData(DataComponentTypes.AXOLOTL_VARIANT, axolotl.getVariant());

                if (axolotl.customName() != null)
                {
                    newBucket.setData(DataComponentTypes.CUSTOM_NAME, axolotl.customName());
                }
            }
            case SALMON ->
            {
                newBucket.setData(DataComponentTypes.SALMON_SIZE, ((Salmon) entity).getVariant());
            }
            case TROPICAL_FISH ->
            {
                TropicalFish tropicalFish = (TropicalFish) entity;
                newBucket.setData(DataComponentTypes.TROPICAL_FISH_BASE_COLOR, tropicalFish.getBodyColor());
                newBucket.setData(DataComponentTypes.TROPICAL_FISH_PATTERN, tropicalFish.getPattern());
                newBucket.setData(DataComponentTypes.TROPICAL_FISH_PATTERN_COLOR,tropicalFish.getPatternColor());
            }
        }

        Sound sound = bucketable.getPickupSound();

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
        dataApply.accept(data);

        if (AnimalPenPlugin.configuration().isTriggerAdvancements())
        {
            // Trigger bucket filling
            CriteriaTriggers.FILLED_BUCKET.trigger(((CraftPlayer) player).getHandle(),
                CraftItemStack.asNMSCopy(newBucket));
        }
    }


    private static void handleShears(Entity entity,
        Player player,
        ItemStack itemStack,
        AnimalData data,
        Consumer<AnimalData> dataApply)
    {
        if (entity.getType() == EntityType.SHEEP)
        {
            InteractionHandler.handleShearsWool(entity, player, itemStack, data, dataApply);
        }
        else if (entity.getType() == EntityType.BEE)
        {
            InteractionHandler.handleShearsHoney(entity, player, itemStack, data, dataApply);
        }
    }


    private static void handleShearsHoney(Entity entity,
        Player player, 
        ItemStack itemStack,
        AnimalData data,
        Consumer<AnimalData> dataApply)
    {
        if (entity.getType() != EntityType.BEE)
        {
            // Only sheep can be interacted with shears
            return;
        }
        
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
            AnimalPenPlugin.configuration().getEntityCooldown(
                entity.getType(),
                Material.SHEARS,
                data.entityCount()));

        // Save data
        dataApply.accept(data);
    }


    private static void handleShearsWool(Entity entity,
        Player player, 
        ItemStack itemStack,
        AnimalData data,
        Consumer<AnimalData> dataApply)
    {
        if (entity.getType() != EntityType.SHEEP)
        {
            // Only sheep can be interacted with shears
            return;
        }
        
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

        Material woolMaterial = Utils.getWoolMaterial(sheep.getColor());

        int woolCount = 1;

        int dropLimits = AnimalPenPlugin.configuration().getDropLimits(Material.WHITE_WOOL);

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
            AnimalPenPlugin.configuration().getEntityCooldown(
                entity.getType(),
                Material.SHEARS,
                data.entityCount()));

        // Remember shear value for snapshot
        data.setAppliedFlag(true);

        // Save data
        dataApply.accept(data);
    }


    private static void handleBucket(Entity entity,
        Player player,
        ItemStack itemStack,
        AnimalData data,
        Consumer<AnimalData> dataApply)
    {
        if (entity.getType() == EntityType.CHICKEN ||
            entity.getType() == EntityType.SNIFFER ||
            entity.getType() == EntityType.TURTLE)
        {
            InteractionHandler.handleBucketEggs(entity, player, itemStack, data, dataApply);
        }
        else if (entity.getType() == EntityType.COW ||
            entity.getType() == EntityType.MOOSHROOM ||
            entity.getType() == EntityType.GOAT)
        {
            InteractionHandler.handleBucketMilk(entity, player, itemStack, data, dataApply);
        }
    }


    private static void handleBucketMilk(Entity entity,
        Player player,
        ItemStack itemStack,
        AnimalData data,
        Consumer<AnimalData> dataApply)
    {
        if (entity.getType() != EntityType.COW &&
            entity.getType() != EntityType.MOOSHROOM &&
            entity.getType() != EntityType.GOAT)
        {
            // Only COW, MOOSHROOM and GOAT can be interacted with bucket to get milk
            return;
        }

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
            AnimalPenPlugin.configuration().getEntityCooldown(
                entity.getType(),
                Material.BUCKET,
                data.entityCount()));

        // Save data
        dataApply.accept(data);
    }


    private static void handleGlassBottle(Entity entity,
        Player player,
        ItemStack itemStack, AnimalData data,
        Consumer<AnimalData> dataApply)
    {
        if (entity.getType() != EntityType.BEE)
        {
            // Only bee has glass bottle interaction
            return;
        }

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
            AnimalPenPlugin.configuration().getEntityCooldown(
                entity.getType(),
                Material.GLASS_BOTTLE,
                data.entityCount()));

        // Save data
        dataApply.accept(data);
    }


    private static void handleBucketEggs(Entity entity,
        Player player,
        ItemStack itemStack,
        AnimalData data,
        Consumer<AnimalData> dataApply)
    {
        if (entity.getType() != EntityType.CHICKEN &&
            entity.getType() != EntityType.SNIFFER &&
            entity.getType() != EntityType.TURTLE)
        {
            // Only chicken, snigger and turtle can be interacted with bucket to get eggs
            return;
        }

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
                Chicken.Variant variant = ((Chicken) entity).getVariant();

                if (variant == Chicken.Variant.WARM)
                {
                    material = Material.BROWN_EGG;
                }
                else if (variant == Chicken.Variant.COLD)
                {
                    material = Material.BLUE_EGG;
                }
                else
                {
                    material = Material.EGG;
                }

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

        int dropLimits = AnimalPenPlugin.configuration().getDropLimits(material);

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
            AnimalPenPlugin.configuration().getEntityCooldown(
                entity.getType(),
                Material.BUCKET,
                data.entityCount()));

        // Save data
        dataApply.accept(data);
    }


    private static void handleDyes(Entity entity,
        Player player,
        ItemStack itemStack,
        AnimalData data,
        Consumer<AnimalData> dataApply)
    {
        if (entity.getType() != EntityType.SHEEP)
        {
            // Only sheep can be interacted with shears
            return;
        }

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

        // Store dye color for snapshot
        data.setAppliedMaterial(itemStack.getType());

        if (player.getGameMode() != GameMode.CREATIVE)
        {
            itemStack.subtract();
        }

        Sheep sheep = (Sheep) entity;
        sheep.setColor(Utils.getDyeColor(itemStack.getType()));

        player.swingMainHand();

        entity.getWorld().playSound(entity,
            Sound.ITEM_DYE_USE,
            new Random().nextFloat(0.8f, 1.2f),
            1);

        data.setCooldown(Material.WHITE_DYE,
            AnimalPenPlugin.configuration().getEntityCooldown(
                entity.getType(),
                Material.WHITE_DYE,
                data.entityCount()));

        // Save data
        dataApply.accept(data);
    }


    private static void handleMagmaBlock(Entity entity,
        Player player,
        ItemStack itemStack,
        AnimalData data,
        Consumer<AnimalData> dataApply)
    {
        if (entity.getType() != EntityType.FROG)
        {
            return;
        }

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
            AnimalPenPlugin.configuration().getDropLimits(Material.PEARLESCENT_FROGLIGHT);

        if (dropLimits > 0)
        {
            froglightCount = Math.min(froglightCount, dropLimits);
        }

        Frog frog = (Frog) entity;
        Material material = Utils.getFrogLight(frog);

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
            AnimalPenPlugin.configuration().getEntityCooldown(
                entity.getType(),
                Material.MAGMA_BLOCK,
                data.entityCount()));

        // Save data
        dataApply.accept(data);
    }


    private static void handleBowl(Entity entity,
        Player player,
        ItemStack itemStack,
        AnimalData data,
        Consumer<AnimalData> dataApply)
    {
        if (entity.getType() == EntityType.MOOSHROOM)
        {
            // Only bee has glass bottle interaction
            InteractionHandler.handleBowlSoup(entity, player, itemStack, data, dataApply);
        }
        else if (entity.getType() == EntityType.SNIFFER)
        {
            // Only bee has glass bottle interaction
            InteractionHandler.handleBowlSeeds(entity, player, itemStack, data, dataApply);
        }
    }


    private static void handleBowlSoup(Entity entity,
        Player player,
        ItemStack itemStack,
        AnimalData data,
        Consumer<AnimalData> dataApply)
    {
        if (entity.getType() != EntityType.MOOSHROOM)
        {
            // Only bee has glass bottle interaction
            return;
        }

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
            AnimalPenPlugin.configuration().getEntityCooldown(
                entity.getType(),
                Material.BOWL,
                data.entityCount()));

        // Remove saved material
        data.setAppliedMaterial(null);

        // Save data
        dataApply.accept(data);
    }


    private static void handleBowlSeeds(Entity entity,
        Player player,
        ItemStack itemStack,
        AnimalData data,
        Consumer<AnimalData> dataApply)
    {
        if (entity.getType() != EntityType.SNIFFER)
        {
            return;
        }

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

        int dropLimits = AnimalPenPlugin.configuration().getDropLimits(Material.TORCHFLOWER_SEEDS);

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
            AnimalPenPlugin.configuration().getEntityCooldown(
                entity.getType(),
                Material.BOWL,
                data.entityCount()));

        // Save data
        dataApply.accept(data);
    }


    private static void handleSmallFlowers(Entity entity,
        Player player,
        ItemStack itemStack,
        AnimalData data,
        Consumer<AnimalData> dataApply)
    {
        if (entity.getType() != EntityType.MOOSHROOM)
        {
            // Only bee has glass bottle interaction
            return;
        }

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

        SuspiciousEffectEntry suspiciousEffectEntry = Utils.FLOWER_EFFECTS.get(itemStack.getType());

        if (suspiciousEffectEntry == null)
        {
            return;
        }

        mushroomCow.addEffectToNextStew(suspiciousEffectEntry, false);

        Utils.triggerItemUse(entity, player, itemStack, 1);

        // Store flower for entity snapshot
        data.setAppliedMaterial(itemStack.getType());

        if (player.getGameMode() != GameMode.CREATIVE)
        {
            itemStack.subtract();
        }

        player.swingMainHand();

        entity.getWorld().playSound(entity,
            Sound.ENTITY_MOOSHROOM_EAT,
            new Random().nextFloat(0.8f, 1.2f),
            1);

        // Save data
        dataApply.accept(data);
    }
}
