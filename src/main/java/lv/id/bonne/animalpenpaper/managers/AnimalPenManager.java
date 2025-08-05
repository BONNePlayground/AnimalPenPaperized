package lv.id.bonne.animalpenpaper.managers;


import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import lv.id.bonne.animalpenpaper.data.AnimalData;
import lv.id.bonne.animalpenpaper.data.AnimalDataType;
import lv.id.bonne.animalpenpaper.util.StyleUtil;
import net.kyori.adventure.text.Component;


/**
 * Clean storage class using the custom PersistentDataType
 */
public class AnimalPenManager
{
    private final static int ANIMAL_CAGE_MODEL = 10101010;

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
}