package org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.textfield.TextInputLayout;
import org.apache.commons.lang3.StringUtils;
import org.hisp.dhis.android.sdk.R;
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis.android.sdk.persistence.Dhis2Application;
import org.hisp.dhis.android.sdk.persistence.models.BaseValue;
import org.hisp.dhis.android.sdk.persistence.models.OrganUnit;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit;
import org.hisp.dhis.android.sdk.ui.adapters.rows.OrganisationUnitAdapter;
import org.hisp.dhis.android.sdk.ui.fragments.dataentry.RowValueChangedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WorkplanChooseOrganisationUnitRow extends Row {

    Context context;
    OrganUnit topUnit;
    List<OrganUnit> villageUnits;
    OrganisationUnitAdapter villageAdapter;

    public WorkplanChooseOrganisationUnitRow(Context context, String mOrganUnitId, String label, boolean mandatory, String warning,
                                     BaseValue baseValue,
                                     DataEntryRowTypes rowType){
        this.context = context;
        this.topUnit = MetaDataController.getOrganisationUnitById(mOrganUnitId);
        mLabel = label;
        mMandatory = mandatory;
        mWarning = warning;
        mValue = baseValue;
        mRowType = rowType;

        checkNeedsForDescriptionButton();
    }

    @Override
    public View getView(FragmentManager fragmentManager, LayoutInflater inflater, View convertView, ViewGroup container) {

        View view;
        final OrganisationUnitRowHolder holder;

        if (convertView != null && convertView.getTag() instanceof OrganisationUnitRowHolder) {
            view = convertView;
            holder = (OrganisationUnitRowHolder) convertView.getTag();
            holder.updateViews(mLabel, mValue);
        }else{
            View root = inflater.inflate(R.layout.row_workplan_choose_organisation_unit_layout, container, false);

            TextView textLabel = (TextView) root.findViewById(R.id.text_label);
            TextView mandatoryIndicator = (TextView) root.findViewById(R.id.mandatory_indicator);

            TextInputLayout tilVillage = (TextInputLayout) root.findViewById(R.id.row_workplan_choose_organisation_unit_layout_til_village);
            EditText etVillage = (EditText) root.findViewById(R.id.row_workplan_choose_organisation_unit_layout_et_village);
            Spinner spVillage = (Spinner) root.findViewById(R.id.row_workplan_choose_organisation_unit_layout_sp_village);

            holder = new OrganisationUnitRowHolder(context, textLabel, mandatoryIndicator, tilVillage, etVillage, spVillage);

            root.setTag(holder);
            view = root;

            holder.updateViews("", mValue);

            holder.textLabel.setText(mLabel);

            if (!mMandatory) {
                holder.mandatoryIndicator.setVisibility(View.GONE);
            } else {
                holder.mandatoryIndicator.setVisibility(View.VISIBLE);
            }
        }

        return view;
    }

    private class OrganisationUnitRowHolder {

        Context context;

        TextView textLabel;
        TextView mandatoryIndicator;

        TextInputLayout tilVillage;
        EditText etVillage;
        Spinner spVillage;

        public OrganisationUnitRowHolder(
                Context context,
                TextView textLabel,
                TextView mandatoryIndicator,
                TextInputLayout tilVillage,
                EditText etVillage,
                Spinner spVillage){
            this.context = context;
            this.textLabel = textLabel;
            this.mandatoryIndicator = mandatoryIndicator;
            this.tilVillage = tilVillage;
            this.etVillage = etVillage;
            this.spVillage = spVillage;
            //this.tilVillage.setHint(context.getResources().getString(R.string.village_ward));
        }

        public void updateViews(String label, BaseValue baseValue) {
            updateVillageDropdownData(baseValue.getValue());
            //updateBaseData(baseValue);
        }

        private void updateVillageDropdownData(String lastVillageId){
            ArrayList<OrganUnit> units = new ArrayList<>();
            villageUnits = new ArrayList<OrganUnit>();

            try{
                if(topUnit != null){
                    if(topUnit.getSChildren() != null && !topUnit.getSChildren().isEmpty()){
                        String[] villageIds = topUnit.getSChildren().trim().split(" ");

                        if(villageIds != null && villageIds.length > 0) {

                            for (String villageId : villageIds) {
                                OrganUnit villageUnit = MetaDataController.getOrganisationUnitById(villageId);
                                if (villageUnit != null) {
                                    villageUnits.add(villageUnit);
                                }
                            }

                        }

                        /*if(villageUnits != null && villageUnits.size() > 0){
                            OrganUnit blank = new OrganUnit();
                            blank.setLevel(-1);
                            blank.setDisplayName(context.getResources().getString(R.string.blank_choose));
                            units.add(blank);
                            units.addAll(villageUnits);
                            villageUnits = new ArrayList<>(units);
                        }*/
                        //

                        villageAdapter = new OrganisationUnitAdapter(context, R.layout.item_dropdown, villageUnits);

                        spVillage.setAdapter(villageAdapter);

                        spVillage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                                OrganUnit unit = (OrganUnit) adapterView.getItemAtPosition(pos);
                                etVillage.setText(unit.getDisplayName());
                                mValue.setValue(unit.getId());
                                System.out.println("Workplan Choose Organisation Unit" + mValue);
                                Dhis2Application.getEventBus()
                                        .post(new RowValueChangedEvent(mValue, DataEntryRowTypes.ORGANISATION_UNIT.toString()));
                                villageAdapter.setSelectedPosition(pos);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        });

                        if(lastVillageId != null){
                            for(int i = 0; i < villageUnits.size(); i++){
                                if(villageUnits.get(i).getUid() != null && villageUnits.get(i).getUid().equals(lastVillageId)){
                                    spVillage.setSelection(i);
                                }
                            }
                        }

                        return;

                    }
                }
            }catch(Exception ex){
                Log.e(this.getClass().getSimpleName() + " Exception", ex.toString());
            }

            etVillage.setText("");
            villageAdapter = new OrganisationUnitAdapter(context, R.layout.item_dropdown, villageUnits);
            spVillage.setAdapter(villageAdapter);
        }
    }

    @Override
    public int getViewType() {
        return DataEntryRowTypes.ORGANISATION_UNIT.ordinal();
    }
}
