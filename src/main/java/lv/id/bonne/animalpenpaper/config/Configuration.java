//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpenpaper.config;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import java.util.*;

import lv.id.bonne.animalpenpaper.config.annotations.JsonComment;
import lv.id.bonne.animalpenpaper.config.annotations.SerializeWithComments;


/**
 * The type Configuration.
 */
public class Configuration
{
    public static Configuration getDefaultConfig()
    {
        Configuration configuration = new Configuration();
        configuration.setDefaults(true);

        return configuration;
    }


    /**
     * Is invalid boolean.
     *
     * @return the boolean
     */
    public boolean isInvalid()
    {
        return this.dropLimitList == null ||
            this.cooldownList == null ||
            this.blockedAnimals == null ||
            this.waterAnimalSize == null ||
            this.waterAnimalSize <= 0 ||
            this.animalSize == null ||
            this.animalSize <= 0 ||
            this.growthMultiplier == null ||
            this.growthMultiplier < 0 ||
            this.attackCooldown == null ||
            this.attackCooldown < 0 ||
            this.maxStoredAnimalVariants == null ||
            this.maxStoredAnimalVariants < 0 ||
            this.triggerAdvancements == null ||
            this.increaseStatistics == null ||
            this.showCooldownsOnlyOnShift == null ||
            this.showAllInteractions == null;
    }


    /**
     * Sets defaults.
     */
    public void setDefaults(boolean init)
    {
        if (this.dropLimitList == null || init)
        {
            this.dropLimitList = new HashMap<>();
            this.populateDefaultDropLimits();
        }

        if (this.cooldownList == null || init)
        {
            this.cooldownList = new HashMap<>();
            this.populateDefaultCooldowns();
        }

        if (this.blockedAnimals == null || init)
        {
            this.blockedAnimals = new HashSet<>();
        }

        if (this.animalSize == null || this.animalSize <= 0 || init)
        {
            this.animalSize = 0.33f;
        }

        if (this.waterAnimalSize == null || this.waterAnimalSize <= 0 || init)
        {
            this.waterAnimalSize = 0.33f;
        }

        if (this.growthMultiplier == null || this.growthMultiplier < 0 || init)
        {
            this.growthMultiplier = 0.001f;
        }

        if (this.attackCooldown == null || this.attackCooldown < 0 || init)
        {
            this.attackCooldown = 5;
        }

        if (this.maxStoredAnimalVariants == null || this.maxStoredAnimalVariants < 0 || init)
        {
            this.maxStoredAnimalVariants = 10;
        }

        if (this.increaseStatistics == null || init)
        {
            this.increaseStatistics = false;
        }

        if (this.triggerAdvancements == null || init)
        {
            this.triggerAdvancements = false;
        }

        if (this.showCooldownsOnlyOnShift == null || init)
        {
            this.showCooldownsOnlyOnShift = false;
        }

        if (this.showAllInteractions == null || init)
        {
            this.showAllInteractions = true;
        }

        if (init)
        {
            this.maximalAnimalCount = Integer.MAX_VALUE;

            this.growAnimals = false;
            this.growWaterAnimals = false;

            this.dropScuteAtStart = false;
        }
    }


    private void populateDefaultCooldowns()
    {
        // Food item
        this.cooldownList.computeIfAbsent(Material.APPLE.getKey(), i -> new ArrayList<>()).
            add(new CooldownEntry(ANY,
                58 * 20,
                20,
                5 * 60 * 20));

        // Sheep and sharing
        this.cooldownList.computeIfAbsent(Material.SHEARS.getKey(), i -> new ArrayList<>()).
            add(new CooldownEntry(EntityType.SHEEP.getKey(),
                59 * 20,
                20,
                5 * 60 * 20));

        // Chicken and bucket
        this.cooldownList.computeIfAbsent(Material.BUCKET.getKey(), i -> new ArrayList<>()).
            add(new CooldownEntry(EntityType.CHICKEN.getKey(),
                60 * 5 * 20 + 20,
                -1 * 20,
                60 * 20));

        // Turtle and bucket
        this.cooldownList.computeIfAbsent(Material.BUCKET.getKey(), i -> new ArrayList<>()).
            add(new CooldownEntry(EntityType.TURTLE.getKey(),
                60 * 5 * 20 + 20,
                -1 * 20,
                60 * 20));

        // Bee and pollen
        this.cooldownList.computeIfAbsent(Material.SHEARS.getKey(), i -> new ArrayList<>()).
            add(new CooldownEntry(EntityType.BEE.getKey(),
                60 * 20 + 20,
                -1 * 20,
                20));

        this.cooldownList.computeIfAbsent(Material.GLASS_BOTTLE.getKey(), i -> new ArrayList<>()).
            add(new CooldownEntry(EntityType.BEE.getKey(),
                60 * 20 + 20,
                -1 * 20,
                20));

        // Init non-used to show options
        this.cooldownList.computeIfAbsent(Material.BUCKET.getKey(), i -> new ArrayList<>()).
            add(new CooldownEntry(EntityType.COW.getKey(),
                0,
                0,
                0));

        this.cooldownList.computeIfAbsent(Material.BOWL.getKey(), i -> new ArrayList<>()).
            add(new CooldownEntry(EntityType.MOOSHROOM.getKey(),
                0,
                0,
                0));

        this.cooldownList.computeIfAbsent(Material.BUCKET.getKey(), i -> new ArrayList<>()).
            add(new CooldownEntry(EntityType.MOOSHROOM.getKey(),
                0,
                0,
                0));

        this.cooldownList.computeIfAbsent(Material.BUCKET.getKey(), i -> new ArrayList<>()).
            add(new CooldownEntry(EntityType.GOAT.getKey(),
                0,
                0,
                0));

        this.cooldownList.computeIfAbsent(Material.MAGMA_BLOCK.getKey(), i -> new ArrayList<>()).
            add(new CooldownEntry(EntityType.FROG.getKey(),
                60 * 5 * 20 + 20,
                -1 * 20,
                10 * 20));
        this.cooldownList.computeIfAbsent(Material.BUCKET.getKey(), i -> new ArrayList<>()).
            add(new CooldownEntry(EntityType.SNIFFER.getKey(),
                60 * 5 * 20 + 20,
                -1 * 20,
                10 * 20));
        this.cooldownList.computeIfAbsent(Material.BOWL.getKey(), i -> new ArrayList<>()).
            add(new CooldownEntry(EntityType.SNIFFER.getKey(),
                60 * 5 * 20 + 20,
                -1 * 20,
                10 * 20));
    }


    private void populateDefaultDropLimits()
    {
        this.dropLimitList = new HashMap<>();
        this.dropLimitList.put(Material.EGG.getKey(), 16 * 5);
        this.dropLimitList.put(Material.BROWN_EGG.getKey(), 16 * 5);
        this.dropLimitList.put(Material.BLUE_EGG.getKey(), 16 * 5);
        this.dropLimitList.put(Material.TURTLE_EGG.getKey(), 64 * 5);
        this.dropLimitList.put(Material.WHITE_WOOL.getKey(), 64 * 5);
        this.dropLimitList.put(Material.PEARLESCENT_FROGLIGHT.getKey(), 64 * 5);
        this.dropLimitList.put(Material.TORCHFLOWER_SEEDS.getKey(), 64 * 5);
    }


// ---------------------------------------------------------------------
// Section: Getters
// ---------------------------------------------------------------------


    /**
     * This method returns cooldowns for given item using on given entity.
     *
     * @param entity Entity that is targeted.
     * @param usedItem Item that is used.
     * @param entityAmount Amount of entities.
     * @return Cooldown for next action.
     */
    public int getEntityCooldown(EntityType entity, Material usedItem, long entityAmount)
    {
        if (!this.cooldownList.containsKey(usedItem.getKey()))
        {
            return 0;
        }

        Collection<CooldownEntry> cooldownEntries = this.cooldownList.get(usedItem.getKey());

        return cooldownEntries.stream().
            filter(cooldownEntry -> cooldownEntry.entity.equals(entity.getKey())).
            findFirst().
            map(cooldownEntry -> cooldownEntry.getValue(entityAmount)).
            orElseGet(() -> cooldownEntries.stream().
                filter(cooldownEntry -> cooldownEntry.entity.equals(ANY)).
                findFirst().
                map(cooldownEntry -> cooldownEntry.getValue(entityAmount)).
                orElse(0));
    }


    /**
     * This method returns drop limit for given item.
     *
     * @param item Drop limit for item.
     * @return Limit of items that can be dropped at once.
     */
    public int getDropLimits(Material item)
    {
        return this.dropLimitList.getOrDefault(item.getKey(), 0);
    }


    /**
     * The maximal amount of animals a pen can store.
     *
     * @return The maximal amount of animals.
     */
    public long getMaximalAnimalCount()
    {
        return this.maximalAnimalCount;
    }


    /**
     * Is drop scute at start boolean.
     *
     * @return the boolean
     */
    public boolean isDropScuteAtStart()
    {
        return this.dropScuteAtStart;
    }


    /**
     * Is grow animals boolean.
     *
     * @return the boolean
     */
    public boolean isGrowAnimals()
    {
        return this.growAnimals;
    }


    /**
     * Gets animal size.
     *
     * @return the animal size
     */
    public float getAnimalSize()
    {
        return this.animalSize;
    }


    /**
     * Is grow water animals boolean.
     *
     * @return the boolean
     */
    public boolean isGrowWaterAnimals()
    {
        return this.growWaterAnimals;
    }


    /**
     * Gets water animal size.
     *
     * @return the water animal size
     */
    public float getWaterAnimalSize()
    {
        return this.waterAnimalSize;
    }


    /**
     * Gets growth multiplier.
     *
     * @return the growth multiplier
     */
    public Float getGrowthMultiplier()
    {
        return this.growthMultiplier;
    }


    /**
     * This indicates is given entity is blocked from being picked up.
     *
     * @param entityType Entity that need to be checked.
     * @return {@code true} if entity is blocked from being picked up, {@code false} otherwise.
     */
    public boolean isBlocked(EntityType entityType)
    {
        return this.blockedAnimals.contains(entityType.getKey());
    }


    /**
     * Gets attack cooldown.
     *
     * @return the attack cooldown
     */
    public int getAttackCooldown()
    {
        return this.attackCooldown;
    }


    /**
     * Gets max stored variants.
     *
     * @return the max stored variants
     */
    public int getMaxStoredVariants()
    {
        return this.maxStoredAnimalVariants;
    }


    /**
     * Gets trigger advancements.
     *
     * @return the trigger advancements
     */
    public boolean isTriggerAdvancements()
    {
        return this.triggerAdvancements;
    }


    /**
     * Gets increase statistics.
     *
     * @return the increase statistics
     */
    public boolean isIncreaseStatistics()
    {
        return this.increaseStatistics;
    }


    /**
     * Is show cooldowns only on shift boolean.
     *
     * @return the boolean
     */
    public boolean isShowCooldownsOnlyOnShift()
    {
        return this.showCooldownsOnlyOnShift;
    }


    /**
     * Is show all interactions value.
     *
     * @return the boolean
     */
    public boolean isShowAllInteractions()
    {
        return this.showAllInteractions;
    }


// ---------------------------------------------------------------------
// Section: variables
// ---------------------------------------------------------------------


    /**
     * The type Cooldown entry.
     */
    @SerializeWithComments
    public static class CooldownEntry
    {
        /**
         * Instantiates a new Cooldown entry.
         *
         * @param entity the entity
         * @param base the base
         * @param increment the increment
         * @param max the max
         */
        public CooldownEntry(NamespacedKey entity, int base, int increment, int max)
        {
            this.entity = entity;
            this.baseCooldown = base;
            this.incrementPerAnimal = increment;
            this.cooldownLimit = max;
        }


        /**
         * Gets value.
         *
         * @param entityAmount the entity amount
         * @return the value
         */
        public int getValue(long entityAmount)
        {
            if (this.incrementPerAnimal > 0)
            {
                long endValue = this.baseCooldown + entityAmount * this.incrementPerAnimal;

                return (int) Math.min(this.cooldownLimit, endValue);
            }
            else if (this.incrementPerAnimal < 0)
            {
                long endValue = this.baseCooldown + entityAmount * this.incrementPerAnimal;

                return (int) Math.max(this.cooldownLimit, endValue);
            }
            else
            {
                return this.baseCooldown;
            }
        }


        @JsonComment("The entity ID on which cooldown is applied.")
        @Expose
        @SerializedName("entity")
        private NamespacedKey entity;

        @JsonComment("The base cooldown value for action.")
        @JsonComment("0 means that there is no cooldown.")
        @JsonComment("Values in game ticks.")
        @Expose
        @SerializedName("base_cooldown")
        private int baseCooldown;

        @JsonComment("The increment of cooldown per each animal.")
        @JsonComment("0 means that there is no cooldown increment.")
        @JsonComment("Negative value decreases base cooldown value.")
        @JsonComment("Values in game ticks.")
        @Expose
        @SerializedName("animal_increment")
        private int incrementPerAnimal;

        @JsonComment("The the maximal cooldown that can be applied.")
        @JsonComment("0 means that there is no cooldown limitation.")
        @JsonComment("Negative animal_increment makes this act as lowest limit.")
        @JsonComment("Values in game ticks.")
        @Expose
        @SerializedName("cooldown_limit")
        private int cooldownLimit;
    }


    @JsonComment("List of cooldowns that are applied when player performs action.")
    @JsonComment("Specifying: `animal_pen:any` will indicate that any entity using that item will have same cooldown.")
    @JsonComment("`minecraft:apple` is universal food item. It is used to indicate for feeding action.")
    @JsonComment("`minecraft:honey_bloc` is used to indicate pollen regeneration cooldown.")
    @JsonComment("<item> : <cooldown>.")
    @Expose
    @SerializedName("cooldowns")
    private Map<NamespacedKey, List<CooldownEntry>> cooldownList = new HashMap<>();

    @JsonComment("A cooldown value in game ticks between attacks that players can perform on animal pens.")
    @JsonComment("Default value: 5 game tick")
    @Expose
    @SerializedName("attack_cooldown")
    private Integer attackCooldown;

    @JsonComment("List of drop limits for items when player harvests Material.")
    @JsonComment("<item> : <drop_limit>.")
    @Expose
    @SerializedName("drop_limits")
    private Map<NamespacedKey, Integer> dropLimitList = new HashMap<>();

    @JsonComment("Allows to set maximal amount of animals in the pen.")
    @JsonComment("Setting 0 will remove any limit.")
    @Expose
    @SerializedName("animal_limit_in_pen")
    private long maximalAnimalCount = Integer.MAX_VALUE;

    @JsonComment("Allows to toggle if animal cooldowns should be showed only if player is crouching.")
    @JsonComment("This may* increase performance impact as will not require recalculations on shift.")
    @JsonComment("Default value = false")
    @Expose
    @SerializedName("show_cooldowns_only_on_crouch")
    private Boolean showCooldownsOnlyOnShift;

    @JsonComment("Allows to toggle if animal interactions should be showed even if the cooldown is not set.")
    @JsonComment("Default value = true")
    @Expose
    @SerializedName("show_all_interactions")
    private Boolean showAllInteractions;

    @JsonComment("Allows to enable animal growing in animal pen.")
    @JsonComment("The more animals are inside it, the larger it will be.")
    @Expose
    @SerializedName("animals_can_grow")
    private boolean growAnimals = false;

    @JsonComment("Allows to change default animal size in pen.")
    @Expose
    @SerializedName("animal_size")
    private Float animalSize;

    @JsonComment("Allows to enable water animal growing in aquarium.")
    @JsonComment("The more animals are inside it, the larger it will be.")
    @Expose
    @SerializedName("water_animals_can_grow")
    private boolean growWaterAnimals = false;

    @JsonComment("Allows to change default water animal size in aquarium.")
    @Expose
    @SerializedName("water_animal_size")
    private Float waterAnimalSize;

    @JsonComment("Allows to set how fast animals grows in pen and aquarium.")
    @JsonComment("Each animal is multiplied by given value to get end size.")
    @JsonComment("This option works only if animals_can_grow or water_animals_can_grow is enabled.")
    @JsonComment("Default value = 0.001")
    @Expose
    @SerializedName("growth_multiplier")
    private Float growthMultiplier;

    @JsonComment("Allows to specify if turtle scute are dropped when player breeds animal (true).")
    @JsonComment("or when food cooldown timer is finished (false).")
    @Expose
    @SerializedName("turtle_scute_drop_time")
    private boolean dropScuteAtStart = false;

    @JsonComment("Allows to set how many different animal variants can be stored per item.")
    @JsonComment("Players will not be able to store more different variants than this value.")
    @JsonComment("Be aware, this increases NBT data size, so not recommended to put infinite amount.")
    @JsonComment("Default value = 10")
    @Expose
    @SerializedName("max_stored_animal_variants")
    private Integer maxStoredAnimalVariants;

    @JsonComment("Allows to set if interactions with animal pens/aquariums should trigger advancements.")
    @JsonComment("Triggered Criteria: player_interacted_with_entity, bred_animals, filled_bucket, player_killed_entity")
    @JsonComment("Default value = false")
    @Expose
    @SerializedName("trigger_advancements")
    private Boolean triggerAdvancements;

    @JsonComment("Allows to set if interactions with animal pens/aquariums should increase statistics.")
    @JsonComment("Increased Statistics: Animals Bred, Mob Kills, Use Item <item>, Kill Entity <entity>")
    @JsonComment("Default value = false")
    @Expose
    @SerializedName("increase_statistics")
    private Boolean increaseStatistics;

    @JsonComment("Set of animals that are blocked from picking up.")
    @Expose
    @SerializedName("blocked_animals")
    private Set<NamespacedKey> blockedAnimals = new HashSet<>();

    @JsonComment("")
    @Expose(serialize = false, deserialize = false)
    private final static NamespacedKey ANY = new NamespacedKey("animal_pen_paper", "any");
}
