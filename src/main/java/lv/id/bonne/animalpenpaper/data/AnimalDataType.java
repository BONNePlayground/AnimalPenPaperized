package lv.id.bonne.animalpenpaper.data;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;

import lv.id.bonne.animalpenpaper.AnimalPenPlugin;


public class AnimalDataType implements PersistentDataType<String, AnimalData>
{

    public static final AnimalDataType INSTANCE = new AnimalDataType();

    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .create();


    @Override
    @NotNull
    public Class<String> getPrimitiveType()
    {
        return String.class;
    }


    @Override
    @NotNull
    public Class<AnimalData> getComplexType()
    {
        return AnimalData.class;
    }


    @Override
    @NotNull
    public String toPrimitive(@NotNull AnimalData complex, @NotNull PersistentDataAdapterContext context)
    {
        try
        {
            return GSON.toJson(complex);
        }
        catch (Exception e)
        {
            AnimalPenPlugin.getInstance().getLogger().warning("Failed to serialize AnimalData: " + e.getMessage());
            // Return a minimal valid JSON as fallback
            return "{\"version\":1,\"cooldowns\":{},\"extra\":{}}";
        }
    }


    @Override
    @NotNull
    public AnimalData fromPrimitive(@NotNull String primitive, @NotNull PersistentDataAdapterContext context)
    {
        try
        {
            AnimalData data = GSON.fromJson(primitive, AnimalData.class);

            if (data.cooldowns == null)
            {
                data.cooldowns = new HashMap<>();
            }
            if (data.extra == null)
            {
                data.extra = new HashMap<>();
            }

            // Handle version migration
            if (data.version == 0)
            {
                data.version = 1;
            }

            return data;
        }
        catch (JsonSyntaxException e)
        {

            AnimalPenPlugin.getInstance().getLogger().warning("Failed to deserialize AnimalData: " + e.getMessage());
            AnimalPenPlugin.getInstance().getLogger().warning("Raw JSON: " + primitive);

            // Return a default AnimalData instead of crashing
            AnimalData fallback = new AnimalData();
            fallback.entityType = null;
            fallback.entityCount = 0;
            return fallback;
        }
    }
}