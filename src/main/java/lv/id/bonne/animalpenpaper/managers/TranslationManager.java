package lv.id.bonne.animalpenpaper.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

import lv.id.bonne.animalpenpaper.AnimalPenPlugin;
import net.kyori.adventure.text.Component;


public class TranslationManager
{
    public TranslationManager()
    {
        this.loadMessages();
    }


    /**
     * Loads the messages.yml file
     */
    private void loadMessages()
    {
        // Create messages.yml if it doesn't exist
        File messagesFile = new File(AnimalPenPlugin.getInstance().getDataFolder(), "messages.yml");

        if (!messagesFile.exists())
        {
            AnimalPenPlugin.getInstance().saveResource("messages.yml", false);
        }

        this.messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        AnimalPenPlugin.getInstance().getLogger().info("Loaded translation fallbacks from messages.yml");
    }


    /**
     * Reloads the messages configuration
     */
    public void reload()
    {
        this.loadMessages();
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
     * Gets a translatable component with custom fallback (overrides messages.yml)
     *
     * @param key The translation key
     * @param customFallback Custom fallback text
     * @return Component with translation and custom fallback
     */
    public Component getTranslatable(String key, String customFallback)
    {
        return Component.translatable(key).fallback(customFallback);
    }


    /**
     * Gets a translatable component with custom fallback and arguments
     *
     * @param key The translation key
     * @param customFallback Custom fallback text
     * @param args Arguments for the translation
     * @return Component with translation, custom fallback and arguments
     */
    public Component getTranslatable(String key, String customFallback, Object... args)
    {
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

        return Component.translatable(key, componentArgs).fallback(customFallback);
    }


    /**
     * Checks if a fallback exists for the given key
     *
     * @param key The translation key
     * @return true if fallback exists, false otherwise
     */
    public boolean hasFallback(String key)
    {
        return this.messagesConfig.contains(key);
    }


    /**
     * Gets the raw fallback string for a key
     *
     * @param key The translation key
     * @return The fallback string or null if not found
     */
    public String getFallback(String key)
    {
        return this.messagesConfig.getString(key);
    }


    /**
     * The message configuration file.
     */
    private FileConfiguration messagesConfig;
}