package lv.id.bonne.animalpenpaper.listeners.container;


import lv.id.bonne.animalpenpaper.managers.container.AbstractContainerManager;
import lv.id.bonne.animalpenpaper.managers.container.AviaryManager;


/**
 * This listener manages animal cage interactions.
 */
public class BirdCatcherListener extends AbstractContainerListener
{

// ---------------------------------------------------------------------
// Section: Abstract configuration
// ---------------------------------------------------------------------


    @Override
    protected AbstractContainerManager getManager()
    {
        return AviaryManager.INSTANCE;
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
}