package org.hisp.dhis.android.sdk.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import org.hisp.dhis.android.sdk.R;
import org.hisp.dhis.android.sdk.ui.fragments.dataentry.DataEntryFragmentSection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ChangeSectionAdapter extends ArrayAdapter<DataEntryFragmentSection> {

    private Context context;
    private List<DataEntryFragmentSection> sections;

    public ChangeSectionAdapter(Context context, int resource, List<DataEntryFragmentSection> objects) {
        super(context, resource, objects);
        this.context = context;
        this.sections = objects;
    }

    @Override
    public int getCount() {
        return sections.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DataEntryFragmentSection section = sections.get(position);
        View row = convertView;
        if(row == null){
            row = LayoutInflater.from(context).inflate(R.layout.toolbar_spinner_item_actionbar, parent, false);
        }

        TextView tvTitle = row.findViewById(android.R.id.text1);
        tvTitle.setText(section.getLabel());

        return row;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        DataEntryFragmentSection section = sections.get(position);
        View row = convertView;
        if(row == null){
            row = LayoutInflater.from(context).inflate(R.layout.toolbar_spinner_item_dropdown, parent, false);
        }

        TextView tvTitle = row.findViewById(android.R.id.text1);
        tvTitle.setText(section.getLabel());

        return row;
    }

    public void swapData(List<DataEntryFragmentSection> data){
        if(sections == null){
            sections = new ArrayList();
        }
        sections.addAll(data);
        notifyDataSetChanged();
    }
}
