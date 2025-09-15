package lv.id.bonne.animalpenpaper.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import lv.id.bonne.animalpenpaper.AnimalPenPlugin;
import net.kyori.adventure.text.Component;


public class TranslationManager
{
    public TranslationManager()
    {
        this.loadMessages(false);
        this.updateConfig();
    }


    /**
     * Loads the messages.yml file
     */
    private void loadMessages(boolean reset)
    {
        // Create messages.yml if it doesn't exist
        File messagesFile = new File(AnimalPenPlugin.getInstance().getDataFolder(), "messages.yml");

        if (!messagesFile.exists() || reset)
        {
            AnimalPenPlugin.getInstance().saveResource("messages.yml", reset);
        }

        this.messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        AnimalPenPlugin.getInstance().getLogger().info("Loaded translation fallbacks from messages.yml");
    }


    /**
     * Reloads the messages configuration
     */
    public void reload()
    {
        this.loadMessages(false);
    }


    /**
     * Resets the messages configuration
     */
    public void reset()
    {
        this.loadMessages(true);
    }


    /**
     * Gets a translatable component with fallback
     *
     * @param key The translation key
     * @return Component with translation and fallback
     */
    public Component getTranslatable(String key)
    {
        String fallback = this.messagesConfig.getString(key);

        if (fallback == null)
        {
            AnimalPenPlugin.getInstance().getLogger().warning("No fallback found for translation key: " + key);
            fallback = key;
        }

        return Component.translatable(key).fallback(fallback);
    }


    /**
     * Gets a translatable component with fallback and arguments
     *
     * @param key The translation key
     * @param args Arguments for the translation
     * @return Component with translation, fallback and arguments
     */
    public Component getTranslatable(String key, Object... args)
    {
        String fallback = this.messagesConfig.getString(key);
        if (fallback == null)
        {
            AnimalPenPlugin.getInstance().getLogger().warning("No fallback found for translation key: " + key);
            fallback = key;
        }

        Component[] componentArgs = new Component[args.length];

        for (int i = 0; i < args.length; i++)
        {
            if (args[i] instanceof Component)
            {
                componentArgs[i] = (Component) args[i];
            }
            else
            {
                componentArgs[i] = Component.text(String.valueOf(args[i]));
            }
        }

        return Component.translatable(key, componentArgs).fallback(fallback);
    }


    /**
     * Alternative method with more control over the merging process
     */
    public void updateConfig()
    {
        try
        {
            File configFile = new File(AnimalPenPlugin.getInstance().getDataFolder(), "messages.yml");

            // Load current config
            FileConfiguration currentConfig = YamlConfiguration.loadConfiguration(configFile);

            // Load default config from jar
            try (InputStream jarConfigStream = AnimalPenPlugin.getInstance().getResource("messages.yml"))
            {

                if (jarConfigStream == null)
                {
                    AnimalPenPlugin.getInstance().getLogger().warning("Default config not found in jar!");
                    return;
                }

                FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(jarConfigStream, StandardCharsets.UTF_8)
                );

                boolean updated = false;

                // Add missing keys with their default values
                for (String key : defaultConfig.getKeys(true))
                {
                    if (!currentConfig.contains(key))
                    {
                        Object defaultValue = defaultConfig.get(key);
                        currentConfig.set(key, defaultValue);
                        updated = true;

                        AnimalPenPlugin.getInstance().getLogger().
                            info("Added missing message key '" + key + "' with default value");
                    }
                }

                // Save if any updates were made
                if (updated)
                {
                    currentConfig.save(configFile);
                    this.messagesConfig = currentConfig; // Update the cached config
                    AnimalPenPlugin.getInstance().getLogger().info("Messages configuration updated successfully");
                }
                else
                {
                    AnimalPenPlugin.getInstance().getLogger().info("Messages configuration is up to date");
                }
            }
        }
        catch (Exception e)
        {
            AnimalPenPlugin.getInstance().getLogger().severe("Failed to update messages configuration: " + e.getMessage());
            AnimalPenPlugin.getInstance().getLogger().throwing(this.getClass().getName(), "updateConfig", e);
        }
    }


    /**
     * The message configuration file.
     */
    private FileConfiguration messagesConfig;
}