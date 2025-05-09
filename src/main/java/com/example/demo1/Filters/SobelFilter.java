package com.example.demo1.Filters;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class SobelFilter implements Filter {

    @Override
    public WritableImage apply(Image input) {
        int width = (int) input.getWidth();
        int height = (int) input.getHeight();

        WritableImage output = new WritableImage(width, height);
        PixelReader reader = input.getPixelReader();

        int[][] gx = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
        int[][] gy = {{1, 2, 1}, {0, 0, 0}, {-1, -2, -1}};

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                double sumX = 0;
                double sumY = 0;

                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        Color color = reader.getColor(x + kx, y + ky);
                        double gray = (color.getRed() + color.getGreen() + color.getBlue()) / 3;

                        sumX += gray * gx[ky + 1][kx + 1];
                        sumY += gray * gy[ky + 1][kx + 1];
                    }
                }

                double magnitude = Math.min(1.0, Math.sqrt(sumX * sumX + sumY * sumY));
                output.getPixelWriter().setColor(x, y, new Color(magnitude, magnitude, magnitude, 1));
            }
        }

        return output;
    }
}
