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

package org.hisp.dhis.android.sdk.controllers.tracker;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.queriable.StringQuery;
import org.hisp.dhis.android.sdk.R;
import org.hisp.dhis.android.sdk.controllers.LoadingController;
import org.hisp.dhis.android.sdk.controllers.ResourceController;
import org.hisp.dhis.android.sdk.controllers.SyncStrategy;
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis.android.sdk.events.LoadingMessageEvent;
import org.hisp.dhis.android.sdk.events.UiEvent;
import org.hisp.dhis.android.sdk.network.APIException;
import org.hisp.dhis.android.sdk.network.DhisApi;
import org.hisp.dhis.android.sdk.persistence.Dhis2Application;
import org.hisp.dhis.android.sdk.persistence.models.*;
import org.hisp.dhis.android.sdk.persistence.preferences.DateTimeManager;
import org.hisp.dhis.android.sdk.persistence.preferences.ResourceType;
import org.hisp.dhis.android.sdk.utils.UiUtils;
import org.hisp.dhis.android.sdk.utils.api.ProgramType;
import org.hisp.dhis.android.sdk.utils.support.DateUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Simen Skogly Russnes on 23.02.15.
 *         Handles management of data values
 */
public final class TrackerController extends ResourceController {

    private static final String CLASS_TAG = "DataValueController";

    private TrackerController() {
    }

    /**
     * Returns false if some data value flags that have been enabled have not been downloaded.
     *
     * @param context
     * @return
     */
    public static boolean isDataLoaded(Context context) {
        Log.d(CLASS_TAG, "isdatavaluesloaded..");
        if (context == null) {
            return false;
        }
        if (LoadingController.isLoadFlagEnabled(context, ResourceType.EVENTS)) {
            if (DateTimeManager.getInstance().getLastUpdated(ResourceType.EVENTS) == null) {
                return false;
            }
        }
        Log.d(CLASS_TAG, "data values are loaded.");
        return true;
    }

    public static void forceUpdateTrackedEntityInstanceToServer(Context context, DhisApi dhisApi, String trackedEntityInstanceId) throws APIException {
        if (dhisApi == null) {
            return;
        }
        SystemInfo serverSystemInfo = null;
        try {
            serverSystemInfo = dhisApi.getSystemInfo().execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        DateTime serverDateTime = serverSystemInfo.getServerDate();

        TrackerDataLoader.getTrackedEntityInstanceDataFromServer(
                dhisApi, trackedEntityInstanceId, true, true,
                serverDateTime);
    }

    public static TrackedEntityInstance getTrackedEntityInstanceByUid(String trackedEntityInstanceUid) {
        return new Select().from(TrackedEntityInstance.class).where(Condition.column
                (TrackedEntityInstance$Table.TRACKEDENTITYINSTANCE).is(trackedEntityInstanceUid)).querySingle();
    }

    public static List<Relationship> getRelationships(String trackedEntityInstance) {
        return new Select().from(Relationship.class).where(Condition.column
                (Relationship$Table.TRACKEDENTITYINSTANCEA).is(trackedEntityInstance)).
                or(Condition.column(Relationship$Table.TRACKEDENTITYINSTANCEB).is
                        (trackedEntityInstance)).queryList();
    }

    public static List<Enrollment> getEnrollments(String program, String organisationUnit) {
        return new Select().from(Enrollment.class).where(Condition.column(Enrollment$Table.PROGRAM).
                is(program)).and(Condition.column(Enrollment$Table.ORGUNIT).is(organisationUnit)).
                orderBy(false, Enrollment$Table.ENROLLMENTDATE).
                queryList();
    }

    public static List<Enrollment> getEnrollmentsForProgram(String program) {
        return new Select().from(Enrollment.class).where(Condition.column(Enrollment$Table.PROGRAM).
                is(program)).orderBy(false, Enrollment$Table.ENROLLMENTDATE).
                queryList();
    }

    public static List<Enrollment> getEnrollments(TrackedEntityInstance trackedEntityInstance) {
        return new Select().from(Enrollment.class).where(Condition.column(Enrollment$Table.LOCALTRACKEDENTITYINSTANCEID).
                is(trackedEntityInstance.getLocalId())).queryList();
    }

    public static List<Enrollment> getEnrollments(TrackedEntityInstance trackedEntityInstance,
            String programUId, String orgUnit) {
        return new Select().from(Enrollment.class).where(Condition.column(Enrollment$Table.LOCALTRACKEDENTITYINSTANCEID).
                is(trackedEntityInstance.getLocalId()))
                .and(Condition.column(Enrollment$Table.STATUS).is(Enrollment.COMPLETED))
                .and(Condition.column(Enrollment$Table.PROGRAM).is(programUId))
                .and(Condition.column(Enrollment$Table.ORGUNIT).is(orgUnit)).queryList();
    }

    /**
     * Returns a list of enrollments for a given program and tracked entity instance
     *
     * @param program
     * @param trackedEntityInstance
     * @return
     */
    public static List<Enrollment> getEnrollments(String program, TrackedEntityInstance trackedEntityInstance) {
        List<Enrollment> enrollments = new Select().from(Enrollment.class).
                where(Condition.column(Enrollment$Table.PROGRAM).is(program)).
                and(Condition.column(Enrollment$Table.LOCALTRACKEDENTITYINSTANCEID).
                        is(trackedEntityInstance.getLocalId())).queryList();
        return enrollments;
    }

    public static Enrollment getLastEnrollment(String program,
            TrackedEntityInstance trackedEntityInstance) {
        Enrollment enrollments = new Select().from(Enrollment.class).
                where(Condition.column(Enrollment$Table.PROGRAM).is(program)).
                and(Condition.column(Enrollment$Table.LOCALTRACKEDENTITYINSTANCEID).
                        is(trackedEntityInstance.getLocalId()))
                .orderBy(false, Enrollment$Table.LOCALID).querySingle();
        return enrollments;
    }

    public static Enrollment getCancelledEnrollment(String enrollment) {
        return new Select().from(Enrollment.class).where(Condition.column
                (Enrollment$Table.ENROLLMENT).is(enrollment))
                .and(Condition.column(Enrollment$Table.STATUS).is(Enrollment.CANCELLED)).querySingle();
    }


    public static Enrollment getNotCancelledEnrollment(String enrollment) {
        return new Select().from(Enrollment.class).where(Condition.column
                (Enrollment$Table.ENROLLMENT).is(enrollment))
                .and(Condition.column(Enrollment$Table.STATUS).isNot(Enrollment.CANCELLED)).querySingle();
    }

    public static Enrollment getEnrollment(String enrollment) {
        return new Select().from(Enrollment.class).where(Condition.column
                (Enrollment$Table.ENROLLMENT).is(enrollment)).querySingle();
    }

    public static Enrollment getEnrollment(long localEnrollmentId) {
        return new Select().from(Enrollment.class).where(Condition.column(Enrollment$Table.LOCALID).
                is(localEnrollmentId)).querySingle();
    }

    public static Enrollment getEnrollment(String programId, TrackedEntityInstance trackedEntityInstance) {
        return new Select().from(Enrollment.class)
                .where(Condition.column(Enrollment$Table.TRACKEDENTITYINSTANCE).is(trackedEntityInstance.getTrackedEntityInstance()))
                .and(Condition.column(Enrollment$Table.PROGRAM).is(programId)).querySingle();
    }

    /**
     * Returns a list of Events that have dueDate between the given dates, and corresponds to
     * program and orgunit.
     * (The given dates are considered inclusive in the defined period.)
     *
     * @param programId
     * @param orgUnitId
     * @param startDate
     * @param endDate
     * @return
     */
    public static List<Event> getScheduledEventsWithActiveEnrollments(String programId, String orgUnitId,
                                                                      String startDate, String endDate) {

        List<Enrollment> activeEnrollments = new Select().from(Enrollment.class).where(Condition.column
                (Enrollment$Table.PROGRAM).is(programId)).and(Condition.column(Enrollment$Table.STATUS).is(Enrollment.ACTIVE)).queryTableList();
        // Modify the endDate to be +1 day, so it includes events from the selected endDate as well:
        DateTime d = new DateTime(endDate);
        d = d.plusDays(1);
        endDate = d.toString("YYYY-MM-dd");
        String activeEnrollmentsSqlSafeString = getSqlSafeStringFromListOfEnrollments(activeEnrollments);

        // scheduled / due events should not show overdue events
        // that is events where due date is before today
        Date today = new Date();
        if (DateUtils.getDefaultDate(startDate).before(today)) {
            startDate = DateUtils.getMediumDateString(today);
        }

        String rawSqlQuery = new Select().from(Event.class).where(Condition.column(Event$Table.PROGRAMID)
                .is(programId))
                .and(Condition.column(Event$Table.ORGANISATIONUNITID)
                        .is(orgUnitId))
                .and(Condition.column(Event$Table.STATUS).isNot(Event.STATUS_COMPLETED))
                .and(Condition.column(Event$Table.EVENTDATE).isNull())
                .and(Condition.column(Event$Table.DUEDATE).isNotNull())
                .and(Condition.column(Event$Table.DUEDATE).between(startDate).and(endDate))
                .and(Condition.column(Event$Table.ENROLLMENT)).toString() + " IN " + activeEnrollmentsSqlSafeString
                + " ORDER BY " + Event$Table.DUEDATE;

        return new StringQuery<Event>(Event.class, rawSqlQuery).queryList();
    }
    /**
     * Returns a list of events for a given org unit and from server
     */
    public static List<Event> getAllConflictingAndNotConflictingEvents(String organisationUnitId, String programId,
            boolean isFromServer) {
        List<Event> events = new Select().from(Event.class)
                .join(FailedItem.class, Join.JoinType.LEFT)
                .on(Condition.column(FailedItem$Table.ITEMID).eq(Event$Table.LOCALID)).where(Condition.column
                (Event$Table.ORGANISATIONUNITID).is(organisationUnitId)).
                and(Condition.column(Event$Table.PROGRAMID).is(programId))
                .and(Condition.column(Event$Table.FROMSERVER).is(isFromServer))
                .or(Condition.column(FailedItem$Table.ITEMTYPE).is("Event"))
                .orderBy(false, Event$Table.LASTUPDATED).queryList();
        return events;
    }

    /**
     * Loads datavalues from the server and stores it in local persistence.
     */
    public static void syncRemotelyDeletedData(Context context, DhisApi dhisApi)
            throws APIException {
        UiUtils.postProgressMessage(context.getString(R.string.synchronize_deleted_data), LoadingMessageEvent.EventType.REMOVE_DATA);
        TrackerDataLoader.deleteRemotelyDeletedData(context, dhisApi);
        Dhis2Application.getEventBus().post(new UiEvent(UiEvent.UiEventType.SYNCING_END));
        UiUtils.postProgressMessage("",LoadingMessageEvent.EventType.FINISH);

    }

    /**
     * Returns a list of events for the given server-assigned UID. Note that if possible,
     * getEventsByEnrollment(long) should always be used if possible, as the UID may change if the
     * enrollment is created locally on the device, and then synced with the server.
     *
     * @param enrollment
     * @return
     */
    public static List<Event> getEventsByEnrollment(String enrollment) {
        return new Select().from(Event.class).where(Condition.column(Event$Table.ENROLLMENT).is(enrollment)).queryList();
    }

    /**
     * returns a list of events for a given localEnrollmentId
     *
     * @param localEnrollmentId
     * @return
     */
    public static List<Event> getEventsByEnrollment(long localEnrollmentId) {
        return new Select().from(Event.class).where(
                Condition.column(Event$Table.LOCALENROLLMENTID).is(localEnrollmentId)).and(
                Condition.column(Event$Table.STATUS).isNot(Event.STATUS_DELETED)).queryList();
    }

    /**
     * Returns a list of events for a given org unit and program
     *
     * @param organisationUnitId
     * @param programId
     * @return
     */
    public static List<Event> getNotDeletedEvents(String organisationUnitId, String programId) {
        List<Event> events = new Select().from(Event.class).where(Condition.column
                (Event$Table.ORGANISATIONUNITID).is(organisationUnitId)).
                and(Condition.column(Event$Table.PROGRAMID).is(programId)).and(
                Condition.column(Event$Table.STATUS).isNot(Event.STATUS_DELETED)).orderBy(false,
                Event$Table.LASTUPDATED).queryList();
        return events;
    }

    /**
     * Returns a list of events for a given org unit and from server
     */
    public static List<Event> getDeletedEvents() {
        List<Event> events = new Select().from(Event.class)
                .where(Condition.column(Event$Table.STATUS).is(Event.STATUS_DELETED))
                .orderBy(false, Event$Table.LASTUPDATED).queryList();
        return events;
    }

    /**
     * Returns a list of events for a given org unit and program and event has enrollment
     *
     * @param organisationUnitId
     * @param programId
     * @return
     */
    public static List<Event> getEventsThatHasEnrollments(String organisationUnitId, String programId) {
        List<Event> events = new Select().from(Event.class).where(Condition.column
                (Event$Table.ORGANISATIONUNITID).is(organisationUnitId))
                .and(Condition.column(Event$Table.PROGRAMID).is(programId))
                .and(Condition.column(Event$Table.ENROLLMENT).isNotNull())
                .orderBy(false, Event$Table.LASTUPDATED).queryList();
        return events;
    }

    public static List<Event> getEventEntries(String organisationUnitId, String programId) {
        List<Event> events = new Select().from(Event.class).where(Condition.column
                (Event$Table.ORGANISATIONUNITID).is(organisationUnitId))
                .and(Condition.column(Event$Table.PROGRAMID).is(programId))
                .and(Condition.column(Event$Table.STATUS).isNot(Event.STATUS_DELETED))
                .orderBy(false, Event$Table.LASTUPDATED).queryList();
        return events;
    }

    /**
     * Returns an Event based on the given localId
     *
     * @param localId
     * @return
     */
    public static Event getEvent(long localId) {
        return new Select().from(Event.class).where(Condition.column(Event$Table.LOCALID).is(localId)).querySingle();
    }

    /**
     * Returns an Event for a given enrollment and program stage
     *
     * @param localEnrollment
     * @param programStage
     * @return
     */
    public static Event getEvent(long localEnrollment, String programStage) {
        return new Select().from(Event.class).where(Condition.column
                        (Event$Table.LOCALENROLLMENTID).is(localEnrollment),
                Condition.column(Event$Table.PROGRAMSTAGEID).is(programStage)).querySingle();
    }

    /**
     * Returns an event based on UID generated on server. Note that this reference may change if
     * an event is created on the device, and then synced with the server. If possible, always use
     * getEvent(localId) which is safer.
     *
     * @param event
     * @return
     */
    public static Event getEventByUid(String event) {
        return new Select().from(Event.class).where(Condition.column(Event$Table.EVENT).is(event)).querySingle();
    }

    public static DataValue getDataValue(long eventId, String dataElement) {
        return new Select().from(DataValue.class).where(Condition.column(DataValue$Table.
                LOCALEVENTID).is(eventId), Condition.column(DataValue$Table.DATAELEMENT).is(dataElement)).querySingle();
    }

    /**
     * Returns a tracked entity instance based on the given id
     *
     * @param trackedEntityInstance
     * @return
     */
    public static TrackedEntityInstance getTrackedEntityInstance(String trackedEntityInstance) {
        return new Select().from(TrackedEntityInstance.class).where(Condition.column(TrackedEntityInstance$Table.TRACKEDENTITYINSTANCE).is(trackedEntityInstance)).querySingle();
    }

    public static TrackedEntityInstance getTrackedEntityInstance(long localId) {
        return new Select().from(TrackedEntityInstance.class).where
                (Condition.column(TrackedEntityInstance$Table.LOCALID).is(localId)).querySingle();
    }

    public static List<TrackedEntityInstance> getTrackedEntityInstances(String organisationUnitUId) {
        return new Select().from(TrackedEntityInstance.class).where
                (Condition.column(TrackedEntityInstance$Table.ORGUNIT).is(organisationUnitUId)).queryList();
    }

    public static List<TrackedEntityInstance> getTrackedEntityInstances() {
        return new Select().from(TrackedEntityInstance.class).queryList();
    }
    /*
   * Returns a list of tracked entity attribute values for an instance in a selected program
   * @param trackedEntityInstance
   * @param program
   * @return
   */
    public static List<TrackedEntityAttributeValue> getProgramTrackedEntityAttributeValues(Program program, TrackedEntityInstance trackedEntityInstance) {
        List<TrackedEntityAttributeValue> programTrackedEntityAttributeValues = new ArrayList<>();
        List<ProgramTrackedEntityAttribute> programTrackedEntityAttributes = MetaDataController.getProgramTrackedEntityAttributes(program.getUid());

        for (ProgramTrackedEntityAttribute ptea : programTrackedEntityAttributes) {
            TrackedEntityAttributeValue v = TrackerController.getTrackedEntityAttributeValue
                    (ptea.getTrackedEntityAttributeId(), trackedEntityInstance.getLocalId());
            if (v != null && v.getValue() != null && !v.getValue().isEmpty()) {
                programTrackedEntityAttributeValues.add(v);
            }
        }
        return programTrackedEntityAttributeValues;
    }

    /**
     * Returns a tracked entity attribute value for a given trackedentityattribute and trackedEntityInstance
     *
     * @param trackedEntityAttribute
     * @param trackedEntityInstance
     * @return
     */
    public static TrackedEntityAttributeValue getTrackedEntityAttributeValue(String trackedEntityAttribute, String trackedEntityInstance) {
        return new Select().from(TrackedEntityAttributeValue.class).where(
                Condition.column(TrackedEntityAttributeValue$Table.
                        TRACKEDENTITYATTRIBUTEID).is(trackedEntityAttribute),
                Condition.column(TrackedEntityAttributeValue$Table.
                        TRACKEDENTITYINSTANCEID).is(trackedEntityInstance)).querySingle();
    }

    /**
     * Returns a list of all trackedEntityAttributeValues for a given TEI
     *
     * @param trackedEntityInstance
     * @return
     */
    public static List<TrackedEntityAttributeValue> getTrackedEntityAttributeValues
    (String trackedEntityInstance) {
        return new Select().from(TrackedEntityAttributeValue.class).where(Condition.column
                (TrackedEntityAttributeValue$Table.TRACKEDENTITYINSTANCEID).is(trackedEntityInstance)).queryList();
    }

    /**
     * Returns a tracked entity attribute value for a given trackedentityattribute and trackedEntityInstance
     *
     * @param trackedEntityAttribute
     * @param trackedEntityInstance
     * @return
     */
    public static TrackedEntityAttributeValue getTrackedEntityAttributeValue(String trackedEntityAttribute, long trackedEntityInstance) {
        List<TrackedEntityAttributeValue> trackedEntityAttributeValue = new Select().from(TrackedEntityAttributeValue.class)
                .where(Condition.column(TrackedEntityAttributeValue$Table.TRACKEDENTITYATTRIBUTEID).eq(trackedEntityAttribute))
                .and(Condition.column(TrackedEntityAttributeValue$Table.LOCALTRACKEDENTITYINSTANCEID).eq(trackedEntityInstance))
                .queryList();
        if (trackedEntityAttributeValue != null && trackedEntityAttributeValue.size() > 0) {
            return trackedEntityAttributeValue.get(0);
        } else return null;
    }

    /**
     * Returns a list of all trackedEntityAttributeValues for a given TEI
     *
     * @param trackedEntityInstance
     * @return
     */
    public static List<TrackedEntityAttributeValue> getTrackedEntityAttributeValues
    (long trackedEntityInstance) {
        return new Select().from(TrackedEntityAttributeValue.class).where(Condition.column
                (TrackedEntityAttributeValue$Table.LOCALTRACKEDENTITYINSTANCEID).is(trackedEntityInstance)).orderBy(TrackedEntityAttributeValue$Table.TRACKEDENTITYATTRIBUTEID).queryList();
    }

    /**
     * Returns a list of all visible trackedEntityAttributeValues for a given TEI
     *
     * @param trackedEntityInstance
     * @return
     */
    public static List<TrackedEntityAttributeValue> getVisibleTrackedEntityAttributeValues
    (long trackedEntityInstance) {
        return new Select().from(TrackedEntityAttributeValue.class)
                .join(TrackedEntityAttribute.class, Join.JoinType.LEFT)
                .on(Condition.column(TrackedEntityAttribute$Table.ID).eq(TrackedEntityAttributeValue$Table.TRACKEDENTITYATTRIBUTEID))
                .where(Condition.column
                        (TrackedEntityAttributeValue$Table.LOCALTRACKEDENTITYINSTANCEID).is(trackedEntityInstance))
                .and(Condition.column
                        (TrackedEntityAttribute$Table.DISPLAYINLISTNOPROGRAM).is(true))
                .orderBy(true, TrackedEntityAttribute$Table.SORTORDERINLISTNOPROGRAM).queryList();
    }

    /**
     * Returns a list of failed items from the database, or null if there are none.
     * Failed items are items that have failed to upload and sync with the server for some reason
     *
     * @return
     */
    public static List<FailedItem> getFailedItems() {
        List<FailedItem> failedItems = new Select().from(FailedItem.class).queryList();
        if (failedItems == null || failedItems.size() <= 0) return null;
        else return failedItems;
    }

    public static List<FailedItem> getFailedItems(String type) {
        return new Select().from(FailedItem.class).where(Condition.column(FailedItem$Table.ITEMTYPE).is(type)).queryList();
    }

    public static FailedItem getFailedItem(String type, long id) {
        return new Select().from(FailedItem.class).where(Condition.column(FailedItem$Table.ITEMTYPE).is(type), Condition.column(FailedItem$Table.ITEMID).is(id)).querySingle();
    }

    /**
     * Clear flags for loaded data values, deleting the status info for when data values were
     * last updated
     * Sets all flags for all loaded data values to false, and all updated dates to null
     */
    public static void clearDataValueLoadedFlags() {
        List<OrganisationUnit> assignedOrganisationUnits =
                MetaDataController.getAssignedOrganisationUnits();
        for (OrganisationUnit organisationUnit : assignedOrganisationUnits) {
            if (organisationUnit.getId() == null)
                break;
            List<Program> programsForOrgUnit = new ArrayList<>();
            List<Program> programsForOrgUnitSEWoR =
                    MetaDataController.getProgramsForOrganisationUnit
                            (organisationUnit.getId(),
                                    ProgramType.WITHOUT_REGISTRATION);
            if (programsForOrgUnitSEWoR != null)
                programsForOrgUnit.addAll(programsForOrgUnitSEWoR);

            for (Program program : programsForOrgUnit) {
                if (program.getUid() == null)
                    break;
                DateTimeManager.getInstance().deleteLastUpdated(ResourceType.EVENTS,
                        organisationUnit.getId() + program.getUid());
            }
        }
    }

    /**
     * Loads datavalues from the server and stores it in local persistence.
     */
    public static void loadDataValues(Context context, DhisApi dhisApi, SyncStrategy syncStrategy) throws APIException {
        UiUtils.postProgressMessage(context.getString(R.string.loading_datavalue), LoadingMessageEvent.EventType.METADATA);
        TrackerDataLoader.updateDataValueDataItems(context, dhisApi, syncStrategy);
    }

    public static List<TrackedEntityInstance> queryTrackedEntityInstancesDataFromServer(DhisApi dhisApi,
                                                                                        String organisationUnitUid,
                                                                                        String programUid,
                                                                                        String queryString,
                                                                                        TrackedEntityAttributeValue... params) throws APIException {
        return TrackerDataLoader.queryTrackedEntityInstanceDataFromServer(dhisApi, organisationUnitUid, programUid, queryString, params);
    }

    public static List<TrackedEntityInstance> queryTrackedEntityInstancesDataFromAllAccessibleOrgUnits(DhisApi dhisApi,
                                                                                                       String organisationUnitUid,
                                                                                                       String programUid,
                                                                                                       String queryString,
                                                                                                       boolean detailedSearch,
                                                                                                       TrackedEntityAttributeValue... params) throws APIException {
        return TrackerDataLoader.queryTrackedEntityInstancesDataFromAllAccessibleOrgunits(dhisApi, organisationUnitUid, programUid, queryString, detailedSearch, params);
    }

    public static List<TrackedEntityInstance> getTrackedEntityInstancesDataFromServer(DhisApi dhisApi, List<TrackedEntityInstance> trackedEntityInstances, boolean getEnrollments, boolean getRecursiveRelations) throws APIException {
        return TrackerDataLoader.getTrackedEntityInstancesDataFromServer(dhisApi, trackedEntityInstances, getEnrollments, getRecursiveRelations);
    }

    public static void getEnrollmentDataFromServer(DhisApi dhisApi, String uid, boolean getEvents, DateTime serverDateTime) throws APIException {
        TrackerDataLoader.getEnrollmentDataFromServer(dhisApi, uid, getEvents, serverDateTime);
    }

    /**
     * Refreshes event statuses after downloading
     *
     * @param dhisApi
     * @param trackedEntityInstance
     * @param serverDateTime
     * @return
     * @throws APIException
     */
    public static List<Enrollment> getEnrollmentDataFromServer(DhisApi dhisApi, TrackedEntityInstance trackedEntityInstance, DateTime serverDateTime) throws APIException {
        List<Enrollment> enrollments = TrackerDataLoader.getEnrollmentsDataFromServer(dhisApi, trackedEntityInstance, serverDateTime);
        return enrollments;
    }

    public static List<Enrollment> getRemoteEnrollmentDatas(DhisApi dhisApi, TrackedEntityInstance trackedEntityInstance, DateTime serverDateTime) throws APIException {
        List<Enrollment> enrollments = TrackerDataLoader.getRemoteEnrollmentsWithDatas(dhisApi, trackedEntityInstance, serverDateTime);
        return enrollments;
    }

    public static void getEventDataFromServer(DhisApi dhisApi, String uid) throws APIException {
        TrackerDataLoader.getEventDataFromServer(dhisApi, uid);
    }

    public static void sendEventChanges(DhisApi dhisApi, Event event) throws APIException {
        TrackerDataSender.sendEventChanges(dhisApi, event);
    }

    public static void sendEnrollmentChanges(DhisApi dhisApi, Enrollment enrollment, boolean sendEvents) throws APIException {
        TrackerDataSender.sendEnrollmentChanges(dhisApi, enrollment, sendEvents);
    }

    public static void sendTrackedEntityInstanceChanges(DhisApi dhisApi, TrackedEntityInstance trackedEntityInstance, boolean sendEnrollments) throws APIException {
        TrackerDataSender.sendTrackedEntityInstanceChanges(dhisApi, trackedEntityInstance, sendEnrollments);
    }

    public static List<Enrollment> getActiveEnrollments() {
        List<Enrollment> activeEnrollments = new Select().from(Enrollment.class)
                .where(Condition.column(Enrollment$Table.STATUS).eq(Enrollment.ACTIVE))
                .queryList();
        if (activeEnrollments != null) {
            return activeEnrollments;
        } else return new ArrayList<>();
    }

    public static List<Event> getOverdueEventsWithActiveEnrollments(String mProgramId, String mOrgUnitId) {

        List<Enrollment> activeEnrollments = new Select().from(Enrollment.class).where(Condition.column
                (Enrollment$Table.PROGRAM).is(mProgramId)).and(Condition.column(Enrollment$Table.STATUS).is(Enrollment.ACTIVE)).queryList();

        String activeEnrollmentsSqlSafeString = getSqlSafeStringFromListOfEnrollments(activeEnrollments);
        String beginningOfTime = DateUtils.getMediumDateString(new Date(0));
        String today = DateUtils.getMediumDateString();
        String rawSqlQuery = new Select().from(Event.class)
                .where(Condition.column(Event$Table.PROGRAMID).eq(mProgramId))
                .and(Condition.column(Event$Table.ORGANISATIONUNITID).eq(mOrgUnitId))
                .and(Condition.column(Event$Table.DUEDATE).between(beginningOfTime).and(today))
                .and(Condition.column(Event$Table.STATUS).isNot(Event.STATUS_COMPLETED))
                .and(Condition.column(Event$Table.STATUS).isNot(Event.STATUS_SKIPPED))
                .and(Condition.column(Event$Table.ENROLLMENT)).toString()
                + " IN " + activeEnrollmentsSqlSafeString
                + " ORDER BY " + Event$Table.DUEDATE;

        return new StringQuery<Event>(Event.class, rawSqlQuery).queryList();
    }

    /*
    * DBFlow does not support collections for IN statements (DBFlow v2.2.1 as of now).
    * Use raw SQL statement to get around this
    * */
    @NonNull
    private static String getSqlSafeStringFromListOfEnrollments(List<Enrollment> activeEnrollments) {
        String activeEnrollmentsSqlSafeString = "(";

        for (int i = 0; i < activeEnrollments.size() - 1; i++) {
            activeEnrollmentsSqlSafeString += "'" + activeEnrollments.get(i).getEnrollment() + "', ";
        }
        if(activeEnrollments.size() > 0) {
            activeEnrollmentsSqlSafeString += "'" + activeEnrollments.get(activeEnrollments.size() - 1).getEnrollment() + "')";
        } else {
            activeEnrollmentsSqlSafeString += ")";
        }
        return activeEnrollmentsSqlSafeString;
    }

    public static List<Event> getActiveEventsWithActiveEnrollments(String mProgramId, String mOrgUnitId, String mStartDate, String mEndDate) {

        List<Enrollment> activeEnrollments = new Select().from(Enrollment.class).where(Condition.column
                (Enrollment$Table.PROGRAM).is(mProgramId)).and(Condition.column(Enrollment$Table.STATUS).is(Enrollment.ACTIVE)).queryTableList();

        String activeEnrollmentsSqlSafeString = getSqlSafeStringFromListOfEnrollments(activeEnrollments);

        String rawSqlQuery = new Select().from(Event.class).where(Condition.column(Event$Table.PROGRAMID)
                .is(mProgramId))
                .and(Condition.column(Event$Table.ORGANISATIONUNITID).is(mOrgUnitId))
                .and(Condition.column(Event$Table.STATUS).isNot(Event.STATUS_SKIPPED))
                .and(Condition.column(Event$Table.EVENTDATE).isNotNull())
                .and(Condition.column(Event$Table.EVENTDATE).between(mStartDate).and(mEndDate))
                .and(Condition.column(Event$Table.ENROLLMENT)).toString() + " IN " + activeEnrollmentsSqlSafeString
                + " ORDER BY " + Event$Table.DUEDATE;

        return new StringQuery<Event>(Event.class, rawSqlQuery).queryList();
    }

    public static void refreshRelationsByTrackedEntity(DhisApi dhisApi, String trackedEntityInstance) {
        TrackerDataLoader.refreshRelationshipsByTrackedEntityInstance(dhisApi, trackedEntityInstance);
    }

    public static void updateTrackedEntityInstances(DhisApi dhisApi,
            List<TrackedEntityInstance> trackedEntityInstances, DateTime serverDateTime) {
        for(TrackedEntityInstance trackedEntityInstance:trackedEntityInstances) {
            TrackerDataLoader.getTrackedEntityInstanceDataFromServer(
                    dhisApi, trackedEntityInstance.getUid(), true, true,
                    serverDateTime);
        }
    }

    /**
     * Returns a list with the failed unique values
     * @return
     */
    public static List<String> getNotValidatedUniqueValues(
            Map<String, TrackedEntityAttributeValue> trackedEntityAttributeValueMap, String organisationUnitUId) {
        List<String> listOFUniqueFields = new ArrayList<>();
        for (String key : trackedEntityAttributeValueMap.keySet()) {
            TrackedEntityAttributeValue value = trackedEntityAttributeValueMap.get(key);
            TrackedEntityAttribute trackedEntityAttribute =
                    MetaDataController.getTrackedEntityAttribute(
                            value.getTrackedEntityAttributeId());
            if (trackedEntityAttribute.isUnique()) {
                if(value.getValue()==null || value.getValue().isEmpty()){
                    continue;
                }
                if(trackedEntityAttribute.isOrgunitScope()){
                    if(TrackerController.countTrackedEntityAttributeValueInActiveOu(value, organisationUnitUId) != 0) {
                        listOFUniqueFields.add(trackedEntityAttribute.getDisplayName());
                    }
                }else if(TrackerController.countTrackedEntityAttributeValue(value) !=0) {
                    listOFUniqueFields.add(trackedEntityAttribute.getDisplayName());
                }
            }
        }
        return listOFUniqueFields;
    }

    /**
     * Returns false if find a unique value not valid
     *
     * @return
     */
    public static boolean validateUniqueValues(
            Map<String, TrackedEntityAttributeValue> trackedEntityAttributeValueMap, String organisationUnitUId) {
        for (String key : trackedEntityAttributeValueMap.keySet()) {
            TrackedEntityAttributeValue value = trackedEntityAttributeValueMap.get(key);
            TrackedEntityAttribute trackedEntityAttribute =
                    MetaDataController.getTrackedEntityAttribute(
                            value.getTrackedEntityAttributeId());
            if (trackedEntityAttribute.isUnique()) {
                if(value.getValue()==null || value.getValue().isEmpty()){
                    continue;
                }
                if(trackedEntityAttribute.isOrgunitScope()){
                    if(TrackerController.countTrackedEntityAttributeValueInActiveOu(value, organisationUnitUId) != 0) {
                        return false;
                    }
                }else if(TrackerController.countTrackedEntityAttributeValue(value) !=0) {
                    return false;
                }
            }
        }
        return true;
    }
    /**
     * Returns the number of the given value by given trackedentityattribute
     *
     * @param value
     * @return
     */
    public static int countTrackedEntityAttributeValue(TrackedEntityAttributeValue value) {
        return (int) new Select().count().from(TrackedEntityAttributeValue.class).where(
                Condition.column(TrackedEntityAttributeValue$Table.
                        VALUE).eq(value.getValue()))
                .and(Condition.column(TrackedEntityAttributeValue$Table.
                        TRACKEDENTITYATTRIBUTEID).eq(value.getTrackedEntityAttributeId()))
                .and(Condition.column(TrackedEntityAttributeValue$Table.
                        LOCALTRACKEDENTITYINSTANCEID).isNot(value.getLocalTrackedEntityInstanceId())).count();
    }

    /**
     * Returns the number of the given value by given trackedentityattribute and organisation unit
     *
     * @param value
     * @return
     */
    private static int countTrackedEntityAttributeValueInActiveOu(TrackedEntityAttributeValue value, String organisationUnitUId) {
        return (int) new Select().count().from(TrackedEntityAttributeValue.class)
                .join(TrackedEntityInstance.class, Join.JoinType.LEFT)
                .on(Condition.column(TrackedEntityInstance$Table.TRACKEDENTITYINSTANCE).eq(TrackedEntityAttributeValue$Table.TRACKEDENTITYINSTANCEID))
                .where(Condition.column
                        (TrackedEntityInstance$Table.ORGUNIT).is(organisationUnitUId))
                .and(Condition.column(TrackedEntityAttributeValue$Table.
                        VALUE).eq(value.getValue()))
                .and(Condition.column(TrackedEntityAttributeValue$Table.
                        TRACKEDENTITYATTRIBUTEID).eq(value.getTrackedEntityAttributeId()))
                .and(Condition.column(TrackedEntityAttributeValue$Table.
                        LOCALTRACKEDENTITYINSTANCEID).isNot(value.getLocalTrackedEntityInstanceId())).count();
    }




    // ADD NEW FUNCTIONS - 2019
    public static Enrollment getEnrollmentByProgramAndTrackedEntityInstance(String program, String trackedEntityInstanceId){
        return new Select().from(Enrollment.class)
                .where(Condition.column(Enrollment$Table.TRACKEDENTITYINSTANCE).is(trackedEntityInstanceId))
                .and(Condition.column(Enrollment$Table.PROGRAM).is(program))
                .querySingle();
    }

    public static List<TrackedEntityInstance> queryLocalTrackedEntityInstances(String organUnitId, String programUid){

        List<TrackedEntityInstance> instances = new ArrayList<>();

        List<Enrollment> enrollments = TrackerController.getEnrollments(programUid, organUnitId);

        for(Enrollment enrollment : enrollments){
            TrackedEntityInstance instance = TrackerController.getLocalTrackedEntityInstanceThoughId(enrollment.getTrackedEntityInstance());
            instances.add(instance);
        }

        return instances;
    }

    public static TrackedEntityInstance getLocalTrackedEntityInstanceThoughId(String trackedEntityInstanceId){
        return new Select().from(TrackedEntityInstance.class)
                .where(Condition.column(TrackedEntityInstance$Table.TRACKEDENTITYINSTANCE).is(trackedEntityInstanceId))
                .querySingle();
    }

    public static ProgramStage getProgramStageByName(String programId, String programStageName){
        return new Select().from(ProgramStage.class)
                .where(Condition.column(ProgramStage$Table.PROGRAM).is(programId))
                .and(Condition.column(ProgramStage$Table.DISPLAYNAME).is(programStageName))
                .querySingle();
    }

    public static List<ProgramStageSection> getProgramStageSections(String programsStageId){
        return new Select().from(ProgramStageSection.class)
                .where(Condition.column(ProgramStageSection$Table.PROGRAMSTAGE).is(programsStageId))
                .queryList();
    }

    public static List<ProgramStageDataElement> getProgramStageDataElements(String programStageId){
        return new Select().from(ProgramStageDataElement.class).where(Condition.column(ProgramStageDataElement$Table.PROGRAMSTAGE).is(programStageId)).queryList();
    }

    public static DataElement getDataElement(String element){
        return new Select().from(DataElement.class)
                .where(Condition.column(DataElement$Table.ID).is(element))
                .querySingle();
    }

    public static List<DataValue> getDataValuesFollowElement(String dataElementId) {
        return new Select().from(DataValue.class)
                .where(Condition.column(DataValue$Table.DATAELEMENT).is(dataElementId))
                .queryList();
    }

    public static TrackedEntityAttribute getTrackedEntityAttribute(String trackedEntityAttributeId){
        return new Select().from(TrackedEntityAttribute.class)
                .where(Condition.column(TrackedEntityAttribute$Table.ID).is(trackedEntityAttributeId))
                .querySingle();
    }

    public static List<Event> getEvents(String organisationUnitId, String programId, String trackedEntityInstance) {
        List<Event> events = new Select().from(Event.class).where(
                    Condition.column(Event$Table.ORGANISATIONUNITID).is(organisationUnitId)).and(
                    Condition.column(Event$Table.PROGRAMID).is(programId)).and(
                    Condition.column(Event$Table.TRACKEDENTITYINSTANCE).is(trackedEntityInstance))
                .and(Condition.column(Event$Table.STATUS).isNot(Event.STATUS_DELETED))
                .orderBy(false, Event$Table.LASTUPDATED).queryList();
        return events;
    }

    public static List<Event> getEventsThoughProgramStage(String organisationUnitId, String programId, String programStageId, String trackedEntityInstance) {
        List<Event> events = new Select().from(Event.class).where(
                    Condition.column(Event$Table.ORGANISATIONUNITID).is(organisationUnitId)).and(
                    Condition.column(Event$Table.PROGRAMID).is(programId)).and(
                    Condition.column(Event$Table.PROGRAMSTAGEID).is(programStageId)).and(
                    Condition.column(Event$Table.TRACKEDENTITYINSTANCE).is(trackedEntityInstance))
                .and(Condition.column(Event$Table.STATUS).isNot(Event.STATUS_DELETED))
                .orderBy(false, Event$Table.LASTUPDATED).queryList();
        return events;
    }

    public static List<ProgramStage> getProgramStages(String programId){
        return new Select().from(ProgramStage.class).where(Condition.column(ProgramStage$Table.PROGRAM).is(programId)).queryList();
    }

    public static List<DataValue> getDataValue(String event) {
        return new Select().from(DataValue.class)
                .where(Condition.column(DataValue$Table.EVENT).is(event))
                .queryList();
    }

    public static List<TrackedEntityInstance> queryLocalTrackedEntityInstances(String programId, String birthday, TrackedEntityAttributeValue phoneValue){

        List<TrackedEntityInstance> instances = new ArrayList<>();

        List<TrackedEntityAttributeValue> trackedEntityValues =
                TrackerController.searchLocalTrackedEntityAttributeValuesFollowPhoneNumber(phoneValue.getValue());

        for(TrackedEntityAttributeValue value : trackedEntityValues){
            TrackedEntityInstance instance = TrackerController.getLocalTrackedEntityInstanceWithAttribute(value.getTrackedEntityInstanceId());

            if(instance != null) {

                Enrollment enrollment = TrackerController.getEnrollment(programId, instance);

                if(enrollment != null) {

                    LocalDate incidentDate = new LocalDate(DateUtils.parseDate(enrollment.getIncidentDate()));
                    LocalDate birthDate = new LocalDate(DateUtils.parseDate(birthday));

                    if (incidentDate.isEqual(birthDate)) {
                        instances.add(instance);
                    }
                }
            }
        }

        return instances;
    }

    public static List<TrackedEntityAttributeValue> searchLocalTrackedEntityAttributeValuesFollowPhoneNumber(String phoneNumber){
        return new Select().distinct().from(TrackedEntityAttributeValue.class)
                .where(Condition.column(TrackedEntityAttributeValue$Table.VALUE).is(phoneNumber))
                .groupBy(TrackedEntityAttributeValue$Table.TRACKEDENTITYINSTANCEID)
                .queryList();
    }

    public static TrackedEntityInstance getLocalTrackedEntityInstanceWithAttribute(String trackedEntityInstanceId){
        TrackedEntityInstance instance = TrackerController.getLocalTrackedEntityInstance(trackedEntityInstanceId);
        if(instance != null){
            List<TrackedEntityAttributeValue> values = TrackerController.getLocalTrackedEntityAttributeValues(trackedEntityInstanceId);
            for(TrackedEntityAttributeValue value : values){
                TrackedEntityAttribute attribute = TrackerController.getTrackedEntityAttribute(value.getTrackedEntityAttributeId());
                value.setDisplayName(attribute.getDisplayName());
            }
            instance.setAttributes(values);
            instance.setFromServer(false);
        }

        return instance;
    }

    public static TrackedEntityInstance getLocalTrackedEntityInstance(String trackedEntityInstanceId){
        return new Select().from(TrackedEntityInstance.class)
                .where(Condition.column(TrackedEntityInstance$Table.TRACKEDENTITYINSTANCE).is(trackedEntityInstanceId))
                //.and(Condition.column(TrackedEntityInstance$Table.FROMSERVER).is(false))
                .querySingle();
    }

    public static List<TrackedEntityAttributeValue> getLocalTrackedEntityAttributeValues(String trackedEntityInstanceId){
        return new Select().from(TrackedEntityAttributeValue.class)
                .where(Condition.column(TrackedEntityAttributeValue$Table.TRACKEDENTITYINSTANCEID).is(trackedEntityInstanceId))
                .queryList();
    }

    public static List<Event> getEventsThoughOrganisationUnit(long localEnrollmentId, String organUnitId) {
        return new Select().from(Event.class).where(
                Condition.column(Event$Table.ORGANISATIONUNITID).is(organUnitId)).and(
                        Condition.column(Event$Table.LOCALENROLLMENTID).is(localEnrollmentId)).and(
                        Condition.column(Event$Table.STATUS).isNot(Event.STATUS_DELETED)).queryList();
    }

    public static Enrollment getEnrollment(TrackedEntityInstance trackedEntityInstance){
        return new Select().from(Enrollment.class)
                .where(Condition.column(Enrollment$Table.TRACKEDENTITYINSTANCE)
                            .is(trackedEntityInstance.getTrackedEntityInstance()))
                .querySingle();
    }

    public static TrackedEntityAttributeValue getUniqueIdAttributeValue(TrackedEntityInstance trackedEntityInstance){
        TrackedEntityAttribute attribute = new Select()
                .from(TrackedEntityAttribute.class)
                .where(Condition.column(TrackedEntityAttribute$Table.DISPLAYNAME)
                    .like("%Case ID%"))
                .querySingle();
        TrackedEntityAttributeValue value = null;
        if(attribute != null && trackedEntityInstance != null){
            value = new Select().from(TrackedEntityAttributeValue.class)
                    .where(Condition.column(TrackedEntityAttributeValue$Table.TRACKEDENTITYATTRIBUTEID).is(attribute.getUid()))
                    .and(Condition.column(TrackedEntityAttributeValue$Table.TRACKEDENTITYINSTANCEID).is(trackedEntityInstance.getTrackedEntityInstance()))
                    .querySingle();
        }
        return value;
    }

    public static TrackedEntityAttributeValue getTrackedEntityAttributeValueByDisplayName(String displayName, TrackedEntityInstance trackedEntityInstance){
        TrackedEntityAttributeValue value = null;
        TrackedEntityAttribute attribute = new Select().from(TrackedEntityAttribute.class)
                .where(Condition.column(TrackedEntityAttribute$Table.DISPLAYNAME).like("%" + displayName + "%"))
                .querySingle();

        if(attribute != null){
            value = TrackerController.getTrackedEntityAttributeValue(attribute.getUid(), trackedEntityInstance.getTrackedEntityInstance());
        }

        return value;
    }

    public static void sendEventDatas(DhisApi dhisApi) throws APIException {
        Log.d(CLASS_TAG, "sending local data");
        TrackerDataSender.deleteLocallyDeletedEvents(dhisApi);
        //TrackerDataSender.sendTrackedEntityInstanceChanges(dhisApi, false);
        //TrackerDataSender.sendEnrollmentChanges(dhisApi, false);
        TrackerDataSender.sendEventChanges(dhisApi);
    }
}