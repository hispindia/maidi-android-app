package org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.custom_row;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.textfield.TextInputLayout;
import org.hisp.dhis.android.sdk.R;
import org.hisp.dhis.android.sdk.persistence.models.BaseValue;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.DataEntryRowTypes;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.Row;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.autocompleterow.TextRow;

public class SectionRow extends Row {

    public SectionRow(String label, boolean mandatory, String warning) {
        mLabel = label;
        mMandatory = mandatory;
        mWarning = warning;

        checkNeedsForDescriptionButton();
    }

    @Override
    public int getViewType() {
        return DataEntryRowTypes.SECTION.ordinal();
    }

    @Override
    public View getView(FragmentManager fragmentManager, LayoutInflater inflater,
                        View convertView, ViewGroup container) {
        View view;

        if (convertView != null && convertView.getTag() instanceof ValueEntryHolder) {
            view = convertView;
        } else {
            View root = inflater.inflate(R.layout.row_section_layout, container, false);
            TextView tvTitle = (TextView) root.findViewById(R.id.row_section_layout_tv_title);
            tvTitle.setText(mLabel);
            view = root;
        }

        return view;
    }
}
