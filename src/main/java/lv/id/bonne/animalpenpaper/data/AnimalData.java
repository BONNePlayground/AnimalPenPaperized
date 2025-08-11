package lv.id.bonne.animalpenpaper.data;


import com.google.gson.annotations.JsonAdapter;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.EntityType;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import lv.id.bonne.animalpenpaper.config.adapters.EntitySnapshotTypeAdapter;


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
    public Map<Extra, Object> extra = new HashMap<>();

    /**
     * The value stores current entity snapshot
     */
    @JsonAdapter(EntitySnapshotTypeAdapter.class)
    public EntitySnapshot entitySnapshot = null;

    /**
     * Current version of data storage.
     */
    public int version = 1;


    public AnimalData(EntityType entityType, EntitySnapshot entitySnapshot, long entityCount)
    {
        this.entityType = entityType;
        this.entityCount = entityCount;
        this.entitySnapshot = entitySnapshot;
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


    public void setExtra(Extra key, Object value)
    {
        this.extra.put(key, value);
    }


    public <T> T removeExtra(Extra extra)
    {
        return (T) this.extra.remove(extra);
    }


    public <T> T getExtra(Extra key)
    {
        return (T) this.extra.get(key);
    }


    public boolean hasExtra(Extra extra)
    {
        return this.extra.containsKey(extra);
    }


    public boolean hasCooldown(Interaction key)
    {
        return this.cooldowns.containsKey(key) && this.cooldowns.get(key) > 0;
    }


    public enum Interaction
    {
        BRUSH, WATER_BUCKET, SHEARS, DYES, BUCKET, GLASS_BOTTLE, MAGMA_BLOCK, BOWL, FOOD
    }

    public enum Extra
    {
        SCUTE
    }
}