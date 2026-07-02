//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpenpaper.events.block;


import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;


/**
 * This event is called after animals are breed in pen/aquarium
 */
public class AnimalBlockBreedEvent extends Event
{
    /**
     * @param player The player who places animal pen/aquarium
     * @param location The location of animal pen/aquarium
     * @param entityType The entity type
     * @param animalsInPen The amount of animals before
     * @param animalsAdded The amount of animals added
     */
    public AnimalBlockBreedEvent(Player player,
        Location location,
        EntityType entityType,
        long animalsInPen,
        long animalsAdded,
        String blockKey)
    {
        this.player = player;
        this.location = location;
        this.entityType = entityType;
        this.animalsInPen = animalsInPen;
        this.animalsAdded = animalsAdded;
        this.blockKey = blockKey;
    }


    @Override
    @NotNull
    public HandlerList getHandlers()
    {
        return HANDLER_LIST;
    }


    @NotNull
    public static HandlerList getHandlerList()
    {
        return HANDLER_LIST;
    }


    public Player player()
    {
        return this.player;
    }


    public Location location()
    {
        return this.location;
    }


    public String blockKey()
    {
        return this.blockKey;
    }


    public EntityType entityType()
    {
        return this.entityType;
    }


    public long animalsInPen()
    {
        return this.animalsInPen;
    }


    public long animalsAdded()
    {
        return this.animalsAdded;
    }


    private final Player player;

    private final Location location;

    private final EntityType entityType;

    private final String blockKey;

    private final long animalsInPen;

    private final long animalsAdded;

    private static final HandlerList HANDLER_LIST = new HandlerList();
}
