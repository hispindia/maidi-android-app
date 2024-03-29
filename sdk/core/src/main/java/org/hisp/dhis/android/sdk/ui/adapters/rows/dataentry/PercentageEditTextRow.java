package org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry;


import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.fragment.app.FragmentManager;
import org.hisp.dhis.android.sdk.R;
import org.hisp.dhis.android.sdk.persistence.models.BaseValue;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.autocompleterow.TextRow;

public class PercentageEditTextRow extends TextRow {
    private static String rowTypeTemp;

    public PercentageEditTextRow(String label, boolean mandatory, String warning,
            BaseValue baseValue,
            DataEntryRowTypes rowType) {
        mLabel = label;
        mMandatory = mandatory;
        mWarning = warning;
        mValue = baseValue;
        mRowType = rowType;

        if (!DataEntryRowTypes.PERCENTAGE.equals(rowType)) {
            throw new IllegalArgumentException("Unsupported row type");
        }
        checkNeedsForDescriptionButton();
    }

    @Override
    public int getViewType() {
        return DataEntryRowTypes.PERCENTAGE.ordinal();
    }

    @Override
    public View getView(FragmentManager fragmentManager, LayoutInflater inflater,
            View convertView, ViewGroup container) {
        View view;
        final ValueEntryHolder holder;

        if (convertView != null && convertView.getTag() instanceof ValueEntryHolder) {
            view = convertView;
            holder = (ValueEntryHolder) view.getTag();
            holder.listener.onRowReused();
        } else {
            View root = inflater.inflate(R.layout.listview_row_edit_text, container, false);
            TextView label = (TextView) root.findViewById(R.id.text_label);
            TextView mandatoryIndicator = (TextView) root.findViewById(R.id.mandatory_indicator);
            EditText editText = (EditText) root.findViewById(R.id.edit_text_row);
//            detailedInfoButton = root.findViewById(R.id.detailed_info_button_layout);


            editText.setInputType(InputType.TYPE_CLASS_NUMBER |
                    InputType.TYPE_NUMBER_FLAG_SIGNED);
            editText.setHint(R.string.enter_percentage);
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3), new MinMaxInputFilter(0, 100)});
            editText.setSingleLine(true);

            OnTextChangeListener listener = new OnTextChangeListener();
            listener.setRow(this);
            listener.setRowType(rowTypeTemp);
            holder = new ValueEntryHolder(label, mandatoryIndicator, editText, listener);
            holder.listener.setBaseValue(mValue);
            holder.editText.addTextChangedListener(listener);

            rowTypeTemp = mRowType.toString();
            root.setTag(holder);
            view = root;
        }

        //when recycling views we don't want to keep the focus on the edittext
        //holder.editText.clearFocus();

        if (!isEditable()) {
            holder.editText.setEnabled(false);
        } else {
            holder.editText.setEnabled(true);
        }

        holder.textLabel.setText(mLabel);
        holder.listener.setBaseValue(mValue);
//        holder.detailedInfoButton.setOnClickListener(new OnDetailedInfoButtonClick(this));

        holder.editText.setText(mValue.getValue());
        holder.editText.setSelection(holder.editText.getText().length());

//        if(isDetailedInfoButtonHidden()) {
//            holder.detailedInfoButton.setVisibility(View.INVISIBLE);
//        }
//        else {
//            holder.detailedInfoButton.setVisibility(View.VISIBLE);
//        }

        if (!mMandatory) {
            holder.mandatoryIndicator.setVisibility(View.GONE);
        } else {
            holder.mandatoryIndicator.setVisibility(View.VISIBLE);
        }
        if (isShouldNeverBeEdited()) {
            holder.editText.setEnabled(false);
        }
        holder.editText.setOnEditorActionListener(mOnEditorActionListener);

        return view;
    }



    public class MinMaxInputFilter implements InputFilter {
        /**
         * Minimum allowed value for the input.
         * Null means there is no minimum limit.
         */
        private Integer minAllowed;

        /**
         * Maximum allowed value for the input.
         * Null means there is no maximum limit.
         */
        private Integer maxAllowed;

        public MinMaxInputFilter(Integer min){
            this.minAllowed=min;
        }

        public MinMaxInputFilter(Integer min, Integer max){
            this(min);
            this.maxAllowed=max;
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            try {
                // Remove the string out of destination that is to be replaced
                String newVal = dest.toString().substring(0, dstart) + dest.toString().substring(dend, dest.toString().length());
                // Add the new string in
                newVal = newVal.substring(0, dstart) + source.toString() + newVal.substring(dstart, newVal.length());
                if(newVal.length()>1 && newVal.startsWith("0")) {
                    return "";
                }
                int input = Integer.parseInt(newVal);
                if (inRange(input)) {
                    return null;
                }
            }catch (NumberFormatException nfe) {
            }
            return "";
        }

        /**
         * Checks if the value is between the specified range.
         *
         * @param value
         * @return
         */
        public boolean inRange(Integer value){
            boolean isMinOk=true;
            boolean isMaxOk=true;
            //No bounds -> ok
            if(minAllowed==null && maxAllowed==null){
                return true;
            }
            //Check minimum
            if(minAllowed!=null){
                if(value==null){
                    isMinOk=false;
                }else{
                    isMinOk=minAllowed<=value;
                }
            }
            //Check maximum
            if(maxAllowed!=null){
                if(value==null){
                    isMaxOk=false;
                }else{
                    isMaxOk=value<=maxAllowed;
                }
            }
            return isMinOk && isMaxOk;
        }
    }
}
