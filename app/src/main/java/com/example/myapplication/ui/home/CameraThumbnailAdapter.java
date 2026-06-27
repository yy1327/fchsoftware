package com.example.myapplication.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.model.Camera;

import java.util.List;

public class CameraThumbnailAdapter extends RecyclerView.Adapter<CameraThumbnailAdapter.CameraViewHolder> {
    private List<Camera> cameras;
    private OnCameraClickListener listener;

    public interface OnCameraClickListener {
        void onCameraClick(Camera camera);
    }

    public CameraThumbnailAdapter(List<Camera> cameras) {
        this.cameras = cameras;
    }

    public void setOnCameraClickListener(OnCameraClickListener listener) {
        this.listener = listener;
    }

    public void updateCameras(List<Camera> newCameras) {
        this.cameras = newCameras;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CameraViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_camera_thumbnail, parent, false);
        return new CameraViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CameraViewHolder holder, int position) {
        Camera camera = cameras.get(position);
        holder.tvCameraName.setText(camera.getName());
        holder.ivThumbnail.setImageResource(camera.getThumbnailResId());

        if (camera.isOnline()) {
            holder.viewStatusDot.setBackgroundResource(R.drawable.bg_status_dot_online);
        } else {
            holder.viewStatusDot.setBackgroundResource(R.drawable.bg_status_dot_offline);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCameraClick(camera);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cameras != null ? cameras.size() : 0;
    }

    static class CameraViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        ImageView ivPlayIcon;
        View viewStatusDot;
        TextView tvCameraName;

        CameraViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            ivPlayIcon = itemView.findViewById(R.id.ivPlayIcon);
            viewStatusDot = itemView.findViewById(R.id.viewStatusDot);
            tvCameraName = itemView.findViewById(R.id.tvCameraName);
        }
    }
}
