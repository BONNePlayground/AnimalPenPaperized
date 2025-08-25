package lv.id.bonne.animalpenpaper.data;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.bukkit.block.BlockFace;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import lv.id.bonne.animalpenpaper.AnimalPenPlugin;


public class BlockDataType implements PersistentDataType<String, BlockData>
{
    @Override
    @NotNull
    public Class<String> getPrimitiveType()
    {
        return String.class;
    }


    @Override
    @NotNull
    public Class<BlockData> getComplexType()
    {
        return BlockData.class;
    }


    @Override
    @NotNull
    public String toPrimitive(@NotNull BlockData complex, @NotNull PersistentDataAdapterContext context)
    {
        try
        {
            return GSON.toJson(complex);
        }
        catch (Exception e)
        {
            AnimalPenPlugin.getInstance().getLogger().warning("Failed to serialize BlockData: " + e.getMessage());
            // Return a minimal valid JSON as fallback
            return "{\"blockFace\":\"NORTH\",\"cooldowns\":[]}";
        }
    }


    @Override
    @NotNull
    public BlockData fromPrimitive(@NotNull String primitive, @NotNull PersistentDataAdapterContext context)
    {
        try
        {
            BlockData data = GSON.fromJson(primitive, BlockData.class);

            if (data.blockFace == null)
            {
                data.blockFace = BlockFace.NORTH;
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

            AnimalPenPlugin.getInstance().getLogger().warning("Failed to deserialize BlockData: " + e.getMessage());
            AnimalPenPlugin.getInstance().getLogger().warning("Raw JSON: " + primitive);

            // Return a default BlockData instead of crashing
            BlockData fallback = new BlockData();
            fallback.blockFace = BlockFace.NORTH;
            return fallback;
        }
    }

    public static final BlockDataType INSTANCE = new BlockDataType();

    private static final Gson GSON = new GsonBuilder().create();
}