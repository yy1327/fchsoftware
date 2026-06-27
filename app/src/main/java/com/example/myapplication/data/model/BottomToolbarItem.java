package com.example.myapplication.data.model;

public class BottomToolbarItem {
    private String label;
    private int iconResId;
    private boolean isActive;

    public BottomToolbarItem(String label, int iconResId) {
        this.label = label;
        this.iconResId = iconResId;
        this.isActive = false;
    }

    public String getLabel() { return label; }
    public int getIconResId() { return iconResId; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
