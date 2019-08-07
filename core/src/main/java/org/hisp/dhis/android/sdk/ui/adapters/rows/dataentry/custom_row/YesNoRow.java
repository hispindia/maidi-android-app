package org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.custom_row;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.fragment.app.FragmentManager;
import org.hisp.dhis.android.sdk.R;
import org.hisp.dhis.android.sdk.persistence.Dhis2Application;
import org.hisp.dhis.android.sdk.persistence.models.BaseValue;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.DataEntryRowTypes;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.RadioButtonsRow;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.Row;
import org.hisp.dhis.android.sdk.ui.fragments.dataentry.RowValueChangedEvent;

public class YesNoRow extends Row {
    private static final String EMPTY_FIELD = "";
    private static final String TRUE = "true";
    private static final String FALSE = "false";

    public YesNoRow(String label, boolean mandatory, String warning, BaseValue baseValue) {
        mLabel = label;
        mMandatory = mandatory;
        mValue = baseValue;
        mRowType = DataEntryRowTypes.BOOLEAN;
        mWarning = warning;

        checkNeedsForDescriptionButton();
    }

    @Override
    public View getView(FragmentManager fragmentManager, LayoutInflater inflater,
                        View convertView, ViewGroup container) {
        View view;
        BooleanRowHolder holder;

        if (convertView != null && convertView.getTag() instanceof BooleanRowHolder) {
            view = convertView;
            holder = (BooleanRowHolder) convertView.getTag();
            RadioGroup.OnCheckedChangeListener listener = holder.radioGroupCheckedChangeListener;
            holder.radioGroup.setOnCheckedChangeListener(null);
            holder.radioGroup.clearCheck();
            holder.radioGroup.setOnCheckedChangeListener(listener);
            holder.updateViews(mLabel, mValue);
        } else {
            View root = inflater.inflate(
                    R.layout.row_choose_picker_layout, container, false);
            TextView label = (TextView)
                    root.findViewById(R.id.row_choose_picker_layout_tv_title);
            RadioButton trueButton = (RadioButton)
                    root.findViewById(R.id.row_choose_picker_rb_true);
            RadioButton falseButton = (RadioButton)
                    root.findViewById(R.id.row_choose_picker_rb_false);
            RadioGroup radioGroup = (RadioGroup) root.findViewById(R.id.row_choose_picker_layout_rg_chooser);
//            detailedInfoButton =
//                    root.findViewById(R.id.detailed_info_button_layout);


            if (DataEntryRowTypes.BOOLEAN.equals(mRowType)) {
                trueButton.setText(R.string.yes);
                falseButton.setText(R.string.no);
            }

            OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener();
            holder = new BooleanRowHolder(mRowType, label, trueButton, falseButton, radioGroup, onCheckedChangeListener);

            holder.radioGroup.setOnCheckedChangeListener(onCheckedChangeListener);

            root.setTag(holder);
            view = root;
        }

        if(!isEditable()) {
            holder.trueButton.setEnabled(false);
            holder.falseButton.setEnabled(false);
        } else {
            holder.trueButton.setEnabled(true);
            holder.falseButton.setEnabled(true);
        }

//        holder.detailedInfoButton.setOnClickListener(new OnDetailedInfoButtonClick(this));
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
        return DataEntryRowTypes.BOOLEAN.ordinal();
    }


    private static class BooleanRowHolder {
        final TextView textLabel;
        final CompoundButton trueButton;
        final CompoundButton falseButton;
        final RadioGroup radioGroup;
        //        final View detailedInfoButton;
        final OnCheckedChangeListener radioGroupCheckedChangeListener;
        final DataEntryRowTypes type;

        public BooleanRowHolder(DataEntryRowTypes type, TextView textLabel, CompoundButton trueButton,
                                CompoundButton falseButton, RadioGroup radioGroup, OnCheckedChangeListener radioGroupCheckedChangeListener) {
            this.type = type;
            this.textLabel = textLabel;
            this.trueButton = trueButton;
            this.falseButton = falseButton;
            this.radioGroup = radioGroup;
            this.radioGroupCheckedChangeListener = radioGroupCheckedChangeListener;
        }

        public void updateViews(String label, BaseValue baseValue) {
            textLabel.setText(label);

            radioGroupCheckedChangeListener.setBaseValue(baseValue);

            String value = baseValue.getValue();
            if (DataEntryRowTypes.BOOLEAN.equals(type)) {
                if (TRUE.equalsIgnoreCase(value)) {
                    trueButton.setChecked(true);
                } else if (FALSE.equalsIgnoreCase(value)) {
                    falseButton.setChecked(true);
                }
            }
        }
    }

    private static class OnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {

        BaseValue baseValue;

        public BaseValue getBaseValue() {
            return baseValue;
        }

        public void setBaseValue(BaseValue baseValue) {
            this.baseValue = baseValue;
        }

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            String newValue;

            if (checkedId == R.id.row_choose_picker_rb_true) {
                newValue = TRUE;
            } else if (checkedId == R.id.row_choose_picker_rb_false) {
                newValue = FALSE;
            } else {
                newValue = EMPTY_FIELD;
            }
            if(!newValue.equals(baseValue.getValue())) {
                baseValue.setValue(newValue);
                Dhis2Application.getEventBus().post(new RowValueChangedEvent(baseValue, DataEntryRowTypes.BOOLEAN.toString()));
            }
            this.baseValue.setValue(baseValue.getValue());
        }
    }
}
