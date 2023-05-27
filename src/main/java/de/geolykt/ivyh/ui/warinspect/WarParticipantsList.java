package de.geolykt.ivyh.ui.warinspect;

import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Align;

import de.geolykt.ivyh.IvyUtil;
import de.geolykt.starloader.api.NullUtils;
import de.geolykt.starloader.api.empire.Empire;
import de.geolykt.starloader.api.gui.AsyncRenderer;
import de.geolykt.starloader.api.gui.Drawing;
import de.geolykt.starloader.api.gui.canvas.Canvas;
import de.geolykt.starloader.api.gui.canvas.CanvasContext;

public class WarParticipantsList implements CanvasContext {

    private int scroll;
    @NotNull
    private final SortedSet<Empire> empires = new TreeSet<>((e1, e2) -> {
        return e1.getEmpireName().compareTo(e2.getEmpireName());
    });

    @NotNull
    private final Collection<Empire> view;

    private final int width;
    private final int height;
    @NotNull
    private final StringBuilder sharedBuilder = new StringBuilder();

    public WarParticipantsList(int width, int height, @NotNull Collection<Empire> view) {
        this.width = width;
        this.height = height;
        this.view = view;
        this.scroll = height - 25;
    }

    @Override
    public void render(@NotNull SpriteBatch surface, @NotNull Camera camera) {
        this.empires.clear();
        this.empires.addAll(this.view);

        TextureRegion texture = Drawing.getTextureProvider().getSinglePixelSquare();
        AsyncRenderer.drawTexture(texture, 0, 0, 2, this.height, 0, NullUtils.requireNotNull(Color.BLACK), camera);
        AsyncRenderer.drawTexture(texture, this.width - 2, 0, 2, this.height, 0, NullUtils.requireNotNull(Color.BLACK), camera);
        AsyncRenderer.drawTexture(texture, 0, 0, this.width, 2, 0, NullUtils.requireNotNull(Color.BLACK), camera);
        AsyncRenderer.drawTexture(texture, 0, this.height - 2, this.width, 2, 0, NullUtils.requireNotNull(Color.BLACK), camera);
        Iterator<Empire> it = this.empires.iterator();
        for (int i = 0; it.hasNext(); i++) {
            Empire e = it.next();
            int y = (i * -50) + this.scroll;
            if (y > this.height) {
                continue;
            } else if (y < 0) {
                break;
            }
            this.sharedBuilder.setLength(0);
            IvyUtil.appendColorString(this.sharedBuilder, e.getEmpireName(), e.getGDXColor().toIntBits());
            AsyncRenderer.drawText(5, y + 10, this.width - 10, this.sharedBuilder.toString(), NullUtils.requireNotNull(Color.WHITE), camera, Align.center);
        }
    }

    @Override
    public void onScroll(int canvasX, int canvasY, @NotNull Camera camera, int amount,
            @NotNull Canvas canvas) {
        this.scroll += amount * 5;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public int getWidth() {
        return this.width;
    }
}
