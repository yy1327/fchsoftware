package com.example.myapplication.ui.camera;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.model.BottomToolbarItem;

import java.util.List;

public class BottomToolbarAdapter extends RecyclerView.Adapter<BottomToolbarAdapter.ToolbarViewHolder> {
    private final List<BottomToolbarItem> toolbarItems;
    private OnToolbarItemClickListener listener;

    public interface OnToolbarItemClickListener {
        void onToolbarItemClick(BottomToolbarItem item, int position);
    }

    public BottomToolbarAdapter(List<BottomToolbarItem> toolbarItems) {
        this.toolbarItems = toolbarItems;
    }

    public void setOnToolbarItemClickListener(OnToolbarItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ToolbarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bottom_toolbar, parent, false);
        return new ToolbarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ToolbarViewHolder holder, int position) {
        BottomToolbarItem item = toolbarItems.get(position);
        holder.tvLabel.setText(item.getLabel());
        holder.ivIcon.setImageResource(item.getIconResId());

        if (item.isActive()) {
            holder.itemView.setBackgroundResource(R.drawable.bg_toolbar_item_active);
        } else {
            holder.itemView.setBackgroundResource(0);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onToolbarItemClick(item, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return toolbarItems.size();
    }

    static class ToolbarViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvLabel;

        ToolbarViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvLabel = itemView.findViewById(R.id.tvLabel);
        }
    }
}
