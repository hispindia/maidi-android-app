package org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.FragmentManager;
import org.hisp.dhis.android.sdk.R;
import org.hisp.dhis.android.sdk.ui.views.FontTextView;

public class SectionRow extends Row{

    public SectionRow(String label){
        mLabel = label;
    }

    @Override
    public View getView(FragmentManager fragmentManager, LayoutInflater inflater, View convertView, ViewGroup container) {
        View root = inflater.inflate(R.layout.listview_row_section, container, false);
        FontTextView tvLabel = (FontTextView) root.findViewById(R.id.row_section_title);
        tvLabel.setText(mLabel);

        return root;
    }

    @Override
    public int getViewType() {
        return 0;
    }
}
