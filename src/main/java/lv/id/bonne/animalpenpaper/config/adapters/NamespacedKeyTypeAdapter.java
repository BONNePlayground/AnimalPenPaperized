package lv.id.bonne.animalpenpaper.config.adapters;


import com.google.gson.*;
import org.bukkit.NamespacedKey;
import java.lang.reflect.Type;

public class NamespacedKeyTypeAdapter implements JsonSerializer<NamespacedKey>, JsonDeserializer<NamespacedKey>
{
    @Override
    public JsonElement serialize(NamespacedKey src, Type typeOfSrc, JsonSerializationContext context)
    {
        return new JsonPrimitive(src.toString());
    }

    @Override
    public NamespacedKey deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException
    {
        return NamespacedKey.fromString(json.getAsString());
    }
}