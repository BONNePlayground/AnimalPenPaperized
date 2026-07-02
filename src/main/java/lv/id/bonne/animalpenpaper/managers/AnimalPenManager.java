package lv.id.bonne.animalpenpaper.managers;


import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.List;

import io.papermc.paper.potion.SuspiciousEffectEntry;
import lv.id.bonne.animalpenpaper.AnimalPenPlugin;
import lv.id.bonne.animalpenpaper.data.AnimalData;
import lv.id.bonne.animalpenpaper.data.BlockData;
import lv.id.bonne.animalpenpaper.util.StyleUtil;
import lv.id.bonne.animalpenpaper.util.Utils;


public class AnimalPenManager extends AbstractContainerManager
{

// ---------------------------------------------------------------------
// Section: Singleton
// ---------------------------------------------------------------------


    public static final AnimalPenManager INSTANCE = new AnimalPenManager();


    private AnimalPenManager() {}


// ---------------------------------------------------------------------
// Section: Abstract configuration
// ---------------------------------------------------------------------


    @Override
    protected NamespacedKey getDataKey()
    {
        return ANIMAL_DATA_KEY;
    }


    @Override
    public String getBlockPrefix()
    {
        return "animal_pen";
    }


    @Override
    public String getItemPrefix()
    {
        return "animal_cage";
    }


    @Override
    public NamespacedKey getPickableTag()
    {
        return ANIMAL_CAGE_PICKABLE;
    }


    @Override
    public String getEmptyContainerModel()
    {
        return ANIMAL_CAGE_MODEL;
    }


    @Override
    protected String getFilledContainerModel()
    {
        return ANIMAL_CAGE_FILLED_MODEL;
    }


    @Override
    public String getStructureModel()
    {
        return ANIMAL_PEN_MODEL;
    }


    @Override
    protected double getAnimalSize()
    {
        return AnimalPenPlugin.configuration().getAnimalSize();
    }


    @Override
    protected boolean isGrowEnabled()
    {
        return AnimalPenPlugin.configuration().isGrowAnimals();
    }


    @Override
    protected double getEntityYOffset()
    {
        return 0.5;
    }


// ---------------------------------------------------------------------
// Section: Lifecycle hooks
// ---------------------------------------------------------------------


    /**
     * Applies sheep shearing state and colour, or mooshroom stew effects,
     * after the display entity is first spawned into the world.
     */
    @Override
    protected void onEntitySpawned(Block block, Entity entity, AnimalData newData, BlockData blockData)
    {
        if (entity instanceof Sheep sheep)
        {
            newData.getAppliedFlag().ifPresent(sheep::setSheared);
            newData.getAppliedMaterial().ifPresent(dye -> sheep.setColor(Utils.getDyeColor(dye)));
        }
        else if (entity instanceof MushroomCow cow)
        {
            newData.getAppliedMaterial().ifPresent(dye ->
            {
                SuspiciousEffectEntry suspiciousEffectEntry = Utils.FLOWER_EFFECTS.get(dye);

                if (suspiciousEffectEntry != null)
                {
                    cow.addEffectToNextStew(suspiciousEffectEntry, true);
                }
            });
        }
    }


// ---------------------------------------------------------------------
// Section: Structure item creation
// ---------------------------------------------------------------------


    @Override
    public ItemStack createDefaultStructureItem()
    {
        return this.createStructureItem("animal_pen_oak");
    }


    /**
     * Creates a placeable structure item with the given model-data {@code variant} string appended (e.g.
     * {@code "animal_pen:animal_pen_oak"}).
     */
    public ItemStack createStructureItem(String variant)
    {
        ItemStack slab = new ItemStack(this.getStructureMaterial());
        ItemMeta meta = slab.getItemMeta();
        if (meta == null)
        {
            return slab;
        }

        CustomModelDataComponent customData = meta.getCustomModelDataComponent();
        customData.setStrings(List.of(getStructureModel(), variant));
        meta.setCustomModelDataComponent(customData);

        meta.displayName(AnimalPenPlugin.translations()
            .getTranslatable("item.animal_pen." + variant.replace(":", "."))
            .style(StyleUtil.WHITE));

        String tipPrefix = this.getStructureTranslationPrefix();
        meta.lore(List.of(
            AnimalPenPlugin.translations().getTranslatable(tipPrefix + ".tip.line1"),
            AnimalPenPlugin.translations().getTranslatable(tipPrefix + ".tip.line2"),
            AnimalPenPlugin.translations().getTranslatable(tipPrefix + ".tip.line3")
        ));

        slab.setItemMeta(meta);
        return slab;
    }


// ---------------------------------------------------------------------
// Section: Constants
// ---------------------------------------------------------------------


    public static final NamespacedKey ANIMAL_DATA_KEY =
        new NamespacedKey("animal_pen", "animal_pen_data");

    public static final NamespacedKey ANIMAL_CAGE_PICKABLE =
        new NamespacedKey("animal_pen", "animal_cage_pickable");

    public static final String ANIMAL_CAGE_MODEL = "animal_pen:animal_cage";

    private static final String ANIMAL_CAGE_FILLED_MODEL = "animal_pen:animal_cage_filled";

    public static final String ANIMAL_PEN_MODEL = "animal_pen:animal_pen";
}