package com.example.myapplication.data.model;

public class TabFilter {
    private String label;
    private boolean isSelected;

    public TabFilter(String label, boolean isSelected) {
        this.label = label;
        this.isSelected = isSelected;
    }

    public String getLabel() { return label; }
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
}
