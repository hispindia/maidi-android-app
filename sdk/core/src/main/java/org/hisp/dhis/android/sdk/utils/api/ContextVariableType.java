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

package org.hisp.dhis.android.sdk.utils.api;

public enum ContextVariableType {
    CURRENT_DATE( "current_date" ),
    EVENT_DATE( "event_date" ),
    DUE_DATE( "due_date" ),
    EVENT_COUNT( "event_count" ),
    ENROLLMENT_DATE( "enrollment_date" ),
    ENROLLMENT_ID( "enrollment_id" ),
    EVENT_ID( "event_id" ),
    INCIDENT_DATE( "incident_date" ),
    ENROLLMENT_COUNT( "enrollment_count" ),
    ORG_UNIT_CODE( "orgunit_code" ),
    TEI_COUNT( "tei_count" );

    final String value;

    private ContextVariableType(String value)
    {
        this.value = value;
    }

    public static ContextVariableType fromValue( String value )
    {
        for ( ContextVariableType type : ContextVariableType.values() )
        {
            if ( type.value.equalsIgnoreCase( value ) )
            {
                return type;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return value;
    }
}
