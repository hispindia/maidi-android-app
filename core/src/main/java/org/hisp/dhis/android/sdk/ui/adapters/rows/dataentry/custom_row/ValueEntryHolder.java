package org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.custom_row;


import android.text.Editable;
import android.widget.EditText;
import android.widget.TextView;
import com.google.android.material.textfield.TextInputLayout;
import org.hisp.dhis.android.sdk.persistence.Dhis2Application;
import org.hisp.dhis.android.sdk.persistence.models.BaseValue;
import org.hisp.dhis.android.sdk.ui.adapters.rows.AbsTextWatcher;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.Row;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.RunProgramRulesDelayedDispatcher;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.RunProgramRulesEvent;
import org.hisp.dhis.android.sdk.ui.fragments.dataentry.RowValueChangedEvent;

import static org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.AbsEnrollmentDatePickerRow.EMPTY_FIELD;

public class ValueEntryHolder {
    final EditText editText;
    //        final View detailedInfoButton;
    final OnTextChangeListener listener;
    final TextInputLayout tilValue;

    public ValueEntryHolder(EditText editText, TextInputLayout tilValue,
                            OnTextChangeListener listener) {
        this.editText = editText;
//            this.detailedInfoButton = detailedInfoButton;
        this.listener = listener;
        this.tilValue = tilValue;
    }
}

class OnTextChangeListener extends AbsTextWatcher {
    private BaseValue value;
    RunProgramRulesDelayedDispatcher runProgramRulesDelayedDispatcher = new RunProgramRulesDelayedDispatcher();
    Row row;
    String rowType;
    public void setRowType(String type){
        rowType = type;
    }
    public void setRow(Row row) {
        this.row = row;
    }

    public void setBaseValue(BaseValue value) {
        this.value = value;
    }

    public void onRowReused() {
        if (runProgramRulesDelayedDispatcher != null) {
            runProgramRulesDelayedDispatcher.dispatchNow();
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        String newValue = s != null ? s.toString() : EMPTY_FIELD;
        if (!newValue.equals(value.getValue())) {
            value.setValue(newValue);
            RowValueChangedEvent rowValueChangeEvent = new RowValueChangedEvent(value, rowType);
            rowValueChangeEvent.setRow(row);
            Dhis2Application.getEventBus().post(rowValueChangeEvent);
            runProgramRulesDelayedDispatcher.dispatchDelayed(new RunProgramRulesEvent(value));
        }
    }


}