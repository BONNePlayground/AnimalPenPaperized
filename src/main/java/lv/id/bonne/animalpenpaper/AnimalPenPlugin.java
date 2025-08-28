package lv.id.bonne.animalpenpaper;


import org.bukkit.plugin.java.JavaPlugin;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lv.id.bonne.animalpenpaper.commands.AnimalPenCommands;
import lv.id.bonne.animalpenpaper.config.AnimalFoodConfiguration;
import lv.id.bonne.animalpenpaper.config.Configuration;
import lv.id.bonne.animalpenpaper.config.ConfigurationManager;
import lv.id.bonne.animalpenpaper.listeners.AnimalCageListener;
import lv.id.bonne.animalpenpaper.listeners.AnimalPenListener;
import lv.id.bonne.animalpenpaper.listeners.AquariumListener;
import lv.id.bonne.animalpenpaper.listeners.WaterAnimalContainerListener;
import lv.id.bonne.animalpenpaper.managers.DisplayTextManager;
import lv.id.bonne.animalpenpaper.managers.TranslationManager;


public class AnimalPenPlugin extends JavaPlugin
{
    @Override
    public void onLoad()
    {
        super.onLoad();
        AnimalPenPlugin.instance = this;
        this.translationManager = new TranslationManager();
    }


    @Override
    public void onEnable()
    {
        this.configuration.readConfig();

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, AnimalPenCommands::register);

        this.getServer().getPluginManager().registerEvents(new AnimalCageListener(), this);
        this.getServer().getPluginManager().registerEvents(new AnimalPenListener(), this);

        this.getServer().getPluginManager().registerEvents(new WaterAnimalContainerListener(), this);
        this.getServer().getPluginManager().registerEvents(new AquariumListener(), this);

        this.getServer().getPluginManager().registerEvents(new DisplayTextManager(), this);

        this.task = new DisplayTextManager();
        this.task.runTask();

        this.getLogger().info("AnimalPen plugin enabled.");
    }


    @Override
    public void onDisable()
    {
        this.task.bukkitTask.cancel();
        this.getLogger().info("AnimalPen plugin disabled.");
    }


    public static AnimalPenPlugin getInstance()
    {
        return AnimalPenPlugin.instance;
    }


    public static TranslationManager translations()
    {
        return AnimalPenPlugin.instance.translationManager;
    }


    public static ConfigurationManager configurationManager()
    {
        return AnimalPenPlugin.instance.configuration;
    }


    public static Configuration configuration()
    {
        return AnimalPenPlugin.instance.configuration.getConfiguration();
    }


    public static AnimalFoodConfiguration animalFoodConfiguration()
    {
        return AnimalPenPlugin.instance.configuration.getAnimalFoodConfiguration();
    }


    public DisplayTextManager task;

    private TranslationManager translationManager;

    private final ConfigurationManager configuration = new ConfigurationManager();

    private static AnimalPenPlugin instance;
}