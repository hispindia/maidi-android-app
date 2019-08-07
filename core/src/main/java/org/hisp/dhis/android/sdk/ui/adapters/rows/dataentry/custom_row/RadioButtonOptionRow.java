/*
 *  Copyright (c) 2016, University of Oslo
 *  * All rights reserved.
 *  *
 *  * Redistribution and use in source and binary forms, with or without
 *  * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright notice, this
 *  * list of conditions and the following disclaimer.
 *  *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *  * this list of conditions and the following disclaimer in the documentation
 *  * and/or other materials provided with the distribution.
 *  * Neither the name of the HISP project nor the names of its contributors may
 *  * be used to endorse or promote products derived from this software without
 *  * specific prior written permission.
 *  *
 *  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.custom_row;

import android.content.Context;
import android.util.TypedValue;
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import org.hisp.dhis.android.sdk.R;
import org.hisp.dhis.android.sdk.persistence.Dhis2Application;
import org.hisp.dhis.android.sdk.persistence.models.BaseValue;
import org.hisp.dhis.android.sdk.persistence.models.Option;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.DataEntryRowTypes;
import org.hisp.dhis.android.sdk.ui.adapters.rows.dataentry.Row;
import org.hisp.dhis.android.sdk.ui.fragments.dataentry.RowValueChangedEvent;
import org.hisp.dhis.android.sdk.ui.views.FontRadioButton;

import java.util.List;

import static org.hisp.dhis.android.sdk.persistence.models.BaseValue.FALSE;
import static org.hisp.dhis.android.sdk.persistence.models.BaseValue.TRUE;

public class RadioButtonOptionRow extends Row {
    private static final String EMPTY_FIELD = "";

    private final List<Option> mOptions;

    private static int BASE_ID = 1000;

    public RadioButtonOptionRow(String label, boolean mandatory, String warning,
                                BaseValue baseValue, List<Option> options) {

        mLabel = label;
        mMandatory = mandatory;
        mValue = baseValue;
        mWarning = warning;

        mOptions = options;

        checkNeedsForDescriptionButton();
    }

    @Override
    public View getView(FragmentManager fragmentManager, LayoutInflater inflater,
                        View convertView, ViewGroup container) {
        View view;
        RadioGroupRowHolder holder;

        if (convertView != null && convertView.getTag() instanceof RadioGroupRowHolder) {
            view = convertView;
            holder = (RadioGroupRowHolder) convertView.getTag();
        } else {
            View root = inflater.inflate(
                    R.layout.row_choose_option_set_layout, container, false);
            TextView label = (TextView)
                    root.findViewById(R.id.row_choose_option_set_layout_tv_title);
            RadioGroup radioGroup = (RadioGroup) root.findViewById(R.id.row_choose_option_set_layout_rg_chooser);
//            detailedInfoButton =
//                    root.findViewById(R.id.detailed_info_button_layout);


            OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener();

            holder = new RadioGroupRowHolder(mRowType, label, radioGroup, onCheckedChangeListener);

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


    private static class RadioGroupRowHolder {
        final TextView textLabel;
        final RadioGroup radioGroup;
        //        final View detailedInfoButton;
        final OnCheckedChangeListener radioGroupCheckedChangeListener;
        final DataEntryRowTypes type;

        public RadioGroupRowHolder(DataEntryRowTypes type, TextView textLabel,
                                   RadioGroup radioGroup, OnCheckedChangeListener radioGroupCheckedChangeListener) {
            this.type = type;
            this.textLabel = textLabel;
            this.radioGroup = radioGroup;
            this.radioGroupCheckedChangeListener = radioGroupCheckedChangeListener;
        }

        public void updateViews(String label, BaseValue baseValue,
                                List<Option> options, boolean isEditable) {
            textLabel.setText(label);

            radioGroupCheckedChangeListener.setBaseValue(baseValue);

            this.radioGroup.setOnCheckedChangeListener(null);
            radioGroup.clearCheck();
            radioGroup.removeAllViews();

            Context context = radioGroup.getContext();

            for (int i = 0; i < options.size(); i++) {

                Option option = options.get(i);

                FontRadioButton fontRadioButton = new FontRadioButton(context, null, android.R.style.Widget_CompoundButton_RadioButton);

                float textSizeInPixels = context.getResources().getDimension(
                        R.dimen.medium_text_size);
                fontRadioButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeInPixels);
                fontRadioButton.setFont(
                        context.getResources().getString(R.string.regular_font_name));

                fontRadioButton.setEnabled(isEditable);


                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

                radioGroup.addView(fontRadioButton, lp);

                fontRadioButton.setText(option.getDisplayName());
                fontRadioButton.setTag(option);
                fontRadioButton.setId(BASE_ID + i);

                String value = baseValue.getValue();

                if (value != null && value.equals(option.getCode())) {
                    fontRadioButton.setChecked(true);
                }
            }

            this.radioGroup.setOnCheckedChangeListener(radioGroupCheckedChangeListener);
        }
    }

    private static class OnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {

        BaseValue baseValue;

        public void setBaseValue(BaseValue baseValue) {
            this.baseValue = baseValue;
        }

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            String newValue;

            if (checkedId == R.id.first_radio_button) {
                newValue = TRUE;
            } else if (checkedId == R.id.second_radio_button) {
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
