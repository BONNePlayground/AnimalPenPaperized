package lv.id.bonne.animalpenpaper.config.adapters;


import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import org.bukkit.craftbukkit.entity.CraftEntitySnapshot;
import java.lang.reflect.Type;
import java.util.Optional;

import lv.id.bonne.animalpenpaper.AnimalPenPlugin;
import net.minecraft.nbt.CompoundTag;


public class EntitySnapshotTypeAdapter implements JsonSerializer<CraftEntitySnapshot>, JsonDeserializer<CraftEntitySnapshot>
{
    @Override
    public JsonElement serialize(CraftEntitySnapshot src, Type typeOfSrc, JsonSerializationContext context)
    {
        CompoundTag data = src.getData();

        Optional<JsonElement> jsonElement = CompoundTag.CODEC.encodeStart(JsonOps.INSTANCE, data).result();

        return jsonElement.orElseGet(() -> {
            AnimalPenPlugin.getInstance().getLogger().warning("Failed to serialize animal data: " + data.toString());
            return JsonNull.INSTANCE;
        });
    }

    @Override
    public CraftEntitySnapshot deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException
    {
        return CraftEntitySnapshot.create(CompoundTag.CODEC.parse(JsonOps.INSTANCE, json).result().
            orElseGet(() -> {
                AnimalPenPlugin.getInstance().getLogger().warning("Failed to deserialize animal data: " + json.getAsString());
                return new CompoundTag();
            }));
    }
}