package com.example.demo1.Filters;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

public interface Filter {
    WritableImage apply(Image input);
}