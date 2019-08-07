package org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry;

import android.content.Context;
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis.android.sdk.persistence.models.BaseValue;
import org.hisp.dhis.android.sdk.persistence.models.Option;
import org.hisp.dhis.android.sdk.persistence.models.OptionSet;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.custom_row.*;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.custom_row.CheckBoxRow;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.custom_row.DatePickerRow;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.custom_row.EditTextRow;
import org.hisp.dhis.android.sdk.utils.api.ValueType;

import java.util.List;

/**
 * Created by katana on 21/10/16.
 */

public class DataEntryRowFactory {
    public static Row createDataEntryView(Context context, String mOrganUnitId, boolean mandatory, boolean allowFutureDate,
                                          String optionSetId, String rowName, BaseValue baseValue,
                                          ValueType valueType, boolean editable,
                                          boolean shouldNeverBeEdited, boolean dataEntryMethod) {
        Row row;
        String trackedEntityAttributeName = rowName;
        if (optionSetId != null) {
            OptionSet optionSet = MetaDataController.getOptionSet(optionSetId);
            if (optionSet == null) {
                row = new EditTextRow(trackedEntityAttributeName, mandatory, null, baseValue, DataEntryRowTypes.TEXT);
            } else {
                List<Option> options = MetaDataController.getOptions(optionSetId);

                if (isDataEntryRadioButtons(dataEntryMethod, options))
                    row = new RadioButtonOptionRow(trackedEntityAttributeName, mandatory, null, baseValue, options);
                else
                    row = new OptionRow(context, trackedEntityAttributeName, baseValue, options);
            }
        } else if (valueType.equals(ValueType.BOOLEAN)) {
            row = new YesNoRow(trackedEntityAttributeName, mandatory, null, baseValue);
        } else if (valueType.equals(ValueType.TRUE_ONLY)) {
            row = new CheckBoxRow(trackedEntityAttributeName, mandatory, null, baseValue);
        } else if (valueType.equals(ValueType.DATE) || valueType.equals(ValueType.AGE)) {
            row = new DatePickerRow(trackedEntityAttributeName, mandatory, null, baseValue, allowFutureDate);
        } else if (valueType.equals(ValueType.TIME)) {
            row = new TimePickerRow(trackedEntityAttributeName, mandatory, null, baseValue, allowFutureDate);
        } else if (valueType.equals(ValueType.IMAGE)) {
            row = new InvalidEditTextRow(trackedEntityAttributeName, mandatory, null, baseValue, DataEntryRowTypes.INVALID_DATA_ENTRY);
        } else if (valueType.equals(ValueType.ORGANISATION_UNIT)) {
            row = new ChooseOrganisationUnitRow(context, mOrganUnitId, trackedEntityAttributeName, mandatory, null, baseValue, DataEntryRowTypes.ORGANISATION_UNIT);
            //row = new YesNoRow(trackedEntityAttributeName, mandatory, null, baseValue/*, DataEntryRowTypes.INVALID_DATA_ENTRY*/);
        } else if (valueType.equals(ValueType.SECTION)) {
            row = new SectionRow(trackedEntityAttributeName, mandatory, null);
        } else {
            row = new EditTextRow(trackedEntityAttributeName, mandatory, null, baseValue, DataEntryRowTypes.TEXT);
        }
        row.setEditable(editable);
        row.setShouldNeverBeEdited(shouldNeverBeEdited);
        return row;
    }

    private static boolean isDataEntryRadioButtons(boolean dataEntryMethod, List<Option> options) {
        return dataEntryMethod && options.size() < 8;
    }

}
