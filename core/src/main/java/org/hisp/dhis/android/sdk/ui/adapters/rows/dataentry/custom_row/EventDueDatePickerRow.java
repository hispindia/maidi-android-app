package org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.custom_row;

import android.app.DatePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.textfield.TextInputLayout;
import org.hisp.dhis.android.sdk.R;
import org.hisp.dhis.android.sdk.persistence.Dhis2Application;
import org.hisp.dhis.android.sdk.persistence.models.DataValue;
import org.hisp.dhis.android.sdk.persistence.models.Event;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.DataEntryRowTypes;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.Row;
import org.hisp.dhis.android.sdk.ui.fragments.dataentry.RowValueChangedEvent;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import static org.hisp.dhis.android.sdk.utils.StringUtils.isEmpty;

public class EventDueDatePickerRow extends Row {
    private static final String EMPTY_FIELD = "";
    private static final String DATE_FORMAT = "YYYY-MM-dd";
    private final Event mEvent;
    private final boolean mAllowDatesInFuture;

    public EventDueDatePickerRow(String label, Event event, boolean allowDatesInFuture) {
        this.mAllowDatesInFuture = allowDatesInFuture;
        mLabel = label;
        mEvent = event;
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

            holder = new DatePickerRowHolder(root, inflater.getContext(), mAllowDatesInFuture);


            root.setTag(holder);
            view = root;
        }

        if(!isEditable()) {
            holder.llDateTimeClicker.setEnabled(false);
        } else {
            holder.llDateTimeClicker.setEnabled(true);
        }
//      holder.detailedInfoButton.setOnClickListener(new OnDetailedInfoButtonClick(this));
//        holder.detailedInfoButton.setOnClickListener(new OnDetailedInfoButtonClick(this));
        holder.updateViews(mLabel, mEvent);
//
//        if(isDetailedInfoButtonHidden())
//        {
//
//            holder.detailedInfoButton.setVisibility(View.INVISIBLE);
//        }
//        else {
//            holder.detailedInfoButton.setVisibility(View.VISIBLE);
//        }


        return view;
    }

    @Override
    public int getViewType() {
        return DataEntryRowTypes.EVENT_DATE.ordinal();
    }

    private class DatePickerRowHolder {
        final EditText etValue;
        final TextInputLayout tilValue;
        final LinearLayout llDateTimeClicker;
        //        final View detailedInfoButton;
        final DateSetListener dateSetListener;
        final OnEditTextClickListener invokerListener;

        public DatePickerRowHolder(View root, Context context, boolean allowDatesInFuture) {
            etValue = (EditText) root.findViewById(R.id.row_date_time_picker_layout_et_value);
            tilValue = (TextInputLayout) root.findViewById(R.id.row_date_time_picker_layout_til_value);
            llDateTimeClicker = (LinearLayout) root.findViewById(R.id.row_date_time_picker_layout_ll_date_time_clicker);

            dateSetListener = new DateSetListener(etValue);
            invokerListener = new OnEditTextClickListener(context, dateSetListener, allowDatesInFuture);

            llDateTimeClicker.setOnClickListener(invokerListener);
        }

        public void updateViews(String label, Event event) {
            dateSetListener.setEvent(event);

            String eventDate = null;
            if (event != null && event.getEventDate() != null
                    && !isEmpty(event.getEventDate())) {
                DateTime eventDateTime = DateTime.parse(event.getEventDate());
                eventDate = eventDateTime.toString(DATE_FORMAT);
            }

            tilValue.setHint(label);
            etValue.setText(eventDate);
        }

    }

    private static class OnEditTextClickListener implements View.OnClickListener {
        private final Context context;
        private final DateSetListener listener;
        private final boolean allowDatesInFuture;

        public OnEditTextClickListener(Context context, DateSetListener listener, boolean allowDatesInFuture) {
            this.context = context;
            this.listener = listener;
            this.allowDatesInFuture = allowDatesInFuture;
        }

        @Override
        public void onClick(View view) {
            LocalDate currentDate = new LocalDate();
            DatePickerDialog picker = new DatePickerDialog(context, listener,
                    currentDate.getYear(), currentDate.getMonthOfYear() - 1, currentDate.getDayOfMonth());
            if(!allowDatesInFuture) {
                picker.getDatePicker().setMaxDate(DateTime.now().getMillis());
            }
            picker.show();
        }
    }

    private class DateSetListener implements DatePickerDialog.OnDateSetListener {
        private static final String DATE_FORMAT = "YYYY-MM-dd";
        private final EditText editText;
        private Event event;
        private DataValue value;

        public DateSetListener(EditText editText) {
            this.editText = editText;
        }

        public void setEvent(Event event) {
            this.event = event;
        }

        @Override
        public void onDateSet(DatePicker view, int year,
                              int monthOfYear, int dayOfMonth) {
            LocalDate date = new LocalDate(year, monthOfYear + 1, dayOfMonth);
            if (value == null) value = new DataValue();

            if (event.getEventDate() != null)
                value.setValue(event.getEventDate());

            String newValue = date.toString(DATE_FORMAT);
            editText.setText(newValue);

            if (!newValue.equals(value.getValue())) {
                value.setValue(newValue);
                event.setEventDate(value.getValue());
                Dhis2Application.getEventBus().post(new RowValueChangedEvent(value, DataEntryRowTypes.EVENT_DATE.toString()));
            }
        }
    }
}
