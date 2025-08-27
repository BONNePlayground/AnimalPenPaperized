package lv.id.bonne.animalpenpaper;


import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lv.id.bonne.animalpenpaper.commands.AnimalPenCommands;
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
        AnimalPenPlugin.CONFIG_MANAGER.readConfig();

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, AnimalPenCommands::register);

        this.getServer().getPluginManager().registerEvents(new AnimalCageListener(), this);
        this.getServer().getPluginManager().registerEvents(new AnimalPenListener(), this);

        this.getServer().getPluginManager().registerEvents(new WaterAnimalContainerListener(), this);
        this.getServer().getPluginManager().registerEvents(new AquariumListener(), this);

        this.getServer().getPluginManager().registerEvents(new DisplayTextManager(), this);

        this.task = new DisplayTextManager();
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


    public static TranslationManager translations()
    {
        return AnimalPenPlugin.instance.translationManager;
    }


    public DisplayTextManager task;

    private TranslationManager translationManager;

    private static AnimalPenPlugin instance;

    public static final ConfigurationManager CONFIG_MANAGER = new ConfigurationManager();
}