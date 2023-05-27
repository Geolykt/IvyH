package de.geolykt.ivyh.ui;

import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import de.geolykt.starloader.api.NullUtils;
import de.geolykt.starloader.api.gui.AsyncRenderer;
import de.geolykt.starloader.api.gui.canvas.CanvasContext;

public abstract class AbstractTextContext implements CanvasContext {

    private final int width;
    private final int height;
    private final int marginX;
    private final int marginY;
    private final int align;

    public AbstractTextContext(int width, int height, int marginX, int marginY, int align) {
        this.width = width;
        this.height = height;
        this.marginX = marginX;
        this.marginY = marginY;
        this.align = align;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public void render(@NotNull SpriteBatch surface, @NotNull Camera camera) {
        AsyncRenderer.drawText(this.marginX, this.height - (this.marginY * 2), this.width - (2 * this.marginX), this.getText(),
                NullUtils.requireNotNull(Color.WHITE), camera, this.align);
    }

    @NotNull
    public abstract String getText();
}
