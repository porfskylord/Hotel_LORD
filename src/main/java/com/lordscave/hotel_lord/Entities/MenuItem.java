package com.lordscave.hotel_lord.Entities;

public class MenuItem {
    private final String name, fxmlPath, iconPath;

    public MenuItem(String name, String fxmlPath, String iconPath) {
        this.name = name;
        this.fxmlPath = fxmlPath;
        this.iconPath = iconPath;
    }

    public String getName() { return name; }
    public String getFxmlPath() { return fxmlPath; }
    public String getIconPath() { return iconPath; }
}
