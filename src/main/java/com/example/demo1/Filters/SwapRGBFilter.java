package com.example.demo1.Filters;

import javafx.scene.paint.Color;

public class SwapRGBFilter extends AbstractRGBFilter {
    @Override
    protected Color applyPixel(Color c) {
        return new Color(c.getBlue(), c.getRed(), c.getGreen(), c.getOpacity());
    }
}
