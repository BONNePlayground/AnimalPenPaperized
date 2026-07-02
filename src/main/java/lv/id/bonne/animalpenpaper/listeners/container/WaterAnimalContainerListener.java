package lv.id.bonne.animalpenpaper.listeners.container;


import lv.id.bonne.animalpenpaper.managers.AbstractContainerManager;
import lv.id.bonne.animalpenpaper.managers.AquariumManager;


/**
 * This listener manages water animal container interactions.
 */
public class WaterAnimalContainerListener extends AbstractContainerListener
{

// ---------------------------------------------------------------------
// Section: Abstract configuration
// ---------------------------------------------------------------------


    @Override
    protected AbstractContainerManager getManager()
    {
        return AquariumManager.INSTANCE;
    }


    @Override
    protected String getNotAnimalErrorKey()
    {
        return "error.not_water_animal";
    }


    @Override
    protected String getOwnershipErrorKey()
    {
        return "error.owned";
    }
}