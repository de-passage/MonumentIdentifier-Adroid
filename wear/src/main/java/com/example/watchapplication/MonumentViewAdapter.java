package com.example.watchapplication;

import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.wear.widget.WearableRecyclerView;

public class MonumentViewAdapter
        extends WearableRecyclerView.Adapter<MonumentViewAdapter.ViewHolder> {
    private String[] dataset;

    public MonumentViewAdapter(String[] dataset) {
        this.dataset = dataset;
    }

    @NonNull
    @Override
    public MonumentViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TextView v = new TextView(parent.getContext());
        RecyclerView.LayoutParams lp =
                new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                              ViewGroup.LayoutParams.MATCH_PARENT);
        v.setLayoutParams(lp);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.textView.setText(dataset[position]);
    }

    @Override
    public int getItemCount() {
        return dataset.length;
    }

    public void setDataset(String[] dataset) {
        this.dataset = dataset;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends WearableRecyclerView.ViewHolder {
        public TextView textView;

        public ViewHolder(TextView v) {
            super(v);
            textView = v;
        }
    }
}
