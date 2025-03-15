package com.lordscave.hotel_lord;

public class MenuItem {
    private String name;
    private String fxmlPath;

    public MenuItem(String name, String fxmlPath) {
        this.name = name;
        this.fxmlPath = fxmlPath;
    }

    public String getName() {
        return name;
    }

    public String getFxmlPath() {
        return fxmlPath;
    }
}
