package lv.id.bonne.animalpenpaper.menu;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
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

        List<ItemStack> customItems = List.of(AnimalPenManager.createEmptyAnimalCage(),
            AnimalPenManager.createAnimalPen("animal_pen_acacia"),
            AnimalPenManager.createAnimalPen("animal_pen_bamboo"),
            AnimalPenManager.createAnimalPen("animal_pen_birch"),
            AnimalPenManager.createAnimalPen("animal_pen_cherry"),
            AnimalPenManager.createAnimalPen("animal_pen_crimson"),
            AnimalPenManager.createAnimalPen("animal_pen_dark_oak"),
            AnimalPenManager.createAnimalPen("animal_pen_jungle"),
            AnimalPenManager.createAnimalPen("animal_pen_mangrove"),
            AnimalPenManager.createAnimalPen("animal_pen_oak"),
            AnimalPenManager.createAnimalPen("animal_pen_pale_oak"),
            AnimalPenManager.createAnimalPen("animal_pen_spruce"),
            AnimalPenManager.createAnimalPen("animal_pen_warped"),
            AquariumManager.createEmptyWaterContainer(),
            AquariumManager.createAquarium());

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

        // Different click types give different amounts
        if (event.isShiftClick())
        {
            // Handle custom item giving
            if (player.getGameMode() != org.bukkit.GameMode.CREATIVE)
            {
                player.sendMessage(AnimalPenPlugin.translations().
                    getTranslatable("menu.animal_pen.creative.error.creative"));
                return;
            }

            itemToGive.setAmount(itemToGive.getMaxStackSize());
        }
        else
        {
            itemToGive.setAmount(1);
        }

        // Try to add to inventory, drop if full
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(itemToGive);

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
    }


    @Override
    @NotNull
    public Inventory getInventory()
    {
        return this.inventory;
    }


    private final Inventory inventory;
}