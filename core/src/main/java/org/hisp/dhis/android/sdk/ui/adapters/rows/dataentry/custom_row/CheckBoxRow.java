package org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.custom_row;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import androidx.fragment.app.FragmentManager;
import org.hisp.dhis.android.sdk.R;
import org.hisp.dhis.android.sdk.persistence.Dhis2Application;
import org.hisp.dhis.android.sdk.persistence.models.BaseValue;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.DataEntryRowTypes;
import org.hisp.dhis.android.sdk.ui.fragments.dataentry.RowValueChangedEvent;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.Row;

import static android.text.TextUtils.isEmpty;

public class CheckBoxRow extends Row {
    private static final String TRUE = "true";
    private static final String EMPTY_FIELD = "";

    private final String mLabel;

    public CheckBoxRow(String label, boolean mandatory, String warning, BaseValue mValue) {
        mLabel = label;
        this.mValue = mValue;
        this.mWarning = warning;
        this.mMandatory = mandatory;

        checkNeedsForDescriptionButton();
    }

    @Override
    public View getView(FragmentManager fragmentManager, LayoutInflater inflater,
                        View convertView, ViewGroup container) {
        View view;
        CheckBoxHolder holder;

        if (convertView != null && convertView.getTag() instanceof CheckBoxHolder) {
            view = convertView;
            holder = (CheckBoxHolder) view.getTag();
        } else {
            View root = inflater.inflate(R.layout.row_checkbox_layout, container, false);
            TextView textLabel = (TextView) root.findViewById(R.id.row_checkbox_tv_title);
            CheckBox checkBox = (CheckBox) root.findViewById(R.id.row_checkbox_cb_checkbox);
//            detailedInfoButton = root.findViewById(R.id.detailed_info_button_layout);

            CheckBoxListener listener = new CheckBoxListener();
            OnCheckBoxRowClickListener onCheckBoxRowClickListener = new OnCheckBoxRowClickListener();
            holder = new CheckBoxHolder(root, textLabel, checkBox ,listener, onCheckBoxRowClickListener);

            holder.checkBox.setOnCheckedChangeListener(holder.listener);
            holder.rootView.setOnClickListener(holder.onCheckBoxRowClickListener);
//            holder.detailedInfoButton.setOnClickListener(new OnDetailedInfoButtonClick(this));


            root.setTag(holder);
            view = root;
        }

        if(!isEditable()) {
            holder.checkBox.setEnabled(false);
            holder.textLabel.setEnabled(false);
        } else {
            holder.textLabel.setEnabled(true);
            holder.checkBox.setEnabled(true);
        }
        holder.textLabel.setText(mLabel);
        holder.listener.setValue(mValue);

        holder.onCheckBoxRowClickListener.setCheckBox(holder.checkBox);
        holder.onCheckBoxRowClickListener.setEditable(holder.checkBox.isEnabled());

        String stringValue = mValue.getValue();
        if (TRUE.equalsIgnoreCase(stringValue)) {
            holder.checkBox.setChecked(true);
        } else if (isEmpty(stringValue)) {
            holder.checkBox.setChecked(false);
        }

//        if(isDetailedInfoButtonHidden()) {
//            holder.detailedInfoButton.setVisibility(View.INVISIBLE);
//        }
//        else {
//            holder.detailedInfoButton.setVisibility(View.VISIBLE);
//        }

        return view;
    }

    @Override
    public int getViewType() {
        return DataEntryRowTypes.TRUE_ONLY.ordinal();
    }


    private static class CheckBoxListener implements CompoundButton.OnCheckedChangeListener {
        private BaseValue value;

        public void setValue(BaseValue value) {
            this.value = value;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            String newValue;
            if(isChecked) {
                newValue = TRUE;
            } else {
                newValue = EMPTY_FIELD;
            }

            if(!newValue.toString().equals(value.getValue())) {
                value.setValue(newValue);
                Dhis2Application.getEventBus().post(new RowValueChangedEvent(value, DataEntryRowTypes.TRUE_ONLY.toString()));
            }

        }
    }

    private static class OnCheckBoxRowClickListener implements View.OnClickListener {
        CheckBox checkBox;
        boolean editable;

        public void setEditable(boolean editable) {
            this.editable = editable;
        }

        public void setCheckBox(CheckBox checkBox) {
            this.checkBox = checkBox;
        }

        @Override
        public void onClick(View view) {
            if(editable) {
                checkBox.setChecked(!checkBox.isChecked());
            }
        }
    }

    private static class CheckBoxHolder {
        final View rootView;
        final TextView textLabel;
        final CheckBox checkBox;
        final CheckBoxListener listener;
        final OnCheckBoxRowClickListener onCheckBoxRowClickListener;

        public CheckBoxHolder(View rootView, TextView textLabel, CheckBox checkBox,
                              CheckBoxListener listener,
                              OnCheckBoxRowClickListener onCheckBoxRowClickListener) {
            this.rootView = rootView;
            this.textLabel = textLabel;
            this.checkBox = checkBox;
            this.listener = listener;
            this.onCheckBoxRowClickListener = onCheckBoxRowClickListener;
        }
    }


}
