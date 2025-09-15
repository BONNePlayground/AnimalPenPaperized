package lv.id.bonne.animalpenpaper.commands;


import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import lv.id.bonne.animalpenpaper.AnimalPenPlugin;
import lv.id.bonne.animalpenpaper.config.ConfigurationManager;
import lv.id.bonne.animalpenpaper.menu.AnimalPenCreativeMenu;


public class AnimalPenCommands
{
    public static void register(ReloadableRegistrarEvent<Commands> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> baseLiteral = Commands.literal("animal_pen").
            requires(stack -> stack.getSender().isOp());

        LiteralArgumentBuilder<CommandSourceStack> reload = Commands.literal("reload").
            executes(ctx ->
            {
                AnimalPenPlugin.configurationManager().reloadConfig();
                AnimalPenPlugin.translations().reload();

                // Recreate menu
                MENU.close();
                MENU = new AnimalPenCreativeMenu();

                ctx.getSource().getSender().sendMessage("Config and locale files reloaded.");

                return 1;
            });

        LiteralArgumentBuilder<CommandSourceStack> reset = Commands.literal("reset").
            executes(ctx ->
            {
                AnimalPenPlugin.configurationManager().generateConfig(ConfigurationManager.Variants.GENERAL);
                AnimalPenPlugin.configurationManager().generateConfig(ConfigurationManager.Variants.ANIMAL_FOOD);
                AnimalPenPlugin.translations().reset();

                // Recreate menu
                MENU.close();
                MENU = new AnimalPenCreativeMenu();

                ctx.getSource().getSender().sendMessage("Config and locale files reset.");
                return 1;
            });

        LiteralArgumentBuilder<CommandSourceStack> items = Commands.literal("items").
            executes(ctx ->
            {
                CommandSender sender = ctx.getSource().getSender();

                if (sender instanceof Player player)
                {
                    MENU.openMenu(player);
                }
                else
                {
                    AnimalPenPlugin.getInstance().getLogger().warning("This is in-game command only.");
                }

                return 1;
            });


        baseLiteral.then(reset).then(reload).then(items);

        dispatcher.registrar().register(baseLiteral.build());
    }


    /**
     * The instance of creative menu. No need to have multiple I think?
     */
    private static AnimalPenCreativeMenu MENU = new AnimalPenCreativeMenu();
}
