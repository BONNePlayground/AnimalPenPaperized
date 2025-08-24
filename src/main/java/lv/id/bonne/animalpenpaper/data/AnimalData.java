package lv.id.bonne.animalpenpaper.data;


import com.google.gson.annotations.JsonAdapter;
import org.bukkit.Material;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.EntityType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lv.id.bonne.animalpenpaper.config.adapters.EntitySnapshotTypeAdapter;


/**
 * The data that is stored for each entity cage.
 */
public class AnimalData
{
    /**
     * Instantiates a new Animal data.
     *
     * @param entityType the entity type
     * @param entitySnapshot the entity snapshot
     * @param entityCount the entity count
     */
    public AnimalData(EntityType entityType, EntitySnapshot entitySnapshot, long entityCount)
    {
        this.entityType = entityType;
        this.entityCount = entityCount;
        this.entitySnapshot = entitySnapshot;
    }


    /**
     * Instantiates a new Animal data.
     */
    public AnimalData()
    {
    }


    /**
     * Sets cooldown.
     *
     * @param key the key
     * @param value the value
     */
    public void setCooldown(Material key, int value)
    {
        this.cooldowns.put(key, value);
    }


    /**
     * Gets cooldown.
     *
     * @param key the key
     * @return the cooldown
     */
    public int getCooldown(Material key)
    {
        return this.cooldowns.getOrDefault(key, 0);
    }


    /**
     * Has cooldown boolean.
     *
     * @param key the key
     * @return the boolean
     */
    public boolean hasCooldown(Material key)
    {
        return this.cooldowns.containsKey(key) && this.cooldowns.get(key) > 0;
    }


    /**
     * Gets cooldowns.
     *
     * @return the cooldowns
     */
    public Map<Material, Integer> getCooldowns()
    {
        return this.cooldowns;
    }


    /**
     * Gets scutes.
     *
     * @return the scutes
     */
    public int scutes()
    {
        return this.scuteCount;
    }


    /**
     * Sets scutes.
     *
     * @param scuteCount the scute count
     */
    public void setScutes(int scuteCount)
    {
        this.scuteCount = scuteCount;
    }


    /**
     * Gets entity type.
     *
     * @return the entity type
     */
    public EntityType entityType()
    {
        return this.entityType;
    }


    /**
     * Entity snapshot entity snapshot.
     *
     * @return the entity snapshot
     */
    public EntitySnapshot entitySnapshot()
    {
        return this.entitySnapshot;
    }


    /**
     * Sets entity snapshot.
     *
     * @param entitySnapshot the entity snapshot
     */
    public void setEntitySnapshot(EntitySnapshot entitySnapshot)
    {
        this.entitySnapshot = entitySnapshot;
    }


    /**
     * Gets entity count.
     *
     * @return the entity count
     */
    public long entityCount()
    {
        return this.entityCount;
    }


    /**
     * Sets entity count.
     *
     * @param entityCount the entity count
     */
    public void setEntityCount(long entityCount)
    {
        this.entityCount = entityCount;
    }


    /**
     * Add entity count.
     *
     * @param amount the amount
     */
    public void addEntityCount(long amount)
    {
        this.entityCount += amount;
    }


    /**
     * Reduce entity count.
     *
     * @param amount the amount
     */
    public void reduceEntityCount(long amount)
    {
        this.entityCount -= amount;
    }


    /**
     * Gets variants.
     *
     * @return the variants
     */
    public List<EntitySnapshot> getVariants()
    {
        return this.variants;
    }


    /**
     * Add variant.
     *
     * @param snapshot the snapshot
     */
    public void addVariant(EntitySnapshot snapshot)
    {
        this.variants.add(snapshot);
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


    /**
     * The map that stores cooldowns.
     */
    private final Map<Material, Integer> cooldowns = new HashMap<>();

    /**
     * The list of animal variants.
     */
    private final List<EntitySnapshot> variants = new ArrayList<>();

    /**
     * The value stores current entity snapshot
     */
    @JsonAdapter(EntitySnapshotTypeAdapter.class)
    private EntitySnapshot entitySnapshot = null;

    /**
     * Current version of data storage.
     */
    public int version = 1;

    /**
     * The entity type that is stored
     */
    private EntityType entityType;

    /**
     * The amount of entities that are stored.
     */
    private long entityCount;

    /**
     * The amount of scutes to be dropped.
     */
    private int scuteCount;
}