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
package org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry;

import android.app.DatePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.fragment.app.FragmentManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import org.hisp.dhis.android.sdk.R;
import org.hisp.dhis.android.sdk.persistence.Dhis2Application;
import org.hisp.dhis.android.sdk.persistence.models.DataValue;
import org.hisp.dhis.android.sdk.persistence.models.Enrollment;
import org.hisp.dhis.android.sdk.ui.fragments.dataentry.RowValueChangedEvent;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import static android.text.TextUtils.isEmpty;


public class EnrollmentDatePickerRow extends AbsEnrollmentDatePickerRow {

    private Enrollment mEnrollment;
    private String mLabel;

    public EnrollmentDatePickerRow(String label, Enrollment enrollment) {
        super();

        this.mEnrollment = enrollment;
        this.mLabel = label;
    }

    @Override
    public View getView(FragmentManager fragmentManager, LayoutInflater inflater, View convertView, ViewGroup container) {
        View view;
        DatePickerRowHolder holder;

        if (convertView != null && convertView.getTag() instanceof DatePickerRowHolder) {
            view = convertView;
            holder = (DatePickerRowHolder) view.getTag();
        } else {
            View root = inflater.inflate(
                    R.layout.row_date_time_picker_layout, container, false);
//            detailedInfoButton = root.findViewById(R.id.detailed_info_button_layout); // need to keep reference
            holder = new DatePickerRowHolder(root, inflater.getContext());

            root.setTag(holder);
            view = root;
        }

        if (!isEditable()) {
            holder.tilValue.setEnabled(false);
            holder.etValue.setEnabled(false);
        } else {
            holder.tilValue.setEnabled(true);
            holder.etValue.setEnabled(true);
        }
//        holder.detailedInfoButton.setOnClickListener(new OnDetailedInfoButtonClick(this));
        holder.updateViews(mLabel, mEnrollment, mEnrollment.getEnrollmentDate());

//        if (isDetailedInfoButtonHidden())
//            holder.detailedInfoButton.setVisibility(View.INVISIBLE);

        return view;
    }

    private class DatePickerRowHolder {
        final TextInputLayout tilValue;
        final EditText etValue;
        final LinearLayout llDateTimeClicker;
        //        final View detailedInfoButton;
        final DateSetListener dateSetListener;
        final OnEditTextClickListener invokerListener;

        public DatePickerRowHolder(View root, Context context) {
            tilValue = (TextInputLayout) root.findViewById(R.id.row_date_time_picker_layout_til_value);
            etValue = (EditText) root.findViewById(R.id.row_date_time_picker_layout_et_value);
            llDateTimeClicker = (LinearLayout) root.findViewById(R.id.row_date_time_picker_layout_ll_date_time_clicker);
//            this.detailedInfoButton = detailedInfoButton;

            dateSetListener = new DateSetListener(etValue);
            invokerListener = new OnEditTextClickListener(context, dateSetListener);

            llDateTimeClicker.setOnClickListener(invokerListener);
        }

        public void updateViews(String label, Enrollment enrollment, String enrollmentDate) {
            dateSetListener.setEnrollment(enrollment);

            String eventDate = null;

            if (enrollment != null && enrollmentDate != null && !isEmpty(enrollmentDate)) {
                dateSetListener.setEnrollmentDate(enrollmentDate);
                DateTime incidentDateTime = DateTime.parse(enrollmentDate);
                eventDate = incidentDateTime.toString(DATE_FORMAT);
            }

            tilValue.setHint(label);
            etValue.setText(eventDate);
        }

    }

    private static class OnEditTextClickListener implements View.OnClickListener {
        private final Context context;
        private final DateSetListener listener;

        public OnEditTextClickListener(Context context,
                                       DateSetListener listener) {
            this.context = context;
            this.listener = listener;
        }

        @Override
        public void onClick(View view) {
            LocalDate currentDate = new LocalDate();
            DatePickerDialog picker = new DatePickerDialog(context, listener,
                    currentDate.getYear(), currentDate.getMonthOfYear() - 1, currentDate.getDayOfMonth());
            picker.getDatePicker().setMaxDate(DateTime.now().getMillis());
            picker.show();
        }
    }

    private class DateSetListener implements DatePickerDialog.OnDateSetListener {
        private static final String DATE_FORMAT = "YYYY-MM-dd";
        private final EditText textView;
        private Enrollment enrollment;
        private DataValue value;
        private String enrollmentDate;

        public DateSetListener(EditText textView) {
            this.textView = textView;
        }

        public void setEnrollment(Enrollment enrollment) {
            this.enrollment = enrollment;
        }

        public void setEnrollmentDate(String enrollmentDate) {
            this.enrollmentDate = enrollmentDate;
        }

        @Override
        public void onDateSet(DatePicker view, int year,
                              int monthOfYear, int dayOfMonth) {
            LocalDate date = new LocalDate(year, monthOfYear + 1, dayOfMonth);
            if (value == null) {
                value = new DataValue();
            }

            if (enrollmentDate != null) {
                value.setValue(enrollmentDate);
            }

            String newValue = date.toString(DATE_FORMAT);
            textView.setText(newValue);

            if (!newValue.equals(value.getValue())) {
                value.setValue(newValue);


                if (enrollmentDate != null) {
                    enrollment.setEnrollmentDate(value.getValue());
                }

                Dhis2Application.getEventBus().post(new RowValueChangedEvent(value, DataEntryRowTypes.ENROLLMENT_DATE.toString()));
            }

        }
    }

}