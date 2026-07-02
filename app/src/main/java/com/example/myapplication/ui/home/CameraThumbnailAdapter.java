package com.example.myapplication.ui.home;

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
import com.example.myapplication.data.model.Camera;

import java.util.List;

public class CameraThumbnailAdapter extends RecyclerView.Adapter<CameraThumbnailAdapter.CameraViewHolder> {
    private List<Camera> cameras;
    private OnCameraClickListener listener;
    private Context context;

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
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_camera_thumbnail, parent, false);
        return new CameraViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CameraViewHolder holder, int position) {
        Camera camera = cameras.get(position);
        String name = camera.getName();
        android.util.Log.d("CameraAdapter", "位置 " + position + ": 名称=" + name);
        holder.tvCameraName.setText(name);

        if (camera.getPhotoUrl() != null && !camera.getPhotoUrl().isEmpty()) {
            Glide.with(context)
                .load(camera.getPhotoUrl())
                .placeholder(R.drawable.ic_camera_thumbnail)
                .error(R.drawable.ic_camera_thumbnail)
                .into(holder.ivThumbnail);
        } else {
            holder.ivThumbnail.setImageResource(camera.getThumbnailResId());
        }

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
