package lv.id.bonne.animalpenpaper;


import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import lv.id.bonne.animalpenpaper.config.ConfigurationManager;
import lv.id.bonne.animalpenpaper.listeners.AnimalCageListener;
import lv.id.bonne.animalpenpaper.listeners.AnimalPenListener;
import lv.id.bonne.animalpenpaper.managers.AnimalPenManager;
import lv.id.bonne.animalpenpaper.managers.AnimalPenTasks;


public class AnimalPenPlugin extends JavaPlugin implements Listener
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

        this.getServer().getPluginManager().registerEvents(new AnimalCageListener(), this);
        this.getServer().getPluginManager().registerEvents(new AnimalPenListener(), this);
        this.getServer().getPluginManager().registerEvents(new AnimalPenTasks(), this);

        this.task = new AnimalPenTasks();
        this.task.runTask();

        this.registerAnimalPenRecipes();

        this.getLogger().info("AnimalPen enabled.");
    }


    @Override
    public void onDisable()
    {
        this.task.bukkitTask.cancel();
        this.getServer().removeRecipe(new NamespacedKey(this, "animal_cage"));
        this.getLogger().info("AnimalPen disabled.");
    }


    /**
     * Register custom recipes for creating animal pens
     */
    private void registerAnimalPenRecipes()
    {
        ShapedRecipe animalCageRecipe = new ShapedRecipe(
            new NamespacedKey(this, "animal_cage"),
            AnimalPenManager.createEmptyAnimalCage()
        );

        animalCageRecipe.shape(
            "BBB",
            "BGB",
            "BBB"
        );

        animalCageRecipe.setIngredient('G', Material.GLASS);
        animalCageRecipe.setIngredient('B', Material.IRON_BARS);

        this.getServer().addRecipe(animalCageRecipe);

        ShapedRecipe animalPenRecipe = new ShapedRecipe(
            new NamespacedKey(this, "animal_pen_oak"),
            AnimalPenManager.createAnimalPen()
        );

        animalPenRecipe.shape(
            "   ",
            "F F",
            "SSS"
        );

        animalPenRecipe.setIngredient('F', Material.OAK_FENCE);
        animalPenRecipe.setIngredient('S', Material.SMOOTH_STONE_SLAB);

        this.getServer().addRecipe(animalPenRecipe);
    }


    public static AnimalPenPlugin getInstance()
    {
        return AnimalPenPlugin.instance;
    }


    private static AnimalPenPlugin instance;

    public AnimalPenTasks task;

    public static final ConfigurationManager CONFIG_MANAGER = new ConfigurationManager();
}