package lv.id.bonne.animalpenpaper;


import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lv.id.bonne.animalpenpaper.commands.AnimalPenCommands;
import lv.id.bonne.animalpenpaper.config.ConfigurationManager;
import lv.id.bonne.animalpenpaper.listeners.AnimalCageListener;
import lv.id.bonne.animalpenpaper.listeners.AnimalPenListener;
import lv.id.bonne.animalpenpaper.managers.AnimalPenTasks;


public class AnimalPenPlugin extends JavaPlugin
{
    @Override
    public void onLoad()
    {
        super.onLoad();
        AnimalPenPlugin.instance = this;
    }


    @Override
    public void onEnable()
    {
        AnimalPenPlugin.CONFIG_MANAGER.readConfig();

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, AnimalPenCommands::register);

        this.getServer().getPluginManager().registerEvents(new AnimalCageListener(), this);
        this.getServer().getPluginManager().registerEvents(new AnimalPenListener(), this);
        this.getServer().getPluginManager().registerEvents(new AnimalPenTasks(), this);

        this.task = new AnimalPenTasks();
        this.task.runTask();

        this.getLogger().info("AnimalPen enabled.");
    }


    @Override
    public void onDisable()
    {
        this.task.bukkitTask.cancel();
        this.getServer().removeRecipe(new NamespacedKey(this, "animal_cage"));
        this.getLogger().info("AnimalPen disabled.");
    }


    public static AnimalPenPlugin getInstance()
    {
        return AnimalPenPlugin.instance;
    }


    private static AnimalPenPlugin instance;

    public AnimalPenTasks task;

    public static final ConfigurationManager CONFIG_MANAGER = new ConfigurationManager();
}