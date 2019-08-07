package org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.custom_row;

import android.content.Context;
import android.graphics.Color;
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
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.DataEntryRowTypes;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.Row;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.RunProgramRulesDelayedDispatcher;
import org.hisp.dhis.android.sdk.ui.fragments.dataentry.RowValueChangedEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ChooseOrganisationUnitRow extends Row {

    Context context;
    OrganisationUnit topUnit;

    List<OrganUnit> stateUnits;
    List<OrganUnit> districtUnits;
    List<OrganUnit> blockUnits;
    List<OrganUnit> villageUnits;

    OrganisationUnitAdapter stateAdapter;
    OrganisationUnitAdapter districtAdapter;
    OrganisationUnitAdapter blockAdapter;
    OrganisationUnitAdapter villageAdapter;

    public ChooseOrganisationUnitRow(Context context, String mOrganUnitId, String label, boolean mandatory, String warning,
                                     BaseValue baseValue,
                                     DataEntryRowTypes rowType){
        this.context = context;
        this.topUnit = MetaDataController.getOrganisationUnit(mOrganUnitId);
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
            View root = inflater.inflate(R.layout.row_choose_organisation_unit_layout, container, false);

            TextView tvTitle = (TextView) root.findViewById(R.id.row_choose_organisation_unit_layout_tv_title);

            TextInputLayout tilState = (TextInputLayout) root.findViewById(R.id.row_choose_organisation_unit_layout_til_state);
            TextInputLayout tilDistrict = (TextInputLayout) root.findViewById(R.id.row_choose_organisation_unit_layout_til_district);
            TextInputLayout tilBlock = (TextInputLayout) root.findViewById(R.id.row_choose_organisation_unit_layout_til_block);
            TextInputLayout tilVillage = (TextInputLayout) root.findViewById(R.id.row_choose_organisation_unit_layout_til_village);

            EditText etState = (EditText) root.findViewById(R.id.row_choose_organisation_unit_layout_et_state);
            EditText etDistrict = (EditText) root.findViewById(R.id.row_choose_organisation_unit_layout_et_district);
            EditText etBlock = (EditText) root.findViewById(R.id.row_choose_organisation_unit_layout_et_block);
            EditText etVillage = (EditText) root.findViewById(R.id.row_choose_organisation_unit_layout_et_village);

            Spinner spState = (Spinner) root.findViewById(R.id.row_choose_organisation_unit_layout_sp_state);
            Spinner spDistrict = (Spinner) root.findViewById(R.id.row_choose_organisation_unit_layout_sp_district);
            Spinner spBlock = (Spinner) root.findViewById(R.id.row_choose_organisation_unit_layout_sp_block);
            Spinner spVillage = (Spinner) root.findViewById(R.id.row_choose_organisation_unit_layout_sp_village);

            tvTitle.setText(mLabel);

            holder = new OrganisationUnitRowHolder(context, tilState, tilDistrict, tilBlock, tilVillage, etState, etDistrict, etBlock, etVillage, spState,
                    spDistrict,
                    spBlock,
                    spVillage);

            root.setTag(holder);
            view = root;

            holder.updateViews("", mValue);
        }

        return view;
    }

    private class OrganisationUnitRowHolder {

        Context context;

        TextInputLayout tilState;
        TextInputLayout tilDistrict;
        TextInputLayout tilBlock;
        TextInputLayout tilVillage;

        EditText etState;
        EditText etDistrict;
        EditText etBlock;
        EditText etVillage;

        Spinner spState;
        Spinner spDistrict;
        Spinner spBlock;
        Spinner spVillage;

        public OrganisationUnitRowHolder(
                Context context,
                TextInputLayout tilState,
                TextInputLayout tilDistrict,
                TextInputLayout tilBlock,
                TextInputLayout tilVillage,
                EditText etState,
                EditText etDistrict,
                EditText etBlock,
                EditText etVillage,
                Spinner spState,
                Spinner spDistrict,
                Spinner spBlock,
                Spinner spVillage){
            this.context = context;
            this.tilState = tilState;
            this.tilDistrict = tilDistrict;
            this.tilBlock = tilBlock;
            this.tilVillage = tilVillage;
            this.etState = etState;
            this.etDistrict = etDistrict;
            this.etBlock = etBlock;
            this.etVillage = etVillage;
            this.spState = spState;
            this.spDistrict = spDistrict;
            this.spBlock = spBlock;
            this.spVillage = spVillage;

            this.tilState.setHint(context.getResources().getString(R.string.state));
            this.tilDistrict.setHint(context.getResources().getString(R.string.district));
            this.tilBlock.setHint(context.getResources().getString(R.string.block));
            this.tilVillage.setHint(context.getResources().getString(R.string.village_ward));
        }

        public void updateViews(String label, BaseValue baseValue) {
            //updateStateDropdownData(null);
            updateBaseData(baseValue);
        }

        private void updateStateDropdownData(String lastStateId, final String lastDistrictId, final String lastBlockId, final String lastVillageId){
            try {
                ArrayList<OrganUnit> units = new ArrayList<OrganUnit>();
                stateUnits = MetaDataController.getOrganisationUnitsStateLevel(topUnit);
                if(stateUnits != null && stateUnits.size() > 0){
                    OrganUnit blank = new OrganUnit();
                    blank.setLevel(-1);
                    blank.setDisplayName(context.getResources().getString(R.string.blank_choose));
                    units.add(blank);
                    units.addAll(stateUnits);
                    stateUnits = new ArrayList<>(units);
                }
                stateAdapter = new OrganisationUnitAdapter(context, R.layout.item_dropdown, stateUnits);
                spState.setAdapter(stateAdapter);
                spState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                        OrganUnit unit = (OrganUnit) adapterView.getItemAtPosition(pos);
                        if (unit.getLevel() != -1) {
                            etState.setText(unit.getDisplayName());
                            updateDataFollowLevel(unit.getUid(), 1);
                        } else {
                            etState.setText("");
                            updateDataFollowLevel(" ", 1);
                        }

                        stateAdapter.setSelectedPosition(pos);

                        updateDistrictDropdownData(lastDistrictId, lastBlockId, lastVillageId);
                        updateBlockDropdownData(lastBlockId, lastVillageId);
                        updateVillageDropdownData(lastVillageId);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
                if(lastStateId != null){
                    for(int i = 0; i < stateUnits.size(); i++){

                        if(stateUnits.get(i).getUid() != null && stateUnits.get(i).getUid().equals(lastStateId)){
                            spState.setSelection(i);
                        }
                    }
                }
            /*if (stateUnits != null && stateUnits.size > 0) {
                etState.setText(stateUnits.get(0).displayName)
                stateAdapter.selectedPosition = 0
            }*/
            }catch(Exception ex){
                Log.e(this.getClass().getSimpleName() + " Exception", ex.toString());
            }
        }

        private void updateDistrictDropdownData(String lastDistrictID, final String lastBlockID, final String lastVillageId){

            ArrayList<OrganUnit> units = new ArrayList<>();
            districtUnits = new ArrayList<OrganUnit>();

            try{
                if(stateAdapter.getItem(stateAdapter.getSelectedPosition()) != null){
                    OrganUnit stateUnit = stateAdapter.getItem(stateAdapter.getSelectedPosition());
                    if(stateUnit.getSChildren() != null && !stateUnit.getSChildren().isEmpty()){
                        String[] districtIds = stateUnit.getSChildren().trim().split(" ");

                        if(districtIds != null && districtIds.length > 0){

                            for(String districtId : districtIds){
                                OrganUnit districtUnit = MetaDataController.getOrganisationUnitsDistrictLevel(districtId);
                                if(districtUnit != null){
                                    districtUnits.add(districtUnit);
                                }
                            }

                            if(districtUnits != null && districtUnits.size() > 0){
                                OrganUnit blank = new OrganUnit();
                                blank.setLevel(-1);
                                blank.setDisplayName(context.getResources().getString(R.string.blank_choose));
                                units.add(blank);
                                units.addAll(districtUnits);
                                districtUnits = new ArrayList<>(units);
                            }

                            districtAdapter = new OrganisationUnitAdapter(context, R.layout.item_dropdown, districtUnits);

                            spDistrict.setAdapter(districtAdapter);
                            spDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                                    OrganUnit unit = (OrganUnit) adapterView.getItemAtPosition(pos);
                                    if (unit.getLevel() != -1) {
                                        etDistrict.setText(unit.getDisplayName());
                                        updateDataFollowLevel(unit.getUid(), 2);
                                    } else {
                                        etDistrict.setText("");
                                        updateDataFollowLevel(" ", 2);
                                    }
                                    districtAdapter.setSelectedPosition(pos);
                                    updateBlockDropdownData(lastBlockID, lastVillageId);
                                    updateVillageDropdownData(lastVillageId);
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> adapterView) {

                                }
                            });

                            if(lastDistrictID != null){
                                for(int i = 0; i < districtUnits.size(); i++){
                                    if(districtUnits.get(i).getUid() != null && districtUnits.get(i).getUid().equals(lastDistrictID)){
                                        spDistrict.setSelection(i);
                                    }
                                }
                            }

                        /*if(districtUnits != null && districtUnits.size > 0) {
                            actvDistrict.setText(districtUnits.get(0).displayName)
                            districtAdapter.selectedPosition = 0
                        }*/

                            return;
                        }

                    }
                }
            }catch(Exception ex){
                Log.e(this.getClass().getSimpleName() + " Exception", ex.toString());
            }

            etDistrict.setText("");
            districtAdapter = new OrganisationUnitAdapter(context, R.layout.item_dropdown, districtUnits);
            spDistrict.setAdapter(districtAdapter);
        }

        private void updateBlockDropdownData(String lastBlockId, final String lastVillageId){

            ArrayList<OrganUnit> units = new ArrayList<>();
            blockUnits = new ArrayList<OrganUnit>();

            try{
                if(districtAdapter.getItem(districtAdapter.getSelectedPosition()) != null){
                    OrganUnit districtUnit = districtAdapter.getItem(districtAdapter.getSelectedPosition());
                    if(districtUnit.getSChildren() != null && !districtUnit.getSChildren().isEmpty()){
                        String[] blockIds = districtUnit.getSChildren().trim().split(" ");

                        if(blockIds != null && blockIds.length > 0){

                            for(String blockId : blockIds){
                                OrganUnit blockUnit = MetaDataController.getOrganisationUnitsBlockLevel(blockId);
                                if(blockUnit != null){
                                    blockUnits.add(blockUnit);
                                }
                            }

                            if(blockUnits != null && blockUnits.size() > 0){
                                OrganUnit blank = new OrganUnit();
                                blank.setLevel(-1);
                                blank.setDisplayName(context.getResources().getString(R.string.blank_choose));
                                units.add(blank);
                                units.addAll(blockUnits);
                                blockUnits = new ArrayList<>(units);
                            }

                            blockAdapter = new OrganisationUnitAdapter(context, R.layout.item_dropdown, blockUnits);

                            spBlock.setAdapter(blockAdapter);

                            spBlock.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                                    OrganUnit unit = (OrganUnit) adapterView.getItemAtPosition(pos);
                                    if (unit.getLevel() != -1) {
                                        etBlock.setText(unit.getDisplayName());
                                        updateDataFollowLevel(unit.getUid(), 3);
                                    } else {
                                        etBlock.setText("");
                                        updateDataFollowLevel(" ", 3);
                                    }
                                    blockAdapter.setSelectedPosition(pos);
                                    updateVillageDropdownData(lastVillageId);
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> adapterView) {

                                }
                            });

                            if(lastBlockId != null){
                                for(int i = 0; i < blockUnits.size(); i++){
                                    if(blockUnits.get(i).getUid() != null && blockUnits.get(i).getUid().equals(lastBlockId)){
                                        spBlock.setSelection(i);
                                    }
                                }
                            }
                        /*if(districtUnits != null && districtUnits.size > 0) {
                            actvDistrict.setText(districtUnits.get(0).displayName)
                            districtAdapter.selectedPosition = 0
                        }*/

                            return;
                        }

                    }
                }
            }catch(Exception ex){
                Log.e(this.getClass().getSimpleName() + " Exception", ex.toString());
            }

            etBlock.setText("");
            blockAdapter = new OrganisationUnitAdapter(context, R.layout.item_dropdown, blockUnits);
            spBlock.setAdapter(blockAdapter);
        }

        private void updateVillageDropdownData(String lastVillageId){
            ArrayList<OrganUnit> units = new ArrayList<>();
            villageUnits = new ArrayList<OrganUnit>();

            try{
                if(blockAdapter.getItem(blockAdapter.getSelectedPosition()) != null){
                    OrganUnit blockUnit = blockAdapter.getItem(blockAdapter.getSelectedPosition());
                    if(blockUnit.getSChildren() != null && !blockUnit.getSChildren().isEmpty()){

                        villageUnits = MetaDataController.getOrganisationUnitsVillageLevel(blockUnit);

                        if(villageUnits != null && villageUnits.size() > 0){
                            OrganUnit blank = new OrganUnit();
                            blank.setLevel(-1);
                            blank.setDisplayName(context.getResources().getString(R.string.blank_choose));
                            units.add(blank);
                            units.addAll(villageUnits);
                            villageUnits = new ArrayList<>(units);
                        }

                        villageAdapter = new OrganisationUnitAdapter(context, R.layout.item_dropdown, villageUnits);

                        spVillage.setAdapter(villageAdapter);

                        spVillage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                                OrganUnit unit = (OrganUnit) adapterView.getItemAtPosition(pos);
                                if (unit.getLevel() != -1) {
                                    etVillage.setText(unit.getDisplayName());
                                    updateDataFollowLevel(unit.getUid(), 4);
                                } else {
                                    etVillage.setText("");
                                    updateDataFollowLevel(" ", 4);
                                }
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

        private void updateBaseData(BaseValue baseValue){
            String[] ids = baseValue.getValue().trim().split(",");
            if(ids != null && ids.length > 0){
                if(ids.length > 0){
                    updateStateDropdownData(ids[0], null, null, null);
                }

                if(ids.length > 1){
                    updateStateDropdownData(ids[0], ids[1], null, null);
                }

                if(ids.length > 2){
                    updateStateDropdownData(ids[0], ids[1], ids[2], null);
                }

                if(ids.length > 3){
                    updateStateDropdownData(ids[0], ids[1], ids[2], ids[3]);
                }

                return;
            }

            updateStateDropdownData(null, null, null, null);
        }

        public void updateDataFollowLevel(String value, int level){
            if(value != null){
                String[] ids = mValue.getValue().trim().split(",");
                ArrayList<String> arrayIds = new ArrayList<String>(Arrays.asList(ids));
                if(arrayIds != null && arrayIds.size() > 0){
                    if(value.equals(" ")){
                        for(int i = 0; i < arrayIds.size(); i++){
                            if(i >= (level - 1)){
                                arrayIds.remove(i);
                            }
                        }
                    }else{
                        boolean hasLevel = false;
                        for(int i = 0; i < arrayIds.size(); i++){
                            if(i == (level - 1)){
                                arrayIds.set(i, value);
                                hasLevel = true;
                                break;
                            }
                        }

                        if(!hasLevel){
                            arrayIds.add(value);
                        }
                    }
                }

                mValue.setValue(StringUtils.join(arrayIds, ','));
                Log.d("Value", mValue.getValue());
            }
            System.out.println("Chooose Organisation Unit" + mValue);
            Dhis2Application.getEventBus()
                    .post(new RowValueChangedEvent(mValue, DataEntryRowTypes.ORGANISATION_UNIT.toString()));
        }
    }

    public void resetValue(){
        mValue.setValue("");
        Dhis2Application.getEventBus()
                .post(new RowValueChangedEvent(mValue, DataEntryRowTypes.ORGANISATION_UNIT.toString()));

        if(stateAdapter != null){
            stateAdapter.resetData();
        }

        if(districtAdapter != null){
            districtAdapter.resetData();
        }

        if(blockAdapter != null){
            blockAdapter.resetData();
        }

        if(villageAdapter != null){
            villageAdapter.resetData();
        }
    }

    @Override
    public int getViewType() {
        return DataEntryRowTypes.ORGANISATION_UNIT.ordinal();
    }
}
