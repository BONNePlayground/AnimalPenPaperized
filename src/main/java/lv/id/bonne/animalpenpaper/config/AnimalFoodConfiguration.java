//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpenpaper.config;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import java.util.*;
import java.util.stream.Collectors;

import lv.id.bonne.animalpenpaper.config.adapters.FoodItemTypeAdapter;
import lv.id.bonne.animalpenpaper.config.annotations.JsonComment;


/**
 * The type Configuration.
 */
public class AnimalFoodConfiguration
{
    public static AnimalFoodConfiguration getDefaultConfig()
    {
        AnimalFoodConfiguration configuration = new AnimalFoodConfiguration();
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
        return this.foodItems == null;
    }


    /**
     * Sets defaults.
     */
    public void setDefaults(boolean init)
    {
        if (this.foodItems == null || init)
        {
            this.foodItems = new HashMap<>();
            this.populateDefaultCooldowns();
        }
    }


    private void populateDefaultCooldowns()
    {
        // Armadillo
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("armadillo"), i -> List.of(
            new FoodItem("#minecraft:armadillo_food")
        ));

        // Axolotl
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("axolotl"), i -> List.of(
            new FoodItem("#minecraft:axolotl_food")
        ));

        // Bee
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("bee"), i -> List.of(
            new FoodItem("#minecraft:bee_food")
        ));

        // Camel
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("camel"), i -> List.of(
            new FoodItem("#minecraft:camel_food")
        ));

        // Cat
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("cat"), i -> List.of(
            new FoodItem("#minecraft:cat_food")
        ));

        // Chicken
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("chicken"), i -> List.of(
            new FoodItem("#minecraft:chicken_food")
        ));

        // Cod
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("cod"), i -> List.of(
            new FoodItem("minecraft:seagrass"),
            new FoodItem("minecraft:kelp")
        ));

        // Cow
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("cow"), i -> List.of(
            new FoodItem("#minecraft:cow_food")
        ));

        // Dolphin
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("dolphin"), i -> List.of(
            new FoodItem("#minecraft:fishes")
        ));

        // Donkey
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("donkey"), i -> List.of(
            new FoodItem("#minecraft:donkey_food")
        ));

        // Fox
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("fox"), i -> List.of(
            new FoodItem("#minecraft:fox_food")
        ));

        // Frog
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("frog"), i -> List.of(
            new FoodItem("#minecraft:frog_food")
        ));

        // Glow Squid
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("glow_squid"), i -> List.of(
            new FoodItem("#minecraft:fishes")
        ));

        // Goat
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("goat"), i -> List.of(
            new FoodItem("#minecraft:goat_food")
        ));

        // Hoglin
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("hoglin"), i -> List.of(
            new FoodItem("#minecraft:hoglin_food")
        ));

        // Horse
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("horse"), i -> List.of(
            new FoodItem("#minecraft:horse_food")
        ));

        // Llama
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("llama"), i -> List.of(
            new FoodItem("#minecraft:llama_food")
        ));

        // Mooshroom
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("mooshroom"), i -> List.of(
            new FoodItem("#minecraft:cow_food")
        ));

        // Mule
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("mule"), i -> List.of(
            new FoodItem("#minecraft:mule_food")
        ));

        // Ocelot
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("ocelot"), i -> List.of(
            new FoodItem("#minecraft:ocelot_food")
        ));

        // Panda
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("panda"), i -> List.of(
            new FoodItem("#minecraft:panda_food")
        ));

        // Parrot
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("parrot"), i -> List.of(
            new FoodItem("#minecraft:parrot_food")
        ));

        // Pig
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("pig"), i -> List.of(
            new FoodItem("#minecraft:pig_food")
        ));

        // Polar Bear
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("polar_bear"), i -> List.of(
            new FoodItem("minecraft:salmon")
        ));

        // Pufferfish
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("pufferfish"), i -> List.of(
            new FoodItem("minecraft:seagrass"),
            new FoodItem("minecraft:kelp")
        ));

        // Rabbit
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("rabbit"), i -> List.of(
            new FoodItem("#minecraft:rabbit_food")
        ));

        // Salmon
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("salmon"), i -> List.of(
            new FoodItem("minecraft:seagrass"),
            new FoodItem("minecraft:kelp")
        ));

        // Sheep
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("sheep"), i -> List.of(
            new FoodItem("#minecraft:sheep_food")
        ));

        // Skeleton Horse
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("skeleton_horse"), i -> List.of(
            new FoodItem("#minecraft:horse_food")
        ));

        // Sniffer
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("sniffer"), i -> List.of(
            new FoodItem("#minecraft:sniffer_food")
        ));

        // Squid
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("squid"), i -> List.of(
            new FoodItem("#minecraft:fishes")
        ));

        // Strider
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("strider"), i -> List.of(
            new FoodItem("#minecraft:strider_food")
        ));

        // Trader Llama
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("trader_llama"), i -> List.of(
            new FoodItem("#minecraft:trader_llama_food")
        ));

        // Tropical Fish
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("tropical_fish"), i -> List.of(
            new FoodItem("minecraft:seagrass"),
            new FoodItem("minecraft:kelp")
        ));

        // Turtle
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("turtle"), i -> List.of(
            new FoodItem("#minecraft:turtle_food")
        ));

        // Wolf
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("wolf"), i -> List.of(
            new FoodItem("#minecraft:wolf_food")
        ));

        // Zombie Horse
        this.foodItems.computeIfAbsent(NamespacedKey.minecraft("zombie_horse"), i -> List.of(
            new FoodItem("#minecraft:horse_food")
        ));
    }


// ---------------------------------------------------------------------
// Section: Getters
// ---------------------------------------------------------------------


    /**
     * This method returns if given item is food item for given entity.
     */
    public boolean isFoodItem(@NotNull Entity entity, ItemStack item)
    {
        return this.foodItems.getOrDefault(entity.getType().getKey(), Collections.emptyList()).stream().
            anyMatch(foodItem -> foodItem.matches(item));
    }


    /**
     * This method returns List of Materials that entity can eat.
     */
    public List<Material> getFoodItems(EntityType entityType)
    {
        return this.foodItems.getOrDefault(entityType.getKey(), Collections.emptyList()).stream().
            flatMap(foodItem -> foodItem.allFoodItems.stream()).
            collect(Collectors.toList());
    }


// ---------------------------------------------------------------------
// Section: variables
// ---------------------------------------------------------------------


    /**
     * This class manages food item matching with either item or tag.
     */
    @JsonAdapter(FoodItemTypeAdapter.class)
    public static class FoodItem
    {
        public FoodItem(String identifier)
        {
            this.identifier = identifier;
            this.isTag = identifier.startsWith("#");
            this.allFoodItems = new ArrayList<>(1);

            if (this.isTag)
            {
                String tagName = identifier.substring(1);
                this.tagKey = NamespacedKey.fromString(tagName);
                this.material = null;

                if (this.tagKey != null)
                {
                    Tag<Material> tag = Bukkit.getTag(Tag.REGISTRY_ITEMS, this.tagKey, Material.class);

                    if (tag != null)
                    {
                        Arrays.stream(Material.values()).
                            filter(tag::isTagged).
                            forEach(this.allFoodItems::add);
                    }
                }
            }
            else
            {
                this.material = Material.matchMaterial(identifier);

                this.allFoodItems.add(this.material);

                this.tagKey = null;
            }
        }


        /**
         * Check if the given ItemStack matches this food item
         */
        public boolean matches(ItemStack itemStack)
        {
            if (itemStack == null || itemStack.getType() == Material.AIR)
            {
                return false;
            }

            if (this.isTag)
            {
                if (this.tagKey == null)
                {
                    return false;
                }

                // Get the tag and check if the item's material is in it
                Tag<Material> tag = Bukkit.getTag(Tag.REGISTRY_ITEMS, this.tagKey, Material.class);
                return tag != null && tag.isTagged(itemStack.getType());
            }
            else
            {
                return this.material != null && itemStack.getType() == this.material;
            }
        }


        public String getIdentifier()
        {
            return this.identifier;
        }


        /**
         * The original identifier for saving/reading.
         */
        private final String identifier;

        /**
         * Indicates if object is item tag or not.
         */
        private final boolean isTag;

        /**
         * Material if object is material.
         */
        private final Material material;

        /**
         * TagKey for object if it is tah.
         */
        private final NamespacedKey tagKey;

        /**
         * The list of materials under given item tag.
         */
        private final List<Material> allFoodItems;
    }


    @JsonComment("List of food items for each animal.")
    @JsonComment("`minecraft:<entity>` is a way how to define entity to have food item.")
    @JsonComment("Food items are list of foods: []")
    @JsonComment("Adding `#` before food item indicates that it is a tag. Otherwise it is parsed as item.")
    @JsonComment("<entity> : [<food_list>].")
    @Expose
    @SerializedName("animal_foods")
    private Map<NamespacedKey, List<FoodItem>> foodItems = new LinkedHashMap<>();
}
