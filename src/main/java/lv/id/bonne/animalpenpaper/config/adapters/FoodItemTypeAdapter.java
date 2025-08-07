package lv.id.bonne.animalpenpaper.config.adapters;


import com.google.gson.*;
import java.lang.reflect.Type;

import lv.id.bonne.animalpenpaper.config.AnimalFoodConfiguration;


public class FoodItemTypeAdapter implements JsonSerializer<AnimalFoodConfiguration.FoodItem>, JsonDeserializer<AnimalFoodConfiguration.FoodItem>
{
    @Override
    public JsonElement serialize(AnimalFoodConfiguration.FoodItem src, Type typeOfSrc, JsonSerializationContext context)
    {
        return new JsonPrimitive(src.getIdentifier());
    }

    @Override
    public AnimalFoodConfiguration.FoodItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException
    {
        return new AnimalFoodConfiguration.FoodItem(json.getAsString());
    }
}