package com.example.myapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.model.Cameras;

import java.util.ArrayList;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private Context context;
    private List<Cameras> videoItems = new ArrayList<>();
    private int selectedPosition = -1;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setVideoItems(List<Cameras> videoItems) {
        this.videoItems = videoItems;
        notifyDataSetChanged();
    }

    public void addData(List<Cameras> newData) {
        this.videoItems.addAll(newData);
        notifyDataSetChanged();
    }

    public VideoAdapter(List<Cameras> videoItems, Context context) {
        this.videoItems = videoItems;
        this.context = context;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        Cameras item = videoItems.get(position);
        holder.videoName.setText(item.getCameraName());

        if (item.getCameraPhoto2() != null && !item.getCameraPhoto2().isEmpty()) {
            Glide.with(context)
                    .load(item.getCameraPhoto2())
                    .into(holder.cameraImg);
        } else {
            Glide.with(context)
                    .load(R.mipmap.ic_launcher_round)
                    .into(holder.cameraImg);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoItems.size();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        TextView videoName;
        ImageView cameraImg;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            videoName = itemView.findViewById(R.id.videoName);
            cameraImg = itemView.findViewById(R.id.cameraImg);
        }
    }
}
