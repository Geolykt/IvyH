package de.geolykt.ivyh.ui;

import org.jetbrains.annotations.NotNull;

public class ConstantTextContext extends AbstractTextContext {
    @NotNull
    private final String text;

    public ConstantTextContext(int width, int height, int marginX, int marginY, int align, @NotNull String text) {
        super(width, height, marginX, marginY, align);
        this.text = text;
    }

    @Override
    @NotNull
    public String getText() {
        return this.text;
    }
}
