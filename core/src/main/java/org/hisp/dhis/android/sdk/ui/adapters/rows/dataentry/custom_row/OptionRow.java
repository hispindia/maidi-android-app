package org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.custom_row;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.textfield.TextInputLayout;
import org.hisp.dhis.android.sdk.R;
import org.hisp.dhis.android.sdk.persistence.Dhis2Application;
import org.hisp.dhis.android.sdk.persistence.models.BaseValue;
import org.hisp.dhis.android.sdk.persistence.models.Option;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.DataEntryRowTypes;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.Row;
import org.hisp.dhis.android.sdk.ui.fragments.dataentry.RowValueChangedEvent;

import java.util.ArrayList;
import java.util.List;

public class OptionRow extends Row {
    private static final String EMPTY_FIELD = "";

    private final Context context;
    private final List<Option> mOptions;

    private static int BASE_ID = 1000;

    public OptionRow(Context context, String label, BaseValue baseValue, List<Option> options) {

        this.context = context;
        mLabel = label;
        mValue = baseValue;

        mOptions = options;

        Option option = new Option();
        option.setSortIndex(-1);
        option.setCode("");
        option.setDisplayName(context.getResources().getString(R.string.blank_choose));

        mOptions.add(0, option);

        checkNeedsForDescriptionButton();
    }

    @Override
    public View getView(FragmentManager fragmentManager, LayoutInflater inflater,
                        View convertView, ViewGroup container) {
        View view;
        OptionRowHolder holder;

        if (convertView != null && convertView.getTag() instanceof OptionRowHolder) {
            view = convertView;
            holder = (OptionRowHolder) convertView.getTag();
        } else {
            View root = inflater.inflate(
                    R.layout.row_option_layout, container, false);

            Spinner spSpinner = (Spinner) root.findViewById(R.id.row_option_layout_sp_option);
            TextInputLayout tilValue = (TextInputLayout) root.findViewById(R.id.row_option_layout_til_value);
            EditText tietValue = (EditText) root.findViewById(R.id.row_option_layout_et_value);
            LinearLayout llClickable = (LinearLayout) root.findViewById(R.id.row_option_layout_ll_clickable);

            holder = new OptionRowHolder(context, mRowType, inflater, spSpinner, tilValue, tietValue, llClickable);

            root.setTag(holder);
            view = root;
        }

//        holder.detailedInfoButton.setOnClickListener(new OnDetailedInfoButtonClick(this));

        holder.updateViews(mLabel, mValue, mOptions, isEditable());

//        if(isDetailedInfoButtonHidden()) {
//            holder.detailedInfoButton.setVisibility(View.INVISIBLE);
//        }
//        else {
//            holder.detailedInfoButton.setVisibility(View.VISIBLE);
//        }

        /*if (mWarning == null) {
            holder.warningLabel.setVisibility(View.GONE);
        } else {
            holder.warningLabel.setVisibility(View.VISIBLE);
            holder.warningLabel.setText(mWarning);
        }

        if (mError == null) {
            holder.errorLabel.setVisibility(View.GONE);
        } else {
            holder.errorLabel.setVisibility(View.VISIBLE);
            holder.errorLabel.setText(mError);
        }

        if (!mMandatory) {
            holder.mandatoryIndicator.setVisibility(View.GONE);
        } else {
            holder.mandatoryIndicator.setVisibility(View.VISIBLE);
        }*/

        return view;
    }

    @Override
    public int getViewType() {
        return DataEntryRowTypes.OPTION_SET.ordinal();
    }


    private class OptionRowHolder {
        final Context context;
        final Spinner spSpinner;
        final TextInputLayout tilValue;
        final EditText tietValue;
        final LinearLayout llClickable;
        //        final View detailedInfoButton;
        final DataEntryRowTypes type;
        final LayoutInflater inflater;
        private BaseValue value;
        RowOptionAdapter adapter = null;

        public OptionRowHolder(Context context, DataEntryRowTypes type, LayoutInflater inflater, Spinner spSpinner, TextInputLayout tilValue,
                                   EditText tietValue, LinearLayout llClickable) {
            this.context = context;
            this.type = type;
            this.inflater = inflater;
            this.spSpinner = spSpinner;
            this.tilValue = tilValue;
            this.tietValue = tietValue;
            this.llClickable = llClickable;
        }

        public void setBaseValue(BaseValue value) {
            this.value = value;
        }

        public void updateViews(String label, BaseValue baseValue,
                                List<Option> options, boolean isEditable) {

            setBaseValue(baseValue);

            tilValue.setHint(label);
            adapter = new RowOptionAdapter(context, inflater, R.layout.item_dropdown, options);
            spSpinner.setAdapter(adapter);
            llClickable.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    spSpinner.performClick();
                }
            });
            spSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    String newValue;
                    Option option = (Option) spSpinner.getSelectedItem();

                    if (option.getSortIndex() != -1) {
                        tietValue.setText(option.getDisplayName());
                    } else {
                        tietValue.setText("");
                    }

                    newValue = option.getCode();

                    if (!newValue.equals(value.getValue())) {
                        value.setValue(newValue);

                        Dhis2Application.getEventBus().post(new RowValueChangedEvent(value,
                                DataEntryRowTypes.OPTION_SET.toString()));
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            for(int i = 0; i < options.size(); i++){
                if(options.get(i).getCode().equals(value.getValue())){
                    spSpinner.setSelection(i);
                    break;
                }
            }
        }
    }

    private class RowOptionAdapter extends ArrayAdapter<Option>{

        Context context;
        int layoutId;
        LayoutInflater inflater;
        List<Option> options;

        RowOptionAdapter(Context context, LayoutInflater inflater, int layoutId, List<Option> options){
            super(context, layoutId);
            this.context = context;
            this.inflater = inflater;
            this.layoutId = layoutId;
            this.options = options;
        }

        @Override
        public int getCount() {
            return options.size();
        }

        @Override
        public Option getItem(int position) {
            return options.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (convertView == null) {
                view = inflater.inflate(layoutId, parent, false);
            }
            TextView tvTitle = (TextView) view.findViewById(R.id.item);
            tvTitle.setTextColor(Color.TRANSPARENT);
            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (convertView == null) {
                view = inflater.inflate(layoutId, parent, false);
            }
            Option option = options.get(position);
            TextView tvTitle = (TextView) view.findViewById(R.id.item);
            tvTitle.setText(option.getDisplayName());
            return view;
        }
    }
}
