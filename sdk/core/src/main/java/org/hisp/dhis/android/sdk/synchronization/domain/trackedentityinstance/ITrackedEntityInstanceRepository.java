package org.hisp.dhis.android.sdk.synchronization.domain.trackedentityinstance;


import org.hisp.dhis.android.sdk.persistence.models.ImportSummary;
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityInstance;

import java.util.List;

public interface ITrackedEntityInstanceRepository {
    void save (TrackedEntityInstance trackedEntityInstance);

    ImportSummary sync (TrackedEntityInstance trackedEntityInstance);

    List<ImportSummary> sync(List<TrackedEntityInstance> trackedEntityInstances);

    List<TrackedEntityInstance> getAllLocalTeis();

    TrackedEntityInstance getTrackedEntityInstance(String uid);
}


