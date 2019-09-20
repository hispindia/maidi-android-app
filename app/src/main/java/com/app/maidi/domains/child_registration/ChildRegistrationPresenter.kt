package com.app.maidi.domains.child_registration

import com.app.maidi.domains.base.BasePresenter
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController
import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController
import org.hisp.dhis.android.sdk.persistence.models.Enrollment
import org.hisp.dhis.android.sdk.persistence.models.Program
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityAttributeValue
import org.hisp.dhis.android.sdk.persistence.models.TrackedEntityInstance
import java.util.*
import javax.inject.Inject

class ChildRegistrationPresenter : BasePresenter<ChildRegistrationView>{

    @Inject
    constructor(){}

    fun getTrackedEntityAttributeValues(program: Program, trackedEntityInstance: TrackedEntityInstance) : List<TrackedEntityAttributeValue>{
        val trackedEntityAttributeValues = ArrayList<TrackedEntityAttributeValue>()
        val programTrackedEntityAttributes  = program.getProgramTrackedEntityAttributes()

        for (ptea in programTrackedEntityAttributes) {
            val value = TrackerController.getTrackedEntityAttributeValue(
                ptea.trackedEntityAttributeId, trackedEntityInstance.getLocalId()
            )
            if (value != null) {
                trackedEntityAttributeValues.add(value)
            } else {
                val trackedEntityAttribute = MetaDataController.getTrackedEntityAttribute(
                    ptea.trackedEntityAttributeId
                )
                if (trackedEntityAttribute.isGenerated) {
                    val trackedEntityAttributeGeneratedValue =
                        MetaDataController.getTrackedEntityAttributeGeneratedValue(
                            ptea.trackedEntityAttribute
                        )

                    if (trackedEntityAttributeGeneratedValue != null) {
                        val trackedEntityAttributeValue = TrackedEntityAttributeValue()
                        trackedEntityAttributeValue.trackedEntityAttributeId = ptea.trackedEntityAttribute.uid
                        trackedEntityAttributeValue.trackedEntityInstanceId = trackedEntityInstance.getUid()
                        trackedEntityAttributeValue.value = trackedEntityAttributeGeneratedValue.value
                        trackedEntityAttributeValues.add(trackedEntityAttributeValue)
                    }
                }
            }
        }

        return trackedEntityAttributeValues
    }

    fun saveTrackedEntityOffline(trackedEntityInstance: TrackedEntityInstance, enrollment: Enrollment, program: Program){
        if(trackedEntityInstance != null){
            if(trackedEntityInstance.localId < 0){
                trackedEntityInstance.isFromServer = false
                trackedEntityInstance.save()
            }

            if(enrollment.events != null){
                for(event in enrollment.events){
                    event.isFromServer = false
                    enrollment.isFromServer = false
                    trackedEntityInstance.isFromServer = false
                }
            }

            enrollment.localTrackedEntityInstanceId = trackedEntityInstance.localId
            enrollment.isFromServer = false
            trackedEntityInstance.isFromServer = false
            enrollment.save()
        }


        for (ptea in program.getProgramTrackedEntityAttributes()) {
            if (ptea.getTrackedEntityAttribute().isGenerated()) {
                val attributeValue = TrackerController
                    .getTrackedEntityAttributeValue(
                        ptea.getTrackedEntityAttributeId(),
                        trackedEntityInstance.getUid()
                    )

                if(attributeValue != null) {
                    var trackedEntityAttributeGeneratedValue =
                        MetaDataController.getTrackedEntityAttributeGeneratedValue(attributeValue.getValue())
                    if (trackedEntityAttributeGeneratedValue != null) {
                        trackedEntityAttributeGeneratedValue!!.delete()
                    } else {
                        trackedEntityAttributeGeneratedValue =
                            MetaDataController.getTrackedEntityAttributeGeneratedValue(ptea.getTrackedEntityAttributeId())
                        if (trackedEntityAttributeGeneratedValue != null) {
                            trackedEntityAttributeGeneratedValue!!.delete()
                        }
                    }
                }
            }
        }
    }
}