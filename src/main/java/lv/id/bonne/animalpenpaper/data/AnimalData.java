package lv.id.bonne.animalpenpaper.data;


import org.bukkit.entity.EntityType;
import java.util.HashMap;
import java.util.Map;


public class AnimalData
{
    /**
     * The entity type that is stored
     */
    public EntityType entityType;

    /**
     * The amount of entities that are stored.
     */
    public long entityCount;

    /**
     * The map that stores cooldowns.
     */
    public Map<Interaction, Integer> cooldowns = new HashMap<>();

    /**
     * The map that stores extra data.
     */
    public Map<String, Integer> extra = new HashMap<>();

    /**
     * Current version of data storage.
     */
    public int version = 1;


    public AnimalData(EntityType entityType, long entityCount)
    {
        this.entityType = entityType;
        this.entityCount = entityCount;
    }


    public AnimalData()
    {
    }


    public void setCooldown(Interaction key, int value)
    {
        this.cooldowns.put(key, value);
    }


    public int getCooldown(Interaction key)
    {
        return this.cooldowns.getOrDefault(key, 0);
    }


    public void setExtra(String key, int value)
    {
        this.extra.put(key, value);
    }


    public int getExtra(String key)
    {
        return this.extra.getOrDefault(key, 0);
    }


    public boolean hasCooldown(Interaction key)
    {
        return this.cooldowns.containsKey(key) && this.cooldowns.get(key) > 0;
    }


    public enum Interaction
    {
        BRUSH, WATER_BUCKET, SHEARS, DYES, BUCKET, FOOD
    }
}