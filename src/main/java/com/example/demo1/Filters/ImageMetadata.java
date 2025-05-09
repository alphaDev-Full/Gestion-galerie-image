package com.example.demo1.model;

import java.util.ArrayList;
import java.util.List;

public class ImageMetadata {
    private String imagePath;
    private List<String> tags = new ArrayList<>();
    private List<String> transformations = new ArrayList<>();

    public ImageMetadata(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImagePath() {
        return imagePath;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<String> getTransformations() {
        return transformations;
    }

    public void addTag(String tag) {
        if (!tags.contains(tag)) tags.add(tag);
    }

    public void addTransformation(String transform) {
        transformations.add(transform);
    }
}
