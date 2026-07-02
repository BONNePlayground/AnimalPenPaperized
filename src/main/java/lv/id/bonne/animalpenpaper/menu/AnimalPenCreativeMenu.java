package lv.id.bonne.animalpenpaper.menu;


import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import lv.id.bonne.animalpenpaper.AnimalPenPlugin;
import lv.id.bonne.animalpenpaper.managers.AnimalPenManager;
import lv.id.bonne.animalpenpaper.managers.AquariumManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;


/**
 * This class manages creative menu for animal pens.
 */
public class AnimalPenCreativeMenu implements Listener, InventoryHolder
{
    public AnimalPenCreativeMenu()
    {
        this.inventory = Bukkit.createInventory(this, 36,
            AnimalPenPlugin.translations().getTranslatable("menu.animal_pen.creative.title"));

        List<ItemStack> customItems = List.of(AnimalPenManager.INSTANCE.createEmptyContainer(),
            AnimalPenManager.INSTANCE.createStructureItem("animal_pen:animal_pen_acacia"),
            AnimalPenManager.INSTANCE.createStructureItem("animal_pen:animal_pen_bamboo"),
            AnimalPenManager.INSTANCE.createStructureItem("animal_pen:animal_pen_birch"),
            AnimalPenManager.INSTANCE.createStructureItem("animal_pen:animal_pen_cherry"),
            AnimalPenManager.INSTANCE.createStructureItem("animal_pen:animal_pen_crimson"),
            AnimalPenManager.INSTANCE.createStructureItem("animal_pen:animal_pen_dark_oak"),
            AnimalPenManager.INSTANCE.createStructureItem("animal_pen:animal_pen_jungle"),
            AnimalPenManager.INSTANCE.createStructureItem("animal_pen:animal_pen_mangrove"),
            AnimalPenManager.INSTANCE.createStructureItem("animal_pen:animal_pen_oak"),
            AnimalPenManager.INSTANCE.createStructureItem("animal_pen:animal_pen_pale_oak"),
            AnimalPenManager.INSTANCE.createStructureItem("animal_pen:animal_pen_spruce"),
            AnimalPenManager.INSTANCE.createStructureItem("animal_pen:animal_pen_warped"),
            AquariumManager.INSTANCE.createEmptyContainer(),
            AquariumManager.INSTANCE.createDefaultStructureItem());

        for (int i = 0; i < customItems.size(); i++)
        {
            this.inventory.setItem(i, customItems.get(i));
        }

        // Add utility buttons in the bottom row
        this.setupUtilityButtons(this.inventory);

        AnimalPenPlugin.getInstance().getServer().getPluginManager().
            registerEvents(this, AnimalPenPlugin.getInstance());
    }


    public void openMenu(Player player)
    {
        player.openInventory(this.inventory);
    }


    private void setupUtilityButtons(Inventory inventory)
    {
        // Close button
        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeButton.getItemMeta();
        closeMeta.displayName(AnimalPenPlugin.translations().getTranslatable("menu.animal_pen.creative.button.close.title").
            color(NamedTextColor.RED));
        closeButton.setItemMeta(closeMeta);
        inventory.setItem(32, closeButton);

        // Info button
        ItemStack infoButton = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoButton.getItemMeta();
        infoMeta.displayName(AnimalPenPlugin.translations().getTranslatable("menu.animal_pen.creative.button.info.title"));
        infoMeta.lore(Arrays.asList(
            AnimalPenPlugin.translations().getTranslatable("menu.animal_pen.creative.button.info.tip.line1"),
            AnimalPenPlugin.translations().getTranslatable("menu.animal_pen.creative.button.info.tip.line2"),
            AnimalPenPlugin.translations().getTranslatable("menu.animal_pen.creative.button.info.tip.line3")
        ));
        infoButton.setItemMeta(infoMeta);
        inventory.setItem(30, infoButton);
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
        if (!(event.getWhoClicked() instanceof Player player))
        {
            return;
        }

        InventoryView view = event.getView();

        if (!(view.getTopInventory().getHolder() == this))
        {
            return;
        }

        int topSize = view.getTopInventory().getSize();
        int rawSlot = event.getRawSlot();

        // BLOCK anything involving GUI
        if (rawSlot < topSize ||
            event.getAction() == InventoryAction.COLLECT_TO_CURSOR ||
            event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY)
        {
            event.setCancelled(true);
        }

        // Only handle clicks inside GUI
        if (rawSlot >= topSize)
        {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType().isAir())
        {
            return;
        }

        int slot = event.getSlot();

        // Handle utility buttons
        if (slot == 32)
        {
            // Close button
            player.closeInventory();
            return;
        }
        else if (slot >= 27)
        {
            // Other bottom row slots
            return;
        }

        // Give the item to the player
        ItemStack itemToGive = clickedItem.clone();

        if (!event.getCursor().isEmpty() && !itemToGive.isSimilar(event.getCursor()) ||
            event.getAction() == InventoryAction.NOTHING ||
            event.getAction() == InventoryAction.UNKNOWN)
        {
            // Wrong item in inventory
            view.setCursor(ItemStack.empty());
            return;
        }

        if (event.getAction() == InventoryAction.HOTBAR_SWAP)
        {
            // Handle swapping with offhand
            if (player.getGameMode() != GameMode.CREATIVE)
            {
                player.getInventory().setItem(EquipmentSlot.OFF_HAND,
                    itemToGive.asQuantity(itemToGive.getMaxStackSize()));
            }
            else
            {
                player.getInventory().setItem(EquipmentSlot.OFF_HAND,
                    itemToGive.asOne());
            }

            return;
        }

        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY)
        {
            // Try to add to inventory, drop if full
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(
                player.getGameMode() == GameMode.CREATIVE ?
                    itemToGive.asQuantity(itemToGive.getMaxStackSize()) :
                    itemToGive.asOne());

            if (!leftover.isEmpty())
            {
                leftover.values().forEach(item -> player.getWorld().dropItem(player.getLocation(), item));
                player.sendMessage(AnimalPenPlugin.translations().
                    getTranslatable("menu.animal_pen.creative.warn.full-inventory"));
            }
            else
            {
                // Success message with amount
                Component itemName = clickedItem.getItemMeta().hasDisplayName() ?
                    clickedItem.getItemMeta().displayName() :
                    Component.text(clickedItem.getType().name().replace("_", " ").toLowerCase());

                player.sendMessage(AnimalPenPlugin.translations().
                    getTranslatable("menu.animal_pen.creative.success.give", itemToGive.getAmount(), itemName));
            }

            return;
        }

        if (event.getAction() == InventoryAction.PICKUP_ALL ||
            event.getAction() == InventoryAction.PICKUP_HALF ||
            event.getAction() == InventoryAction.PICKUP_ONE ||
            event.getAction() == InventoryAction.PLACE_ALL ||
            event.getAction() == InventoryAction.PLACE_SOME ||
            event.getAction() == InventoryAction.PLACE_ONE)
        {
            int amount = view.getCursor().getAmount();

            if (event.getClick() == ClickType.RIGHT && amount > 0)
            {
                amount--;
            }
            else
            {
                amount = Math.min(amount + 1, itemToGive.getMaxStackSize());
            }

            view.setCursor(itemToGive.asQuantity(amount));
        }
        else if (event.getClick() == ClickType.DOUBLE_CLICK ||
            event.getAction() == InventoryAction.CLONE_STACK)
        {
            view.setCursor(itemToGive.asQuantity(itemToGive.getMaxStackSize()));
        }
    }


    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event)
    {
        if (!(event.getWhoClicked() instanceof Player))
        {
            return;
        }

        InventoryView view = event.getView();

        if (!(view.getTopInventory().getHolder() == this))
        {
            return;
        }

        if (event.getRawSlots().stream().anyMatch(slot -> slot < 36))
        {
            event.setCancelled(true);
        }
    }


    public void close()
    {
        HandlerList.unregisterAll(this);

        // Close inventory for all viewers
        for (HumanEntity viewer : this.inventory.getViewers())
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


    private final Inventory inventory;
}