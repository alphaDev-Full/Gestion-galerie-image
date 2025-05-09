package com.example.demo1.Filters;

import javafx.scene.paint.Color;

public class GrayscaleManualFilter extends AbstractRGBFilter {
    @Override
    protected Color applyPixel(Color color) {
        double avg = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
        return new Color(avg, avg, avg, color.getOpacity());
    }
}
