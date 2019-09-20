package com.beesightsoft.caf.services.servicefacade;

import com.beesightsoft.caf.services.common.PurgeableInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kietnh on 5/27/2016.
 */
public abstract class DefaultServiceFacade implements ServiceFacade {
    private List<PurgeableInterface> services = new ArrayList<>();

    public DefaultServiceFacade() {
        this.registerServices(this.services);
    }

    @Override
    public void purge() {
        for (PurgeableInterface service : services) {
            service.purge();
        }
    }

    public abstract List<PurgeableInterface> registerServices(List<PurgeableInterface> services);
}
