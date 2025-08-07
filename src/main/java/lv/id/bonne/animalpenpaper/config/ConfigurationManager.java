package lv.id.bonne.animalpenpaper.config;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.bukkit.NamespacedKey;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import lv.id.bonne.animalpenpaper.AnimalPenPlugin;
import lv.id.bonne.animalpenpaper.config.adapters.FoodItemTypeAdapter;
import lv.id.bonne.animalpenpaper.config.adapters.NamespacedKeyTypeAdapter;
import lv.id.bonne.animalpenpaper.config.util.CommentGeneration;


/**
 * The configuration file that allows modifying some of settings.
 */
public class ConfigurationManager
{
    /**
     * Default constructor.
     */
    public ConfigurationManager()
    {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        // Register type adapter for ResourceLocation
        builder.registerTypeAdapter(NamespacedKey.class, new NamespacedKeyTypeAdapter());
        builder.registerTypeAdapter(AnimalFoodConfiguration.FoodItem.class, new FoodItemTypeAdapter());

        this.gson = builder.create();
    }


    /**
     * This method generates config if it is missing.
     */
    public void generateConfig(Variants config)
    {
        this.reset(config);

        try
        {
            this.writeConfig(config, true);
        }
        catch (IOException e)
        {
            AnimalPenPlugin.getInstance().getLogger().throwing("ConfigManager","Error Generating config file: ", e);
        }
    }


    /**
     * This returns the location of the config file.
     *
     * @return The config file location
     */
    private File getConfigFile(Variants config)
    {
        return new File(AnimalPenPlugin.getInstance().getDataFolder(), config.getFile());
    }


    /**
     * This method reads the config file from file.
     */
    public void readConfig()
    {
        try (FileReader reader = new FileReader(this.getConfigFile(Variants.GENERAL)))
        {
            this.configuration = this.gson.fromJson(reader, Configuration.class);

            if (this.isInvalid(Variants.GENERAL))
            {
                this.configuration.setDefaults(false);
                this.writeConfig(Variants.GENERAL, false);
            }
        }
        catch (JsonSyntaxException var2)
        {
            this.reset(Variants.GENERAL);

            try
            {
                this.writeConfig(Variants.GENERAL, false);
            }
            catch (IOException ignore)
            {
            }

            AnimalPenPlugin.getInstance().getLogger().warning("Failed to read config. Generated default one.");
        }
        catch (IOException var2)
        {
            this.generateConfig(Variants.GENERAL);
            AnimalPenPlugin.getInstance().getLogger().warning("Failed to open config. Generated default one.");
        }

        try (FileReader reader = new FileReader(this.getConfigFile(Variants.ANIMAL_FOOD)))
        {
            this.animalFoodConfiguration = this.gson.fromJson(reader, AnimalFoodConfiguration.class);

            if (this.isInvalid(Variants.ANIMAL_FOOD))
            {
                this.animalFoodConfiguration.setDefaults(false);
                this.writeConfig(Variants.ANIMAL_FOOD, false);
            }
        }
        catch (JsonSyntaxException var2)
        {
            this.reset(Variants.ANIMAL_FOOD);

            try
            {
                this.writeConfig(Variants.ANIMAL_FOOD, false);
            }
            catch (IOException ignore)
            {
            }

            AnimalPenPlugin.getInstance().getLogger().warning("Failed to read animal food config. Generated default one.");
        }
        catch (IOException var2)
        {
            this.generateConfig(Variants.ANIMAL_FOOD);
            AnimalPenPlugin.getInstance().getLogger().warning("Failed to open animal food config. Generated default one.");
        }
    }


    /**
     * Reload config configuration.
     */
    public void reloadConfig()
    {
        try (FileReader reader = new FileReader(this.getConfigFile(Variants.GENERAL)))
        {
            this.configuration = this.gson.fromJson(reader, Configuration.class);

            if (this.isInvalid(Variants.GENERAL))
            {
                AnimalPenPlugin.getInstance().getLogger().warning("Failed to validate config.");
            }
        }
        catch (IOException var2)
        {
            AnimalPenPlugin.getInstance().getLogger().warning("Failed to read config. " + var2.getMessage());
        }

        try (FileReader reader = new FileReader(this.getConfigFile(Variants.ANIMAL_FOOD)))
        {
            this.animalFoodConfiguration = this.gson.fromJson(reader, AnimalFoodConfiguration.class);

            if (this.isInvalid(Variants.ANIMAL_FOOD))
            {
                AnimalPenPlugin.getInstance().getLogger().warning("Failed to validate animal food config.");
            }
        }
        catch (IOException var2)
        {
            AnimalPenPlugin.getInstance().getLogger().warning("Failed to read animal food config. " + var2.getMessage());
        }
    }


    /**
     * This method resets configs to default values.
     */
    protected void reset(Variants config)
    {
        if (config == Variants.GENERAL) this.configuration = Configuration.getDefaultConfig();
        if (config == Variants.ANIMAL_FOOD) this.animalFoodConfiguration = AnimalFoodConfiguration.getDefaultConfig();
    }


    /**
     * This method returns if configs were invalid.
     *
     * @return {@code true} if configs were invalid.
     */
    private boolean isInvalid(Variants config)
    {
        if (config == Variants.GENERAL)
        {
            return this.configuration == null || this.configuration.isInvalid();
        }
        else if (config == Variants.ANIMAL_FOOD)
        {
            return this.animalFoodConfiguration == null || this.animalFoodConfiguration.isInvalid();
        }

        return true;
    }


    /**
     * This method writes the config file.
     *
     * @throws IOException Exception if writing failed.
     */
    public void writeConfig(Variants config, boolean overwrite) throws IOException
    {
        File dir = AnimalPenPlugin.getInstance().getDataFolder();

        if (dir.exists() || dir.mkdirs())
        {
            if (this.getConfigFile(config).exists() && !overwrite)
            {
                // Create backup file.
                int backupNumber = 1;
                File backupFile;

                do
                {
                    backupFile = new File(dir, this.getConfigFile(config).getName() + ".bak" + backupNumber);
                    backupNumber++;
                }
                while (backupFile.exists());

                Files.copy(this.getConfigFile(config).toPath(), backupFile.toPath());
            }

            if (this.getConfigFile(config).exists() || this.getConfigFile(config).createNewFile())
            {
                try
                {
                    Path path = Paths.get(this.getConfigFile(config).toURI());

                    if (config == Variants.GENERAL)
                        Files.write(path, CommentGeneration.writeWithComments(this.gson, this.configuration).getBytes());
                    if (config == Variants.ANIMAL_FOOD)
                        Files.write(path, CommentGeneration.writeWithComments(this.gson, this.animalFoodConfiguration).getBytes());
                }
                catch (IllegalAccessException e)
                {
                    throw new IOException(e);
                }
            }
        }
    }


    public Configuration getConfiguration()
    {
        return this.configuration;
    }


    public AnimalFoodConfiguration getAnimalFoodConfiguration()
    {
        return this.animalFoodConfiguration;
    }


    public enum Variants
    {
        GENERAL("config.json"),
        ANIMAL_FOOD("animal_foods.json");

        Variants(String file)
        {
            this.file = file;
        }


        public String getFile()
        {
            return this.file;
        }


        private final String file;
    }


    private final Gson gson;

    private Configuration configuration;

    private AnimalFoodConfiguration animalFoodConfiguration;
}