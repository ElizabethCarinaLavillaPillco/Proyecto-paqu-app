package com.example.avatar;

public class AvatarItem {
    private final String name;
    private final int imageResId;

    public AvatarItem(String name, int imageResId) {
        this.name = name;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public int getImageResId() {
        return imageResId;
    }
}
