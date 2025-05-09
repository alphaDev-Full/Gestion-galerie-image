package com.example.demo1.Filters;

import javafx.scene.paint.Color;

public class SepiaFilterManual extends AbstractRGBFilter {
    @Override
    protected Color applyPixel(Color c) {
        double r = c.getRed(), g = c.getGreen(), b = c.getBlue();

        double tr = Math.min(1.0, 0.393 * r + 0.769 * g + 0.189 * b);
        double tg = Math.min(1.0, 0.349 * r + 0.686 * g + 0.168 * b);
        double tb = Math.min(1.0, 0.272 * r + 0.534 * g + 0.131 * b);

        return new Color(tr, tg, tb, c.getOpacity());
    }
}
