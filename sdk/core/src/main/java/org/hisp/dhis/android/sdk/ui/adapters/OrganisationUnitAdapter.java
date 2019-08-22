package org.hisp.dhis.android.sdk.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import org.hisp.dhis.android.sdk.R;
import org.hisp.dhis.android.sdk.persistence.models.OrganUnit;

import java.util.List;

public class OrganisationUnitAdapter extends ArrayAdapter<OrganUnit> {

    List<OrganUnit> organUnits;
    int layoutId = -1;
    int selectedPosition = -1;

    public OrganisationUnitAdapter(Context context, int layoutId, List<OrganUnit> organUnits){
        super(context, layoutId);
        this.layoutId = layoutId;
        this.organUnits = organUnits;
    }

    public void resetData(){
        if(organUnits != null) {
            organUnits.clear();
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return organUnits.size();
    }

    @Override
    public OrganUnit getItem(int position) {
        return organUnits.get(position);
    }

    @Override
    public int getPosition(OrganUnit item) {
        return super.getPosition(item);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(layoutId, parent, false);
        }
        TextView tvTitle = view.findViewById(R.id.item);
        tvTitle.setTextColor(Color.TRANSPARENT);
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(layoutId, parent, false);
        }
        OrganUnit organUnit = organUnits.get(position);
        TextView tvTitle = view.findViewById(R.id.item);
        tvTitle.setText(organUnit.getDisplayName());
        return view;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }
}
