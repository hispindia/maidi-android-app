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

package org.hisp.dhis.android.sdk.fragments.trackedentityinstance;

import org.hisp.dhis.android.sdk.persistence.models.*;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.IndicatorRow;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.Row;

import java.util.List;
import java.util.Map;

class TrackedEntityInstanceDataEntryFragmentForm
{
    private Enrollment enrollment;
    private Program program;
    private TrackedEntityInstance trackedEntityInstance;
    private OrganisationUnit organisationUnit;

    private Map<String, TrackedEntityAttributeValue> trackedEntityAttributeValueMap;
    private Map<String, String> dataElementNames;
    private List<IndicatorRow> indicatorRows;
    private List<Row> dataEntryRows;
    private boolean outOfTrackedEntityAttributeGeneratedValues;

    public List<Row> getDataEntryRows() {
        return dataEntryRows;
    }

    public void setDataEntryRows(List<Row> dataEntryRows) {
        this.dataEntryRows = dataEntryRows;
    }

    public Enrollment getEnrollment() {
        return enrollment;
    }

    public Program getProgram() {
        return program;
    }

    public void setProgram(Program program) {
        this.program = program;
    }

    public OrganisationUnit getOrganisationUnit() {
        return organisationUnit;
    }

    public void setOrganisationUnit(OrganisationUnit organisationUnit) {
        this.organisationUnit = organisationUnit;
    }

    public void setEnrollment(Enrollment enrollment) {
        this.enrollment = enrollment;
    }

    public Map<String, String> getDataElementNames() {
        return dataElementNames;
    }

    public void setDataElementNames(Map<String, String> dataElementNames) {
        this.dataElementNames = dataElementNames;
    }

    public TrackedEntityInstance getTrackedEntityInstance() {
        return trackedEntityInstance;
    }

    public void setTrackedEntityInstance(TrackedEntityInstance trackedEntityInstance) {
        this.trackedEntityInstance = trackedEntityInstance;
    }

    public Map<String, TrackedEntityAttributeValue> getTrackedEntityAttributeValueMap() {
        return trackedEntityAttributeValueMap;
    }

    public void setTrackedEntityAttributeValueMap(Map<String, TrackedEntityAttributeValue> trackedEntityAttributeValueMap) {
        this.trackedEntityAttributeValueMap = trackedEntityAttributeValueMap;
    }

    public void setOutOfTrackedEntityAttributeGeneratedValues(boolean outOfTrackedEntityAttributeGeneratedValues) {
        this.outOfTrackedEntityAttributeGeneratedValues = outOfTrackedEntityAttributeGeneratedValues;
    }

    public boolean isOutOfTrackedEntityAttributeGeneratedValues() {
        return outOfTrackedEntityAttributeGeneratedValues;
    }
}
