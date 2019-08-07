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

package org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.custom_row;

import android.app.DatePickerDialog;
import android.content.Context;
import android.widget.*;
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.google.android.material.textfield.TextInputLayout;
import org.hisp.dhis.android.sdk.R;
import org.hisp.dhis.android.sdk.persistence.Dhis2Application;
import org.hisp.dhis.android.sdk.persistence.models.BaseValue;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.DataEntryRowTypes;
import org.hisp.dhis.android.sdk.ui.fragments.dataentry.RowValueChangedEvent;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.Row;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DatePickerRow extends Row {
    private static final String EMPTY_FIELD = "";
    private final boolean mAllowDatesInFuture;
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    public DatePickerRow(String label, boolean mandatory, String warning, BaseValue value, boolean allowDatesInFuture) {
        mAllowDatesInFuture = allowDatesInFuture;
        mLabel = label;
        mMandatory = mandatory;
        mValue = value;
        mWarning = warning;

        checkNeedsForDescriptionButton();
    }

    @Override
    public View getView(FragmentManager fragmentManager, LayoutInflater inflater,
                        View convertView, ViewGroup container) {
        View view;
        DatePickerRowHolder holder;

        if (convertView != null && convertView.getTag() instanceof DatePickerRowHolder) {
            view = convertView;
            holder = (DatePickerRowHolder) view.getTag();
        } else {
            View root = inflater.inflate(
                    R.layout.row_date_time_picker_layout, container, false);
//            detailedInfoButton = root.findViewById(R.id.detailed_info_button_layout);

            holder = new DatePickerRowHolder(root, inflater.getContext(), mAllowDatesInFuture, mValue);


            root.setTag(holder);
            view = root;
        }

        if(!isEditable()) {
            holder.llDateTimeClicker.setEnabled(false);
        } else {
            holder.llDateTimeClicker.setEnabled(true);
        }
//      holder.detailedInfoButton.setOnClickListener(new OnDetailedInfoButtonClick(this));
        holder.updateViews(mLabel, mValue);

//        if(isDetailedInfoButtonHidden()) {
//            holder.detailedInfoButton.setVisibility(View.INVISIBLE);
//        }
//        else {
//            holder.detailedInfoButton.setVisibility(View.VISIBLE);
//        }

        /*if(mWarning == null) {
            holder.warningLabel.setVisibility(View.GONE);
        } else {
            holder.warningLabel.setVisibility(View.VISIBLE);
            holder.warningLabel.setText(mWarning);
        }

        if(mError == null) {
            holder.errorLabel.setVisibility(View.GONE);
        } else {
            holder.errorLabel.setVisibility(View.VISIBLE);
            holder.errorLabel.setText(mError);
        }

        if(!mMandatory) {
            holder.mandatoryIndicator.setVisibility(View.GONE);
        } else {
            holder.mandatoryIndicator.setVisibility(View.VISIBLE);
        }*/

        return view;
    }

    @Override
    public int getViewType() {
        return DataEntryRowTypes.DATE.ordinal();
    }


    private class DatePickerRowHolder {
        final EditText etValue;
        final TextInputLayout tilValue;
        final LinearLayout llDateTimeClicker;
//        final View detailedInfoButton;
        final DateSetListener dateSetListener;
        final OnEditTextClickListener invokerListener;
        DatePickerDialog picker;

        public DatePickerRowHolder(View root, Context context, boolean allowDatesInFuture, BaseValue baseValue) {
            etValue = (EditText) root.findViewById(R.id.row_date_time_picker_layout_et_value);
            tilValue = (TextInputLayout) root.findViewById(R.id.row_date_time_picker_layout_til_value);
            llDateTimeClicker = (LinearLayout) root.findViewById(R.id.row_date_time_picker_layout_ll_date_time_clicker);
//            this.detailedInfoButton = detailedInfoButton;

            dateSetListener = new DateSetListener(etValue);

            LocalDate currentDate = new LocalDate();
            picker = new DatePickerDialog(context, dateSetListener, currentDate.getYear(), currentDate.getMonthOfYear(), currentDate.getDayOfMonth());

            if(!allowDatesInFuture) {
                picker.getDatePicker().setMaxDate(DateTime.now().getMillis());
            }
            invokerListener = new OnEditTextClickListener(picker);
            llDateTimeClicker.setOnClickListener(invokerListener);
        }

        public void updateViews(String label, BaseValue baseValue) {
            dateSetListener.setBaseValue(baseValue);

            if(baseValue !=null && baseValue.getValue()!=null && !baseValue.equals("") && !baseValue.getValue().isEmpty()) {
                try {
                    Date date = new SimpleDateFormat("yyyy-MM-dd").parse(baseValue.getValue());
                    LocalDate currentDate = LocalDate.fromDateFields(date);
                    picker.updateDate(currentDate.getYear(), currentDate.getMonthOfYear() - 1 , currentDate.getDayOfMonth());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            tilValue.setHint(label);
            etValue.setText(baseValue.getValue());
        }
    }

    private static class OnEditTextClickListener implements OnClickListener {
        private DatePickerDialog datePickerDialog;

        public OnEditTextClickListener(DatePickerDialog datePickerDialog) {
            this.datePickerDialog = datePickerDialog;
        }

        @Override
        public void onClick(View view) {
            datePickerDialog.show();
        }
    }

    private static class ClearButtonListener implements OnClickListener {
        private final TextView textView;
        private BaseValue value;

        public ClearButtonListener(TextView textView) {
            this.textView = textView;
        }

        public void setBaseValue(BaseValue value) {
            this.value = value;
        }

        @Override
        public void onClick(View view) {
            textView.setText(EMPTY_FIELD);
            value.setValue(EMPTY_FIELD);
            Dhis2Application.getEventBus()
                    .post(new RowValueChangedEvent(value, DataEntryRowTypes.DATE.toString()));
        }
    }

    private static class DateSetListener implements DatePickerDialog.OnDateSetListener {
        private final EditText editText;
        private BaseValue value;

        public DateSetListener(EditText editText) {
            this.editText = editText;
        }

        public void setBaseValue(BaseValue value) {
            this.value = value;
        }

        @Override
        public void onDateSet(DatePicker view, int year,
                              int monthOfYear, int dayOfMonth) {
            LocalDate date = new LocalDate(year, monthOfYear + 1, dayOfMonth);
            String newValue = date.toString(DATE_FORMAT);
            editText.setText(newValue);
            value.setValue(newValue);
            System.out.println("DatePiker Saving value:" + newValue);
            value.setValue(newValue);
            Dhis2Application.getEventBus()
                    .post(new RowValueChangedEvent(value, DataEntryRowTypes.DATE.toString()));
        }

        public BaseValue getBaseValue() {
            return value;
        }
    }
}
