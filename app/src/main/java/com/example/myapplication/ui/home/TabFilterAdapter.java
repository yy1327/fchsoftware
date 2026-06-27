package com.example.myapplication.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.model.TabFilter;

import java.util.List;

public class TabFilterAdapter extends RecyclerView.Adapter<TabFilterAdapter.TabViewHolder> {
    private final List<TabFilter> tabFilters;
    private OnTabSelectedListener listener;

    public interface OnTabSelectedListener {
        void onTabSelected(int position);
    }

    public TabFilterAdapter(List<TabFilter> tabFilters) {
        this.tabFilters = tabFilters;
    }

    public void setOnTabSelectedListener(OnTabSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TabViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tab_filter, parent, false);
        return new TabViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TabViewHolder holder, int position) {
        TabFilter tab = tabFilters.get(position);
        holder.tvLabel.setText(tab.getLabel());

        if (tab.isSelected()) {
            holder.tvLabel.setBackgroundResource(R.drawable.bg_tab_selected);
            holder.tvLabel.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.tab_selected_text));
        } else {
            holder.tvLabel.setBackgroundResource(R.drawable.bg_tab_unselected);
            holder.tvLabel.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.tab_unselected_text));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTabSelected(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return tabFilters.size();
    }

    static class TabViewHolder extends RecyclerView.ViewHolder {
        TextView tvLabel;

        TabViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLabel = itemView.findViewById(R.id.tvTabLabel);
        }
    }
}
