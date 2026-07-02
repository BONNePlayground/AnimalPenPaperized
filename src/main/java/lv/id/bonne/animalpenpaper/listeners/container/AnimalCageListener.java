package lv.id.bonne.animalpenpaper.listeners.container;


import org.bukkit.entity.Entity;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Sheep;

import io.papermc.paper.potion.SuspiciousEffectEntry;
import lv.id.bonne.animalpenpaper.data.AnimalData;
import lv.id.bonne.animalpenpaper.managers.container.AbstractContainerManager;
import lv.id.bonne.animalpenpaper.managers.container.AnimalPenManager;
import lv.id.bonne.animalpenpaper.util.Utils;


/**
 * This listener manages animal cage interactions.
 */
public class AnimalCageListener extends AbstractContainerListener
{

// ---------------------------------------------------------------------
// Section: Abstract configuration
// ---------------------------------------------------------------------


    @Override
    protected AbstractContainerManager getManager()
    {
        return AnimalPenManager.INSTANCE;
    }


    @Override
    protected String getNotAnimalErrorKey()
    {
        return "error.not_animal";
    }


    @Override
    protected String getOwnershipErrorKey()
    {
        return "error.tame";
    }


// ---------------------------------------------------------------------
// Section: Lifecycle hooks
// ---------------------------------------------------------------------


    /**
     * Restores sheep shearing state / colour and mooshroom stew effects when a stored animal is
     * released back into the world.
     */
    @Override
    protected boolean onEntityReleased(Entity entity, AnimalData storedData)
    {
        boolean updateSnapshot = storedData.getAppliedFlag().isPresent() ||
            storedData.getAppliedMaterial().isPresent();

        if (entity instanceof Sheep sheep)
        {
            storedData.getAppliedFlag().ifPresent(shared -> {
                sheep.setSheared(shared);
                storedData.setAppliedFlag(null);
            });

            storedData.getAppliedMaterial().ifPresent(dye -> {
                sheep.setColor(Utils.getDyeColor(dye));
                storedData.setAppliedMaterial(null);
            });
        }
        else if (entity instanceof MushroomCow cow)
        {
            storedData.getAppliedMaterial().ifPresent(dye -> {
                SuspiciousEffectEntry suspiciousEffectEntry = Utils.FLOWER_EFFECTS.get(dye);

                if (suspiciousEffectEntry != null)
                {
                    cow.addEffectToNextStew(suspiciousEffectEntry, true);
                }

                storedData.setAppliedMaterial(null);
            });
        }

        return updateSnapshot;
    }


    /**
     * Carries scutes and any pending applied dye/shear state over to the withdrawn half.
     */
    @Override
    protected void onWithdrawSplit(AnimalData penData, AnimalData itemData)
    {
        itemData.setScutes(penData.scutes() / 2);
        penData.setScutes(penData.scutes() - itemData.scutes());

        penData.getAppliedMaterial().ifPresent(itemData::setAppliedMaterial);
        penData.getAppliedFlag().ifPresent(itemData::setAppliedFlag);
    }
}