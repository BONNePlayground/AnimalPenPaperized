package lv.id.bonne.animalpenpaper.menu;


import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import lv.id.bonne.animalpenpaper.AnimalPenPlugin;
import lv.id.bonne.animalpenpaper.data.AnimalData;
import lv.id.bonne.animalpenpaper.managers.AnimalPenManager;
import lv.id.bonne.animalpenpaper.managers.AquariumManager;
import lv.id.bonne.animalpenpaper.util.Utils;
import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;


/**
 * This class manages creative menu for animal pens.
 */
public class AnimalPenVariantMenu implements Listener, InventoryHolder
{
    public AnimalPenVariantMenu(Entity entity)
    {
        this.entity = entity;

        AnimalData animalData = this.getAnimalData();

        // from 0 till 45
        this.itemsPerPage = Math.min(45,
            Math.max(AnimalPenPlugin.configuration().getMaxStoredVariants(),
                animalData != null ? animalData.getVariants().size() : 0));

        this.menuSize = (int) (Math.ceil(this.itemsPerPage / 9.0) * 9) + 9;

        this.inventory = Bukkit.createInventory(this, this.menuSize,
            AnimalPenPlugin.translations().getTranslatable("menu.animal_pen.variants.title"));

        // Load and display variants for the first page
        this.loadVariants(0);

        AnimalPenPlugin.getInstance().getServer().getPluginManager().
            registerEvents(this, AnimalPenPlugin.getInstance());
    }


    private void loadVariants(int page)
    {
        this.currentPage = page;
        this.inventory.clear();

        AnimalData animalData = this.getAnimalData();
        if (animalData == null)
        {
            return;
        }

        List<EntitySnapshot> variants = animalData.getVariants();

        // Calculate start and end indices for current page
        int startIndex = page * this.itemsPerPage;
        int endIndex = Math.min(startIndex + this.itemsPerPage, variants.size());

        // Display variants as paper items
        for (int i = startIndex; i < endIndex; i++)
        {
            EntitySnapshot variant = variants.get(i);
            ItemStack variantItem = createVariantItem(variant, i);

            // Place item in inventory (accounting for page offset)
            int slotIndex = i - startIndex;

            if (slotIndex < 45)
            {
                this.inventory.setItem(slotIndex, variantItem);
            }
        }

        this.setupUtilityButtons(this.inventory, page);
    }


    private ItemStack createVariantItem(EntitySnapshot variant, int variantIndex)
    {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();

        // Display name
        meta.displayName(
            AnimalPenPlugin.translations().getTranslatable("menu.animal_pen.variants.variant_number", variantIndex + 1)
        );

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());

        String variantData = variant.getAsString();

        try
        {
            CompoundTag compoundTag = TagParser.parseCompoundFully(variantData);
            lore.addAll(this.getEntityData(this.entity.getType(), compoundTag));
        }
        catch (CommandSyntaxException e)
        {
            // ignored
        }

        lore.add(
            AnimalPenPlugin.translations().getTranslatable("menu.animal_pen.variants.entity_type",
                variant.getEntityType().name())
        );

        if (this.selectedVariantIndex == variantIndex)
        {
            lore.add(Component.empty());
            lore.add(AnimalPenPlugin.translations().getTranslatable("menu.animal_pen.variants.selected"));
        }
        else
        {
            lore.add(Component.empty());
            lore.add(AnimalPenPlugin.translations().getTranslatable("menu.animal_pen.variants.click_to_select"));
        }

        meta.lore(lore);

        // Store variant index in custom data for easy retrieval
        meta.getPersistentDataContainer().set(
            new NamespacedKey(AnimalPenPlugin.getInstance(), "variant_index"),
            PersistentDataType.INTEGER,
            variantIndex
        );

        item.setItemMeta(meta);
        return item;
    }


    private List<Component> getEntityData(EntityType entityType, CompoundTag compoundTag)
    {
        List<Component> componentList = new ArrayList<>();

        compoundTag.getString("CustomName").ifPresent(name ->
            componentList.add(AnimalPenPlugin.translations().getTranslatable(
                "menu.animal_pen.variants.custom_name", name)));

        compoundTag.getInt("AngerTime").ifPresent(angry ->
            componentList.add(AnimalPenPlugin.translations().getTranslatable(
                "menu.animal_pen.variants.angry", angry < 0)));

        compoundTag.getBoolean("variant").ifPresent(variant ->
            componentList.add(AnimalPenPlugin.translations().getTranslatable(
                "menu.animal_pen.variants.variant", variant)));

        compoundTag.getBoolean("EatingHaystack").ifPresent(eating ->
            componentList.add(AnimalPenPlugin.translations().getTranslatable(
                "menu.animal_pen.variants.eating", eating)));

        switch (entityType)
        {
            case ARMADILLO -> compoundTag.getString("state").ifPresent(state ->
                componentList.add(AnimalPenPlugin.translations().getTranslatable(
                    "menu.animal_pen.variants.state", Component.text(state))));
            case AXOLOTL -> compoundTag.getInt("Variant").ifPresent(variant ->
                componentList.add(AnimalPenPlugin.translations().getTranslatable(
                    "menu.animal_pen.variants.variant", Utils.getAxolotlType(variant))));
            case BEE ->
            {
                compoundTag.getBoolean("HasNectar").ifPresent(nectar ->
                    componentList.add(AnimalPenPlugin.translations().getTranslatable(
                        "menu.animal_pen.variants.has_nectar", nectar)));

                compoundTag.getBoolean("HasStung").ifPresent(stung ->
                    componentList.add(AnimalPenPlugin.translations().getTranslatable(
                        "menu.animal_pen.variants.has_stung", stung)));
            }
            case FOX ->
            {
                compoundTag.getBoolean("Crouching").ifPresent(crouching ->
                    componentList.add(AnimalPenPlugin.translations().getTranslatable(
                        "menu.animal_pen.variants.is_crouching", crouching)));
                compoundTag.getBoolean("Sitting").ifPresent(sitting ->
                    componentList.add(AnimalPenPlugin.translations().getTranslatable(
                        "menu.animal_pen.variants.is_sitting", sitting)));
                compoundTag.getBoolean("Sleeping").ifPresent(sleeping ->
                    componentList.add(AnimalPenPlugin.translations().getTranslatable(
                        "menu.animal_pen.variants.is_sleeping", sleeping)));
                compoundTag.getString("Type").ifPresent(type ->
                    componentList.add(AnimalPenPlugin.translations().getTranslatable(
                        "menu.animal_pen.variants.variant", Component.text(type))));
            }
            case GOAT ->
            {
                compoundTag.getBoolean("HasLeftHorn").ifPresent(left ->
                    componentList.add(AnimalPenPlugin.translations().getTranslatable(
                        "menu.animal_pen.variants.has_left_horn", left)));
                compoundTag.getBoolean("HasRightHorn").ifPresent(right ->
                    componentList.add(AnimalPenPlugin.translations().getTranslatable(
                        "menu.animal_pen.variants.has_right_horn", right)));
                compoundTag.getBoolean("IsScreamingGoat").ifPresent(screaming ->
                    componentList.add(AnimalPenPlugin.translations().getTranslatable(
                        "menu.animal_pen.variants.is_screaming", screaming)));
            }
            case HORSE -> compoundTag.getInt("Variant").ifPresent(variant ->
            {
                componentList.add(AnimalPenPlugin.translations().getTranslatable(
                    "menu.animal_pen.variants.color", Utils.getHorseColor(variant)));
                componentList.add(AnimalPenPlugin.translations().getTranslatable(
                    "menu.animal_pen.variants.markings", Utils.getHorseMarkings(variant)));
            });
            case LLAMA, TRADER_LLAMA -> compoundTag.getInt("Variant").ifPresent(variant ->
                componentList.add(AnimalPenPlugin.translations().getTranslatable(
                    "menu.animal_pen.variants.variant", Utils.getLlamaVariant(variant))));
            case MOOSHROOM -> compoundTag.getString("Type").ifPresent(variant ->
                componentList.add(AnimalPenPlugin.translations().getTranslatable(
                    "menu.animal_pen.variants.type", variant)));
            case PANDA -> compoundTag.getString("MainGene").ifPresent(variant ->
                componentList.add(AnimalPenPlugin.translations().getTranslatable(
                    "menu.animal_pen.variants.variant", variant)));
            case PARROT -> compoundTag.getInt("Variant").ifPresent(variant ->
                componentList.add(AnimalPenPlugin.translations().getTranslatable(
                    "menu.animal_pen.variants.variant", Utils.getParrotVariant(variant))));
            case PUFFERFISH -> compoundTag.getInt("PuffState").ifPresent(variant ->
                componentList.add(AnimalPenPlugin.translations().getTranslatable(
                    "menu.animal_pen.variants.state", Utils.getPufferState(variant))));
            case RABBIT -> compoundTag.getInt("RabbitType").ifPresent(variant ->
                componentList.add(AnimalPenPlugin.translations().getTranslatable(
                    "menu.animal_pen.variants.variant", Utils.getRabbitVariant(variant))));
            case SALMON -> compoundTag.getInt("type").ifPresent(variant ->
                componentList.add(AnimalPenPlugin.translations().getTranslatable(
                    "menu.animal_pen.variants.type", variant)));
            case SHEEP -> compoundTag.getByte("Color").ifPresent(color ->
                componentList.add(AnimalPenPlugin.translations().getTranslatable(
                    "menu.animal_pen.variants.color", Utils.getDyeColor(color))));
            case TROPICAL_FISH -> compoundTag.getInt("Variant").ifPresent(variant ->
            {
                int patternColor = (variant >> 24) & 0xFF;
                int bodyColor = (variant >> 16) & 0xFF;
                int pattern = (variant >> 8) & 0xFF;
                int size = variant & 0xFF;

                componentList.add(AnimalPenPlugin.translations().getTranslatable(
                    "menu.animal_pen.variants.color", Utils.getDyeColor((byte) bodyColor)));
                componentList.add(AnimalPenPlugin.translations().getTranslatable(
                    "menu.animal_pen.variants.pattern", Utils.getPattern(pattern)));
                componentList.add(AnimalPenPlugin.translations().getTranslatable(
                    "menu.animal_pen.variants.pattern_color", Utils.getDyeColor((byte) patternColor)));
                componentList.add(AnimalPenPlugin.translations().getTranslatable(
                    "menu.animal_pen.variants.size", size == 0 ? "small" : "large"));
            });
            case WOLF -> compoundTag.getString("sound_variant").ifPresent(soundVariant ->
                componentList.add(AnimalPenPlugin.translations().getTranslatable(
                    "menu.animal_pen.variants.sound_variant", soundVariant)));
        }

        return componentList;
    }


    private void setupUtilityButtons(Inventory inventory, int page)
    {
        AnimalData animalData = this.getAnimalData();

        if (animalData == null)
        {
            return;
        }

        int totalVariants = animalData.getVariants().size();
        int totalPages = (int) Math.ceil((double) totalVariants / this.itemsPerPage);

        // Previous button
        if (page > 0)
        {
            ItemStack prevButton = new ItemStack(Material.TIPPED_ARROW);
            ItemMeta prevMeta = prevButton.getItemMeta();
            ((PotionMeta) prevMeta).setColor(Color.RED);
            prevMeta.displayName(AnimalPenPlugin.translations()
                .getTranslatable("menu.animal_pen.variants.previous_page"));
            prevMeta.getPersistentDataContainer().set(
                new NamespacedKey(AnimalPenPlugin.getInstance(), "button_type"),
                PersistentDataType.STRING,
                "previous_page"
            );

            prevButton.setItemMeta(prevMeta);
            inventory.setItem(this.menuSize - 9, prevButton);
        }

        // Apply button
        ItemStack applyButton = new ItemStack(this.selectedVariantIndex >= 0 ? Material.LIME_DYE : Material.GRAY_DYE);
        ItemMeta applyMeta = applyButton.getItemMeta();
        applyMeta.displayName(AnimalPenPlugin.translations().getTranslatable("menu.animal_pen.variants.apply_title"));

        List<Component> applyLore = new ArrayList<>();

        if (this.selectedVariantIndex >= 0)
        {
            applyLore.add(AnimalPenPlugin.translations().getTranslatable("menu.animal_pen.variants.apply_description"));
        }
        else
        {
            applyLore.add(AnimalPenPlugin.translations().getTranslatable("menu.animal_pen.variants.select_first"));
        }
        applyMeta.lore(applyLore);

        applyMeta.getPersistentDataContainer().set(
            new NamespacedKey(AnimalPenPlugin.getInstance(), "button_type"),
            PersistentDataType.STRING,
            "apply"
        );
        applyButton.setItemMeta(applyMeta);
        inventory.setItem(this.menuSize - 6, applyButton);

        // Delete button
        ItemStack deleteButton = new ItemStack(this.selectedVariantIndex >= 0 ? Material.BARRIER : Material.GRAY_DYE);
        ItemMeta deleteMeta = deleteButton.getItemMeta();
        deleteMeta.displayName(AnimalPenPlugin.translations().
            getTranslatable("menu.animal_pen.variants.delete_title"));

        List<Component> deleteLore = new ArrayList<>();

        if (this.selectedVariantIndex >= 0)
        {
            deleteLore.add(AnimalPenPlugin.translations().
                getTranslatable("menu.animal_pen.variants.delete_description"));
            deleteLore.add(AnimalPenPlugin.translations().
                getTranslatable("menu.animal_pen.variants.delete_warning"));
        }
        else
        {
            deleteLore.add(AnimalPenPlugin.translations().getTranslatable("menu.animal_pen.variants.select_first"));
        }
        deleteMeta.lore(deleteLore);

        deleteMeta.getPersistentDataContainer().set(
            new NamespacedKey(AnimalPenPlugin.getInstance(), "button_type"),
            PersistentDataType.STRING,
            "delete"
        );
        deleteButton.setItemMeta(deleteMeta);
        inventory.setItem(this.menuSize - 4, deleteButton);

        // Next page button
        if (page < totalPages - 1)
        {
            ItemStack nextButton = new ItemStack(Material.TIPPED_ARROW);
            ItemMeta nextMeta = nextButton.getItemMeta();
            ((PotionMeta) nextMeta).setColor(Color.GREEN);
            nextMeta.displayName(AnimalPenPlugin.translations().getTranslatable("menu.animal_pen.variants.next_page"));
            nextMeta.getPersistentDataContainer().set(
                new NamespacedKey(AnimalPenPlugin.getInstance(), "button_type"),
                PersistentDataType.STRING,
                "next_page"
            );
            nextButton.setItemMeta(nextMeta);
            inventory.setItem(this.menuSize - 1, nextButton);
        }

        // Info item
        ItemStack infoItem = new ItemStack(Material.COMPASS);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.displayName(AnimalPenPlugin.translations().
            getTranslatable("menu.animal_pen.variants.page", page + 1, Math.max(1, totalPages)));

        List<Component> infoLore = new ArrayList<>();
        infoLore.add(AnimalPenPlugin.translations().
            getTranslatable("menu.animal_pen.variants.total", totalVariants));

        if (this.selectedVariantIndex >= 0)
        {
            infoLore.add(AnimalPenPlugin.translations().
                getTranslatable("menu.animal_pen.variants.selected_number", this.selectedVariantIndex + 1));
        }
        infoMeta.lore(infoLore);
        infoItem.setItemMeta(infoMeta);
        inventory.setItem(this.menuSize - 5, infoItem);
    }


    private AnimalData getAnimalData()
    {
        if (AnimalPenManager.isAnimalPen(this.entity))
        {
            return AnimalPenManager.getAnimalData(this.entity);
        }
        else if (AquariumManager.isAquarium(this.entity))
        {
            return AquariumManager.getAnimalData(this.entity);
        }
        return null;
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
        if (!(event.getWhoClicked() instanceof Player player))
        {
            return;
        }
        if (event.getInventory().getHolder() != this)
        {
            return;
        }

        event.setCancelled(true);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR)
        {
            return;
        }

        ItemMeta meta = clickedItem.getItemMeta();

        if (meta == null)
        {
            return;
        }

        PersistentDataContainer persistentData = meta.getPersistentDataContainer();
        String buttonType = persistentData.get(new NamespacedKey(AnimalPenPlugin.getInstance(), "button_type"),
            PersistentDataType.STRING);

        if (buttonType != null)
        {
            this.handleButtonClick(player, buttonType);
            return;
        }

        Integer variantIndex = persistentData.get(new NamespacedKey(AnimalPenPlugin.getInstance(), "variant_index"),
            PersistentDataType.INTEGER);

        if (variantIndex != null)
        {
            this.handleVariantSelection(player, variantIndex);
        }
    }


    private void handleButtonClick(Player player, String buttonType)
    {
        switch (buttonType)
        {
            case "previous_page" ->
            {
                if (currentPage > 0)
                {
                    this.loadVariants(currentPage - 1);
                    this.syncInventoryToViewers();
                }
            }
            case "next_page" ->
            {
                AnimalData animalData = this.getAnimalData();
                if (animalData != null)
                {
                    int totalPages = (int) Math.ceil((double) animalData.getVariants().size() / this.itemsPerPage);
                    if (this.currentPage < totalPages - 1)
                    {
                        this.loadVariants(this.currentPage + 1);
                        this.syncInventoryToViewers();
                    }
                }
            }
            case "apply" -> this.handleApplyVariant(player);
            case "delete" -> this.handleDeleteVariant(player);
        }
    }


    private void handleVariantSelection(Player player, int variantIndex)
    {
        if (this.selectedVariantIndex == variantIndex)
        {
            this.selectedVariantIndex = -1;
        }
        else
        {
            this.selectedVariantIndex = variantIndex;
        }

        this.loadVariants(this.currentPage);
        this.syncInventoryToViewers();
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
    }


    private void handleApplyVariant(Player player)
    {
        if (selectedVariantIndex < 0)
        {
            player.sendMessage(AnimalPenPlugin.translations().getTranslatable(
                "menu.animal_pen.variants.no_selection"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }

        AnimalData animalData = this.getAnimalData();
        if (animalData == null)
        {
            return;
        }

        List<EntitySnapshot> variants = animalData.getVariants();
        if (this.selectedVariantIndex >= variants.size())
        {
            return;
        }

        EntitySnapshot selectedVariant = variants.get(this.selectedVariantIndex);

        // Apply the variant (you'll need to implement this method in your managers)
        if (AnimalPenManager.isAnimalPen(this.entity))
        {
            AnimalPenManager.applyVariant(this.entity, selectedVariant);
        }
        else if (AquariumManager.isAquarium(this.entity))
        {
            AquariumManager.applyVariant(this.entity, selectedVariant);
        }

        player.sendMessage(AnimalPenPlugin.translations().
            getTranslatable("menu.animal_pen.variants.apply_success"));
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);

        player.closeInventory();
    }


    private void handleDeleteVariant(Player player)
    {
        if (this.selectedVariantIndex < 0)
        {
            player.sendMessage(AnimalPenPlugin.translations().getTranslatable(
                "menu.animal_pen.variants.no_selection"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }

        AnimalData animalData = this.getAnimalData();
        if (animalData == null)
        {
            return;
        }

        List<EntitySnapshot> variants = animalData.getVariants();
        if (this.selectedVariantIndex >= variants.size())
        {
            return;
        }

        // Remove the variant
        variants.remove(this.selectedVariantIndex);

        // Update the animal data (you may need to save this depending on your implementation)
        if (AnimalPenManager.isAnimalPen(this.entity))
        {
            AnimalPenManager.setAnimalPenData(this.entity, animalData);
        }
        else if (AquariumManager.isAquarium(this.entity))
        {
            AquariumManager.setAquariumData(this.entity, animalData);
        }

        // Reset selection
        this.selectedVariantIndex = -1;

        // If we deleted the last item on this page, go to previous page
        int itemsOnCurrentPage = variants.size() - (this.currentPage * this.itemsPerPage);

        if (itemsOnCurrentPage <= 0 && this.currentPage > 0)
        {
            this.currentPage--;
        }

        // Refresh the display
        this.loadVariants(this.currentPage);
        this.syncInventoryToViewers();

        player.sendMessage(AnimalPenPlugin.translations().getTranslatable(
            "menu.animal_pen.variants.delete_success"));

        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.5f, 1.0f);
    }


    private void syncInventoryToViewers()
    {
        for (HumanEntity viewer : this.inventory.getViewers())
        {
            if (viewer instanceof Player player)
            {
                player.updateInventory();
            }
        }
    }


    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event)
    {
        if (event.getInventory().getHolder() == this)
        {
            event.setCancelled(true); // Prevent dragging items
        }
    }


    public void close()
    {
        HandlerList.unregisterAll(this);

        // Close inventory for all viewers
        for (HumanEntity viewer : new ArrayList<>(this.inventory.getViewers()))
        {
            viewer.closeInventory();
        }

        // Clear and drop reference
        this.inventory.clear();
    }


    @Override
    @NotNull
    public Inventory getInventory()
    {
        return this.inventory;
    }


    public static void openMenu(Entity entity, Player player)
    {
        if (!AnimalPenManager.isAnimalPen(entity) && !AquariumManager.isAquarium(entity))
        {
            return;
        }

        AnimalPenVariantMenu animalPenVariantMenu = MENU_MAP.computeIfAbsent(entity, AnimalPenVariantMenu::new);
        player.openInventory(animalPenVariantMenu.getInventory());
    }


    public static void close(Entity entity)
    {
        AnimalPenVariantMenu removed = MENU_MAP.remove(entity);

        if (removed != null)
        {
            removed.close();
        }
    }


    private final Inventory inventory;

    private final Entity entity;

    private final int itemsPerPage;

    private final int menuSize;

    private int currentPage = 0;

    private int selectedVariantIndex = -1;

    private static final Map<Entity, AnimalPenVariantMenu> MENU_MAP = new WeakHashMap<>(10);
}
