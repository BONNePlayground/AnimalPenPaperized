package lv.id.bonne.animalpenpaper.commands;


import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import lv.id.bonne.animalpenpaper.AnimalPenPlugin;
import lv.id.bonne.animalpenpaper.config.ConfigurationManager;


public class AnimalPenCommands
{
    public static void register(ReloadableRegistrarEvent<Commands> dispatcher)
    {
        LiteralArgumentBuilder<CommandSourceStack> baseLiteral = Commands.literal("animal_pen").
            requires(stack -> stack.getSender().isOp());

        LiteralArgumentBuilder<CommandSourceStack> reload = Commands.literal("reload").
            executes(ctx ->
            {
                AnimalPenPlugin.CONFIG_MANAGER.reloadConfig();

                ctx.getSource().getSender().sendMessage("Config files reloaded.");

                return 1;
            });

        LiteralArgumentBuilder<CommandSourceStack> reset = Commands.literal("reset").
            executes(ctx ->
            {
                AnimalPenPlugin.CONFIG_MANAGER.generateConfig(ConfigurationManager.Variants.GENERAL);
                AnimalPenPlugin.CONFIG_MANAGER.generateConfig(ConfigurationManager.Variants.ANIMAL_FOOD);

                ctx.getSource().getSender().sendMessage("Config files reset.");
                return 1;
            });

        baseLiteral.then(reset).then(reload);

        dispatcher.registrar().register(baseLiteral.build());
    }
}
