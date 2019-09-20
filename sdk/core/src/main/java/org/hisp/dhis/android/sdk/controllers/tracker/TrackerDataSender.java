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

import android.util.Log;
import com.fasterxml.jackson.databind.JsonNode;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Join;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Update;
import okhttp3.ResponseBody;
import org.apache.commons.beanutils.ConversionException;
import org.hisp.dhis.android.sdk.controllers.DhisController;
import org.hisp.dhis.android.sdk.network.APIException;
import org.hisp.dhis.android.sdk.network.DhisApi;
import org.hisp.dhis.android.sdk.persistence.models.*;
import org.hisp.dhis.android.sdk.utils.NetworkUtils;
import org.hisp.dhis.android.sdk.utils.StringConverter;
import org.hisp.dhis.android.sdk.utils.Utils;
import org.joda.time.DateTime;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Simen Skogly Russnes on 24.08.15.
 */
final class TrackerDataSender {

    public static final String CLASS_TAG = TrackerDataSender.class.getSimpleName();

    private TrackerDataSender() {
    }

    static void sendEventChanges(DhisApi dhisApi) throws APIException {
        if (dhisApi == null) {
            dhisApi = DhisController.getInstance().getDhisApi();
            if (dhisApi == null) {
                return;
            }
        }
        List<Event> events = new Select().from(Event.class).where
                (Condition.column(Event$Table.FROMSERVER).is(false))
                .and(Condition.column(Event$Table.STATUS).isNot(Event.STATUS_DELETED))
                .queryList();

        List<Event> eventsWithFailedThreshold = new Select().from(Event.class)
                .join(FailedItem.class, Join.JoinType.LEFT)
                .on(Condition.column(FailedItem$Table.ITEMID).eq(Event$Table.LOCALID))
                .where(Condition.column(FailedItem$Table.ITEMTYPE).eq(FailedItem.EVENT))
                .and(Condition.column(FailedItem$Table.FAILCOUNT).greaterThan(3))
                .and(Condition.column(Event$Table.FROMSERVER).is(false))
                .and(Condition.column(Event$Table.STATUS).isNot(Event.STATUS_DELETED))
                .queryList();

        List<Event> eventsToPost = new ArrayList<>();
        eventsToPost.addAll(events);
        for (Event event : events) {
            for (Event failedEvent : eventsWithFailedThreshold) {
                if (event.getUid().equals(failedEvent.getUid())) {
                    eventsToPost.remove(event);
                }
            }
        }
        sendEventBatch(dhisApi, events);
    }

    static void sendEventBatch(DhisApi dhisApi, List<Event> events) throws APIException {
        if (events == null || events.isEmpty()) {
            return;
        }

        for (int i = 0; i < events.size(); i++) {/* removing events with local enrollment reference. In this case, the enrollment needs to be synced first*/
            Event event = events.get(i);
            if (Utils.isLocal(event.getEnrollment()) && event.getEnrollment() != null/*if enrollments==null, then it is probably a single event without reg*/) {
                events.remove(i);
                i--;
                continue;
            }
        }
        postEventBatch(dhisApi, events);
    }

    static void postEventBatch(DhisApi dhisApi, List<Event> events) throws APIException {
        Map<String, Event> eventMap = new HashMap<>();
        List<ImportSummary> importSummaries = null;

        Response apiResponse = null;
        try {
            Map<String, List<Event>> map = new HashMap<>();
            map.put("events", events);
            apiResponse = dhisApi.postEvents(map).execute();

            importSummaries = null;//apiResponse.getImportSummaries();

            for (Event event : events) {
                eventMap.put(event.getUid(), event);
            }

            // check if all items were synced successfully
            if (importSummaries != null) {
                SystemInfo systemInfo = null;
                try {
                    systemInfo = dhisApi.getSystemInfo().execute().body();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                DateTime eventUploadTime = systemInfo.getServerDate();
                for (ImportSummary importSummary : importSummaries) {
                    Event event = eventMap.get(importSummary.getReference());
                    System.out.println("IMPORT SUMMARY: " + importSummary.getDescription());
                    if (ImportSummary.SUCCESS.equals(importSummary.getStatus()) ||
                            ImportSummary.OK.equals(importSummary.getStatus())) {
                        if (event != null) {
                            event.setFromServer(true);
                            event.setCreated(eventUploadTime.toString());
                            event.setLastUpdated(eventUploadTime.toString());
                            event.save();
                            clearFailedItem(FailedItem.EVENT, event.getLocalId());
                            //UpdateEventTimestamp(event, dhisApi);
                        }
                    }
                }
            }

        } catch (APIException apiException) {
            //batch sending failed. Trying to re-send one by one
            sendEventChanges(dhisApi, events);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void sendEventChanges(DhisApi dhisApi, List<Event> events) throws APIException {
        if (events == null || events.isEmpty()) {
            return;
        }

        for (int i = 0; i < events.size(); i++) {/* removing events with local enrollment reference. In this case, the enrollment needs to be synced first*/
            Event event = events.get(i);
            if (Utils.isLocal(event.getEnrollment()) && event.getEnrollment() != null/*if enrollments==null, then it is probably a single event without reg*/) {
                events.remove(i);
                i--;
                continue;
            }
        }
        Log.d(CLASS_TAG, "got this many events to send:" + events.size());

        for (Event event : events) {
            sendEventChanges(dhisApi, event);
        }
    }

    static void sendEventChanges(DhisApi dhisApi, Event event) throws APIException {
        if (event == null) {
            return;
        }
        if (dhisApi == null) {
            dhisApi = DhisController.getInstance().getDhisApi();
            if (dhisApi == null) {
                return;
            }
        }

        if (Utils.isLocal(event.getEnrollment()) && event.getEnrollment() != null/*if enrollments==null, then it is probably a single event without reg*/) {
            return;
        }

        Enrollment cancelledEnrollment = TrackerController.getCancelledEnrollment(event.getEnrollment());
        if (cancelledEnrollment != null && !cancelledEnrollment.isFromServer()) { // the cancelled enrollment should be pushed before.
            sendEnrollmentChanges(dhisApi, cancelledEnrollment, false);
        }
        Enrollment enrollment = TrackerController.getNotCancelledEnrollment(event.getEnrollment());
        if (enrollment != null && !enrollment.isFromServer()) { // if enrollment is unsent, send it before events
            sendEnrollmentChanges(dhisApi, enrollment, false);
        }

        if (event.getCreated() == null) {
            postEvent(event, dhisApi);
        } else {
            putEvent(event, dhisApi);
        }
    }

    private static void postEvent(Event event, DhisApi dhisApi) throws APIException {
        try {
            Response response = dhisApi.postEvent(event).execute();
            if (response.code() == 200) {
                ImportSummary importSummary = getImportSummary(response);
                handleImportSummary(importSummary, FailedItem.EVENT, event.getLocalId());
                if (ImportSummary.SUCCESS.equals(importSummary.getStatus()) ||
                        ImportSummary.OK.equals(importSummary.getStatus())) {
                    // also, we will need to find UUID of newly created event,
                    // which is contained inside of HTTP Location header
                    //Header header = NetworkUtils.findLocationHeader(response.headers());
                    // change state and save event
                    event.setFromServer(true);
                    event.save();
                    clearFailedItem(FailedItem.EVENT, event.getLocalId());
                    UpdateEventTimestamp(event, dhisApi);
                }
            }
        } catch (APIException apiException) {
            NetworkUtils.handleEventSendException(apiException, event);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void putEvent(Event event, DhisApi dhisApi) throws APIException {
        try {
            Response response = null;
            try {
                response = dhisApi.putEvent(event.getEvent(), event).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (response.code() == 200) {
                ImportSummary importSummary = getImportSummary(response);
                handleImportSummary(importSummary, FailedItem.EVENT, event.getLocalId());
                if (ImportSummary.SUCCESS.equals(importSummary.getStatus()) ||
                        ImportSummary.OK.equals(importSummary.getStatus())) {

                    event.setFromServer(true);
                    event.save();
                    clearFailedItem(FailedItem.EVENT, event.getLocalId());
                    UpdateEventTimestamp(event, dhisApi);
                }
            }
        } catch (APIException apiException) {
            NetworkUtils.handleEventSendException(apiException, event);
        }
    }

    private static void UpdateEventTimestamp(Event event, DhisApi dhisApi) throws APIException {
        try {
            final Map<String, String> QUERY_PARAMS = new HashMap<>();
            QUERY_PARAMS.put("fields", "created,lastUpdated");
            Event updatedEvent = null;
            try {
                updatedEvent = dhisApi
                        .getEvent(event.getEvent(), QUERY_PARAMS).execute().body();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // merging updated timestamp to local event model
            event.setCreated(updatedEvent.getCreated());
            event.setLastUpdated(updatedEvent.getLastUpdated());
            event.save();
        } catch (APIException apiException) {
            NetworkUtils.handleApiException(apiException);
        }
    }

    static void sendEnrollmentChanges(DhisApi dhisApi, List<Enrollment> enrollments, boolean sendEvents) throws APIException {
        if (enrollments == null || enrollments.isEmpty()) {
            return;
        }

        for (int i = 0; i < enrollments.size(); i++) {/* workaround for not attempting to upload enrollments with local tei reference*/
            Enrollment enrollment = enrollments.get(i);
            if (Utils.isLocal(enrollment.getTrackedEntityInstance())) {
                enrollments.remove(i);
                i--;
            }
        }
        Log.d(CLASS_TAG, "got this many enrollments to send:" + enrollments.size());
        for (Enrollment enrollment : enrollments) {
            sendEnrollmentChanges(dhisApi, enrollment, sendEvents);
        }
    }

    static void sendEnrollmentChanges(DhisApi dhisApi, Enrollment enrollment, boolean sendEvents) throws APIException {
        if (enrollment == null) {
            return;
        }
        if (Utils.isLocal(enrollment.getTrackedEntityInstance())) {//donaLoader't send enrollment with locally made uid
            return;
        }
        if (dhisApi == null) {
            dhisApi = DhisController.getInstance().getDhisApi();
            if (dhisApi == null) {
                return;
            }
        }
        TrackedEntityInstance trackedEntityInstance = TrackerController.getTrackedEntityInstance(enrollment.getTrackedEntityInstance());

        if (trackedEntityInstance == null) {
            return;
        } else {
            if (!trackedEntityInstance.isFromServer()) { // if TEI is not sent to server and trying to send enrollment first. Send TEI before enrollment
                sendTrackedEntityInstanceChanges(dhisApi, trackedEntityInstance, false);
            }
        }

        boolean success;

        if (enrollment.getCreated() == null) {
            success = postEnrollment(enrollment, dhisApi);
            if (success && sendEvents) {
                List<Event> events = TrackerController.getEventsByEnrollment(enrollment.getLocalId());
                sendEventChanges(dhisApi, events);
            }
        } else {
            success = putEnrollment(enrollment, dhisApi);
            if (success && sendEvents) {
                List<Event> events = TrackerController.getEventsByEnrollment(enrollment.getLocalId());
                sendEventChanges(dhisApi, events);
            }
        }
    }

    private static boolean postEnrollment(Enrollment enrollment, DhisApi dhisApi) throws APIException {
        try {
            Response response = dhisApi.postEnrollment(enrollment).execute();
            if (response.code() == 200) {
                ImportSummary importSummary = getImportSummary(response);
                handleImportSummary(importSummary, FailedItem.ENROLLMENT, enrollment.getLocalId());

                if (ImportSummary.SUCCESS.equals(importSummary.getStatus()) ||
                        ImportSummary.OK.equals(importSummary.getStatus())) {
                    // change state and save enrollment

                    //enrollment.setState(State.SYNCED);
                    enrollment.setFromServer(true);
                    enrollment.save();
                    clearFailedItem(FailedItem.ENROLLMENT, enrollment.getLocalId());
                    UpdateEnrollmentTimestamp(enrollment, dhisApi);
                }
            }
        } catch (APIException apiException) {
            NetworkUtils.handleEnrollmentSendException(apiException, enrollment);
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private static boolean putEnrollment(Enrollment enrollment, DhisApi dhisApi) throws APIException {
        try {
            Response response = dhisApi.putEnrollment(enrollment.getEnrollment(), enrollment).execute();
            if (response.code() == 200) {
                ImportSummary importSummary = getImportSummary(response);
                handleImportSummary(importSummary, FailedItem.ENROLLMENT, enrollment.getLocalId());

                if (ImportSummary.SUCCESS.equals(importSummary.getStatus()) ||
                        ImportSummary.OK.equals(importSummary.getStatus())) {

                    //enrollment.setState(State.SYNCED);
                    enrollment.setFromServer(true);
                    enrollment.save();
                    clearFailedItem(FailedItem.ENROLLMENT, enrollment.getLocalId());
                    UpdateEnrollmentTimestamp(enrollment, dhisApi);
                }
            }
        } catch (APIException apiException) {
            NetworkUtils.handleEnrollmentSendException(apiException, enrollment);
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private static void updateEnrollmentReferences(long localId, String newReference) {
        //updating any local events that had reference to local enrollment to new
        //reference from server.
        Log.d(CLASS_TAG, "updating enrollment references");
        new Update(Event.class).set(Condition.column
                (Event$Table.ENROLLMENT).is
                (newReference)).where(
                Condition.column(Event$Table.LOCALENROLLMENTID).is(localId)).and(
                Condition.column(Event$Table.STATUS).isNot(Event.STATUS_DELETED)).async().execute();

        new Update(Enrollment.class).set(Condition.column
                (Enrollment$Table.ENROLLMENT).is
                (newReference), Condition.column(Enrollment$Table.FROMSERVER)
                .is(true)).where(Condition.column(Enrollment$Table.LOCALID).is
                (localId)).async().execute();
    }

    private static void UpdateEnrollmentTimestamp(Enrollment enrollment, DhisApi dhisApi) throws APIException {
        try {
            final Map<String, String> QUERY_PARAMS = new HashMap<>();
            QUERY_PARAMS.put("fields", "created,lastUpdated");
            Enrollment updatedEnrollment = dhisApi
                    .getEnrollment(enrollment.getEnrollment(), QUERY_PARAMS).execute().body();

            // merging updated timestamp to local enrollment model
            enrollment.setCreated(updatedEnrollment.getCreated());
            enrollment.setLastUpdated(updatedEnrollment.getLastUpdated());
            enrollment.save();
        } catch (APIException apiException) {
            NetworkUtils.handleApiException(apiException);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void sendTrackedEntityInstanceChanges(DhisApi dhisApi, boolean sendEnrollments) throws APIException {
        List<TrackedEntityInstance> trackedEntityInstances = new Select().from(TrackedEntityInstance.class).where(Condition.column(TrackedEntityInstance$Table.FROMSERVER).is(false)).queryList();
        if (dhisApi == null) {
            dhisApi = DhisController.getInstance().getDhisApi();
            if (dhisApi == null) {
                return;
            }
        }
        if (trackedEntityInstances.size() <= 1) {
            sendTrackedEntityInstanceChanges(dhisApi, trackedEntityInstances, sendEnrollments);
        } else {
            postTrackedEntityInstanceBatch(dhisApi, trackedEntityInstances);
        }
        // sendTrackedEntityInstanceChanges(dhisApi, trackedEntityInstances, sendEnrollments);
    }

    static void sendTrackedEntityInstanceChanges(DhisApi dhisApi, List<TrackedEntityInstance> trackedEntityInstances, boolean sendEnrollments) throws APIException {
        if (trackedEntityInstances == null || trackedEntityInstances.isEmpty()) {
            return;
        }
        Log.d(CLASS_TAG, "got this many teis to send:" + trackedEntityInstances.size());

        for (TrackedEntityInstance trackedEntityInstance : trackedEntityInstances) {
            sendTrackedEntityInstanceChanges(dhisApi, trackedEntityInstance, sendEnrollments);
        }
    }

    static void sendTrackedEntityInstanceChanges(DhisApi dhisApi, TrackedEntityInstance trackedEntityInstance, boolean sendEnrollments) throws APIException {
        if (trackedEntityInstance == null) {
            return;
        }
        if (dhisApi == null) {
            dhisApi = DhisController.getInstance().getDhisApi();
            if (dhisApi == null) {
                return;
            }
        }
        Map<String, TrackedEntityInstance> relatedTeis = new HashMap<String,
                TrackedEntityInstance>();
        SystemInfo systemInfo = null;
        try {
            systemInfo = DhisController.getInstance().getDhisApi().getSystemInfo().execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        DateTime serverDate = systemInfo.getServerDate();
        relatedTeis = getRecursiveRelationatedTeis(trackedEntityInstance, relatedTeis);
        if(relatedTeis.size()>1) {
            pushTeiWithoutRelationFirst(relatedTeis, serverDate);
            trackedEntityInstance.setCreated(serverDate.toString());
            trackedEntityInstance.setCreatedAtClient(serverDate.toString());
            trackedEntityInstance.setFromServer(true);
            sendTrackedEntityInstance(dhisApi, trackedEntityInstance, sendEnrollments);
        }else {
            sendTrackedEntityInstance(dhisApi, trackedEntityInstance, sendEnrollments);
        }
    }

    private static Map<String, TrackedEntityInstance> getRecursiveRelationatedTeis(
            TrackedEntityInstance trackedEntityInstance,
            Map<String, TrackedEntityInstance> relatedTeiList) {
        if (trackedEntityInstance.getRelationships() != null
                && trackedEntityInstance.getRelationships().size() > 0) {
            for (Relationship relationship : trackedEntityInstance.getRelationships()) {
                if (relationship.getTrackedEntityInstanceB().equals(
                        trackedEntityInstance.getUid())) {
                    String target = relationship.getTrackedEntityInstanceA();
                    relatedTeiList = addRelatedNotPushedTeis(relatedTeiList, target);
                } else if (relationship.getTrackedEntityInstanceA().equals(
                        trackedEntityInstance.getUid())) {
                    String target = relationship.getTrackedEntityInstanceB();
                    relatedTeiList = addRelatedNotPushedTeis(relatedTeiList, target);
                }
            }
        }
        return relatedTeiList;
    }

    private static Map<String, TrackedEntityInstance> addRelatedNotPushedTeis(
            Map<String, TrackedEntityInstance> relatedTeiList, String target) {
        TrackedEntityInstance relatedTrackedEntityInstance =
                TrackerController.getTrackedEntityInstance(target);
        if (!relatedTrackedEntityInstance.isFromServer()
                && relatedTrackedEntityInstance.getCreated() == null) {
            if (!relatedTeiList.containsKey(relatedTrackedEntityInstance.getUid())) {
                relatedTeiList.put(relatedTrackedEntityInstance.getUid(),
                        relatedTrackedEntityInstance);
                relatedTeiList = getRecursiveRelationatedTeis(relatedTrackedEntityInstance,
                        relatedTeiList);
            }
        }
        return relatedTeiList;
    }

    private static void pushTeiWithoutRelationFirst(
            Map<String, TrackedEntityInstance> trackedEntityInstances, DateTime serverDate) {
        List<TrackedEntityInstance> trackerEntityInstancesWithRelations = new ArrayList<>();
        if (trackedEntityInstances.size() > 0) {
            for (TrackedEntityInstance trackedEntityInstance : trackedEntityInstances.values()) {
                trackerEntityInstancesWithRelations.add(trackedEntityInstance);
                //set relationships as null
                trackedEntityInstance.setRelationships(new ArrayList<Relationship>());
                TrackerController.sendTrackedEntityInstanceChanges(
                        DhisController.getInstance().getDhisApi(), trackedEntityInstance, false);
            }
            for (TrackedEntityInstance trackedEntityInstance :
                    trackerEntityInstancesWithRelations) {
                if (trackedEntityInstance.getRelationships().size() > 0) {
                    trackedEntityInstance.setFromServer(false);
                    TrackerController.sendTrackedEntityInstanceChanges(
                            DhisController.getInstance().getDhisApi(), trackedEntityInstance, true);
                    trackedEntityInstance.setCreated(serverDate.toString());
                    trackedEntityInstance.setLastUpdated(serverDate.toString());
                    trackedEntityInstance.save();
                }
            }
        }
    }

    private static void sendTrackedEntityInstance(DhisApi dhisApi,
            TrackedEntityInstance trackedEntityInstance, boolean sendEnrollments) {
        boolean success;
        if (trackedEntityInstance.getCreated() == null) {
            success = postTrackedEntityInstance(trackedEntityInstance, dhisApi);
        } else {
            success = putTrackedEntityInstance(trackedEntityInstance, dhisApi);
        }
        if (success && sendEnrollments) {
            List<Enrollment> enrollments = TrackerController.getEnrollments(trackedEntityInstance);
            sendEnrollmentChanges(dhisApi, enrollments, sendEnrollments);
        }
    }

    static void postTrackedEntityInstanceBatch(DhisApi dhisApi, List<TrackedEntityInstance> trackedEntityInstances) throws APIException {
        Map<String, TrackedEntityInstance> trackedEntityInstanceMap = new HashMap<>();
        List<ImportSummary> importSummaries = null;

        ApiResponse apiResponse = null;
        Response response = null;
        try {
            Map<String, List<TrackedEntityInstance>> map = new HashMap<>();
            map.put("trackedEntityInstances", trackedEntityInstances);

            response = dhisApi.postTrackedEntityInstances(map).execute();
            apiResponse = DhisController.getInstance().getObjectMapper().
                    readValue(((ResponseBody)response.body()).string(), ApiResponse.class);
            importSummaries = apiResponse.getImportSummaries();

            for (TrackedEntityInstance trackedEntityInstance : trackedEntityInstances) {
                trackedEntityInstanceMap.put(trackedEntityInstance.getUid(), trackedEntityInstance);
            }

            // check if all items were synced successfully
            if (importSummaries != null) {
                SystemInfo systemInfo = null;
                try {
                    systemInfo = dhisApi.getSystemInfo().execute().body();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                DateTime eventUploadTime = systemInfo.getServerDate();
                for (ImportSummary importSummary : importSummaries) {
                    TrackedEntityInstance trackedEntityInstance = trackedEntityInstanceMap.get(importSummary.getReference());
                    System.out.println("IMPORT SUMMARY: " + importSummary.getDescription());
                    if (importSummary.isSuccessOrOK()) {
                        trackedEntityInstance.setFromServer(true);
                        trackedEntityInstance.setCreated(eventUploadTime.toString());
                        trackedEntityInstance.setLastUpdated(eventUploadTime.toString());
                        trackedEntityInstance.save();
                        clearFailedItem(FailedItem.TRACKEDENTITYINSTANCE, trackedEntityInstance.getLocalId());
                        //UpdateTrackedEntityInstanceTimestamp(trackedEntityInstance, dhisApi);
                    }
                }
            }

        } catch (APIException apiException) {
            //batch sending failed. Trying to re-send one by one
            sendTrackedEntityInstanceChanges(dhisApi, trackedEntityInstances, false);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean postTrackedEntityInstance(TrackedEntityInstance trackedEntityInstance, DhisApi dhisApi) throws APIException {
        try {
            Response response = dhisApi.postTrackedEntityInstance(trackedEntityInstance).execute();
            if (response.code() == 200) {
                ImportSummary importSummary = getImportSummary(response);
                handleImportSummary(importSummary, FailedItem.TRACKEDENTITYINSTANCE, trackedEntityInstance.getLocalId());
                if (ImportSummary.SUCCESS.equals(importSummary.getStatus()) ||
                        ImportSummary.OK.equals(importSummary.getStatus())) {

                    // change state and save trackedentityinstance

                    //trackedEntityInstance.setState(State.SYNCED);
                    trackedEntityInstance.setFromServer(true);
                    trackedEntityInstance.save();

                    clearFailedItem(FailedItem.TRACKEDENTITYINSTANCE, trackedEntityInstance.getLocalId());
                    UpdateTrackedEntityInstanceTimestamp(trackedEntityInstance, dhisApi);
                }
            }
        } catch (APIException apiException) {
            NetworkUtils.handleTrackedEntityInstanceSendException(apiException, trackedEntityInstance);
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private static boolean putTrackedEntityInstance(TrackedEntityInstance trackedEntityInstance, DhisApi dhisApi) throws APIException {
        try {
            Response response = null;
            if(!dhisApi.putTrackedEntityInstance(trackedEntityInstance.getTrackedEntityInstance(), trackedEntityInstance).isExecuted()){
                response = dhisApi.putTrackedEntityInstance(trackedEntityInstance.getTrackedEntityInstance(), trackedEntityInstance).execute();
            }

            if (response.code() == 200) {
                ImportSummary importSummary = getImportSummary(response);
                handleImportSummary(importSummary, FailedItem.TRACKEDENTITYINSTANCE, trackedEntityInstance.getLocalId());
                if (ImportSummary.SUCCESS.equals(importSummary.getStatus()) ||
                        ImportSummary.OK.equals(importSummary.getStatus())) {
                    //trackedentityinstance.setState(State.SYNCED);
                    trackedEntityInstance.setFromServer(true);
                    trackedEntityInstance.save();
                    clearFailedItem(FailedItem.TRACKEDENTITYINSTANCE, trackedEntityInstance.getLocalId());
                    UpdateTrackedEntityInstanceTimestamp(trackedEntityInstance, dhisApi);
                }
            }
        } catch (APIException apiException) {
            NetworkUtils.handleTrackedEntityInstanceSendException(apiException, trackedEntityInstance);
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private static void updateTrackedEntityInstanceReferences(long localId, String newTrackedEntityInstanceReference, String oldTempTrackedEntityInstanceReference) {
        //update references with uid received from server
        new Update(TrackedEntityAttributeValue.class).set(Condition.column
                (TrackedEntityAttributeValue$Table.TRACKEDENTITYINSTANCEID).is
                (newTrackedEntityInstanceReference)).where(Condition.column(TrackedEntityAttributeValue$Table.LOCALTRACKEDENTITYINSTANCEID).is(localId)).async().execute();

        new Update(Event.class).set(Condition.column(Event$Table.
                TRACKEDENTITYINSTANCE).is(newTrackedEntityInstanceReference)).where(Condition.
                column(Event$Table.TRACKEDENTITYINSTANCE).is(oldTempTrackedEntityInstanceReference)).async().execute();

        new Update(Enrollment.class).set(Condition.column
                (Enrollment$Table.TRACKEDENTITYINSTANCE).is(newTrackedEntityInstanceReference)).
                where(Condition.column(Enrollment$Table.TRACKEDENTITYINSTANCE).is
                        (oldTempTrackedEntityInstanceReference)).async().execute();

        long updated = new Update(Relationship.class).set(Condition.column(Relationship$Table.TRACKEDENTITYINSTANCEA
        ).is(newTrackedEntityInstanceReference)).where(Condition.
                column(Relationship$Table.TRACKEDENTITYINSTANCEA).is(oldTempTrackedEntityInstanceReference)).count();

        updated += new Update(Relationship.class).set(Condition.column(Relationship$Table.TRACKEDENTITYINSTANCEB
        ).is(newTrackedEntityInstanceReference)).where(Condition.
                column(Relationship$Table.TRACKEDENTITYINSTANCEB).is(oldTempTrackedEntityInstanceReference)).count();

        Log.d(CLASS_TAG, "updated relationships: " + updated);

                    /* mechanism for triggering updating of relationships
                    * a relationship can only be uploaded if both involved teis are sent to server
                    * and have a valid UID.
                    * So, we check if this tei was just updated with a valid reference, and if there now
                    * exist >0 relationships that are valid. If >0 relationship is valid, it
                    * should get uploaded, as it is the first time it has been valid. */
        boolean hasValidRelationship = false;
        if (Utils.isLocal(oldTempTrackedEntityInstanceReference)) {
            List<Relationship> teiIsB = new Select().from(Relationship.class).where(Condition.column(Relationship$Table.TRACKEDENTITYINSTANCEB).is(newTrackedEntityInstanceReference)).queryList();
            List<Relationship> teiIsA = new Select().from(Relationship.class).where(Condition.column(Relationship$Table.TRACKEDENTITYINSTANCEA).is(newTrackedEntityInstanceReference)).queryList();
            if (teiIsB != null) {
                for (Relationship relationship : teiIsB) {
                    if (!Utils.isLocal(relationship.getTrackedEntityInstanceA())) {
                        hasValidRelationship = true;
                    }
                }
            }
            if (teiIsA != null) {
                for (Relationship relationship : teiIsA) {
                    if (!Utils.isLocal(relationship.getTrackedEntityInstanceB())) {
                        hasValidRelationship = true;
                    }
                }
            }
        }
        boolean fullySynced = !(hasValidRelationship && updated > 0);

        new Update(TrackedEntityInstance.class).set(Condition.column
                (TrackedEntityInstance$Table.TRACKEDENTITYINSTANCE).is
                (newTrackedEntityInstanceReference), Condition.column(TrackedEntityInstance$Table.FROMSERVER).is(fullySynced)).
                where(Condition.column(TrackedEntityInstance$Table.LOCALID).is(localId)).async().execute();
    }

    private static void UpdateTrackedEntityInstanceTimestamp(TrackedEntityInstance trackedEntityInstance, DhisApi dhisApi) throws APIException {
        try {
            final Map<String, String> QUERY_PARAMS = new HashMap<>();
            QUERY_PARAMS.put("fields", "created,lastUpdated");
            TrackedEntityInstance updatedTrackedEntityInstance = null;
            if(!dhisApi.getTrackedEntityInstance(trackedEntityInstance.getTrackedEntityInstance(), QUERY_PARAMS).isExecuted()){
                updatedTrackedEntityInstance = dhisApi
                        .getTrackedEntityInstance(trackedEntityInstance.getTrackedEntityInstance(), QUERY_PARAMS).execute().body();
            }

            // merging updated timestamp to local trackedentityinstance model
            trackedEntityInstance.setCreated(updatedTrackedEntityInstance.getCreated());
            trackedEntityInstance.setLastUpdated(updatedTrackedEntityInstance.getLastUpdated());
            trackedEntityInstance.save();
        } catch (APIException apiException) {
            NetworkUtils.handleApiException(apiException);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    static void clearFailedItem(String type, long id) {
        FailedItem item = TrackerController.getFailedItem(type, id);
        if (item != null) {
            item.async().delete();
        }
    }

    private static void handleImportSummary(ImportSummary importSummary, String type, long id) {
        try {
            if (ImportSummary.ERROR.equals(importSummary.getStatus())) {
                Log.d(CLASS_TAG, "failed.. ");
                NetworkUtils.handleImportSummaryError(importSummary, type, 200, id);
            }
        } catch (Exception e) {
            Log.e(CLASS_TAG, "Unable to process import summary", e);
        }
    }

    private static List<ImportSummary> getImportSummaries(Response response) {
        List<ImportSummary> importSummaries = new ArrayList<>();

        try {
            JsonNode node = DhisController.getInstance().getObjectMapper()
                    .readTree(new StringConverter()
                            .convert(response.body()).toString());
            if (node == null) {
                return null;
            }
            ApiResponse apiResponse = null;
            String body = new StringConverter().convert(response.body()).toString();
            Log.d(CLASS_TAG, body);
            apiResponse = DhisController.getInstance().getObjectMapper().
                    readValue(body, ApiResponse.class);
            if (apiResponse != null && apiResponse.getImportSummaries() != null && !apiResponse.getImportSummaries().isEmpty()) {
                return (apiResponse.getImportSummaries());
            }

        } catch (ConversionException e) {
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return importSummaries;
    }

    private static ImportSummary getImportSummary(Response response) {
        //because the web api almost randomly gives the responses in different forms, this
        //method checks which one it is that is being returned, and parses accordingly.
        try {
            String body = ((ResponseBody)response.body()).string();
            JsonNode node = DhisController.getInstance().getObjectMapper().
                    readTree(body);
            if (node == null) {
                return null;
            }
            if (node.has("response")) {
                return getPutImportSummary(body);
            } else {
                return getPostImportSummary(body);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ConversionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static ImportSummary getPostImportSummary(String body) {
        ImportSummary importSummary = null;
        try {
            Log.d(CLASS_TAG, body);
            importSummary = DhisController.getInstance().getObjectMapper().
                    readValue(body, ImportSummary.class);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ConversionException e) {
            e.printStackTrace();
        }
        return importSummary;
    }

    private static ImportSummary getPutImportSummary(String body) {
        ApiResponse apiResponse = null;
        try {
            Log.d(CLASS_TAG, body);
            apiResponse = DhisController.getInstance().getObjectMapper().
                    readValue(body, ApiResponse.class);
            if (apiResponse != null && apiResponse.getImportSummaries() != null && !apiResponse.getImportSummaries().isEmpty()) {
                return (apiResponse.getImportSummaries().get(0));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ConversionException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void deleteLocallyDeletedEvents(DhisApi dhisApi) {
        List<Event> events = TrackerController.getDeletedEvents();
        Log.d(CLASS_TAG, "got this many events to be removed:" + events.size());
        for (Event event : events) {
            deleteEvent(dhisApi, event);
        }
    }


    static void deleteEvent(DhisApi dhisApi, Event event) throws APIException {
        if (event == null) {
            return;
        }
        try {
            Response response = dhisApi.deleteEvent(event.getUid()).execute();
            if (response.code() == 200) {
                ImportSummary importSummary = getImportSummary(response);
                handleImportSummary(importSummary, FailedItem.EVENT, event.getLocalId());
                if (ImportSummary.SUCCESS.equals(importSummary.getStatus()) ||
                        ImportSummary.OK.equals(importSummary.getStatus())) {
                    // delete locally event
                    event.delete();
                    clearFailedItem(FailedItem.EVENT, event.getLocalId());
                }
            }
        } catch (APIException apiException) {
            NetworkUtils.handleEventSendException(apiException, event);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
