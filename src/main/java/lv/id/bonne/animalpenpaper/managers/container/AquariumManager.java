package lv.id.bonne.animalpenpaper.managers.container;


import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import lv.id.bonne.animalpenpaper.AnimalPenPlugin;
import lv.id.bonne.animalpenpaper.data.BlockDataType;
import lv.id.bonne.animalpenpaper.util.StyleUtil;


public class AquariumManager extends AbstractContainerManager
{

// ---------------------------------------------------------------------
// Section: Singleton
// ---------------------------------------------------------------------


    public static final AquariumManager INSTANCE = new AquariumManager();

    private AquariumManager() {}


// ---------------------------------------------------------------------
// Section: Abstract configuration
// ---------------------------------------------------------------------


    @Override
    protected NamespacedKey getDataKey()
    {
        return AQUARIUM_DATA_KEY;
    }


    @Override
    public String getBlockPrefix()
    {
        return "aquarium";
    }


    @Override
    public String getItemPrefix()
    {
        return "water_animal_container";
    }


    @Override
    public NamespacedKey getPickableTag()
    {
        return WATER_MOB_CONTAINER_PICKABLE;
    }


    @Override
    public String getEmptyContainerModel()
    {
        return WATER_CONTAINER_MODEL;
    }


    @Override
    protected String getFilledContainerModel()
    {
        return WATER_CONTAINER_FILLED_MODEL;
    }


    @Override
    public String getStructureModel()
    {
        return AQUARIUM_MODEL;
    }


    @Override
    protected double getAnimalSize()
    {
        return AnimalPenPlugin.configuration().getWaterAnimalSize();
    }


    @Override
    protected boolean isGrowEnabled()
    {
        return AnimalPenPlugin.configuration().isGrowWaterAnimals();
    }


    @Override
    protected double getEntityYOffset()
    {
        return 1.0;
    }


// ---------------------------------------------------------------------
// Section: Lifecycle hooks
// ---------------------------------------------------------------------


    @Override
    public boolean isStructureBlock(@Nullable Block block)
    {
        if (block == null || block.getType() != this.getStructureMaterial() && block.getType() != DEPRECATED_MATERIAL)
        {
            return false;
        }

        return block.getWorld().getPersistentDataContainer().has(this.penKey(block), BlockDataType.INSTANCE);
    }


    /** Waterlog the slab and place water above it when the first entity is spawned. */
    @Override
    protected void onFirstEntityPlaced(Block block)
    {
        if (block.getBlockData() instanceof Slab slab)
        {
            block.getRelative(BlockFace.UP).setType(Material.WATER);
            slab.setWaterlogged(true);
            block.setBlockData(slab);
        }
    }


    /** Undo waterlogging when the last animal is removed and the entity despawns. */
    @Override
    protected void onEntityRemoved(Entity entity)
    {
        Block block = entity.getLocation().add(0, -0.5, 0).getBlock();
        AquariumManager.removeWater(block);
    }


    /** Undo waterlogging when the block is broken or cleared. */
    @Override
    protected void onBlockCleared(Block block, boolean keepBlock)
    {
        AquariumManager.removeWater(block);
    }


    public Material getStructureMaterial()
    {
        return Material.STONE_SLAB;
    }


// ---------------------------------------------------------------------
// Section: Structure item creation
// ---------------------------------------------------------------------


    /**
     * The aquarium has a single appearance, so the default item carries only
     * the base model string with no variant suffix.
     */
    @Override
    public ItemStack createDefaultStructureItem()
    {
        ItemStack slab = new ItemStack(this.getStructureMaterial());
        var meta = slab.getItemMeta();
        if (meta == null) return slab;

        var customData = meta.getCustomModelDataComponent();
        customData.setStrings(List.of(AQUARIUM_MODEL));
        meta.setCustomModelDataComponent(customData);

        meta.displayName(AnimalPenPlugin.translations()
            .getTranslatable(this.getStructureTranslationName())
            .style(StyleUtil.WHITE));

        String tip = this.getStructureTranslationPrefix();

        meta.lore(List.of(
            AnimalPenPlugin.translations().getTranslatable(tip + ".tip.line1"),
            AnimalPenPlugin.translations().getTranslatable(tip + ".tip.line2"),
            AnimalPenPlugin.translations().getTranslatable(tip + ".tip.line3")
        ));

        slab.setItemMeta(meta);
        return slab;
    }


// ---------------------------------------------------------------------
// Section: Internal helpers
// ---------------------------------------------------------------------


    private static void removeWater(Block block)
    {
        if (block.getBlockData() instanceof Slab slab)
        {
            block.getRelative(BlockFace.UP).setType(Material.AIR);
            slab.setWaterlogged(false);
            block.setBlockData(slab);
        }
    }


// ---------------------------------------------------------------------
// Section: Constants
// ---------------------------------------------------------------------


    public static final NamespacedKey AQUARIUM_DATA_KEY =
        new NamespacedKey("animal_pen", "aquarium_data");

    public static final NamespacedKey WATER_MOB_CONTAINER_PICKABLE =
        new NamespacedKey("animal_pen", "water_mob_container_pickable");

    public static final String WATER_CONTAINER_MODEL = "animal_pen:water_animal_container";

    private static final String WATER_CONTAINER_FILLED_MODEL = "animal_pen:water_animal_container_filled";

    public static final String AQUARIUM_MODEL = "animal_pen:aquarium";

    private final Material DEPRECATED_MATERIAL = Material.SMOOTH_STONE_SLAB;
}