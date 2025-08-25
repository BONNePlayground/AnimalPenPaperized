package lv.id.bonne.animalpenpaper.data;


import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.EntityType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;


public class BlockData
{
    /**
     * The entity that is generated as fake entity.
     */
    @Nullable
    public UUID entity;

    /**
     * The entity that displays count of animals
     */
    @Nullable
    public UUID countEntity;

    /**
     * The decoration entity for animal pen.
     */
    @Nullable
    public UUID decorationEntity;

    /**
     * The block face how animal pen was placed.
     */
    public BlockFace blockFace;

    /**
     * Current version of data storage.
     */
    public int version = 1;


    public BlockData()
    {
    }
}