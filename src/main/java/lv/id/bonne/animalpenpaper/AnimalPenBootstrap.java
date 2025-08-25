package lv.id.bonne.animalpenpaper;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;


public class AnimalPenBootstrap implements PluginBootstrap
{
    @Override
    public void bootstrap(BootstrapContext context) {

        context.getLifecycleManager().registerEventHandler(LifecycleEvents.DATAPACK_DISCOVERY.newHandler(event ->
            {
                try
                {
                    URI uri = Objects.requireNonNull(getClass().getResource("/animal_pen_datapack")).toURI();
                    event.registrar().discoverPack(uri, "provided");
                }
                catch (URISyntaxException | IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        ));

    }
}