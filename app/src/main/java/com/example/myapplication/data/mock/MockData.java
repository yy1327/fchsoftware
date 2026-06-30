package com.example.myapplication.data.mock;

import com.example.myapplication.R;
import com.example.myapplication.data.model.Camera;
import com.example.myapplication.data.model.TabFilter;
import com.example.myapplication.data.model.BottomToolbarItem;

import java.util.Arrays;
import java.util.List;

public class MockData {

    public static List<Camera> getCameras() {
        return Arrays.asList(
            new Camera("1", "行政楼1层 C019", "行政办公楼", R.drawable.ic_camera_thumbnail, true),
            new Camera("2", "行政楼2层 C020", "行政办公楼", R.drawable.ic_camera_thumbnail, true),
            new Camera("3", "行政楼2层 C021", "行政办公楼", R.drawable.ic_camera_thumbnail, true),
            new Camera("4", "行政楼2层 C026", "行政办公楼", R.drawable.ic_camera_thumbnail, true),
            new Camera("5", "行政楼3层 C034", "行政办公楼", R.drawable.ic_camera_thumbnail, true),
            new Camera("6", "行政楼3层 C064", "行政办公楼", R.drawable.ic_camera_thumbnail, false),
            new Camera("7", "教学楼A101", "公共教学楼", R.drawable.ic_camera_thumbnail, true),
            new Camera("8", "教学楼B201", "公共教学楼", R.drawable.ic_camera_thumbnail, true),
            new Camera("9", "教学楼C301", "公共教学楼", R.drawable.ic_camera_thumbnail, true),
            new Camera("10", "广场东入口", "1号主广场", R.drawable.ic_camera_thumbnail, true),
            new Camera("11", "广场西入口", "1号主广场", R.drawable.ic_camera_thumbnail, false),
            new Camera("12", "广场中心", "1号主广场", R.drawable.ic_camera_thumbnail, true)
        );
    }

    public static List<TabFilter> getTabFilters() {
        return Arrays.asList(
            new TabFilter("全部", true),
            new TabFilter("行政办公楼", false),
            new TabFilter("公共教学楼", false),
            new TabFilter("1号主广场", false)
        );
    }

    public static List<BottomToolbarItem> getToolbarItems() {
        return Arrays.asList(
            new BottomToolbarItem("截图", R.drawable.ic_screenshot),
            new BottomToolbarItem("录制", R.drawable.ic_record),
            new BottomToolbarItem("对讲", R.drawable.ic_intercom),
            new BottomToolbarItem("清晰度", R.drawable.ic_resolution),
            new BottomToolbarItem("预置点", R.drawable.ic_preset)
        );
    }
}
