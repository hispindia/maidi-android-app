/*
 *  Copyright (c) 2016, University of Oslo
 *  * All rights reserved.
 *  *
 *  * Redistribution and use in source and binary forms, with or without
 *  * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright notice, this
 *  * list of conditions and the following disclaimer.
 *  *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *  * this list of conditions and the following disclaimer in the documentation
 *  * and/or other materials provided with the distribution.
 *  * Neither the name of the HISP project nor the names of its contributors may
 *  * be used to endorse or promote products derived from this software without
 *  * specific prior written permission.
 *  *
 *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.hisp.dhis.android.sdk.fragments.enrollment;

import android.content.Context;
import org.hisp.dhis.android.sdk.controllers.GpsController;
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis.android.sdk.controllers.tracker.TrackerController;
import org.hisp.dhis.android.sdk.persistence.loaders.Query;
import org.hisp.dhis.android.sdk.persistence.models.*;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.DataEntryRowFactory;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.EnrollmentDatePickerRow;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.IncidentDatePickerRow;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.Row;
import org.hisp.dhis.android.sdk.utils.api.ValueType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class EnrollmentDataEntryFragmentQuery implements Query<EnrollmentDataEntryFragmentForm> {
    public static final String CLASS_TAG = EnrollmentDataEntryFragmentQuery.class.getSimpleName();

    private final String mOrgUnitId;
    private final String mProgramId;
    private final long mTrackedEntityInstanceId;
    private final String enrollmentDate;
    private String incidentDate;
    private TrackedEntityInstance currentTrackedEntityInstance;
    private Enrollment currentEnrollment;
    private EnrollmentDataEntryFragment mFragment;

    private String uniqueCaseID;
    private String childName;
    private String caregiverName;
    private String gender;
    private String dateOfBirth;
    private boolean isNeedUniqueCaseId;

    EnrollmentDataEntryFragmentQuery(String mOrgUnitId, String mProgramId,
            long mTrackedEntityInstanceId,
            String enrollmentDate, String incidentDate,
            boolean isNeedUniqueCaseId,
            String uniqueCaseID,
            String childName,
            String caregiverName,
            String gender,
            String dateOfBirth,
            EnrollmentDataEntryFragment fragment) {
        this.mOrgUnitId = mOrgUnitId;
        this.mProgramId = mProgramId;
        this.mTrackedEntityInstanceId = mTrackedEntityInstanceId;
        this.enrollmentDate = enrollmentDate;
        this.incidentDate = incidentDate;
        this.isNeedUniqueCaseId = isNeedUniqueCaseId;
        this.uniqueCaseID = uniqueCaseID;
        this.childName = childName;
        this.caregiverName = caregiverName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        mFragment = fragment;
    }

    @Override
    public EnrollmentDataEntryFragmentForm query(Context context) {
        EnrollmentDataEntryFragmentForm mForm = new EnrollmentDataEntryFragmentForm();
        final Program mProgram = MetaDataController.getProgram(mProgramId);
        final OrganisationUnit mOrgUnit = MetaDataController.getOrganisationUnit(mOrgUnitId);

        if (mProgram == null || mOrgUnit == null) {
            return mForm;
        }

        if (mTrackedEntityInstanceId < 0) {
            currentTrackedEntityInstance = new TrackedEntityInstance(mProgram, mOrgUnitId);
        } else {
            currentTrackedEntityInstance = TrackerController.getTrackedEntityInstance(
                    mTrackedEntityInstanceId);
        }
        if ("".equals(incidentDate)) {
            incidentDate = null;
        }
        currentEnrollment = new Enrollment(mOrgUnitId,
                currentTrackedEntityInstance.getTrackedEntityInstance(), mProgram, enrollmentDate,
                incidentDate);

        mForm.setProgram(mProgram);
        mForm.setOrganisationUnit(mOrgUnit);
        mForm.setDataElementNames(new HashMap<String, String>());
        mForm.setDataEntryRows(new ArrayList<Row>());
        mForm.setTrackedEntityInstance(currentTrackedEntityInstance);
        mForm.setTrackedEntityAttributeValueMap(new HashMap<String, TrackedEntityAttributeValue>());

        List<TrackedEntityAttributeValue> trackedEntityAttributeValues = new ArrayList<>();
        List<ProgramTrackedEntityAttribute> programTrackedEntityAttributes =
                mProgram.getProgramTrackedEntityAttributes();
        List<Row> dataEntryRows = new ArrayList<>();

        dataEntryRows.add(
                new EnrollmentDatePickerRow(currentEnrollment.getProgram().getEnrollmentDateLabel(),
                        currentEnrollment, true));

        if (currentEnrollment.getProgram().getDisplayIncidentDate()) {
            dataEntryRows.add(
                    new IncidentDatePickerRow(currentEnrollment.getProgram().getIncidentDateLabel(),
                            currentEnrollment, true));
        }

        for (ProgramTrackedEntityAttribute ptea : programTrackedEntityAttributes) {
            TrackedEntityAttributeValue value = TrackerController.getTrackedEntityAttributeValue(
                    ptea.getTrackedEntityAttributeId(), currentTrackedEntityInstance.getLocalId());
            if (value != null) {
                trackedEntityAttributeValues.add(value);
            } else {
                TrackedEntityAttribute trackedEntityAttribute =
                        MetaDataController.getTrackedEntityAttribute(
                                ptea.getTrackedEntityAttributeId());

                if(isNeedUniqueCaseId && trackedEntityAttribute.getDisplayName().contains("Case ID")){
                    TrackedEntityAttributeValue trackedEntityAttributeValue =
                            new TrackedEntityAttributeValue();
                    trackedEntityAttributeValue.setTrackedEntityAttributeId(
                            ptea.getTrackedEntityAttribute().getUid());
                    trackedEntityAttributeValue.setTrackedEntityInstanceId(
                            currentTrackedEntityInstance.getUid());
                    trackedEntityAttributeValue.setValue(uniqueCaseID);
                    trackedEntityAttributeValues.add(trackedEntityAttributeValue);
                }

                if(childName != null && trackedEntityAttribute.getDisplayName().contains("Name")){
                    TrackedEntityAttributeValue trackedEntityAttributeValue =
                            new TrackedEntityAttributeValue();
                    trackedEntityAttributeValue.setTrackedEntityAttributeId(
                            ptea.getTrackedEntityAttribute().getUid());
                    trackedEntityAttributeValue.setTrackedEntityInstanceId(
                            currentTrackedEntityInstance.getUid());
                    trackedEntityAttributeValue.setValue(childName);
                    trackedEntityAttributeValues.add(trackedEntityAttributeValue);
                }

                if(gender != null && trackedEntityAttribute.getDisplayName().contains("Gender")){
                    TrackedEntityAttributeValue trackedEntityAttributeValue =
                            new TrackedEntityAttributeValue();
                    trackedEntityAttributeValue.setTrackedEntityAttributeId(
                            ptea.getTrackedEntityAttribute().getUid());
                    trackedEntityAttributeValue.setTrackedEntityInstanceId(
                            currentTrackedEntityInstance.getUid());
                    trackedEntityAttributeValue.setValue(gender);
                    trackedEntityAttributeValues.add(trackedEntityAttributeValue);
                }

                if(dateOfBirth != null && trackedEntityAttribute.getDisplayName().contains("Date of Birth")){
                    TrackedEntityAttributeValue trackedEntityAttributeValue =
                            new TrackedEntityAttributeValue();
                    trackedEntityAttributeValue.setTrackedEntityAttributeId(
                            ptea.getTrackedEntityAttribute().getUid());
                    trackedEntityAttributeValue.setTrackedEntityInstanceId(
                            currentTrackedEntityInstance.getUid());
                    trackedEntityAttributeValue.setValue(dateOfBirth);
                    trackedEntityAttributeValues.add(trackedEntityAttributeValue);
                }

                if(caregiverName != null && trackedEntityAttribute.getDisplayName().contains("Caregiver name")){
                    TrackedEntityAttributeValue trackedEntityAttributeValue =
                            new TrackedEntityAttributeValue();
                    trackedEntityAttributeValue.setTrackedEntityAttributeId(
                            ptea.getTrackedEntityAttribute().getUid());
                    trackedEntityAttributeValue.setTrackedEntityInstanceId(
                            currentTrackedEntityInstance.getUid());
                    trackedEntityAttributeValue.setValue(caregiverName);
                    trackedEntityAttributeValues.add(trackedEntityAttributeValue);
                }

                if (trackedEntityAttribute.isGenerated()) {
                    TrackedEntityAttributeGeneratedValue trackedEntityAttributeGeneratedValue =
                            MetaDataController.getTrackedEntityAttributeGeneratedValue(
                                    ptea.getTrackedEntityAttribute());

                    if (trackedEntityAttributeGeneratedValue != null) {
                        TrackedEntityAttributeValue trackedEntityAttributeValue =
                                new TrackedEntityAttributeValue();
                        trackedEntityAttributeValue.setTrackedEntityAttributeId(
                                ptea.getTrackedEntityAttribute().getUid());
                        trackedEntityAttributeValue.setTrackedEntityInstanceId(
                                currentTrackedEntityInstance.getUid());
                        trackedEntityAttributeValue.setValue(
                                trackedEntityAttributeGeneratedValue.getValue());
                        trackedEntityAttributeValues.add(trackedEntityAttributeValue);
                    } else {
                        mForm.setOutOfTrackedEntityAttributeGeneratedValues(true);
                    }
                }
            }
        }
        currentEnrollment.setAttributes(trackedEntityAttributeValues);
        for (int i = 0; i < programTrackedEntityAttributes.size(); i++) {
            boolean editable = true;
            boolean shouldNeverBeEdited = false;
            ValueType valueType = programTrackedEntityAttributes.get(i).getTrackedEntityAttribute().getValueType();
            if (programTrackedEntityAttributes.get(i).getTrackedEntityAttribute().isGenerated()
                || (isNeedUniqueCaseId
                        && programTrackedEntityAttributes.get(i)
                                .getTrackedEntityAttribute().getDisplayName().contains("Case ID")
                    )
                ) {
                    editable = false;
                    shouldNeverBeEdited = true;
                    valueType = ValueType.TEXT;
                    mFragment.getListViewAdapter().disableIndex(programTrackedEntityAttributes.get(
                            i).getTrackedEntityAttribute().getUid());
            }
            if (ValueType.COORDINATE.equals(programTrackedEntityAttributes.get(
                    i).getTrackedEntityAttribute().getValueType())) {
                GpsController.activateGps(context);
            }
            boolean isRadioButton = mProgram.getDataEntryMethod();
            if(!isRadioButton){
                isRadioButton = programTrackedEntityAttributes.get(
                        i).isRenderOptionsAsRadio();
            }
            Row row = DataEntryRowFactory.createDataEntryView(context, mOrgUnitId,
                    programTrackedEntityAttributes.get(i).getMandatory(),
                    programTrackedEntityAttributes.get(i).getAllowFutureDate(),
                    programTrackedEntityAttributes.get(
                            i).getTrackedEntityAttribute().getOptionSet(),
                    programTrackedEntityAttributes.get(i).getTrackedEntityAttribute().getName(),
                    getTrackedEntityDataValue(programTrackedEntityAttributes.get(i).
                            getTrackedEntityAttribute().getUid(), trackedEntityAttributeValues),
                    valueType,
                    editable, shouldNeverBeEdited, isRadioButton);
            dataEntryRows.add(row);
        }
        for (TrackedEntityAttributeValue trackedEntityAttributeValue :
                trackedEntityAttributeValues) {
            mForm.getTrackedEntityAttributeValueMap().put(
                    trackedEntityAttributeValue.getTrackedEntityAttributeId(),
                    trackedEntityAttributeValue);
        }
        mForm.setDataEntryRows(dataEntryRows);
        mForm.setEnrollment(currentEnrollment);
        return mForm;
    }

    public TrackedEntityAttributeValue getTrackedEntityDataValue(String trackedEntityAttribute,
                                                                 List<TrackedEntityAttributeValue> trackedEntityAttributeValues) {
        for (TrackedEntityAttributeValue trackedEntityAttributeValue :
                trackedEntityAttributeValues) {
            if (trackedEntityAttributeValue.getTrackedEntityAttributeId().equals(
                    trackedEntityAttribute)) {
                return trackedEntityAttributeValue;
            }
        }

        //the datavalue didnt exist for some reason. Create a new one.
        TrackedEntityAttributeValue trackedEntityAttributeValue = new TrackedEntityAttributeValue();
        trackedEntityAttributeValue.setTrackedEntityAttributeId(trackedEntityAttribute);
        trackedEntityAttributeValue.setTrackedEntityInstanceId(
                currentTrackedEntityInstance.getTrackedEntityInstance());
        trackedEntityAttributeValue.setValue("");

        TrackedEntityAttribute attribute = MetaDataController.getTrackedEntityAttribute(trackedEntityAttribute);
        if(attribute.getDisplayName().contains("Residential")){
            trackedEntityAttributeValue.setIsOrganisationValue("true");
        }

        trackedEntityAttributeValues.add(trackedEntityAttributeValue);
        return trackedEntityAttributeValue;
    }
}
