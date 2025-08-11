package lv.id.bonne.animalpenpaper.data;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import lv.id.bonne.animalpenpaper.AnimalPenPlugin;
import lv.id.bonne.animalpenpaper.config.adapters.EntitySnapshotTypeAdapter;


public class AnimalDataType implements PersistentDataType<String, AnimalData>
{

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
            return "{\"version\":1,\"cooldowns\":{}}";
        }
    }


    @Override
    @NotNull
    public AnimalData fromPrimitive(@NotNull String primitive, @NotNull PersistentDataAdapterContext context)
    {
        try
        {
            AnimalData data = GSON.fromJson(primitive, AnimalData.class);

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
            return new AnimalData(null, null, 0);
        }
    }


    /**
     * Object instance for data assigning/accessing
     */
    public static final AnimalDataType INSTANCE = new AnimalDataType();

    /**
     * Gson serializer.
     */
    private static final Gson GSON = new GsonBuilder().
        registerTypeAdapter(EntitySnapshot.class, new EntitySnapshotTypeAdapter()).
        create();
}