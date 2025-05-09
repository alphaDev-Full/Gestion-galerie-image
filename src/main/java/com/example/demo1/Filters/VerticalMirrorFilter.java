package com.example.demo1.Filters;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class VerticalMirrorFilter implements Filter {
    @Override
    public WritableImage apply(Image input) {
        int width = (int) input.getWidth();
        int height = (int) input.getHeight();

        WritableImage mirroredImage = new WritableImage(width, height);
        PixelReader reader = input.getPixelReader();
        PixelWriter writer = mirroredImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = reader.getColor(x, y);
                writer.setColor(x, height - y - 1, color); // renverse la ligne
            }
        }

        return mirroredImage;
    }
}
