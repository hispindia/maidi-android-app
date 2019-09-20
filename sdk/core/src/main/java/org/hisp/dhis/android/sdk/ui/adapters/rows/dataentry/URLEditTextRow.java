package org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry;


import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.fragment.app.FragmentManager;
import org.hisp.dhis.android.sdk.R;
import org.hisp.dhis.android.sdk.persistence.models.BaseValue;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.autocompleterow.TextRow;

public class URLEditTextRow extends TextRow {
    private static String rowTypeTemp;

    public URLEditTextRow(String label, boolean mandatory, String warning,
            BaseValue baseValue,
            DataEntryRowTypes rowType) {
        mLabel = label;
        mMandatory = mandatory;
        mWarning = warning;
        mValue = baseValue;
        mRowType = rowType;

        if (!DataEntryRowTypes.URL.equals(rowType)) {
            throw new IllegalArgumentException("Unsupported row type");
        }
        checkNeedsForDescriptionButton();
    }

    @Override
    public int getViewType() {
        return DataEntryRowTypes.URL.ordinal();
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

            editText.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
            editText.setHint(R.string.enter_url);
            editText.setSingleLine(true);

            URLWatcher listener = new URLWatcher(editText);
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

        holder.editText.setOnEditorActionListener(mOnEditorActionListener);

        if (isShouldNeverBeEdited()) {
            holder.editText.setEnabled(false);
        }
        return view;
    }

    private class URLWatcher extends OnTextChangeListener{
        final private EditText mEditText;
        //final private TextView mErrorLabel;


        public URLWatcher(EditText editText/*, TextView errorLabel*/) {
            super();
            mEditText = editText;
            //mErrorLabel = errorLabel;
        }

        public void afterTextChanged(Editable s) {
            super.afterTextChanged(s);
            String text = mEditText.getText().toString();
            validateURL(text);
        }

        public void validateURL(String url) {
            //String regExp = "/^(http|https):\\/\\/[a-z0-9]+([\\-\\.]{1}[a-z0-9]+)*\\.[a-z]{2,6}"
            //        + "(:[0-9]{1,5})?(\\/.*)?$/";
            String regExp = "^(http|https)://[a-z0-9]+([-.][a-z0-9]+)*[.][a-z]{2,6}(:[0-9]{1,5})?([/].*)?$";
                if(!url.matches(regExp) && url.length()>0){
                    setError(R.string.error_url);
                }else{
                    setError(null);
                }
        }

        private void setError(Integer stringId) {
            Log.e("URL Input Exception", "has error");
            /*if(stringId == null) {
                mErrorLabel.setVisibility(View.GONE);
                mErrorLabel.setText("");
            }else{
                mErrorLabel.setVisibility(View.VISIBLE);
                mErrorLabel.setText(stringId);
            }
            mErrorStringId = stringId;*/
        }
    }
}
