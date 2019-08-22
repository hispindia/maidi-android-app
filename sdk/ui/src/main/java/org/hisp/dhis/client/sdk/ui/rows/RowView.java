package org.hisp.dhis.client.sdk.ui.rows;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;
import org.hisp.dhis.client.sdk.ui.models.FormEntity;

public interface RowView {

    RecyclerView.ViewHolder onCreateViewHolder(LayoutInflater inflater, ViewGroup parent);

    void onBindViewHolder(RecyclerView.ViewHolder viewHolder, FormEntity formEntity);
}