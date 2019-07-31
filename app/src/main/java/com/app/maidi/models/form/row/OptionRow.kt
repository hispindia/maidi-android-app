package com.app.maidi.models.form.row

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import com.app.maidi.R
import com.app.maidi.domains.child_registration.OptionAdapter
import com.app.maidi.models.form.DataEntryRow
import com.app.maidi.models.form.OptionItem
import com.google.android.material.textfield.TextInputEditText
import org.hisp.dhis.android.sdk.persistence.models.DataElement
import org.hisp.dhis.android.sdk.persistence.models.Option
import org.hisp.dhis.android.sdk.utils.api.ValueType

class OptionRow : DataEntryRow {

    lateinit var spOptions: Spinner
    lateinit var etValue: TextInputEditText
    lateinit var llClickable: LinearLayout

    private var adapter: RowOptionAdapter? = null

    constructor(context: Context?, valueType: ValueType, dataElement: DataElement, title: String, optionList: List<OptionItem>) : super(context) {init(valueType, dataElement, title, optionList)}
    constructor(context: Context?, attrs: AttributeSet?, valueType: ValueType, dataElement: DataElement, title: String, optionList: List<OptionItem>) : super(context, attrs) { init(valueType, dataElement, title, optionList) }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, valueType: ValueType, dataElement: DataElement, title: String, optionList: List<OptionItem>) : super(context, attrs, defStyleAttr) { init(valueType, dataElement, title, optionList) }
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int, valueType: ValueType, dataElement: DataElement, title: String, optionList: List<OptionItem>) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ){ init(valueType, dataElement, title, optionList) }

    fun init(valueType: ValueType, dataElement: DataElement, title: String, optionList: List<OptionItem>) {
        super.init(valueType, dataElement)
        LayoutInflater.from(context).inflate(R.layout.row_checkbox_layout, this, true)
        spOptions = findViewById(R.id.row_option_layout_sp_option)
        etValue = findViewById(R.id.row_option_layout_et_value)
        llClickable = findViewById(R.id.row_option_layout_ll_clickable)

        setTitle(title)
        adapter = RowOptionAdapter(context, R.layout.item_dropdown, optionList)
        spOptions.adapter = adapter
    }

    fun setTitle(title: String){
        etValue.hint = title
    }

    fun getSelectedItem() : OptionItem {
        return spOptions.selectedItem as OptionItem
    }

    private class RowOptionAdapter : ArrayAdapter<OptionItem>{

        var optionList: List<OptionItem>
        var layoutId: Int = -1
        var selectedPosition: Int = -1

        constructor(context: Context, layoutId: Int, optionList: List<OptionItem>) : super(context, layoutId, optionList) {
            this.layoutId = layoutId
            this.optionList = optionList
        }

        override fun getItem(position: Int): OptionItem {
            return optionList.get(position)
        }

        override fun getCount(): Int {
            return optionList.size
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var view = convertView
            if (convertView == null) {
                view = LayoutInflater.from(context).inflate(layoutId, parent, false)
            }
            var option = optionList.get(position)
            var tvTitle = view!!.findViewById<TextView>(R.id.item)
            tvTitle.setText(option.displayName)
            return view
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var view = convertView
            if (convertView == null) {
                view = LayoutInflater.from(context).inflate(layoutId, parent, false)
            }
            var tvTitle = view!!.findViewById<TextView>(R.id.item)
            tvTitle.setTextColor(Color.TRANSPARENT)
            return view
        }

    }
}